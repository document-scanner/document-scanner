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

import richtercloud.document.scanner.ifaces.OCREngineConf;
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
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.message.handler.MessageHandler;

/**
 *
 * @author richter
 */
public class OCREngineSelectDialog extends javax.swing.JDialog {
    private static final long serialVersionUID = 1L;
    private final Map<Class<? extends OCREngineConf>, OCREngineConfPanel<?>> oCREngineConfPanelMap = new HashMap<>();
    private final MutableComboBoxModel<Class<? extends OCREngineConf>> oCREngineComboBoxModel = new DefaultComboBoxModel<>();
    private OCREngineConfPanel<?> currentOCREngineConfPanel;
    //@TODO: implement class path discovery of associated conf panel with annotations
    private OCREngineConf selectedOCREngineConf = null;
    private final MessageHandler messageHandler;
    private final DocumentScannerConf documentScannerConf;
    private ListCellRenderer<Object> oCRDialogEngineComboBoxRenderer = new DefaultListCellRenderer() {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Class<?> valueCast = (Class<?>) value;
            OCREngineConfInfo oCREngineInfo = valueCast.getAnnotation(OCREngineConfInfo.class);
            String value0;
            if (oCREngineInfo != null) {
                value0 = oCREngineInfo.name();
            } else {
                value0 = valueCast.getSimpleName();
            }
            return super.getListCellRendererComponent(list, value0, index, isSelected, cellHasFocus);
        }

    };

    /**
     * Creates new form OCREngineSelectDialog
     */
    public OCREngineSelectDialog(Frame parent,
            DocumentScannerConf documentScannerConf,
            MessageHandler messageHandler) {
        super(parent,
                true //modal
        );
        this.messageHandler = messageHandler;
        this.documentScannerConf = documentScannerConf;
        initComponents();

        OCREngineConfPanelFactory oCREngineConfPanelFactory = new DefaultOCREngineConfPanelFactory(messageHandler);
        for(OCREngineConf availableOCREngineConf : this.documentScannerConf.getAvailableOCREngineConfs()) {
            this.oCREngineComboBoxModel.addElement(availableOCREngineConf.getClass());
            OCREngineConfPanel oCREngineConfPanel = oCREngineConfPanelFactory.create(availableOCREngineConf);
            this.oCREngineConfPanelMap.put(availableOCREngineConf.getClass(),
                    oCREngineConfPanel);
        }
        this.oCRDialogEngineComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Class<? extends OCREngineConf> clazz = (Class<? extends OCREngineConf>) e.getItem();
                oCREngineComboBoxStateChanged(clazz);
            }
        });
        this.oCRDialogPanel.setLayout(new BoxLayout(this.oCRDialogPanel, BoxLayout.X_AXIS));
        //set initial panel state
        this.oCRDialogEngineComboBox.setSelectedItem(this.documentScannerConf.getoCREngineConf().getClass());
            //doesn't trigger ItemListener.itemStateChange above
        oCREngineComboBoxStateChanged(this.documentScannerConf.getoCREngineConf().getClass());
        this.oCRDialogEngineComboBox.setRenderer(oCRDialogEngineComboBoxRenderer);
            //after oCRDialogEngineComboBox.setSelectedItem
    }

    private void oCREngineComboBoxStateChanged(Class<? extends OCREngineConf> clazz) {
        OCREngineConfPanel<?> cREngineConfPanel = OCREngineSelectDialog.this.oCREngineConfPanelMap.get(clazz);
        OCREngineSelectDialog.this.oCRDialogPanel.removeAll();
        OCREngineSelectDialog.this.oCRDialogPanel.add(cREngineConfPanel);
        OCREngineSelectDialog.this.oCRDialogPanel.revalidate();
        OCREngineSelectDialog.this.pack();
        OCREngineSelectDialog.this.oCRDialogPanel.repaint();
    }

    public OCREngineConf getSelectedOCREngineConf() {
        return selectedOCREngineConf;
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
        this.selectedOCREngineConf = null;
        setVisible(false);
    }//GEN-LAST:event_oCRDialogCancelButtonActionPerformed

    private void oCRDialogSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_oCRDialogSaveButtonActionPerformed
        Class<? extends OCREngineConf> oCREngineClass = this.oCRDialogEngineComboBox.getItemAt(this.oCRDialogEngineComboBox.getSelectedIndex());
        this.currentOCREngineConfPanel = this.oCREngineConfPanelMap.get(oCREngineClass);
        assert this.currentOCREngineConfPanel != null;
        this.selectedOCREngineConf = this.currentOCREngineConfPanel.getOCREngineConf();
        this.setVisible(false);
    }//GEN-LAST:event_oCRDialogSaveButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton oCRDialogCancelButton;
    private javax.swing.JComboBox<Class<? extends OCREngineConf>> oCRDialogEngineComboBox;
    private javax.swing.JLabel oCRDialogEngineLabel;
    private javax.swing.JPanel oCRDialogPanel;
    private javax.swing.JButton oCRDialogSaveButton;
    private javax.swing.JSeparator oCRDialogSeparator;
    // End of variables declaration//GEN-END:variables
}
