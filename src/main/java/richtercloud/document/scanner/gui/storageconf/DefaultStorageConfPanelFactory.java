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

import java.io.IOException;
import richtercloud.message.handler.ConfirmMessageHandler;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.jpa.storage.DerbyEmbeddedPersistenceStorageConf;
import richtercloud.reflection.form.builder.jpa.storage.DerbyNetworkPersistenceStorageConf;
import richtercloud.reflection.form.builder.jpa.storage.MySQLAutoPersistenceStorageConf;
import richtercloud.reflection.form.builder.jpa.storage.PostgresqlAutoPersistenceStorageConf;
import richtercloud.reflection.form.builder.storage.StorageConf;

/**
 *
 * @author richter
 */
public class DefaultStorageConfPanelFactory implements StorageConfPanelFactory {
    private final MessageHandler messageHandler;
    private final ConfirmMessageHandler confirmMessageHandler;
    private final boolean skipMD5SumCheck;

    public DefaultStorageConfPanelFactory(MessageHandler messageHandler,
            ConfirmMessageHandler confirmMessageHandler,
            boolean skipMD5SumCheck) {
        this.messageHandler = messageHandler;
        this.confirmMessageHandler = confirmMessageHandler;
        this.skipMD5SumCheck = skipMD5SumCheck;
    }

    @Override
    public StorageConfPanel create(StorageConf storageConf) throws StorageConfPanelCreationException {
        StorageConfPanel retValue;
        if(storageConf instanceof DerbyEmbeddedPersistenceStorageConf) {
            retValue = new DerbyEmbeddedPersistenceStorageConfPanel((DerbyEmbeddedPersistenceStorageConf) storageConf);
        }else if(storageConf instanceof DerbyNetworkPersistenceStorageConf) {
            retValue = new DerbyNetworkPersistenceStorageConfPanel((DerbyNetworkPersistenceStorageConf) storageConf);
        }else if(storageConf instanceof PostgresqlAutoPersistenceStorageConf) {
            retValue = new PostgresqlAutoPersistenceStorageConfPanel((PostgresqlAutoPersistenceStorageConf) storageConf);
        }else if(storageConf instanceof MySQLAutoPersistenceStorageConf) {
            try {
                retValue = new MySQLAutoPersistenceStorageConfPanel((MySQLAutoPersistenceStorageConf) storageConf,
                        messageHandler,
                        confirmMessageHandler,
                        skipMD5SumCheck);
            } catch (IOException ex) {
                throw new StorageConfPanelCreationException(ex);
            }
        }else {
            throw new IllegalArgumentException(String.format("Storage configuration of type '%s' isn't supported", storageConf.getClass()));
        }
        return retValue;
    }
}
