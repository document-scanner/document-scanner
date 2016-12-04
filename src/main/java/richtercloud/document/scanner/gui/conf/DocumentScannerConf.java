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

import com.beust.jcommander.Parameter;
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
import richtercloud.document.scanner.gui.ScannerConf;
import richtercloud.message.handler.MessageHandler;

/**
 * Represents deserialized instances of configurations which are supposed to be
 * serializable to a configuration file.
 *
 * Holds all configuration-related default values in form of constants.
 *
 * Following concept https://richtercloud.de:446/dokuwiki/doku.php?id=programming:java#configuration_files_and_command_line_interface.
 * There's no support for a base directory option which adjusts all path-related
 * options, i.e. the user is forced to change all path-related options which is
 * just fine.
 *
 * @author richter
 */
public class DocumentScannerConf implements Serializable {
    private static final long serialVersionUID = 1L;
    public final static String SCANNER_SANE_ADDRESS_DEFAULT = "localhost";
    private final static OCREngineConf<?> OCR_ENGINE_CONF_DEFAULT = new TesseractOCREngineConf();
    private static final Locale LOCALE_DEFAULT = Locale.getDefault();
    private static final Currency CURRENCY_DEFAULT = new Currency(java.util.Currency.getInstance(LOCALE_DEFAULT).getCurrencyCode());
    private final static boolean AUTO_GENERATE_IDS_DEFAULT = true;
    private final static boolean AUTO_SAVE_IMAGE_DATA_DEFAULT = true;
    private final static boolean AUTO_SAVE_OCR_DATA_DEFAULT = true;
    private final static float ZOOM_LEVEL_MULTIPLIER_DEFAULT = 0.66f;
    /**
     * A minimal limit for the zoom level in order to prevent too non-sensical
     * values.
     */
    private final static float ZOOM_LEVEL_MIN = 0.01f;
    public final static File HOME_DIR = new File(System.getProperty("user.home"));
    private static final String CONFIG_DIR_NAME_DEFAULT = ".document-scanner";
    private final static File CONFIG_DIR_DEFAULT = new File(HOME_DIR, CONFIG_DIR_NAME_DEFAULT);
    private final static String CONFIG_FILE_NAME_DEFAULT = "document-scanner-config.xml";
    private final static File CONFIG_FILE_DEFAULT = new File(CONFIG_DIR_DEFAULT, CONFIG_FILE_NAME_DEFAULT);
    /**
     * The file the this configuration has been loaded from. Might be
     * {@code null} if no initial configuration file has been specified.
     */
    @Parameter(names = {"-c", "--config-file"}, description = "An alternative configuration file location")
    /*
    internal implementation notes:
    - unused in the application, but specification here dramatically facilitates
    command line parsing
    */
    private File configFile = CONFIG_FILE_DEFAULT;
    public static final String DATABASE_DIR_NAME_DEFAULT = "databases";
    private final static File DATABASE_DIR_DEFAULT = new File(CONFIG_DIR_DEFAULT, DATABASE_DIR_NAME_DEFAULT);
    private File databaseDir = DATABASE_DIR_DEFAULT;
    private final static String DERBY_CONNECTION_URL_DEFAULT = String.format("jdbc:derby:%s", DATABASE_DIR_DEFAULT.getAbsolutePath());
    private String derbyConnectionURL = DERBY_CONNECTION_URL_DEFAULT;

    private static Set<StorageConf<?,?>> generateAvailableStorageConfsDefault(EntityManager entityManager,
            Set<Class<?>> entityClasses,
            File schemeChecksumFile,
            File xMLStorageFile) throws IOException {
        Set<StorageConf<?,?>> availableStorageConfs = new HashSet<>();
        availableStorageConfs.add(new DerbyPersistenceStorageConf(entityManager,
                entityClasses,
                schemeChecksumFile));
        availableStorageConfs.add(new XMLStorageConf(xMLStorageFile));
        return availableStorageConfs;
    }

    private String scannerName;
    /**
     * The last (or initial) address where to search scanners for. The address
     * of the selected scanner is supposed to be retrieved from it's associated
     * {@link ScannerConf} (see {@link #scannerConfMap}).
     */
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
     * Whether to select automatic selection of format or locale format
     * intially.*/
    private boolean automaticFormatInitiallySelected = true;
    /**
     * Whether to try to automatically check OCR for values (dates, amounts,
     * etc.) after a document has been added.
     */
    private boolean autoOCRValueDetection = true;
    /**
     * Mapping between scanner name and it's {@link ScannerConf}.
     */
    private Map<String, ScannerConf> scannerConfMap = new HashMap<>();
    /**
     * The zoom level multiplier which decides how large the effect of a zoom-in
     * or zoom-out operation is. Has to be between {@link #ZOOM_LEVEL_MIN} and
     * {@code 1.0}.
     */
    private float zoomLevelMultiplier = ZOOM_LEVEL_MULTIPLIER_DEFAULT;
    public final static int PREFERRED_WIDTH_DEFAULT = 600;
        //300 is pretty small for an average screen
    /**
     * The width of preview images which should be matched as closely as
     * possible with default zoom levels.
     */
    private int preferredWidth = PREFERRED_WIDTH_DEFAULT;
    private final static String XML_STORAGE_FILE_NAME_DEFAULT = "xml-storage.xml";
    private final static File XML_STORAGE_FILE_DEFAULT = new File(CONFIG_DIR_DEFAULT, XML_STORAGE_FILE_NAME_DEFAULT);
    private File xMLStorageFile = XML_STORAGE_FILE_DEFAULT;
    private final static File DERBY_PERSISTENCE_STORAGE_SCHEME_CHECKSUM_FILE_DEFAULT = new File(CONFIG_DIR_DEFAULT, DerbyPersistenceStorageConf.SCHEME_CHECKSUM_FILE_NAME);
    private File derbyPersistenceStorageSchemeChecksumFile = DERBY_PERSISTENCE_STORAGE_SCHEME_CHECKSUM_FILE_DEFAULT;
    private File amountMoneyUsageStatisticsStorageFile = new File(CONFIG_DIR_DEFAULT, AMOUNT_MONEY_USAGE_STATISTICS_STORAGE_FILE_NAME);
    private File amountMoneyCurrencyStorageFile = new File(CONFIG_DIR_DEFAULT, AMOUNT_MONEY_CURRENCY_STORAGE_FILE_NAME);
    private final static String AMOUNT_MONEY_USAGE_STATISTICS_STORAGE_FILE_NAME = "currency-usage-statistics.xml";
    private final static String AMOUNT_MONEY_CURRENCY_STORAGE_FILE_NAME = "currencies.xml";
    private final static String TAG_STORAGE_FILE_NAME = "tags";
    private final static File TAG_STORAGE_FILE_DEFAULT = new File(CONFIG_DIR_DEFAULT, TAG_STORAGE_FILE_NAME);
    private File tagStorageFile = TAG_STORAGE_FILE_DEFAULT;

    @Parameter(names= {"-d", "--debug"}, description= "Print extra debugging statements")
    private boolean debug = false;

    /**
     * Creates an configuration with default values.
     */
    public DocumentScannerConf() {
    }

    public DocumentScannerConf(EntityManager entityManager,
            MessageHandler messageHandler,
            Set<Class<?>> entityClasses,
            File schemeChecksumFile,
            File xMLStorageFile) throws IOException {
        this(new DerbyPersistenceStorageConf(entityManager,
                entityClasses,
                schemeChecksumFile),
                generateAvailableStorageConfsDefault(entityManager,
                        entityClasses,
                        schemeChecksumFile,
                        xMLStorageFile),
                AUTO_GENERATE_IDS_DEFAULT,
                AUTO_SAVE_IMAGE_DATA_DEFAULT,
                AUTO_SAVE_OCR_DATA_DEFAULT,
                new HashMap<String, ScannerConf>(),
                ZOOM_LEVEL_MULTIPLIER_DEFAULT,
                PREFERRED_WIDTH_DEFAULT);
    }

    public DocumentScannerConf(StorageConf storageConf,
            Set<StorageConf<?,?>> availableStorageConfs,
            boolean autoGenerateIDs,
            boolean autoSaveImageData,
            boolean autoSaveOCRData,
            Map<String, ScannerConf> scannerConfMap,
            float zoomLevelMultiplier,
            int preferredWidth) {
        this.storageConf = storageConf;
        this.availableStorageConfs = availableStorageConfs;
        this.autoGenerateIDs = autoGenerateIDs;
        this.autoSaveImageData = autoSaveImageData;
        this.autoSaveOCRData = autoSaveOCRData;
        this.scannerConfMap = scannerConfMap;
        if(zoomLevelMultiplier > 1.0) {
            throw new IllegalArgumentException(String.format("The zoom level "
                    + "multiplier mustn't be greater than 1.0 since the "
                    + "application interprets it between %f and 1.0",
                    ZOOM_LEVEL_MIN));
        }
        if(zoomLevelMultiplier < ZOOM_LEVEL_MIN) {
            throw new IllegalArgumentException(String.format("The zoom level "
                    + "multiplier mustn't be less than %f since the "
                    + "application interprets it between %f and 1.0",
                    ZOOM_LEVEL_MIN,
                    ZOOM_LEVEL_MIN));
        }
        this.zoomLevelMultiplier = zoomLevelMultiplier;
        if(preferredWidth < 10) {
            throw new IllegalArgumentException("A preferred width of less than "
                    + "10 will cause severe displaying issues and will thus "
                    + "not be supported");
        }
        this.preferredWidth = preferredWidth;
    }

    public DocumentScannerConf(DocumentScannerConf documentScannerConf) {
        this(documentScannerConf.getStorageConf(),
                documentScannerConf.getAvailableStorageConfs(),
                documentScannerConf.isAutoGenerateIDs(),
                documentScannerConf.isAutoSaveImageData(),
                documentScannerConf.isAutoSaveOCRData(),
                documentScannerConf.getScannerConfMap(),
                documentScannerConf.getZoomLevelMultiplier(),
                documentScannerConf.getPreferredWidth()
        );
    }

    public File getConfigFile() {
        return configFile;
    }

    public boolean isDebug() {
        return this.debug;
    }

    protected void setDebug(boolean debug) {
        this.debug = debug;
    }

    public File getDatabaseDir() {
        return databaseDir;
    }

    public String getDerbyConnectionURL() {
        return derbyConnectionURL;
    }

    public File getTagStorageFile() {
        return tagStorageFile;
    }

    public int getPreferredWidth() {
        return preferredWidth;
    }

    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    public float getZoomLevelMultiplier() {
        return zoomLevelMultiplier;
    }

    public void setZoomLevelMultiplier(float zoomLevelMultiplier) {
        this.zoomLevelMultiplier = zoomLevelMultiplier;
    }

    public Map<String, ScannerConf> getScannerConfMap() {
        return scannerConfMap;
    }

    public void setScannerConf(Map<String, ScannerConf> scannerConfMap) {
        this.scannerConfMap = scannerConfMap;
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

    public boolean isAutoOCRValueDetection() {
        return autoOCRValueDetection;
    }

    public void setAutoOCRValueDetection(boolean autoOCRValueDetection) {
        this.autoOCRValueDetection = autoOCRValueDetection;
    }

    /**
     * @return the xMLStorageFile
     */
    public File getxMLStorageFile() {
        return xMLStorageFile;
    }

    /**
     * @return the derbyPersistenceStorageSchemeChecksumFile
     */
    public File getDerbyPersistenceStorageSchemeChecksumFile() {
        return derbyPersistenceStorageSchemeChecksumFile;
    }

    /**
     * @return the amountMoneyUsageStatisticsStorageFile
     */
    public File getAmountMoneyUsageStatisticsStorageFile() {
        return amountMoneyUsageStatisticsStorageFile;
    }

    /**
     * @return the amountMoneyCurrencyStorageFile
     */
    public File getAmountMoneyCurrencyStorageFile() {
        return amountMoneyCurrencyStorageFile;
    }
}
