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

import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneOption;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import org.jscience.economics.money.Currency;
import richtercloud.document.scanner.model.Bill;
import richtercloud.reflection.form.builder.message.MessageHandler;

/**
 *
 * @author richter
 */
public class DocumentScannerConf implements Serializable {
    private static final long serialVersionUID = 1L;
    private final static String SCANNER_SANE_ADDRESS_DEFAULT = "localhost";
    private final static OCREngineConf<?> OCR_ENGINE_CONF_DEFAULT = new TesseractOCREngineConf();
    private static final Locale LOCALE_DEFAULT = Locale.getDefault();
    private static final Currency CURRENCY_DEFAULT = new Currency(java.util.Currency.getInstance(LOCALE_DEFAULT).getCurrencyCode());
    private final static boolean AUTO_GENERATE_IDS_DEFAULT = true;
    private final static boolean AUTO_SAVE_IMAGE_DATA_DEFAULT = true;
    private final static boolean AUTO_SAVE_OCR_DATA_DEFAULT = true;

    private static Set<StorageConf<?,?>> generateAvailableStorageConfsDefault(EntityManager entityManager,
            MessageHandler messageHandler,
            Set<Class<?>> entityClasses,
            File schemeChecksumFile,
            File xMLStorageFile) throws IOException {
        Set<StorageConf<?,?>> availableStorageConfs = new HashSet<>();
        availableStorageConfs.add(new DerbyPersistenceStorageConf(entityManager,
                messageHandler,
                entityClasses,
                schemeChecksumFile));
        availableStorageConfs.add(new XMLStorageConf(xMLStorageFile));
        return availableStorageConfs;
    }

    private String scannerName;
    private String scannerSaneAddress = SCANNER_SANE_ADDRESS_DEFAULT;
    /**
     * The selected storage configuration.
     *
     * @see #availableStorageConfs
     */
    private StorageConf<?,?> storageConf;
    /**
     * Available storage configurations (including all changes done to them).
     *
     * @see #storageConf
     */
    private Set<StorageConf<?,?>> availableStorageConfs = new HashSet<>();
    private OCREngineConf<?> oCREngineConf = OCR_ENGINE_CONF_DEFAULT;
    /**
     * The currency initially displayed in data entry components (e.g. for
     * {@link Bill}). Can be different from the currency of the locale.
     */
    private Currency currency = CURRENCY_DEFAULT;
    /**
     * A flag indicating that the IDs are generated automatically if the save
     * button is pressed (without the id generation button of the id panel
     * being pressed)
     */
    private boolean autoGenerateIDs;
    private Locale locale = LOCALE_DEFAULT;
    private boolean autoSaveImageData;
    private boolean autoSaveOCRData;
    /**
     * Stores information about changed SANE options because {@link SaneDevice}
     * and {@link SaneOption} don't provide a way to do that.
     */
    private Map<String, Map<String, Object>> scannerChangedOptions = new HashMap<>();
    /**
     * Whether to select automatic selection of format or locale format
     * intially.*/
    private boolean automaticFormatInitiallySelected = true;

    protected DocumentScannerConf() {
    }

    public DocumentScannerConf(EntityManager entityManager,
            MessageHandler messageHandler,
            Set<Class<?>> entityClasses,
            File schemeChecksumFile,
            File xMLStorageFile) throws IOException {
        this(new DerbyPersistenceStorageConf(entityManager,
                messageHandler,
                entityClasses,
                schemeChecksumFile),
                generateAvailableStorageConfsDefault(entityManager,
                        messageHandler,
                        entityClasses,
                        schemeChecksumFile,
                        xMLStorageFile),
                AUTO_GENERATE_IDS_DEFAULT,
                AUTO_SAVE_IMAGE_DATA_DEFAULT,
                AUTO_SAVE_OCR_DATA_DEFAULT);
    }

    public DocumentScannerConf(StorageConf storageConf,
            Set<StorageConf<?,?>> availableStorageConfs,
            boolean autoGenerateIDs,
            boolean autoSaveImageData,
            boolean autoSaveOCRData) {
        this.storageConf = storageConf;
        this.availableStorageConfs = availableStorageConfs;
        this.autoGenerateIDs = autoGenerateIDs;
        this.autoSaveImageData = autoSaveImageData;
        this.autoSaveOCRData = autoSaveOCRData;
    }

    public DocumentScannerConf(DocumentScannerConf documentScannerConf) {
        this(documentScannerConf.getStorageConf(),
                documentScannerConf.getAvailableStorageConfs(),
                documentScannerConf.isAutoGenerateIDs(),
                documentScannerConf.isAutoSaveImageData(),
                documentScannerConf.isAutoSaveOCRData());
    }

    public Map<String, Map<String, Object>> getScannerChangedOptions() {
        return scannerChangedOptions;
    }

    public void setScannerChangedOptions(Map<String, Map<String, Object>> scannerChangedOptions) {
        this.scannerChangedOptions = scannerChangedOptions;
    }

    /**
     * @return the scannerName
     */
    public String getScannerName() {
        return this.scannerName;
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
        return this.scannerSaneAddress;
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
        return this.oCREngineConf;
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
    public StorageConf<?,?> getStorageConf() {
        return this.storageConf;
    }

    /**
     * @param storageConf the storageConf to set
     */
    public void setStorageConf(StorageConf<?,?> storageConf) {
        this.storageConf = storageConf;
    }

    /**
     * @return the currency
     */
    public Currency getCurrency() {
        return this.currency;
    }

    /**
     * @param currency the currency to set
     */
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setAvailableStorageConfs(Set<StorageConf<?,?>> availableStorageConfs) {
        this.availableStorageConfs = availableStorageConfs;
    }

    public Set<StorageConf<?,?>> getAvailableStorageConfs() {
        return availableStorageConfs;
    }

    public void setAutoGenerateIDs(boolean autoGenerateIDs) {
        this.autoGenerateIDs = autoGenerateIDs;
    }

    public boolean isAutoGenerateIDs() {
        return autoGenerateIDs;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * @return the autoSaveImageData
     */
    public boolean isAutoSaveImageData() {
        return autoSaveImageData;
    }

    /**
     * @param autoSaveImageData the autoSaveImageData to set
     */
    public void setAutoSaveImageData(boolean autoSaveImageData) {
        this.autoSaveImageData = autoSaveImageData;
    }

    /**
     * @return the autoSaveOCRData
     */
    public boolean isAutoSaveOCRData() {
        return autoSaveOCRData;
    }

    /**
     * @param autoSaveOCRData the autoSaveOCRData to set
     */
    public void setAutoSaveOCRData(boolean autoSaveOCRData) {
        this.autoSaveOCRData = autoSaveOCRData;
    }

    public boolean isAutomaticFormatInitiallySelected() {
        return automaticFormatInitiallySelected;
    }

    public void setAutomaticFormatInitiallySelected(boolean automaticFormatInitiallySelected) {
        this.automaticFormatInitiallySelected = automaticFormatInitiallySelected;
    }
}
