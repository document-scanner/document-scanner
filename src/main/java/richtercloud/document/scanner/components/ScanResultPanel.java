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
package richtercloud.document.scanner.components;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author richter
 */
/*
internal implementation notes:
- Due to the quite messed up cyclic dependencies of components contained in
MainPanel it seems necessary to pass null referenced to certain constructors
(see MainPanel for details). Therefore it's discourage - maybe impossible - to
make ScanResultPanel save image data on its own during initialization -> let
caller do that which is maybe even more elegant -> it's too hard to manage which
ScanResultPanels have been generated by FieldHandler -> manage references in
maps in MainPanel and pass reference to MainPanel to components contained in it
*/
public class ScanResultPanel extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;
    private ScanResultPanelFetcher retriever;
    private byte[] scanData;
    private final static String LABEL_DEFAULT_TEXT = "No data scanned";
    private Set<ScanResultPanelUpdateListener> updateListener = new HashSet<>();
    private final byte[] initialValue;

    public ScanResultPanel(ScanResultPanelFetcher retriever,
            byte[] initialValue,
            boolean autoSaveImageData) {
        this.initComponents();
        this.retriever = retriever;
        this.initialValue = initialValue;
        reset0();
        if(autoSaveImageData) {
            save();
        }
    }

    public void addUpdateListerner(ScanResultPanelUpdateListener updateListener) {
        this.updateListener.add(updateListener);
    }

    public void removeUpdateListener(ScanResultPanelUpdateListener updateListener) {
        this.updateListener.remove(updateListener);
    }

    private void handleScanDataUpdate() {
        if(this.scanData != null) {
            this.label.setText(String.format("%d bytes of data scanned", this.scanData.length));
        }else {
            this.label.setText(LABEL_DEFAULT_TEXT);
        }
    }

    private void reset0() {
        if(this.initialValue != null) {
            this.scanData = initialValue;
        }else {
            this.scanData = new byte[0];
        }
        handleScanDataUpdate();
    }

    public void reset() {
        reset0();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        saveButton = new javax.swing.JButton();
        label = new javax.swing.JLabel();
        deleteButton = new javax.swing.JButton();

        saveButton.setText("Save image data");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        label.setText(LABEL_DEFAULT_TEXT);

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(label, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveButton)
                    .addComponent(label)
                    .addComponent(deleteButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void save() {
        this.scanData = this.retriever.fetch();
        for(ScanResultPanelUpdateListener updateListener : this.updateListener) {
            updateListener.onUpdate(new ScanResultPanelUpdateEvent(scanData));
        }
        handleScanDataUpdate();
    }

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        save();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        this.scanData = null;
        for(ScanResultPanelUpdateListener updateListener : this.updateListener) {
            updateListener.onUpdate(new ScanResultPanelUpdateEvent(scanData));
        }
        handleScanDataUpdate();
    }//GEN-LAST:event_deleteButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel label;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables
}
