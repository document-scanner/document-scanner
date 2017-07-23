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
import au.com.southsky.jfreesane.SaneSession;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.Constants;
import richtercloud.document.scanner.gui.DocumentScanner;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.gui.scanresult.DocumentController;
import richtercloud.message.handler.Message;
import richtercloud.message.handler.MessageHandler;

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
    private final static Logger LOGGER = LoggerFactory.getLogger(ScannerSelectionDialog.class);
    private static final String SCANNER_ADDRESS_DEFAULT = "localhost";
    private static final long serialVersionUID = 1L;
    private class SaneDeviceTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 1L;
        private final List<SaneDevice> devices;

        SaneDeviceTableModel(List<SaneDevice> devices) {
            super(new String[] {"Name", "Model", "Type", "Vendor"}, 0);
            this.devices = devices;
        }

        @Override
        public Object getValueAt(int row, int column) {
            switch (column) {
                case 0:
                    return devices.get(row).getName();
                case 1:
                    return devices.get(row).getModel();
                case 2:
                    return devices.get(row).getType();
                case 3:
                    return devices.get(row).getVendor();
                default:
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
    private final SaneDeviceTableModel tableModel = new SaneDeviceTableModel(new LinkedList<SaneDevice>());
    /**
     * A {@link SaneSession} which is necessary to get the names of available
     * {@link SaneDevice}s which are then retrieved with
     * {@link DocumentScanner#getScannerDevice(java.lang.String, java.util.Map, java.lang.String) }
     * in order to work around the configuration mess, i.e. don't use devices
     * retrieved from this session.
     */
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
    private final DocumentScannerConf documentScannerConf;
    private final DocumentController documentController;

    /**
     * Creates new scanner selection dialog. The selection result and initial
     * parameters like the initial scanner search address will be updated in and
     * retrieved from {@code documentScannerConf}.
     *
     * @param parent
     * @param messageHandler
     * @param documentScannerConf
     */
    public ScannerSelectionDialog(java.awt.Frame parent,
            MessageHandler messageHandler,
            DocumentScannerConf documentScannerConf,
            DocumentController documentController) throws UnknownHostException, IOException, SaneException {
        super(parent,
                DocumentScanner.generateApplicationWindowTitle("Select scanner",
                        Constants.APP_NAME,
                        Constants.APP_VERSION),
                true //modal
        );
        if(messageHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.messageHandler = messageHandler;
        if(documentScannerConf == null) {
            throw new IllegalArgumentException("documentScannerConf mustn't be null");
        }
        this.documentScannerConf = documentScannerConf;
        if(documentController == null) {
            throw new IllegalArgumentException("documentController mustn't be null");
        }
        this.documentController = documentController;
        initComponents();
        this.tableModel.addTableModelListener(this.scannerDialogTableModelListener);
        this.scannerDialogTable.getSelectionModel().addListSelectionListener(this.scannerDialogTableSelectionListener);
        this.scannerDialogAddressTextField.setText(documentScannerConf.getScannerSaneAddress());
        for(String scannerName : documentScannerConf.getScannerConfMap().keySet()) {
            SaneDevice existingDevice = documentController.getScannerDevice(scannerName,
                    documentScannerConf.getScannerConfMap(),
                    SCANNER_ADDRESS_DEFAULT,
                    documentScannerConf.getResolutionWish());
            tableModel.addDevice(existingDevice);
        }
    }

    private boolean scannerDialogSelectButtonEnabled() {
        return this.tableModel.getRowCount() > 0
                && this.scannerDialogTable.getSelectedRowCount() > 0;
    }

    private void searchScanner() {
        String addressString = this.scannerDialogAddressTextField.getText();
        InetAddress address0;
        this.scannerDialogStatusLabel.setText("Searching...");
        this.tableModel.clear();
        try {
            address0 = InetAddress.getByName(addressString);
            this.saneSession = SaneSession.withRemoteSane(address0);
            List<SaneDevice> availableDevices = this.saneSession.listDevices();
            for(SaneDevice availableDevice : availableDevices) {
                if(!availableDevice.isOpen()) {
                    availableDevice.open();
                }
                if(availableDevice.getOption(ScannerEditDialog.RESOLUTION_OPTION_NAME) == null) {
                    LOGGER.info(String.format("ignoring device '%s' because it doesn't support the option '%s'",
                            availableDevice.toString(),
                            ScannerEditDialog.RESOLUTION_OPTION_NAME));
                    continue;
                }
                SaneDevice cachedAvailableDevice = documentController.getScannerDevice(availableDevice.getName(),
                        documentScannerConf.getScannerConfMap(),
                        addressString,
                        documentScannerConf.getResolutionWish()); //otherwise option changes are lost
                this.tableModel.addDevice(cachedAvailableDevice);
            }
            this.scannerDialogStatusLabel.setText(" ");
            this.address = addressString;
        } catch (ConnectException ex) {
            this.handleSearchScannerException(ex,
                    Constants.SANED_BUG_INFO);
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
        LOGGER.info("Scanner search failed due to following exception", ex);
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
        this.documentScannerConf.setScannerName(this.selectedDevice.getName());
        this.documentScannerConf.setScannerSaneAddress(address);
        this.setVisible(false);
    }//GEN-LAST:event_scannerDialogSelectButtonActionPerformed

    private void scannerDialogEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scannerDialogEditButtonActionPerformed
        assert this.scannerDialogTable.getSelectedRow() != -1;
        SaneDevice device = this.tableModel.getDevices().get(this.scannerDialogTable.getSelectedRow());
        ScannerEditDialog scannerEditDialog;
        ScannerConf scannerConf = this.documentScannerConf.getScannerConfMap().get(device.getName());
        try {
            documentController.configureDefaultOptionValues(device,
                scannerConf,
                documentScannerConf.getResolutionWish());
            scannerEditDialog = new ScannerEditDialog(this,
                documentController,
                device,
                scannerConf,
                documentScannerConf.getResolutionWish(),
                this.messageHandler);
            scannerEditDialog.setVisible(true);
        } catch (IOException | SaneException ex) {
            LOGGER.error("Exception during scanner configuration", ex);
            this.messageHandler.handle(new Message(String.format("Exception during scanner configuration: %s", ExceptionUtils.getRootCauseMessage(ex)),
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
