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

import java.io.Serializable;
import richtercloud.document.scanner.storage.Storage;

/**
 *
 * @author richter
 */
public class DocumentScannerConf implements Serializable {
    private static final long serialVersionUID = 1L;
    private final static String SCANNER_SANE_ADDRESS_DEFAULT = "localhost";
    private final static StorageConf<?> STORAGE_CONF_DEFAULT = new DerbyPersistenceStorageConf();
    private final static OCREngineConf<?> OCR_ENGINE_CONF_DEFAULT = new TesseractOCREngineConf();
    private String scannerName;
    private String scannerSaneAddress = SCANNER_SANE_ADDRESS_DEFAULT;
    private StorageConf<?> storageConf = STORAGE_CONF_DEFAULT;
    private OCREngineConf<?> oCREngineConf = OCR_ENGINE_CONF_DEFAULT;

    public DocumentScannerConf() {
    }

    /**
     * @return the scannerName
     */
    public String getScannerName() {
        return scannerName;
    }

    /**
     * @param scannerName the scannerName to set
     */
    public void setScannerName(String scannerName) {
        this.scannerName = scannerName;
    }

    /**
     * @return the scannerSaneAddress
     */
    public String getScannerSaneAddress() {
        return scannerSaneAddress;
    }

    /**
     * @param scannerSaneAddress the scannerSaneAddress to set
     */
    public void setScannerSaneAddress(String scannerSaneAddress) {
        this.scannerSaneAddress = scannerSaneAddress;
    }

    /**
     * @return the oCREngineConf
     */
    public OCREngineConf<?> getoCREngineConf() {
        return oCREngineConf;
    }

    /**
     * @param oCREngineConf the oCREngineConf to set
     */
    public void setoCREngineConf(OCREngineConf<?> oCREngineConf) {
        this.oCREngineConf = oCREngineConf;
    }

    /**
     * @return the storageConf
     */
    public StorageConf<?> getStorageConf() {
        return storageConf;
    }

    /**
     * @param storageConf the storageConf to set
     */
    public void setStorageConf(StorageConf<?> storageConf) {
        this.storageConf = storageConf;
    }
}
