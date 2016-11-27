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
import com.google.common.reflect.TypeToken;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.orientechnologies.orient.core.exception.OSecurityAccessException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jscience.economics.money.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.components.tag.FileTagStorage;
import richtercloud.document.scanner.components.tag.TagStorage;
import richtercloud.document.scanner.gui.conf.DerbyPersistenceStorageConf;
import richtercloud.document.scanner.gui.conf.DerbyPersistenceStorageConfInitializationException;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.gui.conf.OCREngineConf;
import richtercloud.document.scanner.gui.conf.StorageConf;
import richtercloud.document.scanner.gui.conf.TesseractOCREngineConf;
import richtercloud.document.scanner.gui.engineconf.OCREngineConfPanel;
import richtercloud.document.scanner.gui.storageconf.StorageConfPanel;
import richtercloud.document.scanner.ifaces.DocumentAddException;
import richtercloud.document.scanner.ifaces.MainPanel;
import richtercloud.document.scanner.model.APackage;
import richtercloud.document.scanner.model.Bill;
import richtercloud.document.scanner.model.Company;
import richtercloud.document.scanner.model.Document;
import richtercloud.document.scanner.model.Email;
import richtercloud.document.scanner.model.EmailAddress;
import richtercloud.document.scanner.model.Employment;
import richtercloud.document.scanner.model.FinanceAccount;
import richtercloud.document.scanner.model.Leaflet;
import richtercloud.document.scanner.model.Location;
import richtercloud.document.scanner.model.Payment;
import richtercloud.document.scanner.model.Person;
import richtercloud.document.scanner.model.Shipping;
import richtercloud.document.scanner.model.TelephoneCall;
import richtercloud.document.scanner.model.TelephoneNumber;
import richtercloud.document.scanner.model.Transport;
import richtercloud.document.scanner.model.TransportTicket;
import richtercloud.document.scanner.model.Withdrawal;
import richtercloud.document.scanner.model.Workflow;
import richtercloud.document.scanner.model.warninghandler.CompanyWarningHandler;
import richtercloud.document.scanner.ocr.BinaryNotFoundException;
import richtercloud.document.scanner.ocr.OCREngine;
import richtercloud.document.scanner.ocr.OCREngineConfInfo;
import richtercloud.document.scanner.ocr.OCREngineFactory;
import richtercloud.document.scanner.ocr.TesseractOCREngineFactory;
import richtercloud.document.scanner.setter.AmountMoneyPanelSetter;
import richtercloud.document.scanner.setter.EmbeddableListPanelSetter;
import richtercloud.document.scanner.setter.LongIdPanelSetter;
import richtercloud.document.scanner.setter.SpinnerSetter;
import richtercloud.document.scanner.setter.StringAutoCompletePanelSetter;
import richtercloud.document.scanner.setter.TextFieldSetter;
import richtercloud.document.scanner.setter.UtilDatePickerSetter;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.message.handler.ConfirmMessageHandler;
import richtercloud.message.handler.DialogConfirmMessageHandler;
import richtercloud.message.handler.DialogMessageHandler;
import richtercloud.message.handler.Message;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.AnyType;
import richtercloud.reflection.form.builder.FieldRetriever;
import richtercloud.reflection.form.builder.ReflectionFormPanel;
import richtercloud.reflection.form.builder.components.date.UtilDatePicker;
import richtercloud.reflection.form.builder.components.money.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetriever;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetrieverException;
import richtercloud.reflection.form.builder.components.money.AmountMoneyPanel;
import richtercloud.reflection.form.builder.components.money.AmountMoneyUsageStatisticsStorage;
import richtercloud.reflection.form.builder.components.money.FailsafeAmountMoneyExchangeRateRetriever;
import richtercloud.reflection.form.builder.components.money.FileAmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.money.FileAmountMoneyUsageStatisticsStorage;
import richtercloud.reflection.form.builder.jpa.JPACachedFieldRetriever;
import richtercloud.reflection.form.builder.jpa.WarningHandler;
import richtercloud.reflection.form.builder.jpa.idapplier.GeneratedValueIdApplier;
import richtercloud.reflection.form.builder.jpa.idapplier.IdApplier;
import richtercloud.reflection.form.builder.jpa.panels.EmbeddableListPanel;
import richtercloud.reflection.form.builder.jpa.panels.LongIdPanel;
import richtercloud.reflection.form.builder.jpa.panels.StringAutoCompletePanel;
import richtercloud.reflection.form.builder.jpa.typehandler.JPAEntityListTypeHandler;
import richtercloud.reflection.form.builder.jpa.typehandler.factory.JPAAmountMoneyMappingTypeHandlerFactory;
import richtercloud.reflection.form.builder.typehandler.TypeHandler;

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
 */
public class DocumentScanner extends javax.swing.JFrame implements Managed<Exception> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentScanner.class);
    private static final int ORIENTDB_PORT_DEFAULT = 2_424;
    private static final String CONNECTION_URL_EXAMPLE = "remote:localhost/GratefulDeadConcerts";
    private static final String CONNECTION_URL_TOOLTIP_TEXT = String.format("[mode]:[path] (where mode is one of <b>remote</b>, <b>plocal</b> or <b>??</b> and path is in the form [IP or hostname]/[database name], e.g. %s)", CONNECTION_URL_EXAMPLE);
    public static final String APP_NAME = "Document scanner";
    public static final String APP_VERSION = "1.0";
    public static final String BUG_URL = "https://github.com/krichter722/document-scanner";
    private SaneDevice scannerDevice;
    private ODatabaseDocumentTx db;
    private final MutableComboBoxModel<Class<? extends OCREngineConf<?>>> oCREngineComboBoxModel = new DefaultComboBoxModel<>();
    private OCREngineConfPanel<?> currentOCREngineConfPanel;
    //@TODO: implement class path discovery of associated conf panel with annotations
    private final TesseractOCREngineConfPanel tesseractOCREngineConfPanel;
    private final Map<Class<? extends OCREngineConf<?>>, OCREngineConfPanel<?>> oCREngineConfPanelMap = new HashMap<>();
    /**
     * The default value for resolution in DPI. The closest value to it might be
     * chosen if the exact resolution isn't available.
     */
    public final static int RESOLUTION_DEFAULT = 300;
    private static final String CONFIG_DIR_NAME = ".document-scanner";
    private final static String CONFIG_FILE_NAME = "document-scanner-config.xml";
    private final File configFile;
    private MutableComboBoxModel<Class<? extends StorageConf<?,?>>> storageCreateDialogTypeComboBoxModel = new DefaultComboBoxModel<>();
    private Map<Class<? extends StorageConf<?,?>>, StorageConfPanel<?>> storageConfPanelMap = new HashMap<>();
    private DefaultListModel<StorageConf<?,?>> storageListModel = new DefaultListModel<>();
    public final static Set<Class<?>> ENTITY_CLASSES = Collections.unmodifiableSet(new HashSet<Class<?>>(
            Arrays.asList(APackage.class,
                    Bill.class,
                    Company.class,
                    Document.class,
                    Email.class,
                    EmailAddress.class,
                    Employment.class,
                    FinanceAccount.class,
                    Leaflet.class,
                    Location.class,
                    Payment.class,
                    Person.class,
                    Shipping.class,
                    TelephoneCall.class,
                    TelephoneNumber.class,
                    Transport.class,
                    TransportTicket.class,
                    Withdrawal.class,
                    Workflow.class
//                    WorkflowItem.class // is an entity, but abstract
                    )));
    public final static Class<?> PRIMARY_CLASS_SELECTION = Document.class;
    public static final String DATABASE_DIR_NAME_DEFAULT = "databases";
    /*
    internal implementation notes:
    - connection and EntityManagerFactory should both be either static or
    dynamic (due to the fact that there might be support for multiple instances
    at some point make it dynamic
     */
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private DocumentScannerConf documentScannerConf;
    private boolean debug = false;
    private DocumentScannerCommandParser cmd = new DocumentScannerCommandParser();
    private final static String PROPERTY_KEY_DEBUG = "document.scanner.debug";
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
    private final AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever = new FailsafeAmountMoneyExchangeRateRetriever();
    private final TagStorage tagStorage;
    private final static String AMOUNT_MONEY_USAGE_STATISTICS_STORAGE_FILE_NAME = "currency-usage-statistics.xml";
    private final static String AMOUNT_MONEY_CURRENCY_STORAGE_FILE_NAME = "currencies.xml";
    private final static String TAG_STORAGE_FILE_NAME = "tags";
    private final Map<java.lang.reflect.Type, TypeHandler<?, ?,?, ?>> typeHandlerMapping;
    private MessageHandler messageHandler = new DialogMessageHandler(this);
    private ConfirmMessageHandler confirmMessageHandler = new DialogConfirmMessageHandler(this);
    private final Map<Class<?>, WarningHandler<?>> warningHandlers = new HashMap<Class<?>, WarningHandler<?>>() {
        private static final long serialVersionUID = 1L;
        {
            put(Company.class,
                    new CompanyWarningHandler(entityManager, confirmMessageHandler));
        }
    };
    private ListCellRenderer<Object> oCRDialogEngineComboBoxRenderer = new DefaultListCellRenderer() {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Class<?> valueCast = (Class<?>) value;
            OCREngineConfInfo oCREngineInfo = valueCast.getAnnotation(OCREngineConfInfo.class);
            String value0;
            if (oCREngineInfo != null) {
                value0 = oCREngineInfo.name();
            } else {
                value0 = valueCast.getSimpleName();
            }
            return super.getListCellRendererComponent(list, value0, index, isSelected, cellHasFocus);
        }

    };
    private final static String XML_STORAGE_FILE_NAME_DEFAULT = "xml-storage.xml";
    private File xMLStorageFile = new File(CONFIG_DIR, XML_STORAGE_FILE_NAME_DEFAULT);
    private File derbyPersistenceStorageSchemeChecksumFile = new File(CONFIG_DIR, DerbyPersistenceStorageConf.SCHEME_CHECKSUM_FILE_NAME);
    public final static int INITIAL_QUERY_LIMIT_DEFAULT = 20;
    public final static String BIDIRECTIONAL_HELP_DIALOG_TITLE = generateApplicationWindowTitle("Bidirectional relations help", APP_NAME, APP_VERSION);
    private final static DocumentScannerConfConverter DOCUMENT_SCANNER_CONF_CONVERTER = new DocumentScannerConfConverter();

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
    private final OCREngineFactory oCREngineFactory = new TesseractOCREngineFactory();
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
    public final static String SANED_BUG_INFO = "<br/>You might suffer from a "
            + "saned bug, try <tt>/usr/sbin/saned -d -s -a saned</tt> with "
            + "appropriate privileges in order to restart saned and try again"
            + "</html>";
    private final IdApplier<?> idApplier = new GeneratedValueIdApplier();
    /**
     * If multiple entities are selected in a {@link EntityEditingDialog} it
     * might take a long time to open documents for all of them, so a warning is
     * displayed if more documents than this value are about to be opened.
     */
    private final static int SELECTED_ENTITIES_EDIT_WARNING = 5;
    /**
     * Creation of a {@link JFXPanel} is necessary to instantiate the JavaFX
     * platform which is used for some parts of the application. The reference
     * isn't used by JavaFX, but stored here in order to indicate that a second
     * {@code JFXPanel} doesn't need to be allocated.
     */
    /*
    internal implementation notes:
    - This could be handled by a flag as well, but all resources aquired by the
    panel will remain in use and thus not be GCed anyway.
    */
    private JFXPanel javaFXInitPanel;

    /**
     * Parses the command line and evaluates system properties. Command line
     * arguments superseed properties.
     */
    private void parseArguments() {
        if (this.cmd.isDebug() != null) {
            this.debug = this.cmd.isDebug();
        } else {
            String debugProp = System.getProperty(PROPERTY_KEY_DEBUG);
            if (debugProp != null) {
                this.debug = Boolean.valueOf(debugProp);
            }
        }

        if (this.debug) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.DEBUG);
        }
    }

    public static SaneDevice getScannerDevice(String scannerName,
            Map<String, ScannerConf> scannerConfMap,
            String scannerAddressFallback) throws IOException, SaneException {
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
                    scannerConf
            );
        }
        return retValue;
    }

    /**
     * Fills {@code conf} from configuration file.
     */
    private void loadProperties() {
        XStream xStream = new XStream();
        xStream.registerConverter(DOCUMENT_SCANNER_CONF_CONVERTER);
        try {
            this.documentScannerConf = (DocumentScannerConf)xStream.fromXML(new FileInputStream(this.configFile));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        //if a scanner address and device name is in persisted conf check if
        //it's accessible and treat it as selected scanner silently
        try {
            String scannerName = this.documentScannerConf.getScannerName();
            this.scannerDevice = getScannerDevice(scannerName,
                    this.documentScannerConf.getScannerConfMap(),
                    DocumentScannerConf.SCANNER_SANE_ADDRESS_DEFAULT);
            afterScannerSelection();
        } catch (IOException | SaneException ex) {
            String text = handleSearchScannerException("An exception during the setup of "
                    + "previously selected scanner occured: ",
                    ex,
                    SANED_BUG_INFO);
            messageHandler.handle(new Message(String.format("Exception during setup of previously selected scanner: %s\n%s", ExceptionUtils.getRootCauseMessage(ex), text),
                    JOptionPane.WARNING_MESSAGE,
                    "Exception occured"));
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

    private void onDeviceSet() {
        assert this.scannerDevice != null;
        this.scannerLabel.setText(this.scannerDevice.toString());
        GroupLayout layout = new GroupLayout(this.statusBar);
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
        hGroup.addGroup(layout.createParallelGroup().
                addComponent(this.scannerLabel));
        layout.setHorizontalGroup(hGroup);
        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
                addComponent(this.scannerLabel));
        layout.setVerticalGroup(vGroup);
        this.statusBar.setLayout(layout);
        this.pack();
        this.invalidate();
    }

    private void onDeviceUnset() {
        assert this.scannerDevice == null;
        GroupLayout layout = new GroupLayout(this.statusBar);
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
        hGroup.addGroup(layout.createParallelGroup().
                addComponent(this.scannerLabel).addComponent(this.selectScannerButton));
        layout.setHorizontalGroup(hGroup);
        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
                addComponent(this.scannerLabel).addComponent(this.selectScannerButton));
        layout.setVerticalGroup(vGroup);
        this.statusBar.setLayout(layout);
        this.pack();
        this.invalidate();
    }

    private void afterScannerSelection() {
        this.scanMenuItem.setEnabled(true);
        this.scanMenuItem.getParent().revalidate();
    }

    private static class DocumentScannerConfConverter implements Converter {

        @Override
        public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            return null;
        }

        @Override
        public boolean canConvert(Class clazz) {
            boolean retValue = EntityManager.class.isAssignableFrom(clazz)
                    || java.awt.Component.class.isAssignableFrom(clazz)
                    || Process.class.isAssignableFrom(clazz)
                    || ReentrantLock.class.isAssignableFrom(clazz);
            return retValue;
        }
    }

    /**
     * Handles all non-resource related cleanup tasks (like persistence of
     * configuration). Sets all handled item references to {@code null} in order
     * to allow this to be run again by shutdown hook.
     * @see #close()
     */
    private void shutdownHook() {
        LOGGER.info("running {} shutdown hooks", DocumentScanner.class);
        if (this.documentScannerConf != null) {
            try {
                XStream xStream = new XStream();
                xStream.registerConverter(DOCUMENT_SCANNER_CONF_CONVERTER);
                xStream.toXML(this.documentScannerConf, new FileOutputStream(this.configFile));
            } catch (FileNotFoundException ex) {
                LOGGER.warn("an unexpected exception occured during save of configurations into file '{}', changes most likely lost", this.configFile.getAbsolutePath());
            }
            this.documentScannerConf = null;
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
        close();
    }

    private final static File HOME_DIR = new File(System.getProperty("user.home"));
    private final static File CONFIG_DIR = new File(HOME_DIR, CONFIG_DIR_NAME);
    private final static File DATABASE_DIR = new File(CONFIG_DIR, DATABASE_DIR_NAME_DEFAULT);
    private final static String DERBY_CONNECTION_URL = String.format("jdbc:derby:%s", DATABASE_DIR.getAbsolutePath());
    /**
     * Start to fetch results and warm up the cache after start.
     */
    private final Thread amountMoneyExchangeRetrieverInitThread = new Thread() {
        @Override
        public void run() {
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
        }
    };

    /**
     * Creates new DocumentScanner which does nothing unless
     *
     * @throws richtercloud.document.scanner.gui.TesseractNotFoundException
     */
    /*
    internal implementation notes:
    - resources are opened in init methods only (see https://richtercloud.de:446/doku.php?id=programming:java#resource_handling for details)
    */
    public DocumentScanner() throws BinaryNotFoundException {
        this.parseArguments();
        assert HOME_DIR.exists();
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdir();
            LOGGER.info("created inexisting configuration directory '{}'", CONFIG_DIR_NAME);
        }
        amountMoneyExchangeRetrieverInitThread.start();

        this.initComponents();

        //loading properties depends on initComponents because exceptions are
        //handled with GUI elements
        this.configFile = new File(CONFIG_DIR, CONFIG_FILE_NAME);
        if (this.configFile.exists()) {
            this.loadProperties(); //initializes this.conf
        } else {
            try {
                this.documentScannerConf = new DocumentScannerConf(entityManager,
                        messageHandler,
                        ENTITY_CLASSES,
                        derbyPersistenceStorageSchemeChecksumFile,
                        xMLStorageFile);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            LOGGER.info("no previous configuration found in configuration directry '{}', using default values", CONFIG_DIR.getAbsolutePath());
            //new configuration will be persisted in shutdownHook
        }

        try {
            this.tesseractOCREngineConfPanel = new TesseractOCREngineConfPanel((TesseractOCREngineConf) this.documentScannerConf.getoCREngineConf(),
                    this.messageHandler);
                //validates the configured binary
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        //after loading DocumentScannerConf
        for(StorageConf<?,?> availableStorageConf : this.documentScannerConf.getAvailableStorageConfs()) {
            this.storageListModel.addElement(availableStorageConf);
        }

        this.onDeviceUnset();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOGGER.info("running {} shutdown hooks", DocumentScanner.class);
                DocumentScanner.this.shutdownHook();
            }
        });
        this.oCREngineConfPanelMap.put(TesseractOCREngineConf.class, this.tesseractOCREngineConfPanel);
        this.oCREngineComboBoxModel.addElement(TesseractOCREngineConf.class);
        this.oCRDialogEngineComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Class<? extends OCREngineConf<?>> clazz = (Class<? extends OCREngineConf<?>>) e.getItem();
                OCREngineConfPanel<?> cREngineConfPanel = DocumentScanner.this.oCREngineConfPanelMap.get(clazz);
                DocumentScanner.this.oCRDialogPanel.removeAll();
                DocumentScanner.this.oCRDialogPanel.add(cREngineConfPanel);
                DocumentScanner.this.oCRDialogPanel.revalidate();
                DocumentScanner.this.pack();
                DocumentScanner.this.oCRDialogPanel.repaint();
            }
        });
        this.oCRDialogPanel.setLayout(new BoxLayout(this.oCRDialogPanel, BoxLayout.X_AXIS));
        //set initial panel state
        this.oCRDialogPanel.removeAll();
        this.oCRDialogPanel.add(this.tesseractOCREngineConfPanel);
        this.oCRDialogPanel.revalidate();
        this.pack();
        this.oCRDialogPanel.repaint();

        this.storageCreateDialogTypeComboBoxModel.addElement(DerbyPersistenceStorageConf.class);
        this.storageCreateDialogTypeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Class<? extends StorageConf<?,?>> clazz = (Class<? extends StorageConf<?,?>>) e.getItem();
                StorageConfPanel<?> storageConfPanel = DocumentScanner.this.storageConfPanelMap.get(clazz);
                DocumentScanner.this.storageCreateDialogPanel.removeAll();
                DocumentScanner.this.storageCreateDialogPanel.add(storageConfPanel);
                DocumentScanner.this.storageCreateDialogPanel.revalidate();
                DocumentScanner.this.pack();
                DocumentScanner.this.storageCreateDialogPanel.repaint();
            }
        });

        this.storageList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                DocumentScanner.this.storageDialogSelectButton.setEnabled(DocumentScanner.this.storageListModel.getSize() > 0
                        && DocumentScanner.this.storageList.getSelectedIndices().length > 0);
            }
        });
        File amountMoneyUsageStatisticsStorageFile = new File(CONFIG_DIR, AMOUNT_MONEY_USAGE_STATISTICS_STORAGE_FILE_NAME);
        File amountMoneyCurrencyStorageFile = new File(CONFIG_DIR, AMOUNT_MONEY_CURRENCY_STORAGE_FILE_NAME);
        try {
            this.amountMoneyUsageStatisticsStorage = new FileAmountMoneyUsageStatisticsStorage(amountMoneyUsageStatisticsStorageFile);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        this.amountMoneyCurrencyStorage = new FileAmountMoneyCurrencyStorage(amountMoneyCurrencyStorageFile);
        File tagStorageFile = new File(CONFIG_DIR, TAG_STORAGE_FILE_NAME);
        this.tagStorage = new FileTagStorage(tagStorageFile);
        JPAAmountMoneyMappingTypeHandlerFactory fieldHandlerFactory = new JPAAmountMoneyMappingTypeHandlerFactory(entityManager,
                INITIAL_QUERY_LIMIT_DEFAULT,
                messageHandler,
                BIDIRECTIONAL_HELP_DIALOG_TITLE);
        this.typeHandlerMapping = fieldHandlerFactory.generateTypeHandlerMapping();

        //after entity manager creation
        this.typeHandlerMapping.put(new TypeToken<List<AnyType>>() {
            }.getType(), new JPAEntityListTypeHandler(entityManager,
                    messageHandler,
                    BIDIRECTIONAL_HELP_DIALOG_TITLE));
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
    public void init() throws DerbyPersistenceStorageConfInitializationException, IOException {
        Map<Object, Object> entityManagerFactoryMap = new HashMap<>();
        entityManagerFactoryMap.put("javax.persistence.jdbc.url",
                String.format("%s;create=%s", DERBY_CONNECTION_URL, !DATABASE_DIR.exists()));
        this.entityManagerFactory = Persistence.createEntityManagerFactory("richtercloud_document-scanner_jar_1.0-SNAPSHOTPU",
                entityManagerFactoryMap);
        this.entityManager = entityManagerFactory.createEntityManager();

        DerbyPersistenceStorageConfPanel derbyStorageConfPanel;
        derbyStorageConfPanel = new DerbyPersistenceStorageConfPanel(entityManager,
                messageHandler,
                ENTITY_CLASSES,
                derbyPersistenceStorageSchemeChecksumFile //schemeChecksumFile
        ); //@TODO: replace with classpath annotation discovery
        this.storageConfPanelMap.put(DerbyPersistenceStorageConf.class, derbyStorageConfPanel);
        this.mainPanel = new DefaultMainPanel(ENTITY_CLASSES,
                PRIMARY_CLASS_SELECTION,
                entityManager,
                amountMoneyUsageStatisticsStorage,
                amountMoneyCurrencyStorage,
                amountMoneyExchangeRateRetriever,
                messageHandler,
                confirmMessageHandler,
                this,
                oCREngineFactory,
                documentScannerConf.getoCREngineConf(),
                typeHandlerMapping,
                documentScannerConf,
                this, //oCRProgressMonitorParent
                tagStorage,
                idApplier,
                warningHandlers
        );
        mainPanelPanel.add(this.mainPanel);
    }

    /**
     * Handles all resource-related shutdown tasks.
     * @see #shutdownHook()
     */
    @Override
    public void close() {
        if(this.entityManager != null && this.entityManager.isOpen()) {
            //might be null if an exception occured in Derby
            this.entityManager.close();
        }
        if(this.entityManagerFactory != null && this.entityManagerFactory.isOpen()) {
            //might be null if an exception occured in Derby
            this.entityManagerFactory.close();
        }

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
        if (this.db != null) {
            this.db.close();
            this.db = null;
        }
        //call to DriverManager.getConnection(String.format("%s;shutdown=true", DERBY_CONNECTION_URL));
        //fails due to `java.sql.SQLNonTransientConnectionException: Database '/home/richter/.document-scanner/databases' shutdown.`
        //with cause `Caused by: org.apache.derby.iapi.error.StandardException: Database '/home/richter/.document-scanner/databases' shutdown.`
        //(maybe EntityManagerFactory.close shuts down the database), but is
        //somehow necessary in order to remove db.lck in the database directory.
        try {
            LOGGER.info("Shutting down database and database server");
            DriverManager.getConnection(String.format("%s;shutdown=true", DERBY_CONNECTION_URL)); //shutdown the database
            DriverManager.getConnection("jdbc:derby:;shutdown=true"); //shutdown Derby (supposed to remove db.lck<ref>http://db.apache.org/derby/docs/10.8/devguide/tdevdvlp40464.html#tdevdvlp40464</ref>, but doesn't)
        } catch (SQLException ex) {
            LOGGER.error("an exception during shutdown of the database connection occured", ex);
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

        scannerLabel = new javax.swing.JLabel();
        selectScannerButton = new javax.swing.JButton();
        databaseDialog = new javax.swing.JDialog();
        databaseConnectionURLTextField = new javax.swing.JTextField();
        databaseConnectionURLLabel = new javax.swing.JLabel();
        databaseUsernameTextField = new javax.swing.JTextField();
        databaseUsernameLabel = new javax.swing.JLabel();
        databasePasswordTextField = new javax.swing.JPasswordField();
        databasePasswordLabel = new javax.swing.JLabel();
        databaseCancelButton = new javax.swing.JButton();
        databaseConnectButton = new javax.swing.JButton();
        databaseConnectionFailureLabel = new javax.swing.JLabel();
        oCRDialog = new javax.swing.JDialog();
        oCRDialogEngineComboBox = new javax.swing.JComboBox<>();
        oCRDialogEngineLabel = new javax.swing.JLabel();
        oCRDialogSeparator = new javax.swing.JSeparator();
        oCRDialogPanel = new javax.swing.JPanel();
        oCRDialogCancelButton = new javax.swing.JButton();
        oCRDialogSaveButton = new javax.swing.JButton();
        storageCreateDialog = new javax.swing.JDialog();
        storageCreateDialogNameTextField = new javax.swing.JTextField();
        storageCreateDialogNameLabel = new javax.swing.JLabel();
        storageCreateDialogTypeComboBox = new javax.swing.JComboBox<>();
        storageCreateDialogTypeLabel = new javax.swing.JLabel();
        storageCreateDialogCancelDialog = new javax.swing.JButton();
        storageCreateDialogSaveButton = new javax.swing.JButton();
        storageCreateDialogSeparator = new javax.swing.JSeparator();
        storageCreateDialogPanel = new javax.swing.JPanel();
        storageDialog = new javax.swing.JDialog();
        storageLabel = new javax.swing.JLabel();
        storageListScrollPane = new javax.swing.JScrollPane();
        storageList = new javax.swing.JList<>();
        storageDialogCancelButton = new javax.swing.JButton();
        storageDialogSelectButton = new javax.swing.JButton();
        storageDialogEditButton = new javax.swing.JButton();
        storageDialogDeleteButton = new javax.swing.JButton();
        storageDialogNewButton = new javax.swing.JButton();
        statusBar = new javax.swing.JPanel();
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
        editEntryMenuItem = new javax.swing.JMenuItem();
        autoOCRValueDetectionMenuItem = new javax.swing.JMenuItem();
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

        scannerLabel.setText("No scanner selected");

        selectScannerButton.setText("Select Scanner");
        selectScannerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectScannerButtonActionPerformed(evt);
            }
        });

        databaseDialog.setTitle(DocumentScanner.generateApplicationWindowTitle("Connect to database", APP_NAME, APP_VERSION));
        databaseDialog.setModal(true);
        databaseDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                databaseDialogWindowClosed(evt);
            }
        });

        databaseConnectionURLTextField.setText(CONNECTION_URL_EXAMPLE);
        databaseConnectionURLTextField.setToolTipText(CONNECTION_URL_TOOLTIP_TEXT);
        databaseConnectionURLTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                databaseConnectionURLTextFieldActionPerformed(evt);
            }
        });

        databaseConnectionURLLabel.setText("Connection URL");
        databaseConnectionURLLabel.setToolTipText(CONNECTION_URL_TOOLTIP_TEXT);

        databaseUsernameTextField.setText("root");

        databaseUsernameLabel.setText("Username");

        databasePasswordLabel.setText("Password");

        databaseCancelButton.setText("Cancel");
        databaseCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                databaseCancelButtonActionPerformed(evt);
            }
        });

        databaseConnectButton.setText("Connect");
        databaseConnectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                databaseConnectButtonActionPerformed(evt);
            }
        });

        databaseConnectionFailureLabel.setText(" ");

        javax.swing.GroupLayout databaseDialogLayout = new javax.swing.GroupLayout(databaseDialog.getContentPane());
        databaseDialog.getContentPane().setLayout(databaseDialogLayout);
        databaseDialogLayout.setHorizontalGroup(
            databaseDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, databaseDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(databaseDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(databaseConnectionFailureLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, databaseDialogLayout.createSequentialGroup()
                        .addGroup(databaseDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(databasePasswordLabel)
                            .addComponent(databaseConnectionURLLabel)
                            .addComponent(databaseUsernameLabel))
                        .addGap(18, 18, 18)
                        .addGroup(databaseDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(databasePasswordTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(databaseUsernameTextField)
                            .addComponent(databaseConnectionURLTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)))
                    .addGroup(databaseDialogLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(databaseConnectButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(databaseCancelButton)))
                .addContainerGap())
        );
        databaseDialogLayout.setVerticalGroup(
            databaseDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(databaseDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(databaseDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databaseConnectionURLTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(databaseConnectionURLLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(databaseDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databaseUsernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(databaseUsernameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(databaseDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databasePasswordTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(databasePasswordLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(databaseConnectionFailureLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(databaseDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databaseCancelButton)
                    .addComponent(databaseConnectButton))
                .addContainerGap())
        );

        oCRDialog.setTitle(DocumentScanner.generateApplicationWindowTitle("OCR setup", APP_NAME, APP_VERSION));
        oCRDialog.setModal(true);

        oCRDialogEngineComboBox.setModel(oCREngineComboBoxModel);
        oCRDialogEngineComboBox.setRenderer(oCRDialogEngineComboBoxRenderer);

        oCRDialogEngineLabel.setText("OCR engine");

        javax.swing.GroupLayout oCRDialogPanelLayout = new javax.swing.GroupLayout(oCRDialogPanel);
        oCRDialogPanel.setLayout(oCRDialogPanelLayout);
        oCRDialogPanelLayout.setHorizontalGroup(
            oCRDialogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        oCRDialogPanelLayout.setVerticalGroup(
            oCRDialogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 178, Short.MAX_VALUE)
        );

        oCRDialogCancelButton.setText("Cancel");
        oCRDialogCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                oCRDialogCancelButtonActionPerformed(evt);
            }
        });

        oCRDialogSaveButton.setText("Save");
        oCRDialogSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                oCRDialogSaveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout oCRDialogLayout = new javax.swing.GroupLayout(oCRDialog.getContentPane());
        oCRDialog.getContentPane().setLayout(oCRDialogLayout);
        oCRDialogLayout.setHorizontalGroup(
            oCRDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(oCRDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(oCRDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(oCRDialogPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(oCRDialogSeparator)
                    .addGroup(oCRDialogLayout.createSequentialGroup()
                        .addComponent(oCRDialogEngineLabel)
                        .addGap(18, 18, 18)
                        .addComponent(oCRDialogEngineComboBox, 0, 277, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, oCRDialogLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(oCRDialogSaveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(oCRDialogCancelButton)))
                .addContainerGap())
        );
        oCRDialogLayout.setVerticalGroup(
            oCRDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(oCRDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(oCRDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(oCRDialogEngineComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(oCRDialogEngineLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(oCRDialogSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(oCRDialogPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(oCRDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(oCRDialogCancelButton)
                    .addComponent(oCRDialogSaveButton))
                .addContainerGap())
        );

        storageCreateDialog.setTitle(DocumentScanner.generateApplicationWindowTitle("Create storage", APP_NAME, APP_VERSION));

        storageCreateDialogNameLabel.setText("Name");

        storageCreateDialogTypeComboBox.setModel(storageCreateDialogTypeComboBoxModel);
        storageCreateDialogTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storageCreateDialogTypeComboBoxActionPerformed(evt);
            }
        });

        storageCreateDialogTypeLabel.setText("Type");

        storageCreateDialogCancelDialog.setText("Cancel");
        storageCreateDialogCancelDialog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storageCreateDialogCancelDialogActionPerformed(evt);
            }
        });

        storageCreateDialogSaveButton.setText("Save");
        storageCreateDialogSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storageCreateDialogSaveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout storageCreateDialogPanelLayout = new javax.swing.GroupLayout(storageCreateDialogPanel);
        storageCreateDialogPanel.setLayout(storageCreateDialogPanelLayout);
        storageCreateDialogPanelLayout.setHorizontalGroup(
            storageCreateDialogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        storageCreateDialogPanelLayout.setVerticalGroup(
            storageCreateDialogPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 148, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout storageCreateDialogLayout = new javax.swing.GroupLayout(storageCreateDialog.getContentPane());
        storageCreateDialog.getContentPane().setLayout(storageCreateDialogLayout);
        storageCreateDialogLayout.setHorizontalGroup(
            storageCreateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, storageCreateDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(storageCreateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(storageCreateDialogPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(storageCreateDialogSeparator)
                    .addGroup(storageCreateDialogLayout.createSequentialGroup()
                        .addGroup(storageCreateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(storageCreateDialogNameLabel)
                            .addComponent(storageCreateDialogTypeLabel))
                        .addGap(18, 18, 18)
                        .addGroup(storageCreateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(storageCreateDialogNameTextField)
                            .addComponent(storageCreateDialogTypeComboBox, 0, 413, Short.MAX_VALUE)))
                    .addGroup(storageCreateDialogLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(storageCreateDialogSaveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(storageCreateDialogCancelDialog)))
                .addContainerGap())
        );
        storageCreateDialogLayout.setVerticalGroup(
            storageCreateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(storageCreateDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(storageCreateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(storageCreateDialogNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(storageCreateDialogNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(storageCreateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(storageCreateDialogTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(storageCreateDialogTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(storageCreateDialogSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(storageCreateDialogPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(storageCreateDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(storageCreateDialogCancelDialog)
                    .addComponent(storageCreateDialogSaveButton))
                .addContainerGap())
        );

        storageDialog.setTitle(DocumentScanner.generateApplicationWindowTitle("Configure storage", APP_NAME, APP_VERSION));

        storageLabel.setText("Storages");

        storageList.setModel(storageListModel);
        storageListScrollPane.setViewportView(storageList);

        storageDialogCancelButton.setText("Cancel");
        storageDialogCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storageDialogCancelButtonActionPerformed(evt);
            }
        });

        storageDialogSelectButton.setText("Select");
        storageDialogSelectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storageDialogSelectButtonActionPerformed(evt);
            }
        });

        storageDialogEditButton.setText("Edit");

        storageDialogDeleteButton.setText("Delete");

        storageDialogNewButton.setText("New...");
        storageDialogNewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storageDialogNewButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout storageDialogLayout = new javax.swing.GroupLayout(storageDialog.getContentPane());
        storageDialog.getContentPane().setLayout(storageDialogLayout);
        storageDialogLayout.setHorizontalGroup(
            storageDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(storageDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(storageDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(storageDialogLayout.createSequentialGroup()
                        .addComponent(storageLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, storageDialogLayout.createSequentialGroup()
                        .addGroup(storageDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(storageListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
                            .addGroup(storageDialogLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(storageDialogSelectButton)))
                        .addGroup(storageDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, storageDialogLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(storageDialogCancelButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, storageDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(storageDialogLayout.createSequentialGroup()
                                    .addGap(27, 27, 27)
                                    .addComponent(storageDialogEditButton))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, storageDialogLayout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(storageDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(storageDialogNewButton, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(storageDialogDeleteButton, javax.swing.GroupLayout.Alignment.TRAILING)))))))
                .addContainerGap())
        );
        storageDialogLayout.setVerticalGroup(
            storageDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(storageDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(storageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(storageDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(storageDialogLayout.createSequentialGroup()
                        .addComponent(storageListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addGroup(storageDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(storageDialogCancelButton)
                            .addComponent(storageDialogSelectButton)))
                    .addGroup(storageDialogLayout.createSequentialGroup()
                        .addComponent(storageDialogNewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(storageDialogEditButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(storageDialogDeleteButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(String.format("%s %s", APP_NAME, APP_VERSION) //generateApplicationWindowTitle not applicable
        );
        setBounds(new java.awt.Rectangle(0, 0, 800, 600));
        setPreferredSize(new java.awt.Dimension(800, 600));

        javax.swing.GroupLayout statusBarLayout = new javax.swing.GroupLayout(statusBar);
        statusBar.setLayout(statusBarLayout);
        statusBarLayout.setHorizontalGroup(
            statusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );
        statusBarLayout.setVerticalGroup(
            statusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );

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

        editEntryMenuItem.setText("Edit entry...");
        editEntryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editEntryMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(editEntryMenuItem);

        autoOCRValueDetectionMenuItem.setText("Auto OCR value detection...");
        fileMenu.add(autoOCRValueDetectionMenuItem);
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
            .addComponent(statusBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mainPanelPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mainPanelPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(statusBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        this.setVisible(false);
        this.close();
        this.shutdownHook();
        this.dispose();
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void selectScannerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectScannerButtonActionPerformed

    }//GEN-LAST:event_selectScannerButtonActionPerformed

    private void scanMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanMenuItemActionPerformed
        this.scan();
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
                    DocumentScannerConf.SCANNER_SANE_ADDRESS_DEFAULT);
        } catch (IOException | SaneException ex) {
            throw new RuntimeException(ex);
        }
        afterScannerSelection();
    }//GEN-LAST:event_selectScannerMenuItemActionPerformed

    private void storageSelectionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageSelectionMenuItemActionPerformed
        this.storageDialog.pack();
        this.storageDialog.setLocationRelativeTo(this);
        this.storageDialog.setVisible(true);
    }//GEN-LAST:event_storageSelectionMenuItemActionPerformed

    private void databaseConnectionURLTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_databaseConnectionURLTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_databaseConnectionURLTextFieldActionPerformed

    private void databaseConnectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_databaseConnectButtonActionPerformed
        try {
            this.databaseConnectionFailureLabel.setText("Connecting...");
            this.connectDatabase();
            this.databaseDialog.setVisible(false);
        } catch (ODatabaseException | OSecurityAccessException ex) {
            String message = ReflectionFormPanel.generateExceptionMessage(ex);
            this.databaseConnectionFailureLabel.setText(String.format("<html>The connection to the specified database with the specified credentials failed with the following error: %s</html>", message));
        }
    }//GEN-LAST:event_databaseConnectButtonActionPerformed

    private void databaseDialogWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_databaseDialogWindowClosed
        this.databaseConnectionFailureLabel.setText(" ");
    }//GEN-LAST:event_databaseDialogWindowClosed

    private void databaseCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_databaseCancelButtonActionPerformed
        this.databaseDialog.setVisible(false);
        this.databaseConnectionFailureLabel.setText(" ");
    }//GEN-LAST:event_databaseCancelButtonActionPerformed

    private void oCRMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_oCRMenuItemActionPerformed
        this.oCRDialog.pack();
        this.oCRDialog.setLocationRelativeTo(this);
        this.oCRDialog.setVisible(true);
    }//GEN-LAST:event_oCRMenuItemActionPerformed

    private void oCRDialogCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_oCRDialogCancelButtonActionPerformed
        this.oCRDialog.setVisible(false);
    }//GEN-LAST:event_oCRDialogCancelButtonActionPerformed

    private void oCRDialogSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_oCRDialogSaveButtonActionPerformed
        Class<? extends OCREngineConf<?>> oCREngineClass = this.oCRDialogEngineComboBox.getItemAt(this.oCRDialogEngineComboBox.getSelectedIndex());
        this.currentOCREngineConfPanel = this.oCREngineConfPanelMap.get(oCREngineClass);
        assert this.currentOCREngineConfPanel != null;
        this.oCRDialog.setVisible(false);
    }//GEN-LAST:event_oCRDialogSaveButtonActionPerformed

    private void storageCreateDialogTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageCreateDialogTypeComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_storageCreateDialogTypeComboBoxActionPerformed

    private void storageCreateDialogSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageCreateDialogSaveButtonActionPerformed
        Class<? extends StorageConf<?,?>> clazz = this.storageCreateDialogTypeComboBox.getItemAt(this.storageCreateDialogTypeComboBox.getSelectedIndex());
        StorageConfPanel<?> responsibleStorageConfPanel = this.storageConfPanelMap.get(clazz);
        StorageConf<?,?> createdStorageConf = responsibleStorageConfPanel.getStorageConf();
        this.storageListModel.addElement(createdStorageConf);
        this.documentScannerConf.getAvailableStorageConfs().add(createdStorageConf);
        this.documentScannerConf.setStorageConf(createdStorageConf);
    }//GEN-LAST:event_storageCreateDialogSaveButtonActionPerformed

    private void storageCreateDialogCancelDialogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageCreateDialogCancelDialogActionPerformed
        this.storageCreateDialog.setVisible(false);
    }//GEN-LAST:event_storageCreateDialogCancelDialogActionPerformed

    private void storageDialogCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageDialogCancelButtonActionPerformed
        this.storageDialog.setVisible(false);
    }//GEN-LAST:event_storageDialogCancelButtonActionPerformed

    private void storageDialogSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageDialogSelectButtonActionPerformed
        StorageConf<?,?> selectedStorage = this.storageList.getSelectedValue();
        assert selectedStorage != null;
        this.documentScannerConf.setStorageConf(selectedStorage);
    }//GEN-LAST:event_storageDialogSelectButtonActionPerformed

    private void storageDialogNewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageDialogNewButtonActionPerformed
        this.storageCreateDialog.pack();
        this.storageCreateDialog.setLocationRelativeTo(this);
        this.storageCreateDialog.setVisible(true);
    }//GEN-LAST:event_storageDialogNewButtonActionPerformed

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "PDF files", "pdf");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        final File selectedFile = chooser.getSelectedFile();
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        OCREngine oCREngine = DocumentScanner.this.retrieveOCREngine();
        if (oCREngine == null) {
            //a warning in form of a dialog has been given
            return;
        }
        try {
            List<BufferedImage> images = this.mainPanel.retrieveImages(selectedFile);
            if(images == null) {
                LOGGER.debug("image retrieval has been canceled, discontinuing adding document");
                return;
            }
            addDocument(images,
                    selectedFile);
        } catch (DocumentAddException | InterruptedException | ExecutionException ex) {
            handleException(ex, "Exception during adding new document");
        }
    }//GEN-LAST:event_openMenuItemActionPerformed

    private void optionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsMenuItemActionPerformed
        DocumentScannerConfDialog documentScannerConfDialog = new DocumentScannerConfDialog(this, documentScannerConf);
        documentScannerConfDialog.setVisible(true);
    }//GEN-LAST:event_optionsMenuItemActionPerformed

    private void editEntryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editEntryMenuItemActionPerformed
        EntityEditingDialog entityEditingDialog = new EntityEditingDialog(this,
                ENTITY_CLASSES,
                PRIMARY_CLASS_SELECTION,
                entityManager,
                messageHandler,
                confirmMessageHandler,
                idApplier,
                warningHandlers);
        entityEditingDialog.setVisible(true); //blocks
        List<Object> selectedEntities = entityEditingDialog.getSelectedEntities();
        if(selectedEntities.size() > SELECTED_ENTITIES_EDIT_WARNING) {
            int answer = JOptionPane.showConfirmDialog(this,
                    String.format("More than %d entities are supposed to be opened for editing. This might take a long time. Continue?", SELECTED_ENTITIES_EDIT_WARNING),
                    DocumentScanner.generateApplicationWindowTitle("Open documents?", APP_NAME, APP_VERSION),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if(answer != JOptionPane.NO_OPTION) {
                return;
            }
        }
        for(Object selectedEntity : selectedEntities) {
            try {
                addDocument(selectedEntity);
            } catch (DocumentAddException ex) {
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
        OCREngine oCREngine = DocumentScanner.this.retrieveOCREngine();
        if (oCREngine == null) {
            //a warning in form of a dialog has been given
            return;
        }
        try {
            List<BufferedImage> images = this.mainPanel.retrieveImages(selectedFile);
            if(images == null) {
                LOGGER.debug("image retrieval has been canceled, discontinuing adding document");
                return;
            }
            if(!images.isEmpty()) {
                final List<List<BufferedImage>> scannerResults = new LinkedList<>();
                JDialog invisibleWaitDialog = new JDialog();
                    //working with Object.wait and Object.notify fails due to
                    //java.lang.IllegalMonitorStateException
                invisibleWaitDialog.setBounds(0, 0, 1, 1);
                invisibleWaitDialog.setModal(true);
                invisibleWaitDialog.setUndecorated(true);
                if(this.javaFXInitPanel == null) {
                    this.javaFXInitPanel = new JFXPanel();
                        //necessary to initialize JavaFX and avoid
                        //failure of Platform.runLater with
                        //`java.lang.IllegalStateException: Toolkit not initialized`
                }
                Platform.runLater(() -> {
                    ScannerResultDialog scannerResultDialog = new ScannerResultDialog(images);
                    Optional<List<List<BufferedImage>>> dialogResult = scannerResultDialog.showAndWait();
                    if(dialogResult.isPresent()) {
                        scannerResults.addAll(scannerResultDialog.getResult());
                    }
                    invisibleWaitDialog.setVisible(false);
                });
                invisibleWaitDialog.setVisible(true);
                for(List<BufferedImage> scannerResult : scannerResults) {
                    addDocument(scannerResult,
                            null //selectedFile
                    );
                }
                //this.validate(); //not necessary
            }
        } catch (DocumentAddException | InterruptedException | ExecutionException ex) {
            handleException(ex, "Exception during adding new document");
        }
    }//GEN-LAST:event_openSelectionMenuItemActionPerformed

    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed
        this.mainPanel.removeActiveDocument();
        if(this.mainPanel.getDocumentCount() == 0) {
            this.closeMenuItem.setEnabled(false);
        }
    }//GEN-LAST:event_closeMenuItemActionPerformed

    private void addDocument(List<BufferedImage> images,
            File selectedFile) throws DocumentAddException {
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
    }

    private void addDocument(Object entityToEdit) throws DocumentAddException {
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
    }

    /**
     * connects to an OrientDB database using the current values of the text
     * properties of the text inputs in the database connection dialog. Expect a
     * {@link ODatabaseException} if the connection doesn't succeed due to an
     * errornous URL or wrong credentials. Excepts the remote engine to be on
     * the classpath (usually provided by orientdb-client module (in doubt check
     * with {@code Orient.instance().registerEngine(new OEngineRemote());}).
     *
     * @throws ODatabaseException if opening the connection pointed to by the
     * connection URL fails
     * @throws OSecurityAccessException if the credentials are wrong
     */
    private void connectDatabase() {
        String connectionURL = this.databaseConnectionURLTextField.getText();
        this.db = new ODatabaseDocumentTx(connectionURL);
        String username = this.databaseUsernameTextField.getText();
        String password = new String(this.databasePasswordTextField.getPassword());
        this.db.open(username, password);
        this.saveMenuItem.setEnabled(true);
        this.saveMenuItem.getParent().revalidate();
    }

    /**
     * Different document sources of the scanner require different scann
     * routines, e.g. calling {@link SaneDevice#acquireImage() } until a
     * {@link SaneException} with {@link SaneStatus#STATUS_NO_DOCS} occurs for
     * an ADF.
     * @return {@code true} if the loop described above has to be used for
     * scanning
     */
    private boolean scannerDocumentSourceRequiresLoop() throws IOException, SaneException {
        String documentSource = this.scannerDevice.getOption(ScannerEditDialog.DOCUMENT_SOURCE_OPTION_NAME).getStringValue();
        boolean retValue = documentSource.equalsIgnoreCase("ADF")
                || documentSource.equalsIgnoreCase("Automatic document feeder")
                || documentSource.equalsIgnoreCase("Duplex");
        return retValue;
    }

    private void scan() {
        assert this.scannerDevice != null;
        try {
            if(!this.scannerDevice.isOpen()) {
                this.scannerDevice.open();
            }
            List<BufferedImage> scannedImages = new LinkedList<>();
            if(scannerDocumentSourceRequiresLoop()) {
                //using ADF according to https://github.com/sjamesr/jfreesane/blob/master/README.md
                ScannerPageSelectDialog scannerPageSelectDialog = new ScannerPageSelectDialog(this);
                scannerPageSelectDialog.setVisible(true);
                if(scannerPageSelectDialog.isCanceled()) {
                    return;
                }
                if(scannerPageSelectDialog.isScanAll()) {
                    while (true) {
                        try {
                            BufferedImage scannedImage = scannerDevice.acquireImage();
                            scannedImages.add(scannedImage);
                        } catch (SaneException e) {
                            if (e.getStatus() == SaneStatus.STATUS_NO_DOCS) {
                                // this is the out of paper condition that we expect
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
                            scannedImages.add(scannedImage);
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
            }else {
                BufferedImage scannedImage = this.scannerDevice.acquireImage();
                scannedImages.add(scannedImage);
            }
            if(!scannedImages.isEmpty()) {
                final List<List<BufferedImage>> scannerResults = new LinkedList<>();
                JDialog invisibleWaitDialog = new JDialog();
                    //working with Object.wait and Object.notify fails due to
                    //java.lang.IllegalMonitorStateException
                invisibleWaitDialog.setBounds(0, 0, 1, 1);
                invisibleWaitDialog.setModal(true);
                invisibleWaitDialog.setUndecorated(true);
                new JFXPanel(); //necessary to initialize JavaFX and avoid
                    //failure of Platform.runLater with
                    //`java.lang.IllegalStateException: Toolkit not initialized`
                Platform.runLater(() -> {
                    ScannerResultDialog scannerResultDialog = new ScannerResultDialog(scannedImages);
                    Optional<List<List<BufferedImage>>> dialogResult = scannerResultDialog.showAndWait();
                    if(dialogResult.isPresent()) {
                        scannerResults.addAll(scannerResultDialog.getResult());
                    }
                    invisibleWaitDialog.setVisible(false);
                });
                invisibleWaitDialog.setVisible(true);
                for(List<BufferedImage> scannerResult : scannerResults) {
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
                "Exception occured"));
    }

    private OCREngine retrieveOCREngine() {
        if (this.documentScannerConf.getoCREngineConf() == null) {
            JOptionPane.showMessageDialog(this, //parent
                    "OCREngine isn't set up",
                    DocumentScanner.generateApplicationWindowTitle("Warning", APP_NAME, APP_VERSION),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return this.documentScannerConf.getoCREngineConf().getOCREngine();
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
                try {
                    documentScanner = new DocumentScanner();
                    documentScanner.init();
                    documentScanner.setVisible(true); //all shutdown and
                        //resource closing routines need to be handled in event
                        //handling and shutdown hooks since this doesn't block
                        //and there's no way of acchieving that without trouble
                } catch (BinaryNotFoundException ex) {
                    String message = "The tesseract binary isn't available. Install it on your system and make sure it's executable (in doubt check if tesseract runs on the console)";
                    LOGGER.error(message);
                    JOptionPane.showMessageDialog(null, //parent
                            message,
                            DocumentScanner.generateApplicationWindowTitle("tesseract binary missing", APP_NAME, APP_VERSION),
                            JOptionPane.ERROR_MESSAGE);
                    if(documentScanner != null) {
                        documentScanner.setVisible(false);
                        documentScanner.close();
                        documentScanner.shutdownHook();
                        documentScanner.dispose();
                    }
                } catch(DerbyPersistenceStorageConfInitializationException ex) {
                    String message = String.format("A change to the metamodel has occured and the database scheme needs to be adjusted externally. It might help to store the entities in an XML file, open the XML file and store the entities in the new format. If you're sure you know what you're doing, consider removing the old scheme checksum file '%s' and restart the application.",
                            ex.getSchemeChecksumFile().getAbsolutePath());
                    LOGGER.error(message, ex);
                    JOptionPane.showMessageDialog(null, //parent
                            message,
                            DocumentScanner.generateApplicationWindowTitle("unexpected exception occurred", APP_NAME, APP_VERSION),
                            JOptionPane.ERROR_MESSAGE);
                    if(documentScanner != null) {
                        documentScanner.setVisible(false);
                        documentScanner.close();
                        documentScanner.shutdownHook();
                        documentScanner.dispose();
                    }
                } catch(Exception ex) {
                    String message = String.format("The unexpected exception '%s' occured", ExceptionUtils.getRootCauseMessage(ex));
                    LOGGER.error(message, ex);
                    JOptionPane.showMessageDialog(null, //parent
                            message,
                            DocumentScanner.generateApplicationWindowTitle("unexpected exception occurred", APP_NAME, APP_VERSION),
                            JOptionPane.ERROR_MESSAGE);
                    if(documentScanner != null) {
                        documentScanner.setVisible(false);
                        documentScanner.close();
                        documentScanner.shutdownHook();
                        documentScanner.dispose();
                    }
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem autoOCRValueDetectionMenuItem;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JButton databaseCancelButton;
    private javax.swing.JButton databaseConnectButton;
    private javax.swing.JLabel databaseConnectionFailureLabel;
    private javax.swing.JLabel databaseConnectionURLLabel;
    private javax.swing.JTextField databaseConnectionURLTextField;
    private javax.swing.JDialog databaseDialog;
    private javax.swing.JPopupMenu.Separator databaseMenuSeparator;
    private javax.swing.JLabel databasePasswordLabel;
    private javax.swing.JPasswordField databasePasswordTextField;
    private javax.swing.JLabel databaseUsernameLabel;
    private javax.swing.JTextField databaseUsernameTextField;
    private javax.swing.JMenuItem editEntryMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JPopupMenu.Separator exitMenuItemSeparator;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPopupMenu.Separator knownScannersMenuItemSeparator;
    private javax.swing.JPopupMenu.Separator knownStoragesMenuItemSeparartor;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JPanel mainPanelPanel;
    private javax.swing.JDialog oCRDialog;
    private javax.swing.JButton oCRDialogCancelButton;
    private javax.swing.JComboBox<Class<? extends OCREngineConf<?>>> oCRDialogEngineComboBox;
    private javax.swing.JLabel oCRDialogEngineLabel;
    private javax.swing.JPanel oCRDialogPanel;
    private javax.swing.JButton oCRDialogSaveButton;
    private javax.swing.JSeparator oCRDialogSeparator;
    private javax.swing.JMenuItem oCRMenuItem;
    private javax.swing.JPopupMenu.Separator oCRMenuSeparator;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem openSelectionMenuItem;
    private javax.swing.JMenuItem optionsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenuItem scanMenuItem;
    private javax.swing.JLabel scannerLabel;
    private javax.swing.JMenu scannerSelectionMenu;
    private javax.swing.JButton selectScannerButton;
    private javax.swing.JMenuItem selectScannerMenuItem;
    private javax.swing.JPanel statusBar;
    private javax.swing.JDialog storageCreateDialog;
    private javax.swing.JButton storageCreateDialogCancelDialog;
    private javax.swing.JLabel storageCreateDialogNameLabel;
    private javax.swing.JTextField storageCreateDialogNameTextField;
    private javax.swing.JPanel storageCreateDialogPanel;
    private javax.swing.JButton storageCreateDialogSaveButton;
    private javax.swing.JSeparator storageCreateDialogSeparator;
    private javax.swing.JComboBox<Class<? extends StorageConf<?,?>>> storageCreateDialogTypeComboBox;
    private javax.swing.JLabel storageCreateDialogTypeLabel;
    private javax.swing.JDialog storageDialog;
    private javax.swing.JButton storageDialogCancelButton;
    private javax.swing.JButton storageDialogDeleteButton;
    private javax.swing.JButton storageDialogEditButton;
    private javax.swing.JButton storageDialogNewButton;
    private javax.swing.JButton storageDialogSelectButton;
    private javax.swing.JLabel storageLabel;
    private javax.swing.JList<StorageConf<?,?>> storageList;
    private javax.swing.JScrollPane storageListScrollPane;
    private javax.swing.JMenu storageSelectionMenu;
    private javax.swing.JMenuItem storageSelectionMenuItem;
    private javax.swing.JMenu toolsMenu;
    // End of variables declaration//GEN-END:variables
}
