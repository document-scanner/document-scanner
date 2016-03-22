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

import java.io.File;

/**
 *
 * @author richter
 */
public class DerbyPersistenceStorageConfInitializationException extends StorageConfInitializationException {
    private static final long serialVersionUID = 1L;
    /**
     * The file containing the checksum for validation (catchers of this
     * exception might want to suggest to remove this file in order to force
     * overwriting of the checksum and ignoring the reason for the storage
     * initialization exception).
     */
    private final File schemeChecksumFile;

    public DerbyPersistenceStorageConfInitializationException(File schemeChecksumFile) {
        this.schemeChecksumFile = schemeChecksumFile;
    }

    public DerbyPersistenceStorageConfInitializationException(String message, File schemeChecksumFile) {
        super(message);
        this.schemeChecksumFile = schemeChecksumFile;
    }

    public DerbyPersistenceStorageConfInitializationException(String message, Throwable cause, File schemeChecksumFile) {
        super(message, cause);
        this.schemeChecksumFile = schemeChecksumFile;
    }

    public DerbyPersistenceStorageConfInitializationException(Throwable cause, File schemeChecksumFile) {
        super(cause);
        this.schemeChecksumFile = schemeChecksumFile;
    }

    public File getSchemeChecksumFile() {
        return schemeChecksumFile;
    }

}
