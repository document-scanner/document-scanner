/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package richtercloud.document.scanner.gui;

import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneException;
import au.com.southsky.jfreesane.SaneSession;
import au.com.southsky.jfreesane.SaneStatus;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import com.beust.jcommander.JCommander;
import com.google.common.reflect.TypeToken;
import com.thoughtworks.xstream.XStream;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javax.cache.Caching;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jscience.economics.money.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.credential.store.EncryptedCredentialStore;
import richtercloud.credential.store.EncryptedFileCredentialStore;
import richtercloud.document.scanner.components.AutoOCRValueDetectionPanel;
import richtercloud.document.scanner.components.tag.FileTagStorage;
import richtercloud.document.scanner.components.tag.TagStorage;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.gui.scanner.DocumentSource;
import richtercloud.document.scanner.gui.scanner.ScannerConf;
import richtercloud.document.scanner.gui.scanner.ScannerEditDialog;
import static richtercloud.document.scanner.gui.scanner.ScannerEditDialog.DOCUMENT_SOURCE_OPTION_NAME;
import richtercloud.document.scanner.gui.scanner.ScannerPageSelectDialog;
import richtercloud.document.scanner.gui.scanner.ScannerSelectionDialog;
import richtercloud.document.scanner.gui.scanresult.ScannerResultDialog;
import richtercloud.document.scanner.gui.storageconf.StorageConfPanelCreationException;
import richtercloud.document.scanner.gui.storageconf.StorageSelectionDialog;
import richtercloud.document.scanner.ifaces.DocumentAddException;
import richtercloud.document.scanner.ifaces.ImageWrapper;
import richtercloud.document.scanner.ifaces.MainPanel;
import richtercloud.document.scanner.ifaces.OCREngine;
import richtercloud.document.scanner.ifaces.OCREngineConf;
import richtercloud.document.scanner.model.Company;
import richtercloud.document.scanner.model.Document;
import richtercloud.document.scanner.model.imagewrapper.CachingImageWrapper;
import richtercloud.document.scanner.model.imagewrapper.ImageWrapperStorageDirExistsException;
import richtercloud.document.scanner.model.warninghandler.CompanyWarningHandler;
import richtercloud.document.scanner.ocr.BinaryNotFoundException;
import richtercloud.document.scanner.ocr.DelegatingOCREngineFactory;
import richtercloud.document.scanner.ocr.OCREngineFactory;
import richtercloud.document.scanner.ocr.OCREngineSelectDialog;
import richtercloud.document.scanner.setter.AmountMoneyPanelSetter;
import richtercloud.document.scanner.setter.EmbeddableListPanelSetter;
import richtercloud.document.scanner.setter.LongIdPanelSetter;
import richtercloud.document.scanner.setter.SpinnerSetter;
import richtercloud.document.scanner.setter.StringAutoCompletePanelSetter;
import richtercloud.document.scanner.setter.TextFieldSetter;
import richtercloud.document.scanner.setter.UtilDatePickerSetter;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.document.scanner.valuedetectionservice.ValueDetectionServiceConf;
import richtercloud.document.scanner.valuedetectionservice.ValueDetectionServiceConfDialog;
import richtercloud.message.handler.BugHandler;
import richtercloud.message.handler.ConfirmMessageHandler;
import richtercloud.message.handler.DefaultIssueHandler;
import richtercloud.message.handler.DialogBugHandler;
import richtercloud.message.handler.DialogConfirmMessageHandler;
import richtercloud.message.handler.DialogMessageHandler;
import richtercloud.message.handler.ExceptionMessage;
import richtercloud.message.handler.IssueHandler;
import richtercloud.message.handler.JavaFXDialogMessageHandler;
import richtercloud.message.handler.Message;
import richtercloud.message.handler.MessageHandler;
import richtercloud.message.handler.raven.bug.handler.RavenBugHandler;
import richtercloud.reflection.form.builder.AnyType;
import richtercloud.reflection.form.builder.FieldRetriever;
import richtercloud.reflection.form.builder.components.date.UtilDatePicker;
import richtercloud.reflection.form.builder.components.money.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetriever;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetrieverException;
import richtercloud.reflection.form.builder.components.money.AmountMoneyPanel;
import richtercloud.reflection.form.builder.components.money.AmountMoneyUsageStatisticsStorage;
import richtercloud.reflection.form.builder.components.money.FailsafeAmountMoneyExchangeRateRetriever;
import richtercloud.reflection.form.builder.components.money.FileAmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.money.FileAmountMoneyUsageStatisticsStorage;
import richtercloud.reflection.form.builder.jpa.IdGenerationException;
import richtercloud.reflection.form.builder.jpa.IdGenerator;
import richtercloud.reflection.form.builder.jpa.JPACachedFieldRetriever;
import richtercloud.reflection.form.builder.jpa.JPAFieldRetriever;
import richtercloud.reflection.form.builder.jpa.SequentialIdGenerator;
import richtercloud.reflection.form.builder.jpa.WarningHandler;
import richtercloud.reflection.form.builder.jpa.idapplier.IdApplier;
import richtercloud.reflection.form.builder.jpa.panels.EmbeddableListPanel;
import richtercloud.reflection.form.builder.jpa.panels.LongIdPanel;
import richtercloud.reflection.form.builder.jpa.panels.QueryHistoryEntryStorage;
import richtercloud.reflection.form.builder.jpa.panels.QueryHistoryEntryStorageCreationException;
import richtercloud.reflection.form.builder.jpa.panels.QueryHistoryEntryStorageFactory;
import richtercloud.reflection.form.builder.jpa.panels.StringAutoCompletePanel;
import richtercloud.reflection.form.builder.jpa.storage.AbstractPersistenceStorageConf;
import richtercloud.reflection.form.builder.jpa.storage.DelegatingPersistenceStorageFactory;
import richtercloud.reflection.form.builder.jpa.storage.FieldInitializer;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;
import richtercloud.reflection.form.builder.jpa.storage.ReflectionFieldInitializer;
import richtercloud.reflection.form.builder.jpa.storage.copy.DelegatingStorageConfCopyFactory;
import richtercloud.reflection.form.builder.jpa.typehandler.JPAEntityListTypeHandler;
import richtercloud.reflection.form.builder.jpa.typehandler.factory.JPAAmountMoneyMappingTypeHandlerFactory;
import richtercloud.reflection.form.builder.storage.StorageConf;
import richtercloud.reflection.form.builder.storage.StorageConfValidationException;
import richtercloud.reflection.form.builder.storage.StorageCreationException;
import richtercloud.reflection.form.builder.storage.copy.StorageConfCopyException;
import richtercloud.reflection.form.builder.storage.copy.StorageConfCopyFactory;
import richtercloud.reflection.form.builder.typehandler.TypeHandler;
import richtercloud.swing.worker.get.wait.dialog.SwingWorkerCompletionWaiter;
import richtercloud.swing.worker.get.wait.dialog.SwingWorkerGetWaitDialog;

/**
 * <h2>Status bar</h2>
 * In order to provide static status for a selected (and eventually
 * configurable) set of components (selected scanner, image selection mode,
 * etc.) and dynamic info, warning and error message, a status bar (at the
 * bottom of the main application window is introduced which is a container and
 * contains a subcontainer for each static information and one for the messages.
 * The latter is a popup displaying a scrollable list of message entries which
 * can be removed from it (e.g. with a close button). Static status subcontainer
 * can be sophisticated, e.g. the display for the currently selected scanner can
 * contain a button to select a scanner while none is selected an be a label
 * only while one is selected. That's why they're containers. The difference
 * between static information and messages is trivial.
 *
 * <h2>Window arrangement</h2>
 * Docking is superior in function and more flexible in programming changes than
 * split panes, so use a docking framework (see
 * <a href="https://richtercloud.de:446/doku.php?id=tasks:java:docking_
 * framework">https://richtercloud.de:446/doku.php?id=tasks:java:docking_
 * framework</a> for choices). In order to allow easy switching between
 * documents a tabbed pane should be used. In order to avoid non-constructive
 * docking/window configuration for the user which is possible if all components
 * of a document are managed in one tab and separate docking is set up for each.
 * Switch document tabs on one component of the document and make its other
 * components change - like NetBeans does with editor and navigator and others.
 *
 * @author richter
 */
/*
internal implementation notes:
- the combobox in the storage create dialog for selection of the type of the new
storage doesn't have to be a StorageConf instance and it's misleading if it is
because such a StorageConf is about to be created -> use Class
- see DocumentScannerConf for design decisions regarding configuration file and
command line parameters
 */
public class DocumentScanner extends javax.swing.JFrame implements Managed<Exception> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentScanner.class);
    /**
     * The Raven/sentry.io DSN. Doesn't make sense to hide from source according
     * to http://stackoverflow.com/questions/41843941/where-to-get-the-sentry-
     * raven-dsn-from-at-runtime.
     */
    private final static String RAVEN_DSN = "https://d4001abecf704dd790bb72bb5a7e0dc8:e131224c9d3e413fa194f3210ba99751@sentry.io/131229";
    private SaneDevice scannerDevice;
    private DocumentScannerConf documentScannerConf;
    public final static Map<Class<? extends JComponent>, ValueSetter<?,?>> VALUE_SETTER_MAPPING_DEFAULT;
    static {
        Map<Class<? extends JComponent>, ValueSetter<?,?>> valueSetterMappingDefault = new HashMap<>();
        valueSetterMappingDefault.put(JTextField.class,
                TextFieldSetter.getInstance());
        valueSetterMappingDefault.put(JSpinner.class,
                SpinnerSetter.getInstance());
        valueSetterMappingDefault.put(LongIdPanel.class,
                LongIdPanelSetter.getInstance());
        valueSetterMappingDefault.put(StringAutoCompletePanel.class,
                StringAutoCompletePanelSetter.getInstance());
        valueSetterMappingDefault.put(AmountMoneyPanel.class,
                AmountMoneyPanelSetter.getInstance());
        valueSetterMappingDefault.put(UtilDatePicker.class,
                UtilDatePickerSetter.getInstance());
        valueSetterMappingDefault.put(EmbeddableListPanel.class,
                EmbeddableListPanelSetter.getInstance());
        VALUE_SETTER_MAPPING_DEFAULT = Collections.unmodifiableMap(valueSetterMappingDefault);
    }
    private final AmountMoneyUsageStatisticsStorage amountMoneyUsageStatisticsStorage;
    private final AmountMoneyCurrencyStorage amountMoneyCurrencyStorage;
    private final AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever;
    private final TagStorage tagStorage;
    private final Map<java.lang.reflect.Type, TypeHandler<?, ?,?, ?>> typeHandlerMapping;
    private final IssueHandler issueHandler;
    private final MessageHandler messageHandler = new DialogMessageHandler(this,
            "", //titlePrefix
            String.format("- %s %s", Constants.APP_NAME, Constants.APP_VERSION));
    private final JavaFXDialogMessageHandler javaFXDialogMessageHandler = new JavaFXDialogMessageHandler();
    private final ConfirmMessageHandler confirmMessageHandler = new DialogConfirmMessageHandler(this);
    private final Map<Class<?>, WarningHandler<?>> warningHandlers = new HashMap<>();
    static {
        new JFXPanel();
    }

    public static String generateApplicationWindowTitle(String title, String applicationName, String applicationVersion) {
        return String.format("%s - %s %s", title, applicationName, applicationVersion);
    }
    private final FieldRetriever fieldRetriever = new JPACachedFieldRetriever();
    /*
    internal implementation notes:
    - can't be final because it's initialized in init because it depends on
    initialized entityManager
    */
    private MainPanel mainPanel;
    private final OCREngineFactory oCREngineFactory = new DelegatingOCREngineFactory();
    private OCREngine oCREngine;
    /**
     * Since {@link SaneSession#getDevice(java.lang.String) } overwrites
     * configuration settings, keep a reference to once retrieved
     * {@link SaneDevice}.
     */
    private final static Map<String, SaneDevice> NAME_DEVICE_MAP = new HashMap<>();
    /**
     * Uses the string representation stored in {@link ScannerConf} in order to
     * avoid confusion with equality of {@link InetAddress}es.
     */
    private final static Map<String, SaneSession> ADDRESS_SESSION_MAP = new HashMap<>();
    /**
     * The default non-zero exit code.
     */
    private final static int SYSTEM_EXIT_ERROR_GENERAL = 1;
    private final IdApplier<AutoOCRValueDetectionPanel> idApplier;
    private final IdGenerator<Long> idGenerator;
    /**
     * If multiple entities are selected in a {@link EntityEditingDialog} it
     * might take a long time to open documents for all of them, so a warning is
     * displayed if more documents than this value are about to be opened.
     */
    private PersistenceStorage storage;
    private final DelegatingPersistenceStorageFactory delegatingStorageFactory = new DelegatingPersistenceStorageFactory("richtercloud_document-scanner_jar_1.0-SNAPSHOTPU",
            24, //@TODo: low limit no longer necessary after ImageWrapper is
                //used for binary data storage in document
            messageHandler,
            fieldRetriever);
    private final FieldInitializer queryComponentFieldInitializer;
    private final StorageConfCopyFactory storageConfCopyFactory = new DelegatingStorageConfCopyFactory();
    private final QueryHistoryEntryStorageFactory entryStorageFactory;
    private final QueryHistoryEntryStorage entryStorage;
    private final JPAFieldRetriever reflectionFormBuilderFieldRetriever = new DocumentScannerFieldRetriever();
    private final FieldRetriever readOnlyFieldRetriever = new JPACachedFieldRetriever();
    private final EncryptedCredentialStore<String, String> credentialStore;
    /**
     * Start to fetch results and warm up the cache after start.
     */
    private final Thread amountMoneyExchangeRetrieverInitThread = new Thread(() -> {
        LOGGER.debug("Starting prefetching of currency exchange rates in "
                + "the background");
        try {
            Set<Currency> supportedCurrencies = DocumentScanner.this.amountMoneyExchangeRateRetriever.getSupportedCurrencies();
            for(Currency supportedCurrency : supportedCurrencies) {
                DocumentScanner.this.amountMoneyExchangeRateRetriever.retrieveExchangeRate(supportedCurrency);
            }
        } catch (AmountMoneyExchangeRateRetrieverException ex) {
            //all parts of FailsafeAmountMoneyExchangeRateRetriever failed
            throw new RuntimeException(ex);
        }
    },
            "amount-money-exchange-rate-retriever-init-thread"
    );
    /**
     * Allows early initialization of Apache Ignite in the background.
     */
    /*
    internal implementatio notes:
    - not necessary that this is a property, but kept as such for convenience.
    */
    private final Thread cachingImageWrapperInitThread = new Thread(() -> {
        try {
            Class.forName(CachingImageWrapper.class.getName());
                //loads the static block in CachingImageWrapper in order
                //to minimize delay when initializing the first
                //CachingImageWrapper
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    },
            "caching-image-wrapper-init-thread"
    );

    /**
     * Creates new DocumentScanner which does nothing unless
     *
     * @throws richtercloud.document.scanner.gui.TesseractNotFoundException
     */
    /*
    internal implementation notes:
    - resources are opened in init methods only (see https://richtercloud.de:446/doku.php?id=programming:java#resource_handling for details)
    */
    public DocumentScanner(DocumentScannerConf documentScannerConf) throws BinaryNotFoundException, IOException, StorageCreationException, ImageWrapperStorageDirExistsException, QueryHistoryEntryStorageCreationException, IdGenerationException {
        this.documentScannerConf = documentScannerConf;

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        //configure debug level of root logger
        if (this.documentScannerConf.isDebug()) {
            rootLogger.setLevel(Level.DEBUG);
        }
        //configure logging to file (from http://stackoverflow.com/questions/16910955/programmatically-configure-logback-appender)
        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        patternLayoutEncoder.setContext(loggerContext);
        patternLayoutEncoder.start();
        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<ILoggingEvent>();
        fileAppender.setFile(documentScannerConf.getLogFilePath());
        fileAppender.setEncoder(patternLayoutEncoder);
        fileAppender.setContext(loggerContext);
        fileAppender.setRollingPolicy(new FixedWindowRollingPolicy());
        fileAppender.start();
        rootLogger.addAppender(fileAppender);
        LOGGER.info(String.format("logging to file '%s'", documentScannerConf.getLogFilePath()));

        this.credentialStore = new EncryptedFileCredentialStore<>(documentScannerConf.getCredentialsStoreFile());

        //check whether user allowed automatic bug tracking
        if(!documentScannerConf.isSkipUserAllowedAutoBugTrackingQuestion()) {
            String yesOnce = "Yes, but in this session only";
            String yesAlways = "Yes, and never ask me again";
            String noOnce = "No, not this time";
            String noAlways = "No, and never ask me again";
            String answer = confirmMessageHandler.confirm(new Message(String.format("Do "
                    + "you allow %s to anonymously and automatically track "
                    + "issues and bugs?",
                    Constants.APP_NAME),
                    JOptionPane.QUESTION_MESSAGE,
                    "Anoymous contribution"),
                    yesOnce,
                    yesAlways,
                    noOnce,
                    noAlways);
            if(answer.equals(yesAlways) || answer.equals(noAlways)) {
                this.documentScannerConf.setSkipUserAllowedAutoBugTrackingQuestion(true);
            }
            this.documentScannerConf.setUserAllowedAutoBugTracking(answer.equals(yesOnce) || answer.equals(yesAlways));
        }
        BugHandler bugHandler;
        if(this.documentScannerConf.isUserAllowedAutoBugTracking()) {
            bugHandler = new RavenBugHandler(RAVEN_DSN);
        }else {
            bugHandler = new DialogBugHandler(this,
                    Constants.BUG_URL,
                    "", //titlePrefix
                    String.format("- %s %s", Constants.APP_NAME, Constants.APP_VERSION) //titleSuffix
            );
        }
        this.issueHandler = new DefaultIssueHandler(messageHandler,
                bugHandler);

        this.amountMoneyExchangeRateRetriever = new FailsafeAmountMoneyExchangeRateRetriever(documentScannerConf.getAmountMoneyExchangeRateRetrieverFileCacheDir(),
                documentScannerConf.getAmountMoneyExchangeRateRetrieverExpirationMillis());

        //Check that emptying image storage directory wasn't skipped at shutdown
        //due to application crash
        if(documentScannerConf.getImageWrapperStorageDir().list().length > 0) {
            int answer = confirmMessageHandler.confirm(new Message(String.format("The image "
                    + "wrapper storage directory '%s' isn't empty which can "
                    + "happen after an application crash. It needs to be "
                    + "emptied in order to make the application work "
                    + "correctly. In case there's any chance someone put file "
                    + "into that directory abort now and investigate the "
                    + "directory, otherwise confirm to delete all files in "
                    + "'%s'.",
                            documentScannerConf.getImageWrapperStorageDir().getAbsolutePath(),
                            documentScannerConf.getImageWrapperStorageDir().getAbsolutePath()),
                    JOptionPane.WARNING_MESSAGE, "Confirm emptying directory"));
            assert answer == JOptionPane.YES_OPTION || answer == JOptionPane.NO_OPTION;
            if(answer == JOptionPane.NO_OPTION) {
                throw new ImageWrapperStorageDirExistsException();
            }else {
                LOGGER.warn(String.format("cleaning image storage directory "
                        + "'%s'", documentScannerConf.getImageWrapperStorageDir().getAbsolutePath()));
                FileUtils.cleanDirectory(documentScannerConf.getImageWrapperStorageDir());
            }
        }

        this.amountMoneyExchangeRetrieverInitThread.start();
        this.cachingImageWrapperInitThread.start();

        this.entryStorageFactory = new DocumentScannerFileQueryHistoryEntryStorageFactory(documentScannerConf.getQueryHistoryEntryStorageFile(),
                Constants.ENTITY_CLASSES,
                false,
                messageHandler);
        this.entryStorage = entryStorageFactory.create();

        StorageConf storageConf = documentScannerConf.getStorageConf();
        assert storageConf instanceof AbstractPersistenceStorageConf;
        this.storage = (PersistenceStorage) delegatingStorageFactory.create(storageConf);

        this.idGenerator = new SequentialIdGenerator(storage);
        this.idApplier = new AutoOCRValueDetectionPanelIdApplier(idGenerator);

        OCREngineConf oCREngineConf = documentScannerConf.getoCREngineConf();
        this.oCREngine = oCREngineFactory.create(oCREngineConf);

        this.initComponents();

        validateProperties();
            //after initComponents because of afterScannerSelection involving
            //GUI components

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("running {} shutdown hooks", DocumentScanner.class);
            DocumentScanner.this.shutdownHook();
        },
                String.format("%s shutdown hook", DocumentScanner.class.getSimpleName())
        ));

        try {
            this.amountMoneyUsageStatisticsStorage = new FileAmountMoneyUsageStatisticsStorage(documentScannerConf.getAmountMoneyUsageStatisticsStorageFile());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        this.amountMoneyCurrencyStorage = new FileAmountMoneyCurrencyStorage(documentScannerConf.getAmountMoneyCurrencyStorageFile());
        this.tagStorage = new FileTagStorage(documentScannerConf.getTagStorageFile());
        JPAAmountMoneyMappingTypeHandlerFactory fieldHandlerFactory = new JPAAmountMoneyMappingTypeHandlerFactory(storage,
                Constants.INITIAL_QUERY_LIMIT_DEFAULT,
                messageHandler,
                Constants.BIDIRECTIONAL_HELP_DIALOG_TITLE,
                readOnlyFieldRetriever);
        this.typeHandlerMapping = fieldHandlerFactory.generateTypeHandlerMapping();

        this.queryComponentFieldInitializer = new ReflectionFieldInitializer(fieldRetriever) {
            @Override
            protected boolean initializeField(Field field) {
                boolean retValue;
                try {
                    retValue = !field.equals(Document.class.getDeclaredField("scanData"));
                        //skip Document.scanData in query components
                } catch (NoSuchFieldException | SecurityException ex) {
                    throw new RuntimeException(ex);
                }
                return retValue;
            }
        };
            //Don't use DocumentScannerFieldInitializer here because it will
            //fetch all scanData of Document on all n query results resulting in
            //n*size of documents byte[]s memory consumption

        //after entity manager creation
        this.typeHandlerMapping.put(new TypeToken<List<AnyType>>() {
            }.getType(), new JPAEntityListTypeHandler(storage,
                    messageHandler,
                    Constants.BIDIRECTIONAL_HELP_DIALOG_TITLE,
                    queryComponentFieldInitializer,
                    entryStorage,
                    readOnlyFieldRetriever));
        //listen to window close button (x)
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
                shutdownHook();
            }
        });
    }

    /**
     * run all initialization routines which open resources which need to be
     * referenced by {@code this} in order to be closable (if everything is
     * initialized in the constructor and the initialization fails there's no
     * way to close opened resources).
     */
    @Override
    public void init() throws StorageConfValidationException, IOException, StorageConfValidationException {
        warningHandlers.put(Company.class,
                new CompanyWarningHandler(storage,
                        messageHandler,
                        confirmMessageHandler));
            //after entityManager has been initialized

        this.mainPanel = new DefaultMainPanel(Constants.ENTITY_CLASSES,
                Constants.PRIMARY_CLASS_SELECTION,
                storage,
                amountMoneyUsageStatisticsStorage,
                amountMoneyCurrencyStorage,
                amountMoneyExchangeRateRetriever,
                issueHandler,
                confirmMessageHandler,
                this,
                oCREngine,
                typeHandlerMapping,
                documentScannerConf,
                this, //oCRProgressMonitorParent
                tagStorage,
                idApplier,
                idGenerator,
                warningHandlers,
                queryComponentFieldInitializer,
                entryStorage,
                reflectionFormBuilderFieldRetriever,
                readOnlyFieldRetriever
        );
        mainPanelPanel.add(this.mainPanel);
    }

    public static SaneDevice getScannerDevice(String scannerName,
            Map<String, ScannerConf> scannerConfMap,
            String scannerAddressFallback,
            int resolutionWish) throws IOException, SaneException {
        if(scannerAddressFallback == null) {
            throw new IllegalArgumentException("scannerAddressFallback mustn't be null");
        }
        SaneDevice retValue = NAME_DEVICE_MAP.get(scannerName);
        if(retValue == null) {
            ScannerConf scannerConf = scannerConfMap.get(scannerName);
            if(scannerConf == null) {
                scannerConf = new ScannerConf(scannerName);
                scannerConfMap.put(scannerName, scannerConf);
            }
            SaneSession saneSession = ADDRESS_SESSION_MAP.get(scannerConf.getScannerAddress());
            if(saneSession == null) {
                String scannerAddress = scannerConf.getScannerAddress();
                if(scannerAddress == null) {
                    scannerAddress = scannerAddressFallback;
                }
                InetAddress scannerInetAddress = InetAddress.getByName(scannerAddress);
                saneSession = SaneSession.withRemoteSane(scannerInetAddress);
                ADDRESS_SESSION_MAP.put(scannerConf.getScannerAddress(), saneSession);
                scannerConf.setScannerAddress(scannerAddress);
            }
            retValue = saneSession.getDevice(scannerName);
            NAME_DEVICE_MAP.put(scannerName, retValue);
            ScannerEditDialog.configureDefaultOptionValues(retValue,
                    scannerConf,
                    resolutionWish
            );
        }
        return retValue;
    }

    /**
     * Validates  {@code conf} from configuration file.
     */
    private void validateProperties() throws IOException {
        //if a scanner address and device name is in persisted conf check if
        //it's accessible and treat it as selected scanner silently
        String scannerName = this.documentScannerConf.getScannerName();
        if(scannerName != null) {
            try {
                this.scannerDevice = getScannerDevice(scannerName,
                        this.documentScannerConf.getScannerConfMap(),
                        DocumentScannerConf.SCANNER_SANE_ADDRESS_DEFAULT,
                        documentScannerConf.getResolutionWish());
                afterScannerSelection();
            } catch (IOException | SaneException ex) {
                String text = handleSearchScannerException("An exception during the setup of "
                        + "previously selected scanner occured: ",
                        ex,
                        Constants.SANED_BUG_INFO);
                messageHandler.handle(new Message(String.format("Exception during setup of previously selected scanner: %s\n%s", ExceptionUtils.getRootCauseMessage(ex), text),
                        JOptionPane.WARNING_MESSAGE,
                        "Exception occured"));
            }
        }
        if(!this.documentScannerConf.getImageWrapperStorageDir().exists()) {
            if(!this.documentScannerConf.getImageWrapperStorageDir().mkdirs()) {
                throw new IOException(String.format("Creation of image wrapper storage directory '%s' failed",
                        this.documentScannerConf.getImageWrapperStorageDir().getAbsolutePath()));
            }
        }else {
            if(!this.documentScannerConf.getImageWrapperStorageDir().isDirectory()) {
                throw new IllegalStateException(String.format("Configured image wrapper storage directory '%s' is a file",
                        this.documentScannerConf.getImageWrapperStorageDir().getAbsolutePath()));
            }
        }
    }

    public static String handleSearchScannerException(String text,
            Exception ex,
            String additional) {
        String message = ex.getMessage();
        if (ex.getCause() != null) {
            message = String.format("%s (caused by '%s')", message, ex.getCause().getMessage());
        }
        String retValue = String.format("<html>%s%s%s</html>", text, message, additional);
        return retValue;
    }

    private void afterScannerSelection() {
        this.scanMenuItem.setEnabled(true);
        this.scanMenuItem.getParent().revalidate();
    }

    /**
     * Handles all non-resource related cleanup tasks (like persistence of
     * configuration). Callers have to make sure that this is invoked only once.
     * @see #close()
     */
    private void shutdownHook() {
        try {
            LOGGER.info(String.format("running shutdown hooks in %s", DocumentScanner.class));
            if (this.documentScannerConf != null) {
                try {
                    XStream xStream = new XStream();
                    xStream.toXML(this.documentScannerConf, new FileOutputStream(this.documentScannerConf.getConfigFile()));
                } catch (FileNotFoundException ex) {
                    LOGGER.warn("an unexpected exception occured during save of configurations into file '{}', changes most likely lost", this.documentScannerConf.getConfigFile().getAbsolutePath());
                }
            }
            if(this.scannerDevice != null) {
                if(this.scannerDevice.isOpen()) {
                    try {
                        this.scannerDevice.close();
                    } catch (IOException ex) {
                        LOGGER.warn(String.format("an unexpected exception occured during closing the scanner device '%s'",
                                this.scannerDevice.getName()));
                    }
                }
            }
            if(this.storage != null) {
                this.storage.shutdown();
            }
            LOGGER.info(String.format("emptying image wrapper storage directory '%s'", this.documentScannerConf.getImageWrapperStorageDir().getAbsolutePath()));
            try {
                FileUtils.deleteDirectory(this.documentScannerConf.getImageWrapperStorageDir());
            } catch (IOException ex) {
                LOGGER.error("removal of image wrapper storage directory failed, see nested exception for details", ex);
            }
            if(!this.documentScannerConf.getImageWrapperStorageDir().mkdirs()) {
                LOGGER.error(String.format("re-creation of image wrapper storage dir '%s' failed", this.documentScannerConf.getImageWrapperStorageDir().getAbsolutePath()));
            }
            close();
            shutdownHookThreads();
            LOGGER.info(String.format("shutdown hooks in %s finished", DocumentScanner.class));
        }catch(Exception ex) {
            issueHandler.handleUnexpectedException(new ExceptionMessage(ex));
        }
        this.issueHandler.shutdown();
    }

    private static void shutdownHookThreads() {
        Caching.getCachingProvider().close();
        Platform.exit();
            //necessary in order to prevent hanging after all shutdown hooks
            //have been processed
    }

    /**
     * Handles all resource-related shutdown tasks.
     * @see #shutdownHook()
     */
    @Override
    public void close() {
        //DocumentScanner doesn't necessarily need to manage device. It'd be
        //more elegant if that was done by a manager or conf class.
        if (this.scannerDevice != null && this.scannerDevice.isOpen()) {
            try {
                this.scannerDevice.close();
            } catch (IOException ex) {
                LOGGER.error("an exception during shutdown of scanner device occured", ex);
            }
            this.scannerDevice = null;
        }
        try {
            //@TODO: this can hang a shutdown (difficult to abort initialization
            //of resources)
            if(this.amountMoneyExchangeRetrieverInitThread.isAlive()) {
                LOGGER.debug("waiting for exchange rate retriever initialization "
                        + "to finish in order to allow clean shutdown");
                this.amountMoneyExchangeRetrieverInitThread.join();
            }
            if(this.cachingImageWrapperInitThread.isAlive()) {
                LOGGER.debug("waiting for caching framework initialization "
                        + "to finish in order to allow clean shutdown");
                this.cachingImageWrapperInitThread.join();
            }
        } catch (InterruptedException ex) {
            LOGGER.error("an exception during shutdown of threads occured, see nested exception for details", ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanelPanel = new javax.swing.JPanel();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        scannerSelectionMenu = new javax.swing.JMenu();
        selectScannerMenuItem = new javax.swing.JMenuItem();
        knownScannersMenuItemSeparator = new javax.swing.JPopupMenu.Separator();
        scanMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        openSelectionMenuItem = new javax.swing.JMenuItem();
        closeMenuItem = new javax.swing.JMenuItem();
        exportMenuItem = new javax.swing.JMenuItem();
        editEntryMenuItem = new javax.swing.JMenuItem();
        valueDetectionMenuItem = new javax.swing.JMenuItem();
        oCRMenuSeparator = new javax.swing.JPopupMenu.Separator();
        oCRMenuItem = new javax.swing.JMenuItem();
        databaseMenuSeparator = new javax.swing.JPopupMenu.Separator();
        storageSelectionMenu = new javax.swing.JMenu();
        storageSelectionMenuItem = new javax.swing.JMenuItem();
        knownStoragesMenuItemSeparartor = new javax.swing.JPopupMenu.Separator();
        saveMenuItem = new javax.swing.JMenuItem();
        exitMenuItemSeparator = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        optionsMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(String.format("%s %s", Constants.APP_NAME, Constants.APP_VERSION) //generateApplicationWindowTitle not applicable
        );
        setBounds(new java.awt.Rectangle(0, 0, 800, 600));
        setSize(new java.awt.Dimension(800, 600));

        mainPanelPanel.setLayout(new java.awt.BorderLayout());

        fileMenu.setText("File");

        scannerSelectionMenu.setText("Scanner selection");

        selectScannerMenuItem.setText("Select scanner...");
        selectScannerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectScannerMenuItemActionPerformed(evt);
            }
        });
        scannerSelectionMenu.add(selectScannerMenuItem);
        scannerSelectionMenu.add(knownScannersMenuItemSeparator);

        fileMenu.add(scannerSelectionMenu);

        scanMenuItem.setText("Scan");
        scanMenuItem.setEnabled(false);
        scanMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(scanMenuItem);

        openMenuItem.setText("Open scan...");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        openSelectionMenuItem.setText("Open scan for selection...");
        openSelectionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openSelectionMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openSelectionMenuItem);

        closeMenuItem.setText("Close");
        closeMenuItem.setEnabled(false);
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(closeMenuItem);

        exportMenuItem.setText("Export...");
        exportMenuItem.setEnabled(false);
        exportMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exportMenuItem);

        editEntryMenuItem.setText("Edit entry...");
        editEntryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editEntryMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(editEntryMenuItem);

        valueDetectionMenuItem.setText("Auto OCR value detection...");
        valueDetectionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueDetectionMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(valueDetectionMenuItem);
        fileMenu.add(oCRMenuSeparator);

        oCRMenuItem.setText("Configure OCR engines");
        oCRMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                oCRMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(oCRMenuItem);
        fileMenu.add(databaseMenuSeparator);

        storageSelectionMenu.setText("Storage selection");

        storageSelectionMenuItem.setText("Select storage...");
        storageSelectionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storageSelectionMenuItemActionPerformed(evt);
            }
        });
        storageSelectionMenu.add(storageSelectionMenuItem);
        storageSelectionMenu.add(knownStoragesMenuItemSeparartor);

        fileMenu.add(storageSelectionMenu);

        saveMenuItem.setText("Save in database");
        saveMenuItem.setEnabled(false);
        fileMenu.add(saveMenuItem);
        fileMenu.add(exitMenuItemSeparator);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        mainMenuBar.add(fileMenu);

        helpMenu.setText("Help");

        aboutMenuItem.setText("About...");
        helpMenu.add(aboutMenuItem);

        mainMenuBar.add(helpMenu);

        toolsMenu.setText("Tools");

        optionsMenuItem.setText("Options...");
        optionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionsMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(optionsMenuItem);

        mainMenuBar.add(toolsMenu);

        setJMenuBar(mainMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanelPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanelPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        this.setVisible(false);
        this.close();
        this.shutdownHook();
        this.dispose();
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void scanMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanMenuItemActionPerformed
        try {
            this.scan();
        }catch(Exception ex) {
            handleUnexpectedException(ex, "Unexpected exception occured during scan");
        }
    }//GEN-LAST:event_scanMenuItemActionPerformed

    private void selectScannerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectScannerMenuItemActionPerformed
        ScannerSelectionDialog scannerSelectionDialog;
        try {
            scannerSelectionDialog = new ScannerSelectionDialog(this,
                    messageHandler,
                    this.documentScannerConf);
        } catch (IOException | SaneException ex) {
            handleException(ex, "Unexpected exception");
            return;
        }
        scannerSelectionDialog.pack();
        scannerSelectionDialog.setLocationRelativeTo(this);
        scannerSelectionDialog.setVisible(true);//blocks and updates
            //documentScannerConf

        if(this.documentScannerConf.getScannerName() == null) {
            //dialog has been canceled (previously selected dialog remains or
            //null
            return;
        }
        try {
            this.scannerDevice = getScannerDevice(documentScannerConf.getScannerName(),
                    documentScannerConf.getScannerConfMap(),
                    DocumentScannerConf.SCANNER_SANE_ADDRESS_DEFAULT,
                    documentScannerConf.getResolutionWish());
        } catch (IOException | SaneException ex) {
            throw new RuntimeException(ex);
        }
        afterScannerSelection();
    }//GEN-LAST:event_selectScannerMenuItemActionPerformed

    private void storageSelectionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageSelectionMenuItemActionPerformed
        StorageSelectionDialog storageSelectionDialog;
        try {
            storageSelectionDialog = new StorageSelectionDialog(this,
                    documentScannerConf,
                    messageHandler,
                    confirmMessageHandler);
        } catch (IOException | StorageConfValidationException | StorageConfPanelCreationException ex) {
            throw new RuntimeException(ex);
        }
        StorageConf storageConfCopy;
        try {
            storageConfCopy = storageConfCopyFactory.copy(documentScannerConf.getStorageConf());
        } catch (StorageConfCopyException ex) {
            messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
            return;
        }
        storageSelectionDialog.setLocationRelativeTo(this);
        storageSelectionDialog.setVisible(true);
        StorageConf selectedStorageConf = storageSelectionDialog.getSelectedStorageConf();
        if(selectedStorageConf == null) {
            //dialog has been canceled
            return;
        }
        if(storageConfCopy.equals(selectedStorageConf)) {
            LOGGER.info("no changes made to storage configuration");
        }else{
            //type of StorageConf changed
            this.documentScannerConf.setStorageConf(selectedStorageConf);
            try {
                LOGGER.debug("storage configuration changes, shutting down current storage");
                this.storage.shutdown();
                LOGGER.debug("creating new storage based on changed configuration");
                this.storage = delegatingStorageFactory.create(selectedStorageConf);
            } catch (StorageCreationException ex) {
                throw new RuntimeException(ex);
            }
            mainPanel.setStorage(this.storage);
        }
    }//GEN-LAST:event_storageSelectionMenuItemActionPerformed

    private void oCRMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_oCRMenuItemActionPerformed
        OCREngineSelectDialog oCREngineSelectDialog = new OCREngineSelectDialog(this,
                this.documentScannerConf,
                this.messageHandler);
        oCREngineSelectDialog.setLocationRelativeTo(this);
        oCREngineSelectDialog.setVisible(true);
        OCREngineConf selectedOCREngineConf = oCREngineSelectDialog.getSelectedOCREngineConf();
        if(selectedOCREngineConf == null) {
            //dialog canceled
            return;
        }
        this.documentScannerConf.setoCREngineConf(selectedOCREngineConf);
        this.oCREngine = oCREngineFactory.create(selectedOCREngineConf);
        mainPanel.setoCREngine(oCREngine);
    }//GEN-LAST:event_oCRMenuItemActionPerformed

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "PDF files", "pdf");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        final File selectedFile = chooser.getSelectedFile();
        try {
            List<ImageWrapper> images = Tools.retrieveImages(selectedFile,
                    this,
                    documentScannerConf.getImageWrapperStorageDir());
            if(images == null) {
                LOGGER.debug("image retrieval has been canceled, discontinuing adding document");
                return;
            }
            addDocument(images,
                    selectedFile);
        } catch (DocumentAddException | InterruptedException | ExecutionException | IOException ex) {
            handleException(ex, "Exception during adding new document");
        }
    }//GEN-LAST:event_openMenuItemActionPerformed

    private void optionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsMenuItemActionPerformed
        DocumentScannerConfDialog documentScannerConfDialog = new DocumentScannerConfDialog(this, documentScannerConf);
        documentScannerConfDialog.setVisible(true);
    }//GEN-LAST:event_optionsMenuItemActionPerformed

    private void editEntryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editEntryMenuItemActionPerformed
        EntityEditingDialog entityEditingDialog = new EntityEditingDialog(this,
                Constants.ENTITY_CLASSES,
                Constants.PRIMARY_CLASS_SELECTION,
                storage,
                messageHandler,
                confirmMessageHandler,
                idApplier,
                warningHandlers,
                queryComponentFieldInitializer,
                entryStorage,
                readOnlyFieldRetriever);
        entityEditingDialog.setVisible(true); //blocks
        List<Object> selectedEntities = entityEditingDialog.getSelectedEntities();
        if(selectedEntities.size() > Constants.SELECTED_ENTITIES_EDIT_WARNING) {
            String answer = confirmMessageHandler.confirm(new Message(
                    String.format("More than %d entities are supposed to be opened for editing. This might take a long time. Continue?",
                            Constants.SELECTED_ENTITIES_EDIT_WARNING),
                    JOptionPane.QUESTION_MESSAGE,
                    "Open documents?"),
                    "Yes", "No");
            if(!answer.equals("Yes")) {
                return;
            }
        }
        for(Object selectedEntity : selectedEntities) {
            try {
                addDocument(selectedEntity);
            } catch (DocumentAddException | IOException ex) {
                handleException(ex, "Exception during adding new document");
            }
        }
    }//GEN-LAST:event_editEntryMenuItemActionPerformed

    private void openSelectionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openSelectionMenuItemActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "PDF files", "pdf");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        final File selectedFile = chooser.getSelectedFile();
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            List<ImageWrapper> images = Tools.retrieveImages(selectedFile,
                    this,
                    documentScannerConf.getImageWrapperStorageDir());
            if(images == null) {
                LOGGER.debug("image retrieval has been canceled, discontinuing adding document");
                return;
            }
            if(!images.isEmpty()) {
                final List<List<ImageWrapper>> scannerResults = new LinkedList<>();
                ScannerResultDialog scannerResultDialog = new ScannerResultDialog(this,
                        images,
                        this.documentScannerConf.getPreferredScanResultPanelWidth(),
                        scannerDevice,
                        documentScannerConf.getImageWrapperStorageDir(),
                        javaFXDialogMessageHandler,
                        issueHandler,
                        this //openDocumentWaitDialogParent
                );
                scannerResultDialog.setLocationRelativeTo(this);
                scannerResultDialog.setVisible(true);
                if(this.documentScannerConf.isRememberPreferredScanResultPanelWidth()) {
                    this.documentScannerConf.setPreferredScanResultPanelWidth(scannerResultDialog.getPanelWidth());
                }
                List<List<ImageWrapper>> dialogResult = scannerResultDialog.getSortedDocuments();
                if(dialogResult == null) {
                    //dialog canceled
                    return;
                }
                scannerResults.addAll(scannerResultDialog.getSortedDocuments());
                for(List<ImageWrapper> scannerResult : scannerResults) {
                    addDocument(scannerResult,
                            null //selectedFile
                    );
                }
                //this.validate(); //not necessary
            }
        } catch (DocumentAddException | InterruptedException | ExecutionException | IOException ex) {
            handleException(ex, "Exception during adding new document");
        }
    }//GEN-LAST:event_openSelectionMenuItemActionPerformed

    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed
        this.mainPanel.removeActiveDocument();
        if(this.mainPanel.getDocumentCount() == 0) {
            this.closeMenuItem.setEnabled(false);
            this.exportMenuItem.setEnabled(false);
        }
    }//GEN-LAST:event_closeMenuItemActionPerformed

    private void exportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportMenuItemActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "PDF files", "pdf");
        chooser.setFileFilter(filter);
        boolean success = false;
        File selectedFile = null;
        while(!success) {
            int returnVal = chooser.showOpenDialog(this);
            selectedFile = chooser.getSelectedFile();
            if (returnVal != JFileChooser.APPROVE_OPTION) {
                return;
            }
            if(selectedFile.exists()) {
                String answer = confirmMessageHandler.confirm(new Message(
                        String.format("selected file '%s' exists. Overwrite?",
                                selectedFile.getName()),
                        JOptionPane.WARNING_MESSAGE,
                        "File exists"),
                        "Yes", "No");
                success = answer.equals("Yes");
            }else {
                success = true;
            }
        }
        assert selectedFile != null; //if dialog is canceled, method returns
        try {
            this.mainPanel.exportActiveDocument(selectedFile,
                    MainPanel.EXPORT_FORMAT_PDF);
        } catch (IOException ex) {
            messageHandler.handle(new Message(ex,
                    JOptionPane.ERROR_MESSAGE));
        }
    }//GEN-LAST:event_exportMenuItemActionPerformed

    private void valueDetectionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valueDetectionMenuItemActionPerformed
        ValueDetectionServiceConfDialog serviceConfDialog = new ValueDetectionServiceConfDialog(this,
                documentScannerConf.getAvailableValueDetectionServiceConfs(),
                documentScannerConf.getSelectedValueDetectionServiceConfs(),
                messageHandler);
        serviceConfDialog.setLocationRelativeTo(this);
        serviceConfDialog.setVisible(true);
        List<ValueDetectionServiceConf> availalbleValueDetectionServiceConfs = serviceConfDialog.getAvailableValueDetectionServiceConfs();
        if(availalbleValueDetectionServiceConfs == null) {
            //dialog canceled
            return;
        }
        this.documentScannerConf.setAvailableValueDetectionServiceConfs(availalbleValueDetectionServiceConfs);
        this.documentScannerConf.setSelectedValueDetectionServiceConfs(serviceConfDialog.getSelectedValueDetectionServiceConfs());
        this.documentScannerConf.setValueDetectionServiceJARPaths(serviceConfDialog.getValueDetectionServiceJARPaths());
        this.mainPanel.applyValueDetectionServiceSelection();
    }//GEN-LAST:event_valueDetectionMenuItemActionPerformed

    private void addDocument(List<ImageWrapper> images,
            File selectedFile) throws DocumentAddException, IOException {
        //wait as long as possible
        if(amountMoneyExchangeRetrieverInitThread.isAlive()) {
            try {
                amountMoneyExchangeRetrieverInitThread.join();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        this.mainPanel.addDocument(images,
                selectedFile);
        closeMenuItem.setEnabled(true);
        exportMenuItem.setEnabled(true);
    }

    private void addDocument(Object entityToEdit) throws DocumentAddException, IOException {
        //wait as long as possible
        if(amountMoneyExchangeRetrieverInitThread.isAlive()) {
            try {
                amountMoneyExchangeRetrieverInitThread.join();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        this.mainPanel.addDocument(entityToEdit);
        closeMenuItem.setEnabled(true);
        exportMenuItem.setEnabled(true);
    }

    /**
     * How to select scan source? There might be inimaginable scan sources
     * besides flatbed, ADF and duplex ADF which will be configurable in the
     * scanner edit dialog. If those are configured, there can't be any dialog
     * displayed because we can't know about it's configuration parameters (and
     * we'll just call {@link SaneDevice#acquireImage() }).
     *
     * If no unknown source is configured in scanner edit dialog, we open a
     * {@link ScannerPageSelectDialog} which allows to configure the following
     * scan for flatbed, ADF and duplex ADF (with the setting configured in the
     * scanner edit dialog as initial values).
     *
     * A {@link ScannerResultDialog} is always opened - even for one page
     * flatbed scan results since it allows to scan more.
     *
     * @return the retrieved images or {@code null} if the wait dialog has been
     * canceled
     * @throws SaneException
     * @throws IOException
     */
    public static List<ImageWrapper> retrieveImages(SaneDevice scannerDevice,
            Window dialogParent,
            File imageWrapperStorageDir,
            MessageHandler messageHandler) throws SaneException, IOException, DocumentSourceOptionMissingException {
        if(scannerDevice.getOption(DOCUMENT_SOURCE_OPTION_NAME) == null) {
            //an exception is thrown in order to allow data to be reported
            //through BugHandler
            throw new DocumentSourceOptionMissingException(scannerDevice);
        }
        DocumentSource configuredDocumentSource = ScannerEditDialog.getDocumentSourceEnum(scannerDevice);
        final DocumentSource selectedDocumentSource;
        final ScannerPageSelectDialog scannerPageSelectDialog;
        if(configuredDocumentSource != DocumentSource.UNKNOWN) {
            scannerPageSelectDialog = new ScannerPageSelectDialog(dialogParent,
                    configuredDocumentSource);
            scannerPageSelectDialog.setVisible(true);
            selectedDocumentSource = scannerPageSelectDialog.getSelectedDocumentSource();
        }else {
            scannerPageSelectDialog = null;
            selectedDocumentSource = DocumentSource.UNKNOWN;
        }
        if(selectedDocumentSource == null) {
            return null;
        }
        final SwingWorkerGetWaitDialog dialog = new SwingWorkerGetWaitDialog(dialogParent,
                DocumentScanner.generateApplicationWindowTitle("Scanning",
                        Constants.APP_NAME,
                        Constants.APP_VERSION),
                "Scanning", //labelText
                "Scanning" //progressBarText
        );
        SwingWorkerCompletionWaiter swingWorkerCompletionWaiter = new SwingWorkerCompletionWaiter(dialog);
        SwingWorker<List<ImageWrapper>, Void> worker = new SwingWorker<List<ImageWrapper>, Void>() {
            @Override
            protected List<ImageWrapper> doInBackground() throws Exception {
                ScannerEditDialog.setDocumentSourceEnum(scannerDevice,
                        selectedDocumentSource);
                List<ImageWrapper> retValue = new LinkedList<>();
                if(selectedDocumentSource == DocumentSource.FLATBED || selectedDocumentSource == DocumentSource.UNKNOWN) {
                    BufferedImage scannedImage = scannerDevice.acquireImage();
                    //catching ScanException and invoking scannerDevice.close
                    //causes all settings to be reset (resolution, color, etc.)
                    //and doesn't avoid SaneException at every following call to
                    //scannerDevice.acquireImage
                    ImageWrapper imageWrapper = new CachingImageWrapper(imageWrapperStorageDir,
                            scannedImage);
                    retValue.add(imageWrapper);
                }else {
                    //ADF or duplex ADF
                    if(selectedDocumentSource == DocumentSource.ADF) {
                        ScannerEditDialog.setDocumentSource(scannerDevice, "ADF");
                    }else {
                        ScannerEditDialog.setDocumentSource(scannerDevice, "Duplex");
                    }
                    if(scannerPageSelectDialog.isScanAll()) {
                        while (true) {
                            try {
                                BufferedImage scannedImage = scannerDevice.acquireImage();
                                ImageWrapper imageWrapper = new CachingImageWrapper(imageWrapperStorageDir,
                                        scannedImage);
                                retValue.add(imageWrapper);
                            } catch (SaneException e) {
                                if (e.getStatus() == SaneStatus.STATUS_NO_DOCS) {
                                    // this is the out of paper condition that we expect
                                    LOGGER.info("no pages left to scan");
                                    break;
                                } else {
                                    // some other exception that was not expected
                                    throw e;
                                }
                            }
                        }
                    }else {
                        int scannedPagesCount = 0;
                        while(scannedPagesCount < scannerPageSelectDialog.getPageCount()) {
                            LOGGER.info(String.format("requested scan of %d pages", scannerPageSelectDialog.getPageCount()));
                            try {
                                BufferedImage scannedImage = scannerDevice.acquireImage();
                                ImageWrapper imageWrapper = new CachingImageWrapper(imageWrapperStorageDir,
                                        scannedImage);
                                retValue.add(imageWrapper);
                            } catch (SaneException e) {
                                if (e.getStatus() == SaneStatus.STATUS_NO_DOCS) {
                                    // this is the out of paper condition that we expect
                                    LOGGER.info("no pages left to scan");
                                    break;
                                } else {
                                    // some other exception that was not expected
                                    throw e;
                                }
                            }
                            scannedPagesCount += 1;
                        }
                        scannerDevice.cancel(); //scanner remains in scan mode otherwise
                    }
                }
                return retValue;
            }
        };
        worker.addPropertyChangeListener(swingWorkerCompletionWaiter);
        worker.execute();
        //the dialog will be visible until the SwingWorker is done
        dialog.setVisible(true);
        if(dialog.isCanceled()) {
            return null;
        }
        try {
            return worker.get();
        } catch (InterruptedException | ExecutionException ex) {
            messageHandler.handle(new Message(ex.getCause(), JOptionPane.ERROR_MESSAGE));
            return null;
        }
    }

    private void scan() throws DocumentSourceOptionMissingException {
        assert this.scannerDevice != null;
        try {
            if(!this.scannerDevice.isOpen()) {
                this.scannerDevice.open();
            }
            List<ImageWrapper> scannedImages = retrieveImages(scannerDevice,
                    this,
                    documentScannerConf.getImageWrapperStorageDir(),
                    messageHandler);
            if(scannedImages == null) {
                //canceled or exception which has been handled inside retrieveImages
                return;
            }
            if(!scannedImages.isEmpty()) {
                LOGGER.debug(String.format("scanned %d pages", scannedImages.size()));
                final List<List<ImageWrapper>> scannerResults = new LinkedList<>();
                ScannerResultDialog scannerResultDialog = new ScannerResultDialog(this,
                        scannedImages,
                        this.documentScannerConf.getPreferredScanResultPanelWidth(),
                        scannerDevice,
                        documentScannerConf.getImageWrapperStorageDir(),
                        javaFXDialogMessageHandler,
                        issueHandler,
                        this //openDocumentWaitDialogParent
                );
                scannerResultDialog.setLocationRelativeTo(this);
                scannerResultDialog.setVisible(true);
                if(this.documentScannerConf.isRememberPreferredScanResultPanelWidth()) {
                    this.documentScannerConf.setPreferredScanResultPanelWidth(scannerResultDialog.getPanelWidth());
                }
                List<List<ImageWrapper>> dialogResult = scannerResultDialog.getSortedDocuments();
                if(dialogResult == null) {
                    //dialog canceled
                    return;
                }
                scannerResults.addAll(scannerResultDialog.getSortedDocuments());
                for(List<ImageWrapper> scannerResult : scannerResults) {
                    addDocument(scannerResult,
                            null //selectedFile
                    );
                }
                this.validate();
            }
        } catch (SaneException | IOException | IllegalArgumentException | IllegalStateException | DocumentAddException ex) {
            this.handleException(ex, "Exception during scanning");
        }
        //Don't call device.close because it resets all options
    }

    private void handleException(Throwable ex, String title) {
        LOGGER.info("handling exception {}", ex);
        this.messageHandler.handle(new Message(String.format("The following exception occured: %s", ExceptionUtils.getRootCauseMessage(ex)),
                JOptionPane.ERROR_MESSAGE,
                title));
    }

    private void handleUnexpectedException(Throwable ex, String title) {
        handleException(ex, title);
        this.issueHandler.handleUnexpectedException(new ExceptionMessage(ex));
        this.shutdownHook();
            //there's few sense to leave the application running after an
            //unexpected exception which the user has been informed about
    }

    /**
     * This methods handles the fact that before checking
     * {@link DocumentScannerConf#isSkipUserAllowedAutoBugTrackingQuestion} the
     * user needs to be asked separately whether he_she allows upload of an
     * occured exception.
     *
     * @param ex the exception to handle
     */
    private static void handleExceptionInEventQueueThread(Throwable ex) {
        ConfirmMessageHandler confirmMessageHandler = new DialogConfirmMessageHandler(null, //parent
                "", //titlePrefix
                String.format("- %s %s", Constants.APP_NAME, Constants.APP_VERSION) //titleSuffix
        );
        String yes = "Yes";
        String no = "No";
        String answer = confirmMessageHandler.confirm(new Message(String.format(
                "The error '%s' occured. Do you allow anonymous upload of "
                        + "information about the reason for issue to the"
                        + " developers?",
                        ExceptionUtils.getRootCauseMessage(ex)),
                JOptionPane.QUESTION_MESSAGE,
                "Anoymous contribution"),
                yes,
                no);
        BugHandler bugHandler;
        if(answer == null //if dialog has been canceled
                || !answer.equals(yes)) {
            //still display the error message with the complete stacktrace
            bugHandler = new DialogBugHandler(null, //parent
                    Constants.BUG_URL,
                    "", //titlePrefix
                    String.format("- %s %s", Constants.APP_NAME, Constants.APP_VERSION) //titleSuffix
            );
        }else {
            bugHandler = new RavenBugHandler(RAVEN_DSN);
        }
        bugHandler.handleUnexpectedException(new ExceptionMessage(ex));
        shutdownHookThreads();
    }

    public IssueHandler getIssueHandler() {
        return issueHandler;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DocumentScanner.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                DocumentScanner documentScanner = null;
                DocumentScannerConf documentScannerConf = null;
                MessageHandler messageHandler = new DialogMessageHandler(null //parent
                        );
                try {
                    assert DocumentScannerConf.HOME_DIR.exists();
                    documentScannerConf = new DocumentScannerConf();
                    //read once for configFile parameter...
                    new JCommander(documentScannerConf, args);

                    if(documentScannerConf.getConfigFile().exists()) {
                        //...then for value detection service JARs to load...
                        XStream xStream = new XStream();
                        xStream.ignoreUnknownElements();
                            //doesn't avoid com.thoughtworks.xstream.mapper.CannotResolveClassException
                        xStream.omitField(DocumentScannerConf.class, "availableValueDetectionServiceConfs");
                        xStream.omitField(DocumentScannerConf.class, "selectedValueDetectionServiceConfs");
                        try {
                            documentScannerConf = (DocumentScannerConf)xStream.fromXML(new FileInputStream(documentScannerConf.getConfigFile()));
                        } catch (FileNotFoundException ex) {
                            throw new RuntimeException(ex);
                        }
                        List<URL> classLoaderURLs = new LinkedList<>();
                        for(String valueDetectionServiceJARPath : documentScannerConf.getValueDetectionServiceJARPaths()) {
                            File valueDetectionServiceJARFile = new File(valueDetectionServiceJARPath);
                            classLoaderURLs.add(valueDetectionServiceJARFile.toURI().toURL());
                        }
                        URLClassLoader classLoader = new URLClassLoader(classLoaderURLs.toArray(new URL[classLoaderURLs.size()]),
                                Thread.currentThread().getContextClassLoader()
                                    //System.class.getClassLoader doesn't work
                        );
                        //...then read config file
                        xStream = new XStream();
                        xStream.setClassLoader(classLoader);
                        documentScannerConf = (DocumentScannerConf)xStream.fromXML(new FileInputStream(documentScannerConf.getConfigFile()));
                    }else {
                        documentScannerConf = new DocumentScannerConf();
                        LOGGER.info("no previous configuration found in configuration directry '{}', using default values", documentScannerConf.getConfigFile().getAbsolutePath());
                        //new configuration will be persisted in shutdownHook
                    }
                    //...and override value from command line
                    new JCommander(documentScannerConf, args);

                    documentScanner = new DocumentScanner(documentScannerConf);
                    documentScanner.init();
                    documentScanner.setVisible(true); //all shutdown and
                        //resource closing routines need to be handled in event
                        //handling and shutdown hooks since this doesn't block
                        //and there's no way of acchieving that without trouble
                } catch(ImageWrapperStorageDirExistsException ex) {
                    LOGGER.warn("aborting application start because user wants "
                            + "to investigate existing files in image storage "
                            + "directory");
                    if(documentScanner != null) {
                        documentScanner.setVisible(false);
                        documentScanner.close();
                        documentScanner.shutdownHook();
                        documentScanner.dispose();
                    }
                } catch (BinaryNotFoundException ex) {
                    String message = "The tesseract binary isn't available. Install it on your system and make sure it's executable (in doubt check if tesseract runs on the console)";
                    LOGGER.error(message);
                    messageHandler.handle(new Message(
                            message,
                            JOptionPane.ERROR_MESSAGE,
                            "tesseract binary missing"));
                    if(documentScanner != null) {
                        documentScanner.setVisible(false);
                        documentScanner.close();
                        documentScanner.shutdownHook();
                        documentScanner.dispose();
                    }
                } catch(StorageConfValidationException | StorageCreationException ex) {
                    LOGGER.error("An unexpected exception during initialization of storage occured, see nested exception for details", ex);
                    messageHandler.handle(new Message(ex,
                            JOptionPane.ERROR_MESSAGE));
                    if(documentScanner != null) {
                        documentScanner.setVisible(false);
                        documentScanner.close();
                        documentScanner.shutdownHook();
                        documentScanner.dispose();
                    }
                } catch(Exception ex) {
                    LOGGER.error("An unexpected exception occured, see nested exception for details", ex);
                    if(documentScanner != null
                            && documentScanner.getIssueHandler() != null) {
                        documentScanner.getIssueHandler().handleUnexpectedException(new ExceptionMessage(ex));
                    }else {
                        handleExceptionInEventQueueThread(ex);
                    }
                    if(documentScanner != null) {
                        documentScanner.setVisible(false);
                        documentScanner.close();
                        documentScanner.shutdownHook();
                        documentScanner.dispose();
                    }
                    System.exit(SYSTEM_EXIT_ERROR_GENERAL);
                        //calling System.exit is the only way to be able to
                        //close DocumentScanner resources which have been
                        //initialized in the constructor after an exception in
                        //the constructor occured
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JPopupMenu.Separator databaseMenuSeparator;
    private javax.swing.JMenuItem editEntryMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JPopupMenu.Separator exitMenuItemSeparator;
    private javax.swing.JMenuItem exportMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPopupMenu.Separator knownScannersMenuItemSeparator;
    private javax.swing.JPopupMenu.Separator knownStoragesMenuItemSeparartor;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JPanel mainPanelPanel;
    private javax.swing.JMenuItem oCRMenuItem;
    private javax.swing.JPopupMenu.Separator oCRMenuSeparator;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem openSelectionMenuItem;
    private javax.swing.JMenuItem optionsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenuItem scanMenuItem;
    private javax.swing.JMenu scannerSelectionMenu;
    private javax.swing.JMenuItem selectScannerMenuItem;
    private javax.swing.JMenu storageSelectionMenu;
    private javax.swing.JMenuItem storageSelectionMenuItem;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenuItem valueDetectionMenuItem;
    // End of variables declaration//GEN-END:variables
}
