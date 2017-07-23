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
package richtercloud.document.scanner.gui.scanner;

import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneException;
import au.com.southsky.jfreesane.SaneWord;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.Constants;
import richtercloud.document.scanner.gui.DocumentScanner;
import richtercloud.document.scanner.gui.scanresult.DocumentController;
import richtercloud.message.handler.Message;
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
    public final static String TOP_LEFT_X = "tl-x";
    public final static String TOP_LEFT_Y = "tl-y";
    public final static String BOTTOM_RIGHT_X = "br-x";
    public final static String BOTTOM_RIGHT_Y = "br-y";
    private final ScannerConf scannerConf;
    private final DefaultListModel<ScannerConfPaperFormat> paperFormatListModel = new DefaultListModel<>();
    private final DocumentController documentController;

    public ScannerEditDialog(Dialog parent,
            DocumentController documentController,
            final SaneDevice device,
            ScannerConf scannerConf,
            int resolutionWish,
            MessageHandler messageHandler) throws IOException, SaneException {
        super(parent,
                true //modal
        );
        if(documentController == null) {
            throw new IllegalArgumentException("documentController mustn't be null");
        }
        this.documentController = documentController;
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
                scannerConf,
                resolutionWish);
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
            DocumentController documentController,
            final SaneDevice device,
            ScannerConf scannerConf,
            int resolutionWish,
            MessageHandler messageHandler) throws IOException, SaneException {
        super(parent,
                DocumentScanner.generateApplicationWindowTitle(String.format("Editing scanner settings of %s", device.toString()),
                        Constants.APP_NAME,
                        Constants.APP_VERSION),
                true //modal
        );
        if(documentController == null) {
            throw new IllegalArgumentException("documentController mustn't be null");
        }
        this.documentController = documentController;
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
                scannerConf,
                resolutionWish);
    }

    private void init(final SaneDevice device,
            final ScannerConf scannerConf,
            int resolutionWish) throws IOException, SaneException {
        initComponents();
        if(!device.isOpen()) {
            device.open();
                //No need to think about opening in background because the user
                //requested an edit dialog and wants to edit as far as possible
        }
        documentController.configureDefaultOptionValues(device,
                scannerConf,
                resolutionWish);
        //values in scannerConf should be != null after
        //configureDefaultOptionValues
        //set values after adding listeners below
        for(String mode : device.getOption("mode").getStringConstraints()) {
            modeComboBoxModel.addElement(mode);
        }
        for(SaneWord resolution : device.getOption("resolution").getWordConstraints()) {
            resolutionComboBoxModel.addElement(resolution.integerValue());
        }
        List<String> documentSourceConstraints = device.getOption("source").getStringConstraints();
        for(String documentSource : documentSourceConstraints) {
            this.documentSourceComboBoxModel.addElement(documentSource);
        }
        for(ScannerConfPaperFormat paperFormat : scannerConf.getAvailablePaperFormats()) {
            this.paperFormatListModel.addElement(paperFormat);
        }
        assert !scannerConf.getAvailablePaperFormats().isEmpty();
        this.paperFormatList.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                assert value instanceof ScannerConfPaperFormat;
                ScannerConfPaperFormat valueCast = (ScannerConfPaperFormat) value;
                String paperFormatString = String.format("%s (%d x %d)",
                        valueCast.getName(),
                        (int)valueCast.getWidth(), //skip trailing zeros
                            //because 0.1 mm are not interesting for
                            //paper format selection
                        (int)valueCast.getHeight());
                return super.getListCellRendererComponent(list,
                        paperFormatString,
                        index,
                        isSelected,
                        cellHasFocus);
            }
        });
        //add ItemListener after setup
        this.modeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String mode;
                try {
                    mode = (String) ScannerEditDialog.this.modeComboBox.getSelectedItem();
                    documentController.setMode(device,
                            mode);
                    scannerConf.setMode(mode);
                } catch(IllegalArgumentException ex) {
                    messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
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
                    resolution = (Integer) ScannerEditDialog.this.resolutionComboBox.getSelectedItem();
                    documentController.setResolution(device, resolution);
                    scannerConf.setResolution(resolution);
                } catch(IllegalArgumentException ex) {
                    messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
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
                    documentController.setDocumentSource(device,
                            documentSource);
                    scannerConf.setSource(documentSource);
                } catch(IllegalArgumentException ex) {
                    messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
                } catch (IOException | SaneException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        this.paperFormatList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                try {
                    assert paperFormatList.getSelectedValue() != null;
                    ScannerConfPaperFormat selectedFormat = paperFormatList.getSelectedValue();
                    documentController.setPaperFormat(device,
                            selectedFormat.getWidth(),
                            selectedFormat.getHeight());
                    scannerConf.setPaperFormat(selectedFormat);
                } catch(IllegalArgumentException ex) {
                    messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
                } catch (IOException | SaneException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        this.modeComboBox.setSelectedItem(scannerConf.getMode());
        this.resolutionComboBox.setSelectedItem(scannerConf.getResolution());
        this.documentSourceComboBox.setSelectedItem(scannerConf.getSource());
        this.paperFormatList.setSelectedValue(scannerConf.getPaperFormat(),
                true //shouldScroll
        );
            //should trigger selection listeners
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
        paperFormatListLabel = new javax.swing.JLabel();
        paperFormatListScrollPane = new javax.swing.JScrollPane();
        paperFormatList = new javax.swing.JList<>();
        paperFormatAddButton = new javax.swing.JButton();
        paperFormatEditButton = new javax.swing.JButton();

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

        paperFormatListLabel.setText("Paper format");

        paperFormatList.setModel(paperFormatListModel);
        paperFormatList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        paperFormatListScrollPane.setViewportView(paperFormatList);

        paperFormatAddButton.setText("Add");
        paperFormatAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paperFormatAddButtonActionPerformed(evt);
            }
        });

        paperFormatEditButton.setText("Edit");
        paperFormatEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paperFormatEditButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 375, Short.MAX_VALUE)
                                .addComponent(closeButton))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(documentSourceComboBoxLabel)
                                    .addComponent(paperFormatListLabel))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(modeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(resolutionComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(documentSourceComboBox, 0, 278, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(paperFormatListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(paperFormatAddButton, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
                                            .addComponent(paperFormatEditButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
                        .addGap(12, 12, 12))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(resolutionComboBoxLabel)
                            .addComponent(modeComboBoxLabel))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(paperFormatListLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(paperFormatAddButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(paperFormatEditButton)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(closeButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(paperFormatListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 66, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed

    private void paperFormatAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paperFormatAddButtonActionPerformed
        ScannerConfPaperFormat paperFormat = new ScannerConfPaperFormat();
        ScannerConfPaperFormatDialog paperFormatDialog = new ScannerConfPaperFormatDialog(this,
                messageHandler,
                paperFormat);
        paperFormatDialog.setVisible(true);
    }//GEN-LAST:event_paperFormatAddButtonActionPerformed

    private void paperFormatEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paperFormatEditButtonActionPerformed
        ScannerConfPaperFormatDialog paperFormatDialog = new ScannerConfPaperFormatDialog(this,
                messageHandler,
                scannerConf.getPaperFormat());
        paperFormatDialog.setVisible(true);
    }//GEN-LAST:event_paperFormatEditButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JComboBox<String> documentSourceComboBox;
    private javax.swing.JLabel documentSourceComboBoxLabel;
    private javax.swing.JComboBox<String> modeComboBox;
    private javax.swing.JLabel modeComboBoxLabel;
    private javax.swing.JButton paperFormatAddButton;
    private javax.swing.JButton paperFormatEditButton;
    private javax.swing.JList<ScannerConfPaperFormat> paperFormatList;
    private javax.swing.JLabel paperFormatListLabel;
    private javax.swing.JScrollPane paperFormatListScrollPane;
    private javax.swing.JComboBox<Integer> resolutionComboBox;
    private javax.swing.JLabel resolutionComboBoxLabel;
    // End of variables declaration//GEN-END:variables
}
