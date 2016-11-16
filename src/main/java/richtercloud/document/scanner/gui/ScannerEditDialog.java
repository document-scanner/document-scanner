/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package richtercloud.document.scanner.gui;

import au.com.southsky.jfreesane.OptionValueType;
import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneException;
import au.com.southsky.jfreesane.SaneOption;
import au.com.southsky.jfreesane.SaneWord;
import java.awt.Dialog;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.MutableComboBoxModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.message.handler.MessageHandler;

/**
 * Provides configuration for scan mode and resolution of SANE device with GUI
 * components in a {@link JDialog}.
 *
 * Option value changes are performed on the referenced {@link SaneDevice}
 * directly in order to KISS.
 *
 * @author richter
 */
public class ScannerEditDialog extends javax.swing.JDialog {
    private static final long serialVersionUID = 1L;
    private MutableComboBoxModel<String> modeComboBoxModel = new DefaultComboBoxModel<>();
    private MutableComboBoxModel<Integer> resolutionComboBoxModel = new DefaultComboBoxModel<>();
    private MutableComboBoxModel<String> documentSourceComboBoxModel = new DefaultComboBoxModel<>();
    private final static Logger LOGGER = LoggerFactory.getLogger(ScannerEditDialog.class);
    private final SaneDevice device;
    private final MessageHandler messageHandler;
    public final static String MODE_OPTION_NAME = "mode";
    public final static String RESOLUTION_OPTION_NAME = "resolution";
    public final static String DOCUMENT_SOURCE_OPTION_NAME = "source";
    private final ScannerConf scannerConf;

    public ScannerEditDialog(Dialog parent,
            final SaneDevice device,
            ScannerConf scannerConf,
            MessageHandler messageHandler) throws IOException, SaneException {
        super(parent,
                true //modal
        );
        if(device == null) {
            throw new IllegalArgumentException("device mustn't be null");
        }
        this.device = device;
        if(messageHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.scannerConf = scannerConf;
        this.messageHandler = messageHandler;
        init(device,
                scannerConf);
    }

    /**
     * Creates new form ScannerEditDialog
     * @param parent
     * @param device
     * @param scannerConf
     * @param messageHandler
     * @throws java.io.IOException if {@link SaneDevice#open() } fails
     * @throws au.com.southsky.jfreesane.SaneException if
     * {@link SaneDevice#open() } fails
     */
    public ScannerEditDialog(java.awt.Frame parent,
            final SaneDevice device,
            ScannerConf scannerConf,
            MessageHandler messageHandler) throws IOException, SaneException {
        super(parent,
                DocumentScanner.generateApplicationWindowTitle(String.format("Editing scanner settings of %s", device.toString()),
                        DocumentScanner.APP_NAME,
                        DocumentScanner.APP_VERSION),
                true //modal
        );
        if(device == null) {
            throw new IllegalArgumentException("device mustn't be null");
        }
        this.device = device;
        if(messageHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.scannerConf = scannerConf;
        this.messageHandler = messageHandler;
        init(device,
                scannerConf);
    }

    private void init(final SaneDevice device,
            final ScannerConf scannerConf) throws IOException, SaneException {
        initComponents();
        if(!device.isOpen()) {
            device.open();
        }
        configureDefaultOptionValues(device,
                scannerConf);
        //values in scannerConf should be != null after
        //configureDefaultOptionValues
        for(String mode : device.getOption("mode").getStringConstraints()) {
            modeComboBoxModel.addElement(mode);
        }
        this.modeComboBox.setSelectedItem(scannerConf.getMode());
        for(SaneWord resolution : device.getOption("resolution").getWordConstraints()) {
            resolutionComboBoxModel.addElement(resolution.integerValue());
        }
        this.resolutionComboBox.setSelectedItem(scannerConf.getResolution());
        List<String> documentSourceConstraints = device.getOption("source").getStringConstraints();
        for(String documentSource : documentSourceConstraints) {
            this.documentSourceComboBoxModel.addElement(documentSource);
        }
        this.documentSourceComboBox.setSelectedItem(scannerConf.getSource());
        //add ItemListener after setup
        this.modeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String mode;
                try {
                    mode = (String) ScannerEditDialog.this.modeComboBox.getSelectedItem();
                    ScannerEditDialog.this.device.getOption("mode").setStringValue(mode);
                    scannerConf.setMode(mode);
                } catch (IOException | SaneException ex) {
                    //not supposed to happen
                    throw new RuntimeException(ex);
                }
            }
        });
        this.resolutionComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int resolution;
                try {
                    resolution = Integer.valueOf((String)ScannerEditDialog.this.resolutionComboBox.getSelectedItem());
                    ScannerEditDialog.this.device.getOption("resolution").setIntegerValue(resolution);
                    scannerConf.setResolution(resolution);
                } catch (IOException | SaneException ex) {
                    //not supposed to happen
                    throw new RuntimeException(ex);
                }
            }
        });
        this.documentSourceComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String documentSource;
                try {
                    documentSource = (String) ScannerEditDialog.this.documentSourceComboBox.getSelectedItem();
                    ScannerEditDialog.this.device.getOption("source").setStringValue(documentSource);
                    scannerConf.setSource(documentSource);
                } catch (IOException | SaneException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private static String selectBestDocumentSource(List<String> documentSourceConstraints) {
        String documentSource = null;
        for(String documentSourceConstraint : documentSourceConstraints) {
            if("Duplex".equalsIgnoreCase(documentSourceConstraint)) {
                documentSource = documentSourceConstraint;
                break;
            }
        }
        if(documentSource == null) {
            for(String documentSourceConstraint : documentSourceConstraints) {
                if("ADF".equalsIgnoreCase(documentSourceConstraint)
                        || "Automatic document feeder".equalsIgnoreCase(documentSourceConstraint)) {
                    documentSource = documentSourceConstraint;
                    break;
                }
            }
        }
        if(documentSource == null) {
            documentSource = documentSourceConstraints.get(0);
        }
        return documentSource;
    }

    /**
     * Sets convenient default values on the scanner ("color" scan mode and the
     * resolution value closest to 300 DPI). Readability and writability of
     * options are checked. The type of the options are checked as well in order
     * to fail with a helpful error message in case of an errornous SANE
     * implementation.
     *
     * For a list of SANE options see
     * http://www.sane-project.org/html/doc014.html.
     *
     * @param device
     * @param scannerConf the {@link ScannerConf} to retrieve eventually
     * existing values from (e.g. persisted values from previous runs)
     * @throws IOException
     * @throws SaneException
     * @throws IllegalArgumentException if the option denoted by
     * {@link #MODE_OPTION_NAME} or {@link #RESOLUTION_OPTION_NAME} isn't
     * readable or writable
     */
    public static void configureDefaultOptionValues(SaneDevice device,
            ScannerConf scannerConf) throws IOException, SaneException {
        if(!device.isOpen()) {
            device.open();
        }
        SaneOption modeOption = device.getOption(MODE_OPTION_NAME);
        if(!modeOption.isReadable()) {
            throw new IllegalArgumentException(String.format("option '%s' isn't readable", MODE_OPTION_NAME));
        }
        if(!modeOption.getType().equals(OptionValueType.STRING)) {
            throw new IllegalArgumentException(String.format("Option '%s' isn't of type STRING. This indicates an errornous SANE implementation. Can't proceed.", MODE_OPTION_NAME));
        }
        String mode = scannerConf.getMode();
        if(mode == null) {
            for(String modeConstraint : modeOption.getStringConstraints()) {
                if("color".equalsIgnoreCase(modeConstraint.trim())) {
                    mode = modeConstraint;
                    break;
                }
            }
            if(mode == null) {
                mode = modeOption.getStringConstraints().get(0);
            }
            scannerConf.setMode(mode);
        }
        if(!modeOption.isWriteable()) {
            throw new IllegalArgumentException(String.format("Option '%s' isn't writable.", MODE_OPTION_NAME));
        }
        LOGGER.debug(String.format("setting default mode '%s' on device '%s'", mode, device));
        modeOption.setStringValue(mode);
        SaneOption resolutionOption = device.getOption(RESOLUTION_OPTION_NAME);
        if(!resolutionOption.isReadable()) {
            throw new IllegalArgumentException(String.format("Option '%s' isn't readable.", RESOLUTION_OPTION_NAME));
        }
        if(!resolutionOption.getType().equals(OptionValueType.INT)) {
            throw new IllegalArgumentException(String.format("Option '%s' isn't of type INT. This indicates an errornous SANE implementation. Can't proceed.", RESOLUTION_OPTION_NAME));
        }
        Integer resolution = scannerConf.getResolution();
        if(resolution == null) {
            int resolutionDifference = Integer.MAX_VALUE, resolutionWish = 300;
            for(SaneWord resolutionConstraint : resolutionOption.getWordConstraints()) {
                int resolutionConstraintValue = resolutionConstraint.integerValue();
                int resolutionConstraintDifference = Math.abs(resolutionWish-resolutionConstraintValue);
                if(resolutionConstraintDifference < resolutionDifference) {
                    resolution = resolutionConstraintValue;
                    resolutionDifference = resolutionConstraintDifference;
                    if(resolutionDifference == 0) {
                        //not possible to find more accurate values
                        break;
                    }
                }
            }
            assert resolution != null;
            scannerConf.setResolution(resolution);
        }
        if(!resolutionOption.isWriteable()) {
            throw new IllegalArgumentException(String.format("option '%s' isn't writable", RESOLUTION_OPTION_NAME));
        }
        LOGGER.debug(String.format("setting default resolution '%d' on device '%s'", resolution, device));
        resolutionOption.setIntegerValue(resolution);
        SaneOption documentSourceOption = device.getOption(DOCUMENT_SOURCE_OPTION_NAME);
        if(!documentSourceOption.isReadable()) {
            throw new IllegalArgumentException(String.format("Option '%s' isn't readable.", DOCUMENT_SOURCE_OPTION_NAME));
        }
        if(!documentSourceOption.getType().equals(OptionValueType.STRING)) {
            throw new IllegalArgumentException(String.format("Option '%s' "
                    + "isn't of type STRING. This indicates an errornous SANE "
                    + "implementation. Can't proceed.",
                    DOCUMENT_SOURCE_OPTION_NAME));
        }
        String documentSource = scannerConf.getSource();
        if(documentSource == null) {
            documentSource = selectBestDocumentSource(documentSourceOption.getStringConstraints());
            assert documentSource != null;
            scannerConf.setSource(documentSource);
        }
        if(!documentSourceOption.isWriteable()) {
            throw new IllegalArgumentException(String.format("option '%s' isn't writable", DOCUMENT_SOURCE_OPTION_NAME));
        }
        LOGGER.debug(String.format("setting default document source '%s' on device '%s'", documentSource, device));
        documentSourceOption.setStringValue(documentSource);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        modeComboBoxLabel = new javax.swing.JLabel();
        modeComboBox = new javax.swing.JComboBox<>();
        resolutionComboBox = new javax.swing.JComboBox<>();
        resolutionComboBoxLabel = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();
        documentSourceComboBox = new javax.swing.JComboBox<>();
        documentSourceComboBoxLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        modeComboBoxLabel.setText("Mode");

        modeComboBox.setModel(modeComboBoxModel);

        resolutionComboBox.setModel(resolutionComboBoxModel);

        resolutionComboBoxLabel.setText("Resolution");

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        documentSourceComboBox.setModel(documentSourceComboBoxModel);

        documentSourceComboBoxLabel.setText("Document source");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(closeButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(resolutionComboBoxLabel)
                            .addComponent(modeComboBoxLabel)
                            .addComponent(documentSourceComboBoxLabel))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(documentSourceComboBox, 0, 228, Short.MAX_VALUE)
                            .addComponent(resolutionComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(modeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modeComboBoxLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resolutionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resolutionComboBoxLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(documentSourceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(documentSourceComboBoxLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(closeButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JComboBox<String> documentSourceComboBox;
    private javax.swing.JLabel documentSourceComboBoxLabel;
    private javax.swing.JComboBox<String> modeComboBox;
    private javax.swing.JLabel modeComboBoxLabel;
    private javax.swing.JComboBox<Integer> resolutionComboBox;
    private javax.swing.JLabel resolutionComboBoxLabel;
    // End of variables declaration//GEN-END:variables
}
