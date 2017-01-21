/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package richtercloud.document.scanner.gui.storageconf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.Constants;
import richtercloud.document.scanner.gui.DocumentScanner;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.message.handler.ConfirmMessageHandler;
import richtercloud.message.handler.Message;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.jpa.storage.MySQLAutoPersistenceStorageConf;
import richtercloud.swing.worker.get.wait.dialog.SwingWorkerGetWaitDialog;

/**
 *
 * @author richter
 */
public class MySQLAutoPersistenceStorageConfPanel extends StorageConfPanel<MySQLAutoPersistenceStorageConf> {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(MySQLAutoPersistenceStorageConfPanel.class);
    public final static String DOWNLOAD_URL_LINUX_64 = "http://dev.mysql.com/get/Downloads/MySQL-5.7/mysql-5.7.17-linux-glibc2.5-x86_64.tar.gz";
    public final static String DOWNLOAD_URL_LINUX_32 = "http://dev.mysql.com/get/Downloads/MySQL-5.7/mysql-5.7.17-linux-glibc2.5-i686.tar.gz";
    public final static String DOWNLOAD_URL_WINDOWS_64 = "http://dev.mysql.com/get/Downloads/MySQL-5.7/mysql-5.7.17-winx64.zip";
    public final static String DOWNLOAD_URL_WINDOWS_32 = "http://dev.mysql.com/get/Downloads/MySQL-5.7/mysql-5.7.17-win32.zip";
    public final static String DOWNLOAD_URL_MAC_OSX_64 = "http://dev.mysql.com/get/Downloads/MySQL-5.7/mysql-5.7.17-macos10.12-x86_64.tar.gz";
        //Mac OSX doesn't have 32-bit versions
    private final MySQLAutoPersistenceStorageConf storageConf;
    private final MessageHandler messageHandler;
    private final ConfirmMessageHandler confirmMessageHandler;
    /**
     * The target of the MySQL download. Doesn't need to be exposed to the user.
     */
    protected final static String MYSQL_DOWNLOAD_TARGET_LINUX_64 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.17-linux-glibc2.5-x86_64.tar.gz").getAbsolutePath();
    protected final static String MYSQL_DOWNLOAD_TARGET_LINUX_32 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.17-linux-glibc2.5-i686.tar.gz").getAbsolutePath();
        //downloading same file with `i386` instead of `i686` works as well, but
        //it's unclear what it refers to (MD5 sums need to be adjusted and the
        //`i686` file proposed on the MySQL download page
    protected final static String MYSQL_DOWNLOAD_TARGET_WINDOWS_64 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.17-winx64.zip").getAbsolutePath();
    protected final static String MYSQL_DOWNLOAD_TARGET_WINDOWS_32 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.17-win32.zip").getAbsolutePath();
    protected final static String MYSQL_DOWNLOAD_TARGET_MAC_OSX_64 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.17-macos10.12-x86_64.tar.gz").getAbsolutePath();
    /**
     * The resulting directory after extraction. Doesn't need to be exposed to
     * the user.
     */
    protected final static String MYSQL_EXTRACTION_TARGET_LINUX_64 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.17-linux-glibc2.5-x86_64").getAbsolutePath();
    protected final static String MYSQL_EXTRACTION_TARGET_LINUX_32 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.17-linux-glibc2.5-i686").getAbsolutePath();
    protected final static String MYSQL_EXTRACTION_TARGET_WINDOWS_64 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.17-winx64").getAbsolutePath();
    protected final static String MYSQL_EXTRACTION_TARGET_WINDOWS_32 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.17-win32").getAbsolutePath();
    protected final static String MYSQL_EXTRACTION_TARGET_MAC_OSX_64 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.17-macos10.12-x86_64").getAbsolutePath();
    protected final static String MD5_SUM_LINUX_64 = "699aeb2ad680d178171fa95a7ba7b347";
    protected final static String MD5_SUM_LINUX_32 = "8363887fa2af67af414675d09cbb3262";
    protected final static String MD5_SUM_WINDOWS_64 = "95155e6addfbd35ec6624d5807f7a27d";
    protected final static String MD5_SUM_WINDOWS_32 = "d7497e614856d8f41b55b7ddabf82142";
    protected final static String MD5_SUM_MAC_OSX_64 = "c618b15bb316f35561cbbd9df2dc9ac8";
    protected final static int EXTRACTION_MODE_TAR_GZ = 1;
    protected final static int EXTRACTION_MODE_ZIP = 2;
    protected final static int MYSQL_EXTRACTION_MODE_LINUX_64 = EXTRACTION_MODE_TAR_GZ;
    protected final static int MYSQL_EXTRACTION_MODE_LINUX_32 = EXTRACTION_MODE_TAR_GZ;
    protected final static int MYSQL_EXTRACTION_MODE_WINDOWS_64 = EXTRACTION_MODE_ZIP;
    protected final static int MYSQL_EXTRACTION_MODE_WINDOWS_32 = EXTRACTION_MODE_ZIP;
    protected final static int MYSQL_EXTRACTION_MODE_MAC_OSX_64 = EXTRACTION_MODE_TAR_GZ;
    /**
     * Return value of {@link #getWindowsBitness() }.
     */
    protected final static int WINDOWS_BITNESS_32 = 1;
    /**
     * Return value of {@link #getWindowsBitness() }.
     */
    protected final static int WINDOWS_BITNESS_64 = 2;
    protected final static String MD5_SUM_CHECK_FAILED_RETRY = "Retry download";
    protected final static String MD5_SUM_CHECK_FAILED_ABORT = "Abort download";
    private final boolean skipMD5SumCheck;

    public MySQLAutoPersistenceStorageConfPanel(MySQLAutoPersistenceStorageConf storageConf,
            MessageHandler messageHandler,
            ConfirmMessageHandler confirmMessageHandler,
            boolean skipMD5SumCheck) throws IOException {
        initComponents();
        this.storageConf = new MySQLAutoPersistenceStorageConf(storageConf.getDatabaseDir(),
                storageConf.getBaseDir(),
                storageConf.getPort(),
                storageConf.getDatabaseDriver(),
                storageConf.getEntityClasses(),
                storageConf.getUsername(),
                storageConf.getPassword(),
                storageConf.getDatabaseName(),
                storageConf.getSchemeChecksumFile());
        this.messageHandler = messageHandler;
        this.confirmMessageHandler = confirmMessageHandler;
        this.skipMD5SumCheck = skipMD5SumCheck;
        this.baseDirTextField.setText(storageConf.getBaseDir());
        this.databaseNameTextField.setText(storageConf.getDatabaseName());
        this.databaseDirTextField.setText(storageConf.getDatabaseDir());
        this.hostnameTextField.setText(storageConf.getHostname());
        this.portSpinner.setValue(storageConf.getPort());
        this.usernameTextField.setText(storageConf.getUsername());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        passwordLabel = new javax.swing.JLabel();
        passwordPasswordField = new javax.swing.JPasswordField();
        portSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        databaseDirTextField = new javax.swing.JTextField();
        databaseDirTextFieldLabel = new javax.swing.JLabel();
        hostnameTextField = new javax.swing.JTextField();
        hostnameTextFieldLabel = new javax.swing.JLabel();
        usernameTextField = new javax.swing.JTextField();
        usernameLabel = new javax.swing.JLabel();
        baseDirTextField = new javax.swing.JTextField();
        baseDirTextFieldLabel = new javax.swing.JLabel();
        databaseNameTextField = new javax.swing.JTextField();
        databaseNameTextFieldLabel = new javax.swing.JLabel();
        downloadButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        mysqldTextField = new javax.swing.JTextField();
        mysqladminTextField = new javax.swing.JTextField();
        mysqlTextField = new javax.swing.JTextField();
        mysqldTextFieldLabel = new javax.swing.JLabel();
        mysqladminTextFieldLabel = new javax.swing.JLabel();
        mysqlTextFieldLabel = new javax.swing.JLabel();
        binariesLabel = new javax.swing.JLabel();

        passwordLabel.setText("Password");

        passwordPasswordField.setText("jPasswordField1");

        jLabel1.setText("Port");

        databaseDirTextFieldLabel.setText("Database directory");

        hostnameTextFieldLabel.setText("Hostname");

        usernameLabel.setText("Username");

        baseDirTextFieldLabel.setText("Base directory");

        databaseNameTextFieldLabel.setText("Database name");

        downloadButton.setText("Download");
        downloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadButtonActionPerformed(evt);
            }
        });

        mysqldTextFieldLabel.setText("mysqld path");

        mysqladminTextFieldLabel.setText("mysqladmin path");

        mysqlTextFieldLabel.setText("mysql path");

        binariesLabel.setText("MySQL binaries (resolved relative to base directory if left empty):");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(binariesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(baseDirTextFieldLabel)
                        .addGap(18, 18, 18)
                        .addComponent(baseDirTextField))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mysqladminTextFieldLabel)
                            .addComponent(mysqldTextFieldLabel)
                            .addComponent(mysqlTextFieldLabel))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mysqlTextField)
                            .addComponent(mysqldTextField)
                            .addComponent(mysqladminTextField)))
                    .addComponent(jSeparator1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(downloadButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(databaseNameTextFieldLabel)
                            .addComponent(databaseDirTextFieldLabel)
                            .addComponent(hostnameTextFieldLabel)
                            .addComponent(jLabel1)
                            .addComponent(usernameLabel)
                            .addComponent(passwordLabel))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(passwordPasswordField)
                            .addComponent(usernameTextField)
                            .addComponent(portSpinner)
                            .addComponent(hostnameTextField)
                            .addComponent(databaseDirTextField)
                            .addComponent(databaseNameTextField))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(baseDirTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(baseDirTextFieldLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(downloadButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(binariesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mysqldTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mysqldTextFieldLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mysqladminTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mysqladminTextFieldLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mysqlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mysqlTextFieldLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databaseNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(databaseNameTextFieldLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databaseDirTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(databaseDirTextFieldLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostnameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hostnameTextFieldLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void downloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadButtonActionPerformed
        String downloadURL;
        String downloadTarget;
        int extractionMode;
        String extractionLocation;
        String md5Sum;
        LOGGER.debug(String.format("system properties os.name is '%s' and "
                + "os.arch is '%s'",
                System.getProperty("os.name"),
                System.getProperty("os.arch")));
        if(SystemUtils.IS_OS_LINUX) {
            if("amd64".equals(SystemUtils.OS_ARCH)) {
                LOGGER.debug("assuming Linux 64-bit");
                downloadURL = DOWNLOAD_URL_LINUX_64;
                downloadTarget = MYSQL_DOWNLOAD_TARGET_LINUX_64;
                extractionMode = MYSQL_EXTRACTION_MODE_LINUX_64;
                extractionLocation = MYSQL_EXTRACTION_TARGET_LINUX_64;
                md5Sum = MD5_SUM_LINUX_64;
            }else if("i386".equals(SystemUtils.OS_ARCH)) {
                LOGGER.debug("assuming Linux 32-bit");
                downloadURL = DOWNLOAD_URL_LINUX_32;
                downloadTarget = MYSQL_DOWNLOAD_TARGET_LINUX_32;
                extractionMode = MYSQL_EXTRACTION_MODE_LINUX_32;
                extractionLocation = MYSQL_EXTRACTION_TARGET_LINUX_32;
                md5Sum = MD5_SUM_LINUX_32;
            }else {
                messageHandler.handle(new Message(String.format("Linux "
                        + "operating system architecture %s isn't supported "
                        + "for automatic download, please download MySQL "
                        + "manually and set pathes accordingly",
                                SystemUtils.OS_ARCH),
                        JOptionPane.WARNING_MESSAGE,
                        "Linux OS architecture not supported"));
                return;
            }
        }else if(SystemUtils.IS_OS_WINDOWS) {
            if(getWindowsBitness() == WINDOWS_BITNESS_64) {
                LOGGER.debug("assuming Windows 64-bit");
                downloadURL = DOWNLOAD_URL_WINDOWS_64;
                downloadTarget = MYSQL_DOWNLOAD_TARGET_WINDOWS_64;
                extractionMode = MYSQL_EXTRACTION_MODE_WINDOWS_64;
                extractionLocation = MYSQL_EXTRACTION_TARGET_WINDOWS_64;
                md5Sum = MD5_SUM_WINDOWS_64;
            }else if(getWindowsBitness() == WINDOWS_BITNESS_32) {
                LOGGER.debug("assuming Windows 32-bit");
                downloadURL = DOWNLOAD_URL_WINDOWS_32;
                downloadTarget = MYSQL_DOWNLOAD_TARGET_WINDOWS_32;
                extractionMode = MYSQL_EXTRACTION_MODE_WINDOWS_32;
                extractionLocation = MYSQL_EXTRACTION_TARGET_WINDOWS_32;
                md5Sum = MD5_SUM_WINDOWS_32;
            }else {
                messageHandler.handle(new Message(String.format("Windows "
                        + "operating system architecture %s isn't supported "
                        + "for automatic download, please download MySQL "
                        + "manually and set pathes accordingly",
                                SystemUtils.OS_ARCH),
                        JOptionPane.WARNING_MESSAGE,
                        "Linux OS architecture not supported"));
                return;
            }
        }else if(SystemUtils.IS_OS_MAC) {
            if("x84_86".equals(SystemUtils.OS_ARCH)) {
                LOGGER.debug("assuming Mac OSX 64-bit");
                downloadURL = DOWNLOAD_URL_MAC_OSX_64;
                downloadTarget = MYSQL_DOWNLOAD_TARGET_MAC_OSX_64;
                extractionMode = MYSQL_EXTRACTION_MODE_MAC_OSX_64;
                extractionLocation = MYSQL_EXTRACTION_TARGET_MAC_OSX_64;
                md5Sum = MD5_SUM_MAC_OSX_64;
            }else {
                messageHandler.handle(new Message(String.format("Mac "
                        + "operating system architecture %s isn't supported "
                        + "for automatic download, please download MySQL "
                        + "manually and set pathes accordingly",
                                SystemUtils.OS_ARCH),
                        JOptionPane.WARNING_MESSAGE,
                        "Linux OS architecture not supported"));
                return;
            }
        }else {
            this.messageHandler.handle(new Message(String.format("Operating "
                    + "system %s isn't supported for automatic download, "
                    + "please download MySQL manually and set pathes "
                    + "accordingly",
                            SystemUtils.OS_NAME),
                    JOptionPane.WARNING_MESSAGE,
                    "Operating system not supported"));
            return;
        }
        while(!mySQLDownload(downloadURL,
                downloadTarget,
                extractionMode,
                extractionLocation,
                md5Sum)) {
            MySQLDownloadDialog mySQLDownloadDialog = new MySQLDownloadDialog(SwingUtilities.getWindowAncestor(this));
            mySQLDownloadDialog.setLocationRelativeTo(this);
            mySQLDownloadDialog.setVisible(true);
            if(mySQLDownloadDialog.isCanceled()) {
                return;
            }
            downloadURL = mySQLDownloadDialog.getDownloadURL();
            extractionLocation = mySQLDownloadDialog.getExtractionLocation();
            md5Sum = mySQLDownloadDialog.getMd5Sum();
        }
        baseDirTextField.setText(extractionLocation);
            //not necessary to set on storageConf because it'll be set in save
    }//GEN-LAST:event_downloadButtonActionPerformed

    /**
     * Windows lies about 64-bit systems in order to make 32-bit programs work
     * on 64-bit systems. Taken from
     * http://stackoverflow.com/questions/4748673/how-can-i-check-the-bitness-of-my-os-using-java-j2se-not-os-arch.
     * @return {@link #WINDOWS_BITNESS_32} or {@link #WINDOWS_BITNESS_64}
     */
    private int getWindowsBitness() {
        assert SystemUtils.IS_OS_WINDOWS;
        String arch = System.getenv("PROCESSOR_ARCHITECTURE");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
        int realArch = arch != null && arch.endsWith("64")
                          || wow64Arch != null && wow64Arch.endsWith("64")
                              ? WINDOWS_BITNESS_64 : WINDOWS_BITNESS_32;
        return realArch;
    }

    /**
     * One step in a MySQL download loop.
     * @param downloadURL
     * @param extractionDir the directory where the directory contained in the
     * MySQL tarball ought to be placed
     * @param md5Sum
     * @return {@code true} if the validation, download and extraction were
     * successful, {@code false} otherwise
     */
    protected boolean mySQLDownload(String downloadURL,
            String downloadTarget,
            int extractionMode,
            String extractionDir,
            String md5Sum) {
        final SwingWorkerGetWaitDialog dialog = new SwingWorkerGetWaitDialog(JOptionPane.getFrameForComponent(this),
                DocumentScanner.generateApplicationWindowTitle("Downloading MySQL",
                        Constants.APP_NAME,
                        Constants.APP_VERSION),
                "Downloading MySQL",
                "Downloading MySQL");
        SwingWorker<Boolean, Void> downloadWorker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    boolean needDownload;
                    if(skipMD5SumCheck) {
                        needDownload = !new File(downloadTarget).exists();
                    }else {
                        needDownload = true;
                        if(!md5Sum.isEmpty() && new File(downloadTarget).exists()) {
                            LOGGER.debug(String.format("reading download file '%s' for MD5 sum calculation", downloadTarget));
                            String md5 = DigestUtils.md5Hex(new BufferedInputStream(new FileInputStream(downloadTarget)));
                            if(md5Sum.equals(md5)) {
                                LOGGER.debug(String.format("MD5 sum %s of download file '%s' matches", md5Sum, downloadTarget));
                                needDownload = false;
                            }else {
                                LOGGER.debug(String.format("MD5 sum %s of download file '%s' doesn't match (should be %s), requesting new download", md5, downloadTarget, md5Sum));
                            }
                        }
                    }
                    if(dialog.isCanceled()) {
                        return false;
                    }
                    if(needDownload) {
                        boolean success = false;
                        while(!success) {
                            URL downloadURLURL = new URL(downloadURL);
                            FileOutputStream out =
                                    new FileOutputStream(downloadTarget);
                            InputStream downloadURLInputStream = downloadURLURL.openStream();
                            IOUtils.copy(downloadURLInputStream,
                                    out);
                            out.flush();
                            out.close();
                            downloadURLInputStream.close();
                            if(dialog.isCanceled()) {
                                return false;
                            }
                            if(md5Sum.isEmpty()) {
                                success = true;
                            }else {
                                String md5 = DigestUtils.md5Hex(new BufferedInputStream(new FileInputStream(downloadTarget)));
                                if(md5Sum.equals(md5)) {
                                    success = true;
                                }else {
                                    String answer = confirmMessageHandler.confirm(new Message(String.format(
                                            "MD5 sum %s "
                                            + "of download '%s' doesn't match the "
                                            + "specified MD5 sum %s. This "
                                            + "indicates an incomplete download, a "
                                            + "wrong specified MD5 sum or an error "
                                            + "during transfer.",
                                                    md5,
                                                    downloadTarget,
                                                    md5Sum),
                                            JOptionPane.ERROR_MESSAGE,
                                            "MD5 sum verification failed"),
                                            MD5_SUM_CHECK_FAILED_RETRY,
                                            MD5_SUM_CHECK_FAILED_ABORT);
                                    if(MD5_SUM_CHECK_FAILED_ABORT.equals(answer)) {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                    if(dialog.isCanceled()) {
                        return false;
                    }
                    if(!new File(extractionDir).exists()) {
                        FileInputStream fileInputStream = new FileInputStream(downloadTarget);
                        if(extractionMode == EXTRACTION_MODE_TAR_GZ) {
                            GZIPInputStream gZIPInputStream = new GZIPInputStream(fileInputStream);
                            TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gZIPInputStream);
                            String extractionDirTar = new File(extractionDir).getParent();
                            LOGGER.debug(String.format("extracting into '%s'", extractionDirTar));
                            TarArchiveEntry entry = null;
                            while ((entry = (TarArchiveEntry)tarArchiveInputStream.getNextEntry()) != null) {
                                final File outputFile = new File(extractionDirTar, entry.getName());
                                if (entry.isDirectory()) {
                                    LOGGER.trace(String.format("Attempting to write output directory %s.", outputFile.getAbsolutePath()));
                                    if (!outputFile.exists()) {
                                        LOGGER.trace(String.format("Attempting to create output directory %s.", outputFile.getAbsolutePath()));
                                        if (!outputFile.mkdirs()) {
                                            throw new IllegalStateException(String.format("Couldn't create directory %s.", outputFile.getAbsolutePath()));
                                        }
                                    }
                                } else {
                                    LOGGER.trace(String.format("Creating output file %s.", outputFile.getAbsolutePath()));
                                    final File outputFileParent = outputFile.getParentFile();
                                    if (!outputFileParent.exists()) {
                                        if(!outputFileParent.mkdirs()) {
                                            throw new IOException(String.format("Couldn't create directory %s.", outputFileParent.getAbsolutePath()));
                                        }
                                    }
                                    final OutputStream outputFileStream = new FileOutputStream(outputFile);
                                    IOUtils.copy(tarArchiveInputStream, outputFileStream);
                                    outputFileStream.close();
                                }
                                //not the most efficient way, but certainly a
                                //comprehensive one
                                int modeOctal = Integer.parseInt(Integer.toOctalString(entry.getMode()));
                                Path outputFilePath = Paths.get(outputFile.getAbsolutePath());
                                StringBuilder permStringBuilder = new StringBuilder(9);
                                int modeUser = modeOctal / 100;
                                int modeGroup = (modeOctal % 100) / 10;
                                int modeOthers = modeOctal % 10;
                                //from http://stackoverflow.com/questions/34234598/how-to-convert-an-input-of-3-octal-numbers-into-chmod-permissions-into-binary
                                permStringBuilder.append((modeUser & 4) == 0 ? '-' : 'r');
                                permStringBuilder.append((modeUser & 2) == 0 ? '-' : 'w');
                                permStringBuilder.append((modeUser & 1) == 0 ? '-' : 'x');
                                permStringBuilder.append((modeGroup & 4) == 0 ? '-' : 'r');
                                permStringBuilder.append((modeGroup & 2) == 0 ? '-' : 'w');
                                permStringBuilder.append((modeGroup & 1) == 0 ? '-' : 'x');
                                permStringBuilder.append((modeOthers & 4) == 0 ? '-' : 'r');
                                permStringBuilder.append((modeOthers & 2) == 0 ? '-' : 'w');
                                permStringBuilder.append((modeOthers & 1) == 0 ? '-' : 'x');
                                String permString = permStringBuilder.toString();
                                Files.setPosixFilePermissions(outputFilePath, PosixFilePermissions.fromString(permString));
                            }
                            tarArchiveInputStream.close();
                        }else if(extractionMode == EXTRACTION_MODE_ZIP) {
                            File destDir = new File(extractionDir);
                            if (!destDir.exists()) {
                                destDir.mkdir();
                            }
                            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(downloadTarget));
                            ZipEntry entry = zipIn.getNextEntry();
                            // iterates over entries in the zip file
                            while (entry != null) {
                                String filePath = extractionDir + File.separator + entry.getName();
                                if (!entry.isDirectory()) {
                                    // if the entry is a file, extracts it
                                    extractFile(zipIn, filePath);
                                } else {
                                    // if the entry is a directory, make the directory
                                    File dir = new File(filePath);
                                    dir.mkdir();
                                }
                                zipIn.closeEntry();
                                entry = zipIn.getNextEntry();
                            }
                            zipIn.close();
                        }else {
                            throw new IllegalArgumentException(String.format(
                                    "extractionMode %d isn't supported",
                                    extractionMode));
                        }
                    }else {
                        if(!new File(extractionDir).isDirectory()) {
                            messageHandler.handle(new Message(String.format("extraction directory '%s' exists, but is not a directory", extractionDir),
                                    JOptionPane.ERROR_MESSAGE,
                                    "Invalid extraction target"));
                            return false;
                        }
                    }
                } catch(IOException ex) {
                    LOGGER.error("unexpected exception, see nested exception for details", ex);
                    messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
                    return false;
                }
                return true;
            }

            @Override
            protected void done() {
                dialog.setVisible(false);
            }
        };
        downloadWorker.execute();
        dialog.setVisible(true);
        if(dialog.isCanceled()) {
            return false;
                //returning false here will result in another
                //MySQLDownloadDialog being displayed in which the whole
                //download action can be canceled
        }
        try {
            return downloadWorker.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        IOUtils.copy(zipIn, bos);
        bos.flush();
        bos.close();
    }

    @Override
    public MySQLAutoPersistenceStorageConf getStorageConf() {
        return storageConf;
    }

    @Override
    public void save() {
        String baseDir = baseDirTextField.getText();
        this.storageConf.setBaseDir(baseDir);
        String mysqld = mysqldTextField.getText();
        this.storageConf.setMysqld(mysqld.isEmpty() ? null : mysqld);
        String mysqladmin = mysqladminTextField.getText();
        this.storageConf.setMysqladmin(mysqladmin.isEmpty() ? null : mysqladmin);
        String mysql = mysqlTextField.getText();
        this.storageConf.setMysql(mysql.isEmpty() ? null : mysql);
        this.storageConf.setDatabaseName(this.databaseNameTextField.getText());
        this.storageConf.setDatabaseDir(this.databaseDirTextField.getText());
        this.storageConf.setHostname(this.hostnameTextField.getText());
        this.storageConf.setPort((int) this.portSpinner.getValue());
        String username = this.usernameTextField.getText();
        this.storageConf.setUsername(username);
        String password = new String(this.passwordPasswordField.getPassword());
        this.storageConf.setPassword(password);
    }

    @Override
    public void cancel() {
        //do nothing
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField baseDirTextField;
    private javax.swing.JLabel baseDirTextFieldLabel;
    private javax.swing.JLabel binariesLabel;
    private javax.swing.JTextField databaseDirTextField;
    private javax.swing.JLabel databaseDirTextFieldLabel;
    private javax.swing.JTextField databaseNameTextField;
    private javax.swing.JLabel databaseNameTextFieldLabel;
    private javax.swing.JButton downloadButton;
    private javax.swing.JTextField hostnameTextField;
    private javax.swing.JLabel hostnameTextFieldLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField mysqlTextField;
    private javax.swing.JLabel mysqlTextFieldLabel;
    private javax.swing.JTextField mysqladminTextField;
    private javax.swing.JLabel mysqladminTextFieldLabel;
    private javax.swing.JTextField mysqldTextField;
    private javax.swing.JLabel mysqldTextFieldLabel;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JPasswordField passwordPasswordField;
    private javax.swing.JSpinner portSpinner;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JTextField usernameTextField;
    // End of variables declaration//GEN-END:variables
}
