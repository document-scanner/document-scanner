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

import richtercloud.document.scanner.gui.storageconf.StorageConfPanel;
import richtercloud.reflection.form.builder.jpa.storage.DerbyEmbeddedPersistenceStorageConf;
import richtercloud.reflection.form.builder.jpa.storage.DerbyNetworkPersistenceStorageConf;
import richtercloud.reflection.form.builder.storage.StorageConf;

/**
 *
 * @author richter
 */
public class DefaultStorageConfPanelFactory implements StorageConfPanelFactory {
    private final DerbyEmbeddedPersistenceStorageConfPanel derbyEmbeddedPersistenceStorageConfPanel = new DerbyEmbeddedPersistenceStorageConfPanel();
    private final DerbyNetworkPersistenceStorageConfPanel derbyNetworkPersistenceStorageConfPanel = new DerbyNetworkPersistenceStorageConfPanel();

    @Override
    public StorageConfPanel create(StorageConf storageConf) {
        StorageConfPanel retValue;
        if(storageConf instanceof DerbyEmbeddedPersistenceStorageConf) {
            derbyEmbeddedPersistenceStorageConfPanel.applyStorageConf((DerbyEmbeddedPersistenceStorageConf) storageConf);
            retValue = derbyEmbeddedPersistenceStorageConfPanel;
        }else if(storageConf instanceof DerbyNetworkPersistenceStorageConf) {
            derbyNetworkPersistenceStorageConfPanel.applyStorageConf((DerbyNetworkPersistenceStorageConf) storageConf);
            retValue = derbyNetworkPersistenceStorageConfPanel;
        }else {
            throw new IllegalArgumentException(String.format("Storage configuration of type '%s' isn't supported", storageConf.getClass()));
        }
        return retValue;
    }
}
