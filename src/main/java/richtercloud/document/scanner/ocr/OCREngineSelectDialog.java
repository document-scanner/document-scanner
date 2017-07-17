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
package richtercloud.document.scanner.ocr;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.MutableComboBoxModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.ifaces.OCREngineConf;
import richtercloud.document.scanner.ifaces.OCREngineConfValidationException;
import richtercloud.message.handler.ExceptionMessage;
import richtercloud.message.handler.IssueHandler;
import richtercloud.message.handler.Message;

/**
 *
 * @author richter
 */
public class OCREngineSelectDialog extends javax.swing.JDialog {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(OCREngineSelectDialog.class);
    private final Map<OCREngineConfPanel<?>, OCREngineConf> originalEngineConfs = new HashMap<>();
    private final Map<Class<? extends OCREngineConf>, OCREngineConfPanel<?>> oCREngineConfPanelMap = new HashMap<>();
    private final MutableComboBoxModel<OCREngineConf> oCREngineComboBoxModel = new DefaultComboBoxModel<>();
    //@TODO: implement class path discovery of associated conf panel with annotations
    private final IssueHandler issueHandler;
    private DocumentScannerConf documentScannerConf;
    private ListCellRenderer<Object> oCRDialogEngineComboBoxRenderer = new DefaultListCellRenderer() {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            OCREngineConf valueCast = (OCREngineConf) value;
            OCREngineConfInfo oCREngineInfo = valueCast.getClass().getAnnotation(OCREngineConfInfo.class);
            String value0;
            if (oCREngineInfo != null) {
                value0 = oCREngineInfo.name();
            } else {
                value0 = valueCast.getClass().getSimpleName();
            }
            return super.getListCellRendererComponent(list, value0, index, isSelected, cellHasFocus);
        }

    };

    /**
     * Creates new form OCREngineSelectDialog
     */
    public OCREngineSelectDialog(Frame parent,
            DocumentScannerConf documentScannerConf,
            IssueHandler issueHandler) {
        super(parent,
                true //modal
        );
        this.issueHandler = issueHandler;
        this.documentScannerConf = documentScannerConf;
        initComponents();

        OCREngineConfPanelFactory oCREngineConfPanelFactory = new DefaultOCREngineConfPanelFactory(issueHandler);
        for(OCREngineConf availableOCREngineConf : this.documentScannerConf.getAvailableOCREngineConfs()) {
            OCREngineConf oCREngineConf = DelegatingOCREngineConfCopyFactory.getInstance().copy(availableOCREngineConf);
            this.oCREngineComboBoxModel.addElement(oCREngineConf);
            OCREngineConfPanel oCREngineConfPanel;
            try {
                oCREngineConfPanel = oCREngineConfPanelFactory.create(oCREngineConf);
            } catch (OCREngineConfCreationException ex) {
                issueHandler.handle(new Message(ex));
                continue;
            }
            this.originalEngineConfs.put(oCREngineConfPanel,
                    availableOCREngineConf);
            this.oCREngineConfPanelMap.put(oCREngineConf.getClass(),
                    oCREngineConfPanel);
        }
        this.oCRDialogEngineComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                OCREngineConf oCREngineConf = (OCREngineConf) e.getItem();
                oCREngineComboBoxStateChanged(oCREngineConf);
            }
        });
        this.oCRDialogPanel.setLayout(new BoxLayout(this.oCRDialogPanel, BoxLayout.X_AXIS));
        //set initial panel state
        this.oCRDialogEngineComboBox.setSelectedItem(this.documentScannerConf.getoCREngineConf().getClass());
            //doesn't trigger ItemListener.itemStateChange above
        oCREngineComboBoxStateChanged(this.documentScannerConf.getoCREngineConf());
        this.oCRDialogEngineComboBox.setRenderer(oCRDialogEngineComboBoxRenderer);
            //after oCRDialogEngineComboBox.setSelectedItem
    }

    private void oCREngineComboBoxStateChanged(OCREngineConf cREngineConf) {
        OCREngineConfPanel<?> cREngineConfPanel = OCREngineSelectDialog.this.oCREngineConfPanelMap.get(cREngineConf.getClass());
        OCREngineSelectDialog.this.oCRDialogPanel.removeAll();
        OCREngineSelectDialog.this.oCRDialogPanel.add(cREngineConfPanel);
        OCREngineSelectDialog.this.oCRDialogPanel.revalidate();
        OCREngineSelectDialog.this.pack();
        OCREngineSelectDialog.this.oCRDialogPanel.repaint();
    }

    public DocumentScannerConf getDocumentScannerConf() {
        return documentScannerConf;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        oCRDialogEngineComboBox = new javax.swing.JComboBox<>();
        oCRDialogEngineLabel = new javax.swing.JLabel();
        oCRDialogSeparator = new javax.swing.JSeparator();
        oCRDialogPanel = new javax.swing.JPanel();
        oCRDialogCancelButton = new javax.swing.JButton();
        oCRDialogSaveButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        oCRDialogEngineComboBox.setModel(oCREngineComboBoxModel);

        oCRDialogEngineLabel.setText("OCR engine");

        javax.swing.GroupLayout oCRDialogPanelLayout = new javax.swing.GroupLayout(oCRDialogPanel);
        oCRDialogPanel.setLayout(oCRDialogPanelLayout);
        oCRDialogPanelLayout.setHorizontalGroup(
            oCRDialogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        oCRDialogPanelLayout.setVerticalGroup(
            oCRDialogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 178, Short.MAX_VALUE)
        );

        oCRDialogCancelButton.setText("Cancel");
        oCRDialogCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                oCRDialogCancelButtonActionPerformed(evt);
            }
        });

        oCRDialogSaveButton.setText("Save");
        oCRDialogSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                oCRDialogSaveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(oCRDialogPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(oCRDialogSeparator)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(oCRDialogEngineLabel)
                        .addGap(18, 18, 18)
                        .addComponent(oCRDialogEngineComboBox, 0, 273, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(oCRDialogSaveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(oCRDialogCancelButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(oCRDialogEngineComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(oCRDialogEngineLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(oCRDialogSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(oCRDialogPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(oCRDialogCancelButton)
                    .addComponent(oCRDialogSaveButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void oCRDialogCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_oCRDialogCancelButtonActionPerformed
        this.documentScannerConf = null;
        setVisible(false);
    }//GEN-LAST:event_oCRDialogCancelButtonActionPerformed

    private void oCRDialogSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_oCRDialogSaveButtonActionPerformed
        try {
            OCREngineConf oCREngineConf = this.oCRDialogEngineComboBox.getItemAt(this.oCRDialogEngineComboBox.getSelectedIndex());
            OCREngineConfPanel<?> currentOCREngineConfPanel = this.oCREngineConfPanelMap.get(oCREngineConf.getClass());
            assert currentOCREngineConfPanel != null;
            currentOCREngineConfPanel.save();
            OCREngineConf selectedOCREngineConf0 = currentOCREngineConfPanel.getOCREngineConf();
            try {
                selectedOCREngineConf0.validate();
            }catch(OCREngineConfValidationException ex) {
                issueHandler.handle(new Message(ex));
                return;
            }
            this.documentScannerConf.setoCREngineConf(selectedOCREngineConf0);
            this.documentScannerConf.getAvailableOCREngineConfs().remove(originalEngineConfs.get(currentOCREngineConfPanel));
            this.documentScannerConf.getAvailableOCREngineConfs().add(selectedOCREngineConf0);
            this.setVisible(false);
        }catch(Throwable ex) {
            LOGGER.error("unexpected exception during saving of OCR engine configuration",
                    ex);
            issueHandler.handleUnexpectedException(new ExceptionMessage(ex));
        }
    }//GEN-LAST:event_oCRDialogSaveButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton oCRDialogCancelButton;
    private javax.swing.JComboBox<OCREngineConf> oCRDialogEngineComboBox;
    private javax.swing.JLabel oCRDialogEngineLabel;
    private javax.swing.JPanel oCRDialogPanel;
    private javax.swing.JButton oCRDialogSaveButton;
    private javax.swing.JSeparator oCRDialogSeparator;
    // End of variables declaration//GEN-END:variables
}
