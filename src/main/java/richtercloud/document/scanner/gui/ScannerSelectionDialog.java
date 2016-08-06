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

import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneException;
import au.com.southsky.jfreesane.SaneOption;
import au.com.southsky.jfreesane.SaneSession;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.lang3.exception.ExceptionUtils;
import richtercloud.reflection.form.builder.message.Message;
import richtercloud.reflection.form.builder.message.MessageHandler;

/**
 * Allows displaying all SANE devices at an address and to view and edit its
 * setting as well select one for the application.
 *
 * The selected device and its address can be retrieved with
 * {@link #getSelectedDevice() } and {@link #getAddress() } after the dialog has
 * been closed.
 * @author richter
 */
public class ScannerSelectionDialog extends javax.swing.JDialog {
    private static final String SCANNER_ADDRESS_DEFAULT = "localhost";
    private static final long serialVersionUID = 1L;
    private class SelectionTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 1L;
        private final List<SaneDevice> devices;

        SelectionTableModel(List<SaneDevice> devices) {
            super(new String[] {"Name", "Model", "Type", "Vendor"}, 0);
            this.devices = devices;
        }

        @Override
        public Object getValueAt(int row, int column) {
            if(column == 0) {
                return devices.get(row).getName();
            }else if(column == 1) {
                return devices.get(row).getModel();
            }else if(column == 2) {
                return devices.get(row).getType();
            }else if(column == 3) {
                return devices.get(row).getVendor();
            }else {
                throw new IllegalArgumentException();
            }
        }

        public void addDevices(List<SaneDevice> devices) {
            int lastRow = this.devices.size();
            this.devices.addAll(devices);
            fireTableRowsInserted(lastRow, this.devices.size());
        }

        public void addDevice(SaneDevice device) {
            this.devices.add(device);
            fireTableRowsInserted(devices.size(), devices.size());
        }

        public void clear() {
            int rowCount = this.devices.size();
            this.devices.clear();
            fireTableRowsDeleted(0, rowCount);
        }

        @Override
        public int getRowCount() {
            if(this.devices == null) {
                //during initialization (this is inefficient, but due to the bad design of DefaultTableModel
                return 0;
            }
            return this.devices.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        public List<SaneDevice> getDevices() {
            return Collections.unmodifiableList(devices);
        }
    }
    private final SelectionTableModel tableModel = new SelectionTableModel(new LinkedList<SaneDevice>());
    private SaneSession saneSession;
    private final TableModelListener scannerDialogTableModelListener = new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent e) {
            ScannerSelectionDialog.this.scannerDialogSelectButton.setEnabled(ScannerSelectionDialog.this.scannerDialogSelectButtonEnabled());
            ScannerSelectionDialog.this.scannerDialogEditButton.setEnabled(ScannerSelectionDialog.this.scannerDialogSelectButtonEnabled());
        }
    };
    private final ListSelectionListener scannerDialogTableSelectionListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            ScannerSelectionDialog.this.scannerDialogSelectButton.setEnabled(ScannerSelectionDialog.this.scannerDialogSelectButtonEnabled());
            ScannerSelectionDialog.this.scannerDialogEditButton.setEnabled(ScannerSelectionDialog.this.scannerDialogSelectButtonEnabled());
        }
    };
    private SaneDevice selectedDevice;
    /**
     * The address of the last search. Can't be retrieved reliably from the
     * address text field because its text might have changed after the last
     * search.
     */
    private String address;
    private final MessageHandler messageHandler;
    private final Map<String, Map<String, Object>> changedOptions;

    /**
     * Creates new scanner selection dialog. Scanners referenced (by name) in
     * {@code changedOptions} will be added to the list/table of selectable
     * devices if they're accessible in a {@link SaneSession} created with
     * {@code initialAddress}.
     *
     * @param parent
     * @param messageHandler
     * @param changedOptions
     * @param initialAddress
     */
    public ScannerSelectionDialog(java.awt.Frame parent,
            MessageHandler messageHandler,
            Map<String, Map<String, Object>> changedOptions,
            String initialAddress) throws UnknownHostException, IOException, SaneException {
        super(parent,
                DocumentScanner.generateApplicationWindowTitle("Select scanner",
                        DocumentScanner.APP_NAME,
                        DocumentScanner.APP_VERSION),
                true //modal
        );
        if(messageHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.messageHandler = messageHandler;
        if(changedOptions == null) {
            throw new IllegalArgumentException("changedOptions mustn't be null");
        }
        this.changedOptions = changedOptions;
        initComponents();
        this.tableModel.addTableModelListener(this.scannerDialogTableModelListener);
        this.scannerDialogTable.getSelectionModel().addListSelectionListener(this.scannerDialogTableSelectionListener);
        this.scannerDialogAddressTextField.setText(initialAddress);
        InetAddress address0 = InetAddress.getByName(initialAddress);
        this.saneSession = SaneSession.withRemoteSane(address0);
        for(String scannerName : changedOptions.keySet()) {
            SaneDevice existingDevice = DocumentScanner.getScannerDevice(scannerName,
                    saneSession,
                    changedOptions);
            tableModel.addDevice(existingDevice);
        }
    }

    public SaneDevice getSelectedDevice() {
        return selectedDevice;
    }

    public String getAddress() {
        return address;
    }

    private boolean scannerDialogSelectButtonEnabled() {
        return this.tableModel.getRowCount() > 0
                && this.scannerDialogTable.getSelectedRowCount() > 0;
    }

    private void searchScanner() {
        String addressString = this.scannerDialogAddressTextField.getText();
        InetAddress address0;
        this.scannerDialogStatusLabel.setText("Searching...");
        try {
            address0 = InetAddress.getByName(addressString);
            this.saneSession = SaneSession.withRemoteSane(address0);
            List<SaneDevice> availableDevices = this.saneSession.listDevices();
            for(SaneDevice availableDevice : availableDevices) {
                if(!changedOptions.keySet().contains(availableDevice.getName())) {
                    SaneDevice cachedAvailableDevice = DocumentScanner.getScannerDevice(availableDevice.getName(),
                            saneSession,
                            changedOptions); //otherwise option changes are lost
                    this.tableModel.addDevice(cachedAvailableDevice);
                }
            }
            this.scannerDialogStatusLabel.setText(" ");
            this.address = addressString;
        } catch (ConnectException ex) {
            this.handleSearchScannerException(ex,
                    DocumentScanner.SANED_BUG_INFO);
        } catch (IOException | SaneException ex) {
            this.handleSearchScannerException(ex, "");
        }
    }

    /**
     * for code reusage in {@link #searchScanner() }
     *
     * @param ex
     * @param additional
     */
    private void handleSearchScannerException(Exception ex, String additional) {
        String labelText = DocumentScanner.handleSearchScannerException("The search at the specified address failed with the following error: ", ex, additional);
        this.scannerDialogStatusLabel.setText(labelText);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scannerDialogAddressTextField = new javax.swing.JTextField();
        scannerDialogAddressLabel = new javax.swing.JLabel();
        scannerDialogSearchButton = new javax.swing.JButton();
        scannerDialogSeparator = new javax.swing.JSeparator();
        scannerDialogScrollPane = new javax.swing.JScrollPane();
        scannerDialogTable = new javax.swing.JTable();
        scannerDialogCancelButton = new javax.swing.JButton();
        scannerDialogSelectButton = new javax.swing.JButton();
        scannerDialogStatusLabel = new javax.swing.JLabel();
        scannerDialogEditButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        scannerDialogAddressTextField.setText(SCANNER_ADDRESS_DEFAULT);

        scannerDialogAddressLabel.setText("Address");

        scannerDialogSearchButton.setText("Search");
        scannerDialogSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scannerDialogSearchButtonActionPerformed(evt);
            }
        });

        scannerDialogTable.setModel(tableModel);
        scannerDialogScrollPane.setViewportView(scannerDialogTable);

        scannerDialogCancelButton.setText("Cancel");
        scannerDialogCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scannerDialogCancelButtonActionPerformed(evt);
            }
        });

        scannerDialogSelectButton.setText("Select scanner");
        scannerDialogSelectButton.setEnabled(false);
        scannerDialogSelectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scannerDialogSelectButtonActionPerformed(evt);
            }
        });

        scannerDialogStatusLabel.setText(" ");

        scannerDialogEditButton.setText("Edit");
        scannerDialogEditButton.setEnabled(false);
        scannerDialogEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scannerDialogEditButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(scannerDialogAddressLabel)
                        .addGap(18, 18, 18)
                        .addComponent(scannerDialogAddressTextField))
                    .addComponent(scannerDialogSeparator)
                    .addComponent(scannerDialogScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(scannerDialogEditButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scannerDialogSelectButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scannerDialogCancelButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(scannerDialogStatusLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(scannerDialogSearchButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scannerDialogAddressTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scannerDialogAddressLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scannerDialogSearchButton)
                    .addComponent(scannerDialogStatusLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scannerDialogSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scannerDialogScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scannerDialogCancelButton)
                    .addComponent(scannerDialogSelectButton)
                    .addComponent(scannerDialogEditButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void scannerDialogSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scannerDialogSearchButtonActionPerformed
        this.searchScanner();
    }//GEN-LAST:event_scannerDialogSearchButtonActionPerformed

    private void scannerDialogCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scannerDialogCancelButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_scannerDialogCancelButtonActionPerformed

    private void scannerDialogSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scannerDialogSelectButtonActionPerformed
        assert this.scannerDialogTable.getSelectedRow() != -1;
        this.selectedDevice = this.tableModel.getDevices().get(this.scannerDialogTable.getSelectedRow());
        this.setVisible(false);
    }//GEN-LAST:event_scannerDialogSelectButtonActionPerformed

    private void scannerDialogEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scannerDialogEditButtonActionPerformed
        assert this.scannerDialogTable.getSelectedRow() != -1;
        SaneDevice device = this.tableModel.getDevices().get(this.scannerDialogTable.getSelectedRow());
        ScannerEditDialog scannerEditDialog;
        try {
            ScannerEditDialog.configureDefaultOptionValues(device,
                this.changedOptions,
                false);
            scannerEditDialog = new ScannerEditDialog(this,
                device,
                this.changedOptions,
                this.messageHandler);
            scannerEditDialog.setVisible(true);
        } catch (IOException | SaneException ex) {
            this.messageHandler.handle(new Message(String.format("Exception during scanner configuration", ExceptionUtils.getRootCauseMessage(ex)),
                    JOptionPane.ERROR_MESSAGE,
                    "Exception occured"));
        }
    }//GEN-LAST:event_scannerDialogEditButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel scannerDialogAddressLabel;
    private javax.swing.JTextField scannerDialogAddressTextField;
    private javax.swing.JButton scannerDialogCancelButton;
    private javax.swing.JButton scannerDialogEditButton;
    private javax.swing.JScrollPane scannerDialogScrollPane;
    private javax.swing.JButton scannerDialogSearchButton;
    private javax.swing.JButton scannerDialogSelectButton;
    private javax.swing.JSeparator scannerDialogSeparator;
    private javax.swing.JLabel scannerDialogStatusLabel;
    private javax.swing.JTable scannerDialogTable;
    // End of variables declaration//GEN-END:variables
}
