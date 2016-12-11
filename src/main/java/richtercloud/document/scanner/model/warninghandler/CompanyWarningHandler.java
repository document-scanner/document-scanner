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
package richtercloud.document.scanner.model.warninghandler;

import java.util.List;
import javax.swing.JOptionPane;
import richtercloud.document.scanner.model.Company;
import richtercloud.message.handler.ConfirmMessageHandler;
import richtercloud.message.handler.Message;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.jpa.WarningHandler;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;
import richtercloud.reflection.form.builder.storage.StorageException;

/**
 *
 * @author richter
 */
public class CompanyWarningHandler implements WarningHandler<Company> {
    private final PersistenceStorage storage;
    private final MessageHandler messageHandler;
    private final ConfirmMessageHandler confirmMessageHandler;

    public CompanyWarningHandler(PersistenceStorage storage,
            MessageHandler messageHandler,
            ConfirmMessageHandler confirmMessageHandler) {
        if(storage == null) {
            throw new IllegalArgumentException("storage mustn't be null");
        }
        this.storage = storage;
        if(messageHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.messageHandler = messageHandler;
        if(confirmMessageHandler == null) {
            throw new IllegalArgumentException("confirmMessageHandler mustn't be null");
        }
        this.confirmMessageHandler = confirmMessageHandler;
    }

    @Override
    public boolean handleWarning(Company instance) {
        List<Company> results;
        try {
            results = storage.runQuery("name", instance.getName(), Company.class);
        } catch (StorageException ex) {
            messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
            return false;
        }
        if(!results.isEmpty()) {
            int answer = confirmMessageHandler.confirm(new Message(String.format("An instance with the name '%s' already exists in the database. Continue anyway?", instance.getName()),
                    JOptionPane.WARNING_MESSAGE,
                    "Name already used"));
            if(answer != JOptionPane.YES_OPTION) {
                return false;
            }
        }
        return true;
    }
}
