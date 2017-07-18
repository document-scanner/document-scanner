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
package richtercloud.document.scanner.valuedetectionservice;

import java.awt.Component;
import java.awt.Window;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.apache.commons.lang3.tuple.Pair;
import richtercloud.message.handler.IssueHandler;

/**
 *
 * @author richter
 */
public class ValueDetectionServiceConfDialog extends javax.swing.JDialog {
    private static final long serialVersionUID = 1L;
    private final DefaultListModel<ValueDetectionServiceConf> availableListModel = new DefaultListModel<>();
    private final DefaultListModel<ValueDetectionServiceConf> selectedListModel = new DefaultListModel<>();
    private final IssueHandler issueHandler;
    /**
     * The values changed by dialog operations. {@code null} indicates that the
     * dialog has been canceled.
     */
    private List<ValueDetectionServiceConf> availableValueDetectionServiceConfs = new LinkedList<>();
    private List<ValueDetectionServiceConf> selectedValueDetectionServiceConfs = new LinkedList<>();
    private Set<String> valueDetectionServiceJARPaths = new HashSet<>();
    private final ListCellRenderer valueDetectionServiceListCellRenderer = new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            assert value != null;
            assert value instanceof ValueDetectionServiceConf;
            ValueDetectionServiceConf valueCast = (ValueDetectionServiceConf) value;
            return super.getListCellRendererComponent(list,
                    valueCast.getDescription(),
                    index,
                    isSelected,
                    cellHasFocus);
        }
    };

    /**
     * Creates new form ValueDetectionConfDialog
     */
    public ValueDetectionServiceConfDialog(Window parent,
            List<ValueDetectionServiceConf> availableValueDetectionServiceConfs,
            List<ValueDetectionServiceConf> selectedValueDetectionServiceConfs,
            IssueHandler issueHandler) {
        super(parent,
                ModalityType.APPLICATION_MODAL);
        this.issueHandler = issueHandler;
        initComponents();
        this.splitPane.setDividerLocation(splitPane.getPreferredSize().width/2);
        assert Collections.disjoint(availableValueDetectionServiceConfs,
                selectedValueDetectionServiceConfs);
        this.availableValueDetectionServiceConfs = availableValueDetectionServiceConfs;
        this.selectedValueDetectionServiceConfs = selectedValueDetectionServiceConfs;
        for(ValueDetectionServiceConf availableService : availableValueDetectionServiceConfs) {
            assert availableService != null;
            availableListModel.addElement(availableService);
        }
        for(ValueDetectionServiceConf selectedService : selectedValueDetectionServiceConfs) {
            assert selectedService != null;
            selectedListModel.addElement(selectedService);
        }
        this.availableList.setCellRenderer(valueDetectionServiceListCellRenderer);
        this.selectedList.setCellRenderer(valueDetectionServiceListCellRenderer);
        this.availableList.addListSelectionListener((listSelectionEvent) -> {
            this.selectButton.setEnabled(this.availableList.getSelectedIndex() != -1);
        });
        this.selectedList.addListSelectionListener((listSelectionEvent) -> {
            this.deselectButton.setEnabled(this.selectedList.getSelectedIndex() != -1);
        });
    }

    public List<ValueDetectionServiceConf> getAvailableValueDetectionServiceConfs() {
        return availableValueDetectionServiceConfs;
    }

    public List<ValueDetectionServiceConf> getSelectedValueDetectionServiceConfs() {
        return selectedValueDetectionServiceConfs;
    }

    public Set<String> getValueDetectionServiceJARPaths() {
        return valueDetectionServiceJARPaths;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitPane = new javax.swing.JSplitPane();
        availablePanel = new javax.swing.JPanel();
        availableListLabel = new javax.swing.JLabel();
        availableListScrollPane = new javax.swing.JScrollPane();
        availableList = new javax.swing.JList<>();
        availableAddButton = new javax.swing.JButton();
        selectButton = new javax.swing.JButton();
        availableRemoveButton = new javax.swing.JButton();
        selectedPanel = new javax.swing.JPanel();
        selectedListScrollPane = new javax.swing.JScrollPane();
        selectedList = new javax.swing.JList<>();
        selectedListLabel = new javax.swing.JLabel();
        deselectButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        availableListLabel.setText("Available Auto OCR detection services:");

        availableList.setModel(availableListModel);
        availableListScrollPane.setViewportView(availableList);

        availableAddButton.setText("Add");
        availableAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                availableAddButtonActionPerformed(evt);
            }
        });

        selectButton.setText("Select");
        selectButton.setEnabled(false);
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        availableRemoveButton.setText("Remove");

        javax.swing.GroupLayout availablePanelLayout = new javax.swing.GroupLayout(availablePanel);
        availablePanel.setLayout(availablePanelLayout);
        availablePanelLayout.setHorizontalGroup(
            availablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(availablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(availablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(availableListScrollPane)
                    .addGroup(availablePanelLayout.createSequentialGroup()
                        .addComponent(availableListLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(availablePanelLayout.createSequentialGroup()
                        .addComponent(availableRemoveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(availableAddButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(selectButton)))
                .addContainerGap())
        );
        availablePanelLayout.setVerticalGroup(
            availablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(availablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(availableListLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(availableListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(availablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(availableAddButton)
                    .addComponent(availableRemoveButton)
                    .addComponent(selectButton))
                .addContainerGap())
        );

        splitPane.setLeftComponent(availablePanel);

        selectedList.setModel(selectedListModel);
        selectedListScrollPane.setViewportView(selectedList);

        selectedListLabel.setText("Selected auto OCR value detection services:");

        deselectButton.setText("Deselect");
        deselectButton.setEnabled(false);
        deselectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout selectedPanelLayout = new javax.swing.GroupLayout(selectedPanel);
        selectedPanel.setLayout(selectedPanelLayout);
        selectedPanelLayout.setHorizontalGroup(
            selectedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(selectedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectedListScrollPane)
                    .addGroup(selectedPanelLayout.createSequentialGroup()
                        .addGroup(selectedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(selectedListLabel)
                            .addComponent(deselectButton))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        selectedPanelLayout.setVerticalGroup(
            selectedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectedListLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectedListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(deselectButton)
                .addContainerGap())
        );

        splitPane.setRightComponent(selectedPanel);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(closeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(splitPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(saveButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void availableAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_availableAddButtonActionPerformed
        ValueDetectionServiceAddDialog addDialog = new ValueDetectionServiceAddDialog(this,
                issueHandler);
        addDialog.setLocationRelativeTo(this);
        addDialog.setVisible(true);
        Pair<String, ValueDetectionServiceConf> createdConf = addDialog.getCreatedConf();
            //already validated in addDialog
        if(createdConf == null) {
            //dialog has been canceled
            return;
        }
        this.valueDetectionServiceJARPaths.add(createdConf.getKey());
        this.availableValueDetectionServiceConfs.add(createdConf.getValue());
        this.availableListModel.addElement(createdConf.getValue());
    }//GEN-LAST:event_availableAddButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        this.availableValueDetectionServiceConfs = null;
        this.selectedValueDetectionServiceConfs = null;
        this.valueDetectionServiceJARPaths = null;
        this.setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        ValueDetectionServiceConf serviceConf = this.availableList.getSelectedValue();
        this.availableListModel.removeElement(serviceConf);
        this.selectedListModel.addElement(serviceConf);
        this.availableValueDetectionServiceConfs.remove(serviceConf);
        this.selectedValueDetectionServiceConfs.add(serviceConf);
    }//GEN-LAST:event_selectButtonActionPerformed

    private void deselectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectButtonActionPerformed
        ValueDetectionServiceConf serviceConf = this.selectedList.getSelectedValue();
        this.selectedListModel.removeElement(serviceConf);
        this.availableListModel.addElement(serviceConf);
        this.selectedValueDetectionServiceConfs.remove(serviceConf);
        this.availableValueDetectionServiceConfs.add(serviceConf);
    }//GEN-LAST:event_deselectButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_saveButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton availableAddButton;
    private javax.swing.JList<ValueDetectionServiceConf> availableList;
    private javax.swing.JLabel availableListLabel;
    private javax.swing.JScrollPane availableListScrollPane;
    private javax.swing.JPanel availablePanel;
    private javax.swing.JButton availableRemoveButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton deselectButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton selectButton;
    private javax.swing.JList<ValueDetectionServiceConf> selectedList;
    private javax.swing.JLabel selectedListLabel;
    private javax.swing.JScrollPane selectedListScrollPane;
    private javax.swing.JPanel selectedPanel;
    private javax.swing.JSplitPane splitPane;
    // End of variables declaration//GEN-END:variables
}
