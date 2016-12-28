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

import java.awt.Window;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import richtercloud.message.handler.Message;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.storage.StorageConf;
import richtercloud.reflection.form.builder.storage.StorageConfValidationException;

/**
 * Wraps a {@link StorageConfPanel} and a close button. A possibility to reset
 * changes isn't provided in order to KISS, i.e. all option changes are written
 * on the configuration object immediately.
 * @author richter
 */
/*
internal implementation notes:
- needs a save and cancel buttion in order to deal with invalid StorageConfs
(validated with StorageConf.save)
*/
public class StorageEditDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final JButton cancelButton = new JButton("Cancel");
    private final JButton saveButton = new JButton("Save");
    private final MessageHandler messageHandler;
    /**
     * The (eventually) edited {@code StorageConf}. {@code null} indicates that
     * the dialog has been aborted.
     */
    /*
    internal implementation notes:
    - It might be possible to get a StorageConf instance from StorageConfPanel,
    but that's not too elegant and there's need for an indication for dialog
    cancelation.
    */
    private StorageConf editedStorageConf = null;

    public StorageEditDialog(Window parent,
            StorageConfPanel storageConfPanel,
            MessageHandler messageHandler) {
        super(parent,
                ModalityType.APPLICATION_MODAL);
        if(storageConfPanel == null) {
            throw new IllegalArgumentException("storageConfPanel mustn't be null");
        }
        if(messageHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.messageHandler = messageHandler;
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(storageConfPanel).
                addGroup(layout.createSequentialGroup()
                        .addComponent(cancelButton)
                        .addComponent(saveButton)));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(storageConfPanel)
                .addGroup(layout.createParallelGroup()
                        .addComponent(cancelButton)
                        .addComponent(saveButton)));
        pack();
        cancelButton.addActionListener((event) ->  setVisible(false));
        saveButton.addActionListener((event) -> {
            storageConfPanel.save();
            try {
                storageConfPanel.getStorageConf().validate();
            }catch(StorageConfValidationException ex) {
                //keep create dialog displayed until a valid StorageConf is created
                //or the creation has been canceled
                messageHandler.handle(new Message(ex,
                        JOptionPane.ERROR_MESSAGE));
                return;
            }
            this.editedStorageConf = storageConfPanel.getStorageConf();
            this.setVisible(false);
        });
    }

    public StorageConf getEditedStorageConf() {
        return editedStorageConf;
    }
}
