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

import java.awt.Component;
import java.awt.Window;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.gui.storageconf.StorageConfPanel;
import richtercloud.message.handler.Message;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.storage.StorageConf;
import richtercloud.reflection.form.builder.storage.StorageConfInitializationException;

/**
 * Allows selection of storage implementation. Caller is responsible for
 * evaluating the selection.
 * @author richter
 */
public class StorageSelectionDialog extends javax.swing.JDialog {
    private static final long serialVersionUID = 1L;
    private final DefaultListModel<StorageConf> storageListModel = new DefaultListModel<>();
    private final ListCellRenderer storageListCellRenderer = new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            assert value instanceof StorageConf;
            StorageConf valueCast = (StorageConf) value;
            return super.getListCellRendererComponent(list,
                    valueCast.getShortDescription(),
                    index,
                    isSelected,
                    cellHasFocus);
        }
    };
    private final DocumentScannerConf documentScannerConf;
    private Map<Class<? extends StorageConf>, StorageConfPanel<?>> storageConfPanelMap = new HashMap<>();
    /**
     * Reference to the selected storage configuration after closing.
     * {@code null} indicates that the selection has been aborted/hasn't
     * changed. Keeping an extra reference instead of just selecting from
     * {@code storageList} avoids possible trouble after dialog has been
     * disposed.
     */
    private StorageConf selectedStorageConf = null;
    private final MessageHandler messageHandler;

    /**
     * Creates new form StorageSelectionDialog
     */
    public StorageSelectionDialog(Window parent,
            DocumentScannerConf documentScannerConf,
            MessageHandler messageHandler) throws IOException, StorageConfInitializationException {
        super(parent,
                ModalityType.APPLICATION_MODAL //modalityType
        );
        this.documentScannerConf = documentScannerConf;
        this.messageHandler = messageHandler;

        StorageConfPanelFactory storageConfPanelFactory = new DefaultStorageConfPanelFactory();
        for(StorageConf availableStorageConf : this.documentScannerConf.getAvailableStorageConfs()) {
            this.storageListModel.addElement(availableStorageConf);
            StorageConfPanel storageConfPanel = storageConfPanelFactory.create(availableStorageConf);
            this.storageConfPanelMap.put(availableStorageConf.getClass(),
                    storageConfPanel);
        }
        initComponents();
        this.storageList.setCellRenderer(storageListCellRenderer);
            //after initComponents
        this.storageList.setSelectedValue(this.documentScannerConf.getStorageConf(),
                true //shouldScroll
        );
        this.storageList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                StorageSelectionDialog.this.storageDialogSelectButton.setEnabled(StorageSelectionDialog.this.storageListModel.getSize() > 0
                        && StorageSelectionDialog.this.storageList.getSelectedIndices().length > 0);
            }
        });
    }

    public StorageConf getSelectedStorageConf() {
        return this.storageList.getSelectedValue();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        storageLabel = new javax.swing.JLabel();
        storageListScrollPane = new javax.swing.JScrollPane();
        storageList = new javax.swing.JList<>();
        storageDialogCancelButton = new javax.swing.JButton();
        storageDialogSelectButton = new javax.swing.JButton();
        storageDialogEditButton = new javax.swing.JButton();
        storageDialogDeleteButton = new javax.swing.JButton();
        storageDialogNewButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        storageLabel.setText("Storages");

        storageList.setModel(storageListModel);
        storageListScrollPane.setViewportView(storageList);

        storageDialogCancelButton.setText("Cancel");
        storageDialogCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storageDialogCancelButtonActionPerformed(evt);
            }
        });

        storageDialogSelectButton.setText("Select");
        storageDialogSelectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storageDialogSelectButtonActionPerformed(evt);
            }
        });

        storageDialogEditButton.setText("Edit");
        storageDialogEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storageDialogEditButtonActionPerformed(evt);
            }
        });

        storageDialogDeleteButton.setText("Delete");

        storageDialogNewButton.setText("New...");
        storageDialogNewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storageDialogNewButtonActionPerformed(evt);
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
                        .addComponent(storageLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(storageListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(storageDialogSelectButton)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(storageDialogCancelButton, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(storageDialogNewButton, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(storageDialogEditButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(storageDialogDeleteButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(storageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(storageListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(storageDialogCancelButton)
                            .addComponent(storageDialogSelectButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(storageDialogNewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(storageDialogEditButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(storageDialogDeleteButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void storageDialogCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageDialogCancelButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_storageDialogCancelButtonActionPerformed

    private void storageDialogSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageDialogSelectButtonActionPerformed
        this.selectedStorageConf = this.storageList.getSelectedValue();
        assert this.selectedStorageConf != null;
        try {
            this.selectedStorageConf.validate();
                //some configurations, e.g. DerbyNetworkPersistenceStorageConf
                //can't be created in a valid state because they require values
                //to be set (e.g. a password)
        } catch (StorageConfInitializationException ex) {
            messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
            return;
        }
        this.setVisible(false);
    }//GEN-LAST:event_storageDialogSelectButtonActionPerformed

    private void storageDialogNewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageDialogNewButtonActionPerformed
        StorageCreateDialog storageCreateDialog = new StorageCreateDialog(this,
                this.storageConfPanelMap);
        storageCreateDialog.setLocationRelativeTo(this);
        storageCreateDialog.setVisible(true);
        StorageConf createdStorageConf = storageCreateDialog.getCreatedStorageConf();
        this.storageListModel.addElement(createdStorageConf);
        this.documentScannerConf.getAvailableStorageConfs().add(createdStorageConf);
    }//GEN-LAST:event_storageDialogNewButtonActionPerformed

    private void storageDialogEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageDialogEditButtonActionPerformed
        Object storageListSelectedValue = storageList.getSelectedValue();
        assert storageListSelectedValue instanceof StorageConf;
        StorageConf selectedStorageConf = (StorageConf) storageListSelectedValue;
        StorageConfPanel storageConfPanel = this.storageConfPanelMap.get(selectedStorageConf.getClass());
        assert storageConfPanel != null;
        StorageEditDialog storageEditDialog = new StorageEditDialog(this,
                storageConfPanel,
                messageHandler);
        storageEditDialog.setLocationRelativeTo(this);
        storageEditDialog.setVisible(true);
    }//GEN-LAST:event_storageDialogEditButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton storageDialogCancelButton;
    private javax.swing.JButton storageDialogDeleteButton;
    private javax.swing.JButton storageDialogEditButton;
    private javax.swing.JButton storageDialogNewButton;
    private javax.swing.JButton storageDialogSelectButton;
    private javax.swing.JLabel storageLabel;
    private javax.swing.JList<StorageConf> storageList;
    private javax.swing.JScrollPane storageListScrollPane;
    // End of variables declaration//GEN-END:variables
}
