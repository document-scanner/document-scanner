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
package richtercloud.document.scanner.gui.conf;

import richtercloud.document.scanner.storage.Storage;

/**
 * Both a data container and factory for instance of {@code S}.
 * @author richter
 * @param <S> the type of the storage managed by this configuration
 */
public interface StorageConf<S extends Storage> {

    /**
     * The managed instance of {@link Storage}.
     * @return
     */
    S getStorage();

    /**
     * Validates the storage configuration (e.g. a database could validate the
     * current database scheme against a persisted one and offer a migration
     * guide if changes were detected).
     * @throws richtercloud.document.scanner.gui.conf.StorageConfInitializationException
     */
    void validate() throws StorageConfInitializationException;
}
