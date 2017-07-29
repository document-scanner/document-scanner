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

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.Constants;
import richtercloud.document.scanner.gui.DocumentScanner;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.jhbuild.java.wrapper.ArchitectureNotRecognizedException;
import richtercloud.jhbuild.java.wrapper.DownloadCombi;
import richtercloud.jhbuild.java.wrapper.DownloadFailureCallbackReation;
import richtercloud.jhbuild.java.wrapper.DownloadTools;
import richtercloud.jhbuild.java.wrapper.ExtractionException;
import richtercloud.jhbuild.java.wrapper.ExtractionMode;
import richtercloud.jhbuild.java.wrapper.MD5SumCheckUnequalsCallbackReaction;
import richtercloud.jhbuild.java.wrapper.OSNotRecognizedException;
import richtercloud.jhbuild.java.wrapper.SupportedOS;
import richtercloud.message.handler.ConfirmMessageHandler;
import richtercloud.message.handler.ExceptionMessage;
import richtercloud.message.handler.IssueHandler;
import richtercloud.message.handler.Message;
import richtercloud.reflection.form.builder.jpa.storage.MySQLAutoPersistenceStorageConf;

/**
 *
 * @author richter
 */
public class MySQLAutoPersistenceStorageConfPanel extends StorageConfPanel<MySQLAutoPersistenceStorageConf> {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(MySQLAutoPersistenceStorageConfPanel.class);
    public final static String MYSQL_DOWNLOAD_URL_LINUX_64 = "https://cdn.mysql.com/Downloads/MySQL-5.7/mysql-5.7.19-linux-glibc2.12-x86_64.tar.gz";
    public final static String MYSQL_DOWNLOAD_URL_LINUX_32 = "https://cdn.mysql.com/Downloads/MySQL-5.7/mysql-5.7.19-linux-glibc2.12-i686.tar.gz";
    public final static String MYSQL_DOWNLOAD_URL_WINDOWS_64 = "https://cdn.mysql.com/Downloads/MySQL-5.7/mysql-5.7.19-winx64.zip";
    public final static String MYSQL_DOWNLOAD_URL_WINDOWS_32 = "https://cdn.mysql.com/Downloads/MySQL-5.7/mysql-5.7.19-win32.zip";
    public final static String MYSQL_DOWNLOAD_URL_MAC_OSX_64 = "https://cdn.mysql.com/Downloads/MySQL-5.7/mysql-5.7.19-macos10.12-x86_64.tar.gz";
        //Mac OSX doesn't have 32-bit versions
    private final MySQLAutoPersistenceStorageConf storageConf;
    private final IssueHandler issueHandler;
    private final ConfirmMessageHandler confirmMessageHandler;
    /**
     * The target of the MySQL download. Doesn't need to be exposed to the user.
     */
    protected final static String MYSQL_DOWNLOAD_TARGET_LINUX_64 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.19-linux-glibc2.12-x86_64.tar.gz").getAbsolutePath();
    protected final static String MYSQL_DOWNLOAD_TARGET_LINUX_32 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.19-linux-glibc2.12-i686.tar.gz").getAbsolutePath();
        //downloading same file with `i386` instead of `i686` works as well, but
        //it's unclear what it refers to (MD5 sums need to be adjusted and the
        //`i686` file proposed on the MySQL download page
    protected final static String MYSQL_DOWNLOAD_TARGET_WINDOWS_64 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.19-winx64.zip").getAbsolutePath();
    protected final static String MYSQL_DOWNLOAD_TARGET_WINDOWS_32 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.19-win32.zip").getAbsolutePath();
    protected final static String MYSQL_DOWNLOAD_TARGET_MAC_OSX_64 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.19-macos10.12-x86_64.tar.gz").getAbsolutePath();
    /**
     * The resulting directory after extraction. Doesn't need to be exposed to
     * the user.
     */
    protected final static String MYSQL_EXTRACTION_LOCATION_LINUX_64 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.19-linux-glibc2.12-x86_64").getAbsolutePath();
    protected final static String MYSQL_EXTRACTION_LOCATION_LINUX_32 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.19-linux-glibc2.12-i686").getAbsolutePath();
    protected final static String MYSQL_EXTRACTION_LOCATION_WINDOWS_64 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.19-winx64").getAbsolutePath();
    protected final static String MYSQL_EXTRACTION_LOCATION_WINDOWS_32 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.19-win32").getAbsolutePath();
    protected final static String MYSQL_EXTRACTION_LOCATION_MAC_OSX_64 = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT, "mysql-5.7.19-macos10.12-x86_64").getAbsolutePath();
    protected final static String MD5_SUM_LINUX_64 = "dbe7e5e820377c29d8681005065e5728";
    protected final static String MD5_SUM_LINUX_32 = "32c4e286b7016bc1f45995849c235e41";
    protected final static String MD5_SUM_WINDOWS_64 = "f4397d33052c5257c5e6cdabd8819024";
    protected final static String MD5_SUM_WINDOWS_32 = "ae800b20d9e5eb3f7c0d647c3569be1e";
    protected final static String MD5_SUM_MAC_OSX_64 = "48dfa875670f3c6468acc3ab72252f9e";
    protected final static ExtractionMode MYSQL_EXTRACTION_MODE_LINUX_64 = ExtractionMode.EXTRACTION_MODE_TAR_GZ;
    protected final static ExtractionMode MYSQL_EXTRACTION_MODE_LINUX_32 = ExtractionMode.EXTRACTION_MODE_TAR_GZ;
    protected final static ExtractionMode MYSQL_EXTRACTION_MODE_WINDOWS_64 = ExtractionMode.EXTRACTION_MODE_ZIP;
    protected final static ExtractionMode MYSQL_EXTRACTION_MODE_WINDOWS_32 = ExtractionMode.EXTRACTION_MODE_ZIP;
    protected final static ExtractionMode MYSQL_EXTRACTION_MODE_MAC_OSX_64 = ExtractionMode.EXTRACTION_MODE_TAR_GZ;
    private final boolean skipMD5SumCheck;
    private final Map<SupportedOS, DownloadCombi> oSDownloadCombiMap = new ImmutableMap.Builder<SupportedOS, DownloadCombi>()
            .put(SupportedOS.LINUX_32, new DownloadCombi(MYSQL_DOWNLOAD_URL_LINUX_32,
                    MYSQL_DOWNLOAD_TARGET_LINUX_32,
                    MYSQL_EXTRACTION_MODE_LINUX_32,
                    MYSQL_EXTRACTION_LOCATION_LINUX_32,
                    MD5_SUM_LINUX_32))
            .put(SupportedOS.LINUX_64, new DownloadCombi(MYSQL_DOWNLOAD_URL_LINUX_64,
                    MYSQL_DOWNLOAD_TARGET_LINUX_64,
                    MYSQL_EXTRACTION_MODE_LINUX_64,
                    MYSQL_EXTRACTION_LOCATION_LINUX_64,
                    MD5_SUM_LINUX_64))
            .put(SupportedOS.WINDOWS_32, new DownloadCombi(MYSQL_DOWNLOAD_URL_WINDOWS_32,
                    MYSQL_DOWNLOAD_TARGET_WINDOWS_32,
                    MYSQL_EXTRACTION_MODE_WINDOWS_32,
                    MYSQL_EXTRACTION_LOCATION_WINDOWS_32,
                    MD5_SUM_WINDOWS_32))
            .put(SupportedOS.WINDOWS_64, new DownloadCombi(MYSQL_DOWNLOAD_URL_WINDOWS_64,
                    MYSQL_DOWNLOAD_TARGET_WINDOWS_64,
                    MYSQL_EXTRACTION_MODE_WINDOWS_64,
                    MYSQL_EXTRACTION_LOCATION_WINDOWS_64,
                    MD5_SUM_WINDOWS_64))
            .put(SupportedOS.MAC_OSX_64, new DownloadCombi(MYSQL_DOWNLOAD_URL_MAC_OSX_64,
                    MYSQL_DOWNLOAD_TARGET_MAC_OSX_64,
                    MYSQL_EXTRACTION_MODE_MAC_OSX_64,
                    MYSQL_EXTRACTION_LOCATION_MAC_OSX_64,
                    MD5_SUM_MAC_OSX_64))
            .build();

    public MySQLAutoPersistenceStorageConfPanel(MySQLAutoPersistenceStorageConf storageConf,
            IssueHandler issueHandler,
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
        this.issueHandler = issueHandler;
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
        portSpinnerLabel = new javax.swing.JLabel();
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
        directorySeparator = new javax.swing.JSeparator();
        mysqldTextField = new javax.swing.JTextField();
        mysqladminTextField = new javax.swing.JTextField();
        mysqlTextField = new javax.swing.JTextField();
        mysqldTextFieldLabel = new javax.swing.JLabel();
        mysqladminTextFieldLabel = new javax.swing.JLabel();
        mysqlTextFieldLabel = new javax.swing.JLabel();
        binariesLabel = new javax.swing.JLabel();

        passwordLabel.setText("Password");

        passwordPasswordField.setText("jPasswordField1");

        portSpinnerLabel.setText("Port");

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
                    .addComponent(directorySeparator)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(downloadButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(databaseNameTextFieldLabel)
                            .addComponent(databaseDirTextFieldLabel)
                            .addComponent(hostnameTextFieldLabel)
                            .addComponent(portSpinnerLabel)
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
                .addComponent(directorySeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(portSpinnerLabel))
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

    protected DownloadCombi getDownloadCombi() {
        SupportedOS currentOS;
        try {
            currentOS = DownloadTools.getCurrentOS();
        } catch (OSNotRecognizedException ex) {
            this.issueHandler.handle(new Message(String.format("Operating "
                    + "system %s isn't supported for automatic download, "
                    + "please download MySQL manually and set pathes "
                    + "accordingly",
                            SystemUtils.OS_NAME),
                    JOptionPane.WARNING_MESSAGE,
                    "Operating system not supported"));
            return null;
        } catch (ArchitectureNotRecognizedException ex) {
            this.issueHandler.handle(new Message(String.format("%s "
                    + "operating system architecture %s isn't supported "
                    + "for automatic download, please download MySQL "
                    + "manually and set pathes accordingly",
                            ex.getoSName(),
                            SystemUtils.OS_ARCH),
                    JOptionPane.WARNING_MESSAGE,
                    "Linux OS architecture not supported"));
            return null;
        }
        DownloadCombi retValue = oSDownloadCombiMap.get(currentOS);
        assert retValue != null;
        return retValue;
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void downloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadButtonActionPerformed
        try {
            DownloadCombi downloadCombi = getDownloadCombi();
            if(downloadCombi == null) {
                //system could not be recognized
                return;
            }
            mySQLDownload(downloadCombi);
            baseDirTextField.setText(downloadCombi.getExtractionLocation());
                //not necessary to set on storageConf because it'll be set in save
        }catch(Throwable ex) {
            LOGGER.error("unexpected exception during download of MySQL occured",
                    ex);
            issueHandler.handleUnexpectedException(new ExceptionMessage(ex));
        }
    }//GEN-LAST:event_downloadButtonActionPerformed

    protected boolean mySQLDownload(DownloadCombi downloadCombi) throws IOException, ExtractionException {
        boolean retValue = DownloadTools.downloadFile(downloadCombi,
                SwingUtilities.getWindowAncestor(this), //downloadDialogParent
                DocumentScanner.generateApplicationWindowTitle("Downloading MySQL",
                        Constants.APP_NAME,
                        Constants.APP_VERSION), //downloadDialogTitle
                "Downloading MySQL", //labelText
                "Downloading MySQL", //progressBarText
                skipMD5SumCheck,
            ex -> {
                DownloadFailureCallbackReation answer = confirmMessageHandler.confirm(new Message(String.format(
                        "Download failed because of the following exception: %s",
                                ex.getMessage()),
                        JOptionPane.ERROR_MESSAGE,
                        "Download failed"),
                        DownloadFailureCallbackReation.CANCEL,
                        DownloadFailureCallbackReation.RETRY);
                return answer;
            },
            (String md5SumExpected, String md5SumActual) -> {
                MD5SumCheckUnequalsCallbackReaction answer = confirmMessageHandler.confirm(new Message(String.format(
                        "MD5 sum %s "
                        + "of download '%s' doesn't match the "
                        + "specified MD5 sum %s. This "
                        + "indicates an incomplete download, a "
                        + "wrong specified MD5 sum or an error "
                        + "during transfer.",
                                md5SumActual,
                                downloadCombi.getDownloadTarget(),
                                md5SumExpected),
                        JOptionPane.ERROR_MESSAGE,
                        "MD5 sum verification failed"),
                        MD5SumCheckUnequalsCallbackReaction.RETRY,
                        MD5SumCheckUnequalsCallbackReaction.CANCEL);
                return answer;
            } //mD5SumCheckUnequalsCallback
        );
        return retValue;
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
        String password = String.valueOf(this.passwordPasswordField.getPassword());
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
    private javax.swing.JSeparator directorySeparator;
    private javax.swing.JButton downloadButton;
    private javax.swing.JTextField hostnameTextField;
    private javax.swing.JLabel hostnameTextFieldLabel;
    private javax.swing.JTextField mysqlTextField;
    private javax.swing.JLabel mysqlTextFieldLabel;
    private javax.swing.JTextField mysqladminTextField;
    private javax.swing.JLabel mysqladminTextFieldLabel;
    private javax.swing.JTextField mysqldTextField;
    private javax.swing.JLabel mysqldTextFieldLabel;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JPasswordField passwordPasswordField;
    private javax.swing.JSpinner portSpinner;
    private javax.swing.JLabel portSpinnerLabel;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JTextField usernameTextField;
    // End of variables declaration//GEN-END:variables
}
