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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.jscience.economics.money.Currency;
import richtercloud.document.scanner.components.DateOCRResultFormatter;
import richtercloud.document.scanner.components.OCRResultFormatter;
import richtercloud.document.scanner.gui.Constants;
import richtercloud.document.scanner.gui.scanner.ScannerConf;
import richtercloud.document.scanner.ifaces.OCREngineConf;
import richtercloud.document.scanner.ocr.TesseractOCREngineConf;
import richtercloud.document.scanner.valuedetectionservice.ContactValueDetectionServiceConf;
import richtercloud.document.scanner.valuedetectionservice.CurrencyFormatValueDetectionServiceConf;
import richtercloud.document.scanner.valuedetectionservice.DateFormatValueDetectionServiceConf;
import richtercloud.document.scanner.valuedetectionservice.TrieCurrencyFormatValueDetectionServiceConf;
import richtercloud.document.scanner.valuedetectionservice.ValueDetectionServiceConf;
import richtercloud.reflection.form.builder.jpa.storage.DerbyEmbeddedPersistenceStorageConf;
import richtercloud.reflection.form.builder.jpa.storage.DerbyNetworkPersistenceStorageConf;
import richtercloud.reflection.form.builder.jpa.storage.MySQLAutoPersistenceStorageConf;
import richtercloud.reflection.form.builder.jpa.storage.PostgresqlAutoPersistenceStorageConf;
import richtercloud.reflection.form.builder.storage.StorageConf;
import richtercloud.reflection.form.builder.storage.XMLStorageConf;

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
    private final static OCREngineConf OCR_ENGINE_CONF_DEFAULT = new TesseractOCREngineConf();
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
    public final static File CONFIG_DIR_DEFAULT = new File(HOME_DIR, CONFIG_DIR_NAME_DEFAULT);
    private final static String CONFIG_FILE_NAME_DEFAULT = "document-scanner-config.xml";
    private final static File CONFIG_FILE_DEFAULT = new File(CONFIG_DIR_DEFAULT, CONFIG_FILE_NAME_DEFAULT);
    //keep some database configuration-related constants here since they ought
    //to be managed agnostic of default values in reflection-form-builder
    public final static String SCHEME_CHECKSUM_FILE_NAME_DEFAULT = "last-scheme.xml";
    private final static File SCHEME_CHECKSUM_FILE_DEFAULT = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT,
            SCHEME_CHECKSUM_FILE_NAME_DEFAULT);
    public static final String DATABASE_DIR_NAME_DEFAULT = "databases";
    public final static String DATABASE_NAME_DEFAULT = new File(DocumentScannerConf.CONFIG_DIR_DEFAULT,
            DATABASE_DIR_NAME_DEFAULT).getAbsolutePath();
    public final static int PREFERRED_SCAN_RESULT_PANEL_WIDTH_DEFAULT = 600;
        //300 is pretty small for an average screen
    public final static int PREFERRED_OCR_SELECT_PANEL_WIDTH = 600;
    private final static String XML_STORAGE_FILE_NAME_DEFAULT = "xml-storage.xml";
    private final static File XML_STORAGE_FILE_DEFAULT = new File(CONFIG_DIR_DEFAULT, XML_STORAGE_FILE_NAME_DEFAULT);
    private final static File DERBY_PERSISTENCE_STORAGE_SCHEME_CHECKSUM_FILE_DEFAULT = new File(CONFIG_DIR_DEFAULT,
            SCHEME_CHECKSUM_FILE_NAME_DEFAULT);
    private final static String AMOUNT_MONEY_USAGE_STATISTICS_STORAGE_FILE_NAME = "currency-usage-statistics.xml";
    private final static String AMOUNT_MONEY_CURRENCY_STORAGE_FILE_NAME = "currencies.xml";
    private final static String TAG_STORAGE_FILE_NAME = "tags";
    private final static File TAG_STORAGE_FILE_DEFAULT = new File(CONFIG_DIR_DEFAULT, TAG_STORAGE_FILE_NAME);
    private final static String IMAGE_WRAPPER_STORAGE_FILE_NAME_DEFAULT = "image-storage";
    private final static File IMAGE_WRAPPER_STORAGE_DIR = new File(CONFIG_DIR_DEFAULT, IMAGE_WRAPPER_STORAGE_FILE_NAME_DEFAULT);
    private final static String HOSTNAME_DEFAULT = "localhost";
    private final static String POSTGRESQL_DATABASE_DIR_DEFAULT = new File(CONFIG_DIR_DEFAULT, "databases-postgresql").getAbsolutePath();
    private final static String MYSQL_DATABASE_DIR_DEFAULT = new File(CONFIG_DIR_DEFAULT, "databases-mysql").getAbsolutePath();
    public final static String LOG_FILE_PATH_DEFAULT = new File(CONFIG_DIR_DEFAULT, "document-scanner.log").getAbsolutePath();
    public final static int RESOLUTION_WISH_DEFAULT = 200;
    public final static String QUERY_HISTORY_ENTRY_STORAGE_FILE_NAME_DEFAULT = "query-history-storage.xml";
    public final static File QUERY_HISTORY_ENTRY_STORAGE_FILE_DEFAULT = new File(CONFIG_DIR_DEFAULT,
            QUERY_HISTORY_ENTRY_STORAGE_FILE_NAME_DEFAULT);
    public final static String AMOUNT_MONEY_EXCHANGE_RATE_RETRIEVER_FILE_CACHE_DIR_NAME_DEFAULT = "amount-money-exchange-rate-retriever-cache";
    public final static File AMOUNT_MONEY_EXCHANGE_RATE_RETRIEVER_FILE_CACHE_DIR_DEFAULT = new File(CONFIG_DIR_DEFAULT,
            AMOUNT_MONEY_EXCHANGE_RATE_RETRIEVER_FILE_CACHE_DIR_NAME_DEFAULT);
    public final static int AMOUNT_MONEY_EXCHANGE_RATE_RETRIEVER_EXPIRATION_MILLIS = 24*60*60*1000; //24 hours
    public final static String CREDENTIALS_STORE_FILE_NAME_DEFAULT = "credentials.xml";
    public final static File CREDENTIALS_STORE_FILE_DEFAULT = new File(CONFIG_DIR_DEFAULT, CREDENTIALS_STORE_FILE_NAME_DEFAULT);
    public final static Map<Class<?>, OCRResultFormatter<?>> AUTO_OCR_VALUE_DETECTION_FORMATTER_MAP;
    static {
        AUTO_OCR_VALUE_DETECTION_FORMATTER_MAP = new HashMap<>();
        AUTO_OCR_VALUE_DETECTION_FORMATTER_MAP.put(Date.class,
                new DateOCRResultFormatter());
    }
    private final static long SCANNER_OPEN_WAIT_TIME_DEFAULT = 15;
    private final static TimeUnit SCANNER_OPEN_WAIT_TIME_UNIT_DEFAULT = TimeUnit.SECONDS;
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
    private StorageConf storageConf;
    /**
     * Available storage configurations (including all changes done to them).
     *
     * @see #storageConf
     */
    private Set<StorageConf> availableStorageConfs = new HashSet<>();
    private OCREngineConf oCREngineConf;
    private Set<OCREngineConf> availableOCREngineConfs = new HashSet<>();
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
    private boolean autoGenerateIDs = AUTO_GENERATE_IDS_DEFAULT;
    private Locale locale = LOCALE_DEFAULT;
    private boolean autoSaveImageData = AUTO_SAVE_IMAGE_DATA_DEFAULT;
    private boolean autoSaveOCRData = AUTO_SAVE_OCR_DATA_DEFAULT;
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
    /**
     * The width of preview images in {@link ScanResultDialog}s.
     * @see #rememberScanResultPanelWidth
     */
    private int preferredScanResultPanelWidth = PREFERRED_SCAN_RESULT_PANEL_WIDTH_DEFAULT;
    private boolean rememberPreferredScanResultPanelWidth = true;
    /**
     * The width of preview images in {@link OCRSelectPanel}s. User preference
     * is most likely different from {@link #preferredScanResultPanelWidth}
     * since only one document is displayed and needs to be well readable in OCR
     * selection components.
     */
    private int preferredOCRSelectPanelWidth = PREFERRED_OCR_SELECT_PANEL_WIDTH;
    private boolean rememberPreferredOCRSelectPanelWidth = true;
    private File xMLStorageFile = XML_STORAGE_FILE_DEFAULT;
    private File derbyPersistenceStorageSchemeChecksumFile = DERBY_PERSISTENCE_STORAGE_SCHEME_CHECKSUM_FILE_DEFAULT;
    private File amountMoneyUsageStatisticsStorageFile = new File(CONFIG_DIR_DEFAULT, AMOUNT_MONEY_USAGE_STATISTICS_STORAGE_FILE_NAME);
    private File amountMoneyCurrencyStorageFile = new File(CONFIG_DIR_DEFAULT, AMOUNT_MONEY_CURRENCY_STORAGE_FILE_NAME);
    private File tagStorageFile = TAG_STORAGE_FILE_DEFAULT;
    private File imageWrapperStorageDir = IMAGE_WRAPPER_STORAGE_DIR;

    @Parameter(names= {"-d", "--debug"}, description= "Print extra debugging statements")
    private boolean debug = false;
    /**
     * Skip validation of MD5 sums in download routine in
     * {@link MySQLAutoPersistenceStorageConfPanel} which is extremely slow
     * (takes up to 10 minutes for an average MySQL download) when debugging.
     * This should only be configurable by editing the configuration file.
     */
    private boolean skipMD5SumCheck = false;
    private String logFilePath = LOG_FILE_PATH_DEFAULT;
    private Set<String> valueDetectionServiceJARPaths = new HashSet<>();
    private List<ValueDetectionServiceConf> availableValueDetectionServiceConfs = new LinkedList<>();
    private List<ValueDetectionServiceConf> selectedValueDetectionServiceConfs = new LinkedList<>();
    /** A value which is used in determining the default resolution. The value
     * the closest to this wish is selected from the supported values of the
     * device.
     */
    private int resolutionWish = RESOLUTION_WISH_DEFAULT;
    private File queryHistoryEntryStorageFile = QUERY_HISTORY_ENTRY_STORAGE_FILE_DEFAULT;
    private File amountMoneyExchangeRateRetrieverFileCacheDir = AMOUNT_MONEY_EXCHANGE_RATE_RETRIEVER_FILE_CACHE_DIR_DEFAULT;
    private int amountMoneyExchangeRateRetrieverExpirationMillis = AMOUNT_MONEY_EXCHANGE_RATE_RETRIEVER_EXPIRATION_MILLIS;
    /**
     * Whether or not the user has confirmed the usage of external automatic bug
     * tracking based on stacktraces of uncaught exceptions.
     */
    private boolean userAllowedAutoBugTracking = false;
    /**
     * Whether or not the user doesn't want to be asked again about whether
     * he_she allows the usage of external automatic bug tracking.
     */
    private boolean skipUserAllowedAutoBugTrackingQuestion = false;
    private File credentialsStoreFile = CREDENTIALS_STORE_FILE_DEFAULT;
    /**
     * A mapping for classes and associated
     * {@link AutoOCRValueDetectionResultFormatter}s.
     *
     * @see AutoOCRValueDetectionResultFormatter
     */
    /*
    internal implementation notes:
    - type chosen in order to allow easy configurability in GUI later (e.g. with
    annotation based discovery of AutoOCRValueDetectionResultFormatter on
    classpath which has an edit dialog associated where details (e.g. which
    date format to use) can be configured
    */
    private Map<Class<?>, OCRResultFormatter<?>> autoOCRValueDetectionFormatterMap = AUTO_OCR_VALUE_DETECTION_FORMATTER_MAP;
    private long scannerOpenWaitTime = SCANNER_OPEN_WAIT_TIME_DEFAULT;
    private TimeUnit scannerOpenWaitTimeUnit = SCANNER_OPEN_WAIT_TIME_UNIT_DEFAULT;

    private static Set<StorageConf> generateAvailableStorageConfsDefault(Set<Class<?>> entityClasses,
            File xMLStorageFile) throws IOException {
        Set<StorageConf> availableStorageConfs = new HashSet<>();
        availableStorageConfs.add(new DerbyEmbeddedPersistenceStorageConf(entityClasses,
                        DATABASE_NAME_DEFAULT,
                        SCHEME_CHECKSUM_FILE_DEFAULT));
        availableStorageConfs.add(new XMLStorageConf(xMLStorageFile));
        return availableStorageConfs;
    }

    /**
     * Creates an configuration with default values.
     */
    public DocumentScannerConf() throws IOException {
        this.storageConf = new DerbyEmbeddedPersistenceStorageConf(Constants.ENTITY_CLASSES,
            DATABASE_NAME_DEFAULT,
            SCHEME_CHECKSUM_FILE_DEFAULT);
        this.availableStorageConfs.add(this.storageConf);
        this.availableStorageConfs.add(new DerbyNetworkPersistenceStorageConf(Constants.ENTITY_CLASSES,
                HOSTNAME_DEFAULT,
                SCHEME_CHECKSUM_FILE_DEFAULT));
        Pair<String, String> bestInitialPostgresqlBasePathPair = PostgresqlAutoPersistenceStorageConf.findBestInitialPostgresqlBasePath();
        this.availableStorageConfs.add(new PostgresqlAutoPersistenceStorageConf(Constants.ENTITY_CLASSES,
                "document-scanner",
                SCHEME_CHECKSUM_FILE_DEFAULT,
                POSTGRESQL_DATABASE_DIR_DEFAULT,
                bestInitialPostgresqlBasePathPair.getKey(),
                bestInitialPostgresqlBasePathPair.getValue()));
        this.availableStorageConfs.add(new MySQLAutoPersistenceStorageConf(Constants.ENTITY_CLASSES,
                "document-scanner",
                MYSQL_DATABASE_DIR_DEFAULT,
                SCHEME_CHECKSUM_FILE_DEFAULT
        ));
        this.oCREngineConf = OCR_ENGINE_CONF_DEFAULT;
        this.availableOCREngineConfs.add(oCREngineConf);
        this.selectedValueDetectionServiceConfs.add(new ContactValueDetectionServiceConf());
        this.selectedValueDetectionServiceConfs.add(new TrieCurrencyFormatValueDetectionServiceConf());
        this.selectedValueDetectionServiceConfs.add(new CurrencyFormatValueDetectionServiceConf());
        this.selectedValueDetectionServiceConfs.add(new DateFormatValueDetectionServiceConf());
    }

    public void validate() {
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
        if(preferredScanResultPanelWidth < 10) {
            throw new IllegalArgumentException("A preferred width of less than "
                    + "10 will cause severe displaying issues and will thus "
                    + "not be supported");
        }
    }

    /**
     * Copy constructor.
     */
    public DocumentScannerConf(File configFile,
            String scannerName,
            String scannerSaneAddress,
            StorageConf storageConf,
            Set<StorageConf> availableStorageConfs,
            OCREngineConf oCREngineConf,
            Set<OCREngineConf> availableOCREngineConfs,
            Currency currency,
            boolean autoGenerateIDs,
            Locale locale,
            boolean autoSaveImageData,
            boolean autoSaveOCRData,
            boolean automaticFormatInitiallySelected,
            boolean autoOCRValueDetection,
            Map<String, ScannerConf> scannerConfMap,
            float zoomLevelMultiplier,
            int preferredScanResultPanelWidth,
            boolean rememberPreferredScanResultPanelWidth,
            int preferredOCRSelectPanelWidth,
            boolean rememberPreferredOCRSelectPanelWidth,
            File xMLStorageFile,
            File derbyPersistenceStorageSchemeChecksumFile,
            File amountMoneyUsageStatisticsStorageFile,
            File amountMoneyCurrencyStorageFile,
            File tagStorageFile,
            File imageWrapperStorageDir,
            boolean debug,
            boolean skipMD5SumCheck,
            String logFilePath,
            Set<String> valueDetectionServiceJARPaths,
            List<ValueDetectionServiceConf> availableValueDetectionServiceConfs,
            List<ValueDetectionServiceConf> selectedValueDetectionServiceConfs,
            int resolutionWish,
            File queryHistoryEntryStorageFile,
            File amountMoneyExchangeRateRetrieverFileCacheDir,
            int amountMoneyExchangeRateRetrieverExpirationMillis,
            boolean userAllowedAutoBugTracking,
            boolean skipUserAllowedAutoBugTrackingQuestion,
            File credentialsStoreFile,
            Map<Class<?>, OCRResultFormatter<?>> autoOCRValueDetectionFormatterMap,
            long scannerOpenWaitTime,
            TimeUnit scannerOpenWaitTimeUnit
    ) {
        this.configFile = configFile;
        this.scannerName = scannerName;
        this.scannerSaneAddress = scannerSaneAddress;
        this.storageConf = storageConf;
        this.availableStorageConfs = availableStorageConfs;
        this.oCREngineConf = oCREngineConf;
        this.availableOCREngineConfs = availableOCREngineConfs;
        this.currency = currency;
        this.autoGenerateIDs = autoGenerateIDs;
        this.locale = locale;
        this.autoSaveImageData = autoSaveImageData;
        this.autoSaveOCRData = autoSaveOCRData;
        this.automaticFormatInitiallySelected = automaticFormatInitiallySelected;
        this.autoOCRValueDetection = autoOCRValueDetection;
        this.scannerConfMap = scannerConfMap;
        this.zoomLevelMultiplier = zoomLevelMultiplier;
        this.preferredScanResultPanelWidth = preferredScanResultPanelWidth;
        this.rememberPreferredScanResultPanelWidth = rememberPreferredScanResultPanelWidth;
        this.preferredOCRSelectPanelWidth = preferredOCRSelectPanelWidth;
        this.rememberPreferredOCRSelectPanelWidth = rememberPreferredOCRSelectPanelWidth;
        this.xMLStorageFile = xMLStorageFile;
        this.derbyPersistenceStorageSchemeChecksumFile = derbyPersistenceStorageSchemeChecksumFile;
        this.amountMoneyUsageStatisticsStorageFile = amountMoneyUsageStatisticsStorageFile;
        this.amountMoneyCurrencyStorageFile = amountMoneyCurrencyStorageFile;
        this.tagStorageFile = tagStorageFile;
        this.imageWrapperStorageDir = imageWrapperStorageDir;
        this.debug = debug;
        this.skipMD5SumCheck = skipMD5SumCheck;
        this.logFilePath = logFilePath;
        this.valueDetectionServiceJARPaths = valueDetectionServiceJARPaths;
        this.availableValueDetectionServiceConfs = availableValueDetectionServiceConfs;
        this.selectedValueDetectionServiceConfs = selectedValueDetectionServiceConfs;
        this.resolutionWish = resolutionWish;
        this.queryHistoryEntryStorageFile = queryHistoryEntryStorageFile;
        this.amountMoneyExchangeRateRetrieverFileCacheDir = amountMoneyExchangeRateRetrieverFileCacheDir;
        this.amountMoneyExchangeRateRetrieverExpirationMillis = amountMoneyExchangeRateRetrieverExpirationMillis;
        this.userAllowedAutoBugTracking = userAllowedAutoBugTracking;
        this.skipUserAllowedAutoBugTrackingQuestion = this.skipUserAllowedAutoBugTrackingQuestion;
        this.autoOCRValueDetectionFormatterMap = autoOCRValueDetectionFormatterMap;
        this.scannerOpenWaitTime = scannerOpenWaitTime;
        this.scannerOpenWaitTimeUnit = scannerOpenWaitTimeUnit;
    }

    /**
     * Copy constructor.
     * @param documentScannerConf the instance to copy
     */
    public DocumentScannerConf(DocumentScannerConf documentScannerConf) {
        this(documentScannerConf.getConfigFile(),
                documentScannerConf.getScannerName(),
                documentScannerConf.getScannerSaneAddress(),
                documentScannerConf.getStorageConf(),
                documentScannerConf.getAvailableStorageConfs(),
                documentScannerConf.getoCREngineConf(),
                documentScannerConf.getAvailableOCREngineConfs(),
                documentScannerConf.getCurrency(),
                documentScannerConf.isAutoGenerateIDs(),
                documentScannerConf.getLocale(),
                documentScannerConf.isAutoSaveImageData(),
                documentScannerConf.isAutoSaveOCRData(),
                documentScannerConf.isAutomaticFormatInitiallySelected(),
                documentScannerConf.isAutoOCRValueDetection(),
                documentScannerConf.getScannerConfMap(),
                documentScannerConf.getZoomLevelMultiplier(),
                documentScannerConf.getPreferredScanResultPanelWidth(),
                documentScannerConf.isRememberPreferredScanResultPanelWidth(),
                documentScannerConf.getPreferredOCRSelectPanelWidth(),
                documentScannerConf.isRememberPreferredOCRSelectPanelWidth(),
                documentScannerConf.getxMLStorageFile(),
                documentScannerConf.getDerbyPersistenceStorageSchemeChecksumFile(),
                documentScannerConf.getAmountMoneyUsageStatisticsStorageFile(),
                documentScannerConf.getAmountMoneyCurrencyStorageFile(),
                documentScannerConf.getTagStorageFile(),
                documentScannerConf.getImageWrapperStorageDir(),
                documentScannerConf.isDebug(),
                documentScannerConf.isSkipMD5SumCheck(),
                documentScannerConf.getLogFilePath(),
                documentScannerConf.getValueDetectionServiceJARPaths(),
                documentScannerConf.getAvailableValueDetectionServiceConfs(),
                documentScannerConf.getSelectedValueDetectionServiceConfs(),
                documentScannerConf.getResolutionWish(),
                documentScannerConf.getQueryHistoryEntryStorageFile(),
                documentScannerConf.getAmountMoneyExchangeRateRetrieverFileCacheDir(),
                documentScannerConf.getAmountMoneyExchangeRateRetrieverExpirationMillis(),
                documentScannerConf.isUserAllowedAutoBugTracking(),
                documentScannerConf.isSkipUserAllowedAutoBugTrackingQuestion(),
                documentScannerConf.getCredentialsStoreFile(),
                documentScannerConf.getAutoOCRValueDetectionFormatterMap(),
                documentScannerConf.getScannerOpenWaitTime(),
                documentScannerConf.getScannerOpenWaitTimeUnit()
        );
    }

    public long getScannerOpenWaitTime() {
        return scannerOpenWaitTime;
    }

    public void setScannerOpenWaitTime(long scannerOpenWaitTime) {
        this.scannerOpenWaitTime = scannerOpenWaitTime;
    }

    public TimeUnit getScannerOpenWaitTimeUnit() {
        return scannerOpenWaitTimeUnit;
    }

    public void setScannerOpenWaitTimeUnit(TimeUnit scannerOpenWaitTimeUnit) {
        this.scannerOpenWaitTimeUnit = scannerOpenWaitTimeUnit;
    }

    public Map<Class<?>, OCRResultFormatter<?>> getAutoOCRValueDetectionFormatterMap() {
        return autoOCRValueDetectionFormatterMap;
    }

    public void setAutoOCRValueDetectionFormatterMap(Map<Class<?>, OCRResultFormatter<?>> autoOCRValueDetectionFormatterMap) {
        this.autoOCRValueDetectionFormatterMap = autoOCRValueDetectionFormatterMap;
    }

    public File getCredentialsStoreFile() {
        return credentialsStoreFile;
    }

    public void setCredentialsStoreFile(File credentialsStoreFile) {
        this.credentialsStoreFile = credentialsStoreFile;
    }

    public boolean isUserAllowedAutoBugTracking() {
        return userAllowedAutoBugTracking;
    }

    public void setUserAllowedAutoBugTracking(boolean userAllowedAutoBugTracking) {
        this.userAllowedAutoBugTracking = userAllowedAutoBugTracking;
    }

    public boolean isSkipUserAllowedAutoBugTrackingQuestion() {
        return skipUserAllowedAutoBugTrackingQuestion;
    }

    public void setSkipUserAllowedAutoBugTrackingQuestion(boolean skipUserAllowedAutoBugTrackingQuestion) {
        this.skipUserAllowedAutoBugTrackingQuestion = skipUserAllowedAutoBugTrackingQuestion;
    }

    public int getAmountMoneyExchangeRateRetrieverExpirationMillis() {
        return amountMoneyExchangeRateRetrieverExpirationMillis;
    }

    public void setAmountMoneyExchangeRateRetrieverExpirationMillis(int amountMoneyExchangeRateRetrieverExpirationMillis) {
        this.amountMoneyExchangeRateRetrieverExpirationMillis = amountMoneyExchangeRateRetrieverExpirationMillis;
    }

    public File getAmountMoneyExchangeRateRetrieverFileCacheDir() {
        return amountMoneyExchangeRateRetrieverFileCacheDir;
    }

    public void setAmountMoneyExchangeRateRetrieverFileCacheDir(File amountMoneyExchangeRateRetrieverFileCacheDir) {
        this.amountMoneyExchangeRateRetrieverFileCacheDir = amountMoneyExchangeRateRetrieverFileCacheDir;
    }

    public File getQueryHistoryEntryStorageFile() {
        return queryHistoryEntryStorageFile;
    }

    public void setQueryHistoryEntryStorageFile(File queryHistoryEntryStorageFile) {
        this.queryHistoryEntryStorageFile = queryHistoryEntryStorageFile;
    }

    public int getResolutionWish() {
        return resolutionWish;
    }

    public void setResolutionWish(int resolutionWish) {
        this.resolutionWish = resolutionWish;
    }

    public Set<String> getValueDetectionServiceJARPaths() {
        return valueDetectionServiceJARPaths;
    }

    public void setValueDetectionServiceJARPaths(Set<String> valueDetectionServiceJARPaths) {
        this.valueDetectionServiceJARPaths = valueDetectionServiceJARPaths;
    }

    /**
     * @return the availableValueDetectionServiceConfs
     */
    public List<ValueDetectionServiceConf> getAvailableValueDetectionServiceConfs() {
        return availableValueDetectionServiceConfs;
    }

    /**
     * @param availableValueDetectionServiceConfs the availableValueDetectionServiceConfs to set
     */
    public void setAvailableValueDetectionServiceConfs(List<ValueDetectionServiceConf> availableValueDetectionServiceConfs) {
        this.availableValueDetectionServiceConfs = availableValueDetectionServiceConfs;
    }

    /**
     * @return the selectedValueDetectionServiceConfs
     */
    public List<ValueDetectionServiceConf> getSelectedValueDetectionServiceConfs() {
        return selectedValueDetectionServiceConfs;
    }

    /**
     * @param selectedValueDetectionServiceConfs the selectedValueDetectionServiceConfs to set
     */
    public void setSelectedValueDetectionServiceConfs(List<ValueDetectionServiceConf> selectedValueDetectionServiceConfs) {
        this.selectedValueDetectionServiceConfs = selectedValueDetectionServiceConfs;
    }

    public boolean isSkipMD5SumCheck() {
        return skipMD5SumCheck;
    }

    public void setSkipMD5SumCheck(boolean skipMD5SumCheck) {
        this.skipMD5SumCheck = skipMD5SumCheck;
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

    public File getTagStorageFile() {
        return tagStorageFile;
    }

    public File getImageWrapperStorageDir() {
        return imageWrapperStorageDir;
    }

    public int getPreferredScanResultPanelWidth() {
        return preferredScanResultPanelWidth;
    }

    public void setPreferredScanResultPanelWidth(int preferredScanResultPanelWidth) {
        this.preferredScanResultPanelWidth = preferredScanResultPanelWidth;
    }

    public boolean isRememberPreferredScanResultPanelWidth() {
        return rememberPreferredScanResultPanelWidth;
    }

    public void setRememberPreferredScanResultPanelWidth(boolean rememberPreferredScanResultPanelWidth) {
        this.rememberPreferredScanResultPanelWidth = rememberPreferredScanResultPanelWidth;
    }

    public int getPreferredOCRSelectPanelWidth() {
        return preferredOCRSelectPanelWidth;
    }

    public void setPreferredOCRSelectPanelWidth(int preferredOCRSelectPanelWidth) {
        this.preferredOCRSelectPanelWidth = preferredOCRSelectPanelWidth;
    }

    public boolean isRememberPreferredOCRSelectPanelWidth() {
        return rememberPreferredOCRSelectPanelWidth;
    }

    public void setRememberPreferredOCRSelectPanelWidth(boolean rememberPreferredOCRSelectPanelWidth) {
        this.rememberPreferredOCRSelectPanelWidth = rememberPreferredOCRSelectPanelWidth;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
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
    public OCREngineConf getoCREngineConf() {
        return this.oCREngineConf;
    }

    /**
     * @param oCREngineConf the oCREngineConf to set
     */
    public void setoCREngineConf(OCREngineConf oCREngineConf) {
        this.oCREngineConf = oCREngineConf;
    }

    /**
     * @return the storageConf
     */
    public StorageConf getStorageConf() {
        return this.storageConf;
    }

    /**
     * @param storageConf the storageConf to set
     */
    public void setStorageConf(StorageConf storageConf) {
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

    public Set<StorageConf> getAvailableStorageConfs() {
        return availableStorageConfs;
    }

    public Set<OCREngineConf> getAvailableOCREngineConfs() {
        return availableOCREngineConfs;
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

    @Override
    public String toString() {
        ReflectionToStringBuilder toStringBuilder = new ReflectionToStringBuilder(this);
        toStringBuilder.setAppendStatics(false);
        String retValue = toStringBuilder.toString();
        return retValue;
    }
}
