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
package richtercloud.document.scanner.gui;

import au.com.southsky.jfreesane.OptionValueType;
import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneException;
import au.com.southsky.jfreesane.SaneOption;
import au.com.southsky.jfreesane.SaneSession;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.orientechnologies.orient.core.exception.OSecurityAccessException;
import java.awt.HeadlessException;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math4.stat.descriptive.DescriptiveStatistics;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.components.OCRResultPanel;
import richtercloud.document.scanner.components.OCRResultPanelFetcher;
import richtercloud.document.scanner.components.ScanResultPanelFetcher;
import richtercloud.document.scanner.components.annotations.OCRResult;
import richtercloud.document.scanner.components.annotations.ScanResult;
import richtercloud.document.scanner.gui.conf.DerbyPersistenceStorageConf;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.gui.conf.OCREngineConf;
import richtercloud.document.scanner.gui.conf.StorageConf;
import richtercloud.document.scanner.gui.conf.TesseractOCREngineConf;
import richtercloud.document.scanner.gui.engineconf.OCREngineConfPanel;
import richtercloud.document.scanner.gui.storageconf.StorageConfPanel;
import richtercloud.document.scanner.model.APackage;
import richtercloud.document.scanner.model.Bill;
import richtercloud.document.scanner.model.Company;
import richtercloud.document.scanner.model.Document;
import richtercloud.document.scanner.model.EmailAddress;
import richtercloud.document.scanner.model.Employment;
import richtercloud.document.scanner.model.FinanceAccount;
import richtercloud.document.scanner.model.Leaflet;
import richtercloud.document.scanner.model.Payment;
import richtercloud.document.scanner.model.Person;
import richtercloud.document.scanner.model.Shipping;
import richtercloud.document.scanner.model.TelephoneCall;
import richtercloud.document.scanner.model.TransportTicket;
import richtercloud.document.scanner.ocr.OCREngine;
import richtercloud.document.scanner.retriever.OCRResultPanelRetriever;
import richtercloud.document.scanner.setter.IdPanelSetter;
import richtercloud.document.scanner.setter.SpinnerSetter;
import richtercloud.document.scanner.setter.TextFieldSetter;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.reflection.form.builder.ClassAnnotationHandler;
import richtercloud.reflection.form.builder.FieldAnnotationHandler;
import richtercloud.reflection.form.builder.FieldHandler;
import richtercloud.reflection.form.builder.ReflectionFormPanel;
import richtercloud.reflection.form.builder.jpa.IdPanel;
import richtercloud.reflection.form.builder.jpa.JPAReflectionFormBuilder;
import richtercloud.reflection.form.builder.retriever.ValueRetriever;

/**
 * <h2>Status bar</h2>
 * In order to provide static status for a selected (and eventually configurable) set of components (selected scanner, image selection mode, etc.) and dynamic info, warning and error message, a status bar (at the bottom of the main application
 * window is introduced which is a container and contains a subcontainer for each static information and one for the messages. The latter is a popup displaying a scrollable list of message entries which can be removed from it (e.g. with a close button). Static status subcontainer can be sophisticated, e.g. the display for the currently selected scanner can contain a button to select a scanner while none is selected an be a label only while one is selected. That's why they're containers. The difference between static information and messages is trivial.
 *
 * @author richter
 */
/*
internal implementation notes:
- the combobox in the storage create dialog for selection of the type of the new
storage doesn't have to be a StorageConf instance and it's misleading if it is
because such a StorageConf is about to be created -> use Class
*/
public class DocumentScanner extends javax.swing.JFrame {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentScanner.class);
    private static final int ORIENTDB_PORT_DEFAULT = 2_424;
    private static final String CONNECTION_URL_EXAMPLE = "remote:localhost/GratefulDeadConcerts";
    private static final String CONNECTION_URL_TOOLTIP_TEXT = String.format("[mode]:[path] (where mode is one of <b>remote</b>, <b>plocal</b> or <b>??</b> and path is in the form [IP or hostname]/[database name], e.g. %s)", CONNECTION_URL_EXAMPLE);
    private static final String APP_NAME = "Document scanner";
    private static final String APP_VERSION = "1.0";
    private static final String UNSAVED_NAME = "unsaved";
    private static final String SCANNER_ADDRESS_DEFAULT = "localhost";
    private SaneDevice device;
    private ODatabaseDocumentTx db;
    private final DefaultTableModel scannerDialogTableModel = new DefaultTableModel();
    private SaneSession scannerDialogSaneSession;
    private final TableModelListener scannerDialogTableModelListener = new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent e) {
            DocumentScanner.this.scannerDialogSelectButton.setEnabled(DocumentScanner.this.scannerDialogSelectButtonEnabled());
        }
    };
    private final ListSelectionListener scannerDialogTableSelectionListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            DocumentScanner.this.scannerDialogSelectButton.setEnabled(DocumentScanner.this.scannerDialogSelectButtonEnabled());
        }
    };
    private final MutableComboBoxModel<Class<? extends OCREngineConf<?>>> oCREngineComboBoxModel = new DefaultComboBoxModel<>();
    private OCREngineConfPanel<?> currentOCREngineConfPanel;
    //@TODO: implement class path discovery of associated conf panel with annotations
    private final TesseractOCREngineConfPanel tesseractOCREngineConfPanel;
    private final Map<Class<? extends OCREngineConf<?>>, OCREngineConfPanel<?>> oCREngineConfPanelMap = new HashMap<>();
    private final static int RESOLUTION_DEFAULT = 150;
    private static final String CONFIG_DIR_NAME = ".document-scanner";
    private final static String CONFIG_FILE_NAME = "document-scanner-config.xml";
    private final File configFile;
    private MutableComboBoxModel<Class<? extends StorageConf<?>>> storageCreateDialogTypeComboBoxModel = new DefaultComboBoxModel<>();
    private Map<Class<? extends StorageConf<?>>, StorageConfPanel<?>> storageConfPanelMap = new HashMap<>();
    private DefaultListModel<StorageConf<?>> storageListModel = new DefaultListModel<>();
    private final static Set<Class<?>> ENTITY_CLASSES = Collections.unmodifiableSet(new HashSet<Class<?>>(
            Arrays.asList(Bill.class,
            Company.class,
            Document.class,
            EmailAddress.class,
            Employment.class,
            FinanceAccount.class,
            Leaflet.class,
            APackage.class,
            Payment.class,
            Person.class,
            Shipping.class,
            TelephoneCall.class,
            TransportTicket.class)));
    public static final String DATABASE_DIR_NAME_DEFAULT = "databases";
    public static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory("richtercloud_document-scanner_jar_1.0-SNAPSHOTPU");
    private EntityManager entityManager;
    private Connection conn;
    private final static int EXIT_SUCCESS = 0;
    private DocumentScannerConf conf;
    private boolean debug = false;
    private DocumentScannerCommandParser cmd = new DocumentScannerCommandParser();
    private final static String PROPERTY_KEY_DEBUG = "document.scanner.debug";
    public final static Map<java.lang.reflect.Type, FieldHandler> CLASS_MAPPING_DEFAULT;
    static {
        Map<java.lang.reflect.Type, FieldHandler> classMappingDefault = new HashMap<>(JPAReflectionFormBuilder.CLASS_MAPPING_DEFAULT);
        CLASS_MAPPING_DEFAULT = Collections.unmodifiableMap(classMappingDefault);
    }
    public final static Map<Class<?>, Class<? extends JComponent>> PRIMITIVE_MAPPING_DEFAULT;
    static {
        Map<Class<?>, Class<? extends JComponent>> primitiveMappingDefault = new HashMap<>(JPAReflectionFormBuilder.PRIMITIVE_MAPPING_DEFAULT);
        PRIMITIVE_MAPPING_DEFAULT = Collections.unmodifiableMap(primitiveMappingDefault);
    }
    public final static Map<Class<? extends JComponent>, ValueRetriever<?,?>> VALUE_RETRIEVER_MAPPING_DEFAULT;
    static {
        Map<Class<? extends JComponent>, ValueRetriever<?,?>> valueRetrieverMappingDefault = new HashMap<>(JPAReflectionFormBuilder.VALUE_RETRIEVER_MAPPING_JPA_DEFAULT);
        valueRetrieverMappingDefault.put(OCRResultPanel.class, OCRResultPanelRetriever.getInstance());
        VALUE_RETRIEVER_MAPPING_DEFAULT = valueRetrieverMappingDefault;
    }
    public final static Map<Class<? extends JComponent>, ValueSetter<?>> VALUE_SETTER_MAPPING_DEFAULT;
    static {
        Map<Class<? extends JComponent>, ValueSetter<?>>  valueSetterMappingDefault = new HashMap<>();
        valueSetterMappingDefault.put(JTextField.class, TextFieldSetter.getInstance());
        valueSetterMappingDefault.put(JSpinner.class, SpinnerSetter.getInstance());
        valueSetterMappingDefault.put(IdPanel.class, IdPanelSetter.getInstance());
        VALUE_SETTER_MAPPING_DEFAULT = valueSetterMappingDefault;
    }

    public static String generateApplicationWindowTitle(String title) {
        return String.format("%s - %s %s", title, APP_NAME, APP_VERSION);
    }

    /**
     * Parses the command line and evaluates system properties. Command line
     * arguments superseed properties.
     */
    private void parseArguments() {
        if(this.cmd.isDebug() != null) {
            this.debug = this.cmd.isDebug();
        }else {
            String debugProp = System.getProperty(PROPERTY_KEY_DEBUG);
            if(debugProp != null) {
                this.debug = Boolean.valueOf(debugProp);
            }
        }

        if(this.debug) {
            LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
            ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.DEBUG);
        }
    }

    /**
     * expected {@code conf} to be filled from configuration file
     */
    private void loadProperties() {
        XMLDecoder xMLDecoder;
        try {
            xMLDecoder = new XMLDecoder(new FileInputStream(this.configFile));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        this.conf = (DocumentScannerConf) xMLDecoder.readObject();
        try {
            InetAddress address = InetAddress.getByName(this.conf.getScannerSaneAddress());
            this.scannerDialogSaneSession = SaneSession.withRemoteSane(address);
            this.device = this.scannerDialogSaneSession.getDevice(this.conf.getScannerName());
        }catch(IOException ex) {
            this.handleException(ex);
        }
    }

    private boolean scannerDialogSelectButtonEnabled() {
        return this.scannerDialogTableModel.getDataVector().size() > 0
                && this.scannerDialogTable.getSelectedRowCount() > 0;
    }

    private void onDeviceSet() {
        assert this.device != null;
        this.scannerLabel.setText(this.device.toString());
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
        assert this.device == null;
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

    /**
     * set all closed resource references to {@code null} in order to allow this
     * to be run again by shutdown hook
     */
    private void shutdownHook() {
        LOGGER.info("running {} shutdown hooks", DocumentScanner.class);
        if(this.device != null && this.device.isOpen()) {
            try {
                this.device.close();
            } catch (IOException ex) {
                LOGGER.error("an exception during shutdown of scanner device occured", ex);
            }
            this.device = null;
        }
        if(this.db != null) {
            this.db.close();
            this.db = null;
        }
        if(this.conf != null) {
            try {
                try (XMLEncoder xMLEncoder = new XMLEncoder(new FileOutputStream(this.configFile))) {
                    xMLEncoder.writeObject(this.conf);
                    xMLEncoder.flush();
                }
            } catch (FileNotFoundException ex) {
                LOGGER.warn("an unexpected exception occured during save of configurations into file '{}', changes most likely lost", this.configFile.getAbsolutePath());
            }
            this.conf = null;
        }
        if(DocumentScanner.this.conn != null) {
            try {
                DocumentScanner.this.conn.commit();
                DocumentScanner.this.conn.close();
                DriverManager.getConnection(String.format("%s;shutdown=true", DERBY_CONNECTION_URL));
                DocumentScanner.this.conn = null;
            } catch (SQLException ex) {
                LOGGER.error("an exception during shutdown of the database connection occured", ex);
            }
        }
    }

    private final static File HOME_DIR = new File(System.getProperty("user.home"));
    private final static File CONFIG_DIR = new File(HOME_DIR, CONFIG_DIR_NAME);
    private final static File DATABASE_DIR = new File(CONFIG_DIR, DATABASE_DIR_NAME_DEFAULT);
    private final static String DERBY_CONNECTION_URL = String.format("jdbc:derby:%s", DATABASE_DIR.getAbsolutePath());

    /**
     * Creates new form OrientdbDocumentScanner
     * @throws richtercloud.document.scanner.gui.TesseractNotFoundException
     */
    public DocumentScanner() throws TesseractNotFoundException {
        this.parseArguments();
        assert HOME_DIR.exists();
        if(!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdir();
            LOGGER.info("created inexisting configuration directory '{}'", CONFIG_DIR_NAME);
        }

        Class<?> driver = EmbeddedDriver.class;
        try {
            driver.newInstance();
            this.conn = DriverManager.getConnection(String.format("%s;create=%s", DERBY_CONNECTION_URL, !DATABASE_DIR.exists()));
            this.entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
        } catch (InstantiationException | IllegalAccessException | SQLException ex) {
            throw new RuntimeException(ex);
        }

        this.initComponents();

        //loading properties depends on initComponents because exceptions are
        //handled with GUI elements
        this.configFile = new File(CONFIG_DIR, CONFIG_FILE_NAME);
        if(this.configFile.exists()) {
            this.loadProperties(); //initializes this.conf
        }else {
            this.conf = new DocumentScannerConf();
            LOGGER.info("no previous configuration found in configuration directry '{}', using default values", CONFIG_DIR.getAbsolutePath());
        }

        this.onDeviceUnset();
        this.scannerDialogTableModel.addColumn("Name");
        this.scannerDialogTableModel.addColumn("Model");
        this.scannerDialogTableModel.addColumn("Type");
        this.scannerDialogTableModel.addColumn("Vendor");
        this.scannerDialogTableModel.addTableModelListener(this.scannerDialogTableModelListener);
        this.scannerDialogTable.getSelectionModel().addListSelectionListener(this.scannerDialogTableSelectionListener);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOGGER.info("running {} shutdown hooks", DocumentScanner.class);
                DocumentScanner.this.shutdownHook();
            }
        });
        try {
            this.tesseractOCREngineConfPanel = new TesseractOCREngineConfPanel();
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
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
        DerbyPersistenceStorageConfPanel derbyStorageConfPanel;
        derbyStorageConfPanel = new DerbyPersistenceStorageConfPanel(); //@TODO: replace with classpath annotation discovery
        this.storageConfPanelMap.put(DerbyPersistenceStorageConf.class, derbyStorageConfPanel);
        this.storageCreateDialogTypeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Class<? extends StorageConf<?>> clazz = (Class<? extends StorageConf<?>>) e.getItem();
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
        scannerDialog = new javax.swing.JDialog();
        scannerDialogAddressTextField = new javax.swing.JTextField();
        scannerDialogAddressLabel = new javax.swing.JLabel();
        scannerDialogSearchButton = new javax.swing.JButton();
        scannerDialogSeparator = new javax.swing.JSeparator();
        scannerDialogScrollPane = new javax.swing.JScrollPane();
        scannerDialogTable = new javax.swing.JTable();
        scannerDialogCancelButton = new javax.swing.JButton();
        scannerDialogSelectButton = new javax.swing.JButton();
        scannerDialogStatusLabel = new javax.swing.JLabel();
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
        oCRDialogEngineComboBox = new javax.swing.JComboBox<Class<? extends OCREngineConf<?>>>();
        oCRDialogEngineLabel = new javax.swing.JLabel();
        oCRDialogSeparator = new javax.swing.JSeparator();
        oCRDialogPanel = new javax.swing.JPanel();
        oCRDialogCancelButton = new javax.swing.JButton();
        oCRDialogSaveButton = new javax.swing.JButton();
        storageCreateDialog = new javax.swing.JDialog();
        storageCreateDialogNameTextField = new javax.swing.JTextField();
        storageCreateDialogNameLabel = new javax.swing.JLabel();
        storageCreateDialogTypeComboBox = new javax.swing.JComboBox<Class<? extends StorageConf<?>>>();
        storageCreateDialogTypeLabel = new javax.swing.JLabel();
        storageCreateDialogCancelDialog = new javax.swing.JButton();
        storageCreateDialogSaveButton = new javax.swing.JButton();
        storageCreateDialogSeparator = new javax.swing.JSeparator();
        storageCreateDialogPanel = new javax.swing.JPanel();
        storageDialog = new javax.swing.JDialog();
        storageLabel = new javax.swing.JLabel();
        storageListScrollPane = new javax.swing.JScrollPane();
        storageList = new javax.swing.JList<StorageConf<?>>();
        storageDialogCancelButton = new javax.swing.JButton();
        storageDialogSelectButton = new javax.swing.JButton();
        storageDialogEditButton = new javax.swing.JButton();
        storageDialogDeleteButton = new javax.swing.JButton();
        storageDialogNewButton = new javax.swing.JButton();
        statusBar = new javax.swing.JPanel();
        mainTabbedPane = new javax.swing.JTabbedPane();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        scannerSelectionMenu = new javax.swing.JMenu();
        selectScannerMenuItem = new javax.swing.JMenuItem();
        knownScannersMenuItemSeparator = new javax.swing.JPopupMenu.Separator();
        scanMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
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

        scannerLabel.setText("No scanner selected");

        selectScannerButton.setText("Select Scanner");
        selectScannerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectScannerButtonActionPerformed(evt);
            }
        });

        scannerDialog.setTitle(DocumentScanner.generateApplicationWindowTitle("Select scanner"));
        scannerDialog.setModal(true);

        scannerDialogAddressTextField.setText(SCANNER_ADDRESS_DEFAULT);

        scannerDialogAddressLabel.setText("Address");

        scannerDialogSearchButton.setText("Search");
        scannerDialogSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scannerDialogSearchButtonActionPerformed(evt);
            }
        });

        scannerDialogTable.setModel(scannerDialogTableModel);
        scannerDialogScrollPane.setViewportView(scannerDialogTable);

        scannerDialogCancelButton.setText("Cancel");
        scannerDialogCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scannerDialogCancelButtonActionPerformed(evt);
            }
        });

        scannerDialogSelectButton.setText("Select scanner");
        scannerDialogSelectButton.setEnabled(false);
        scannerDialogSelectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scannerDialogSelectButtonActionPerformed(evt);
            }
        });

        scannerDialogStatusLabel.setText(" ");

        javax.swing.GroupLayout scannerDialogLayout = new javax.swing.GroupLayout(scannerDialog.getContentPane());
        scannerDialog.getContentPane().setLayout(scannerDialogLayout);
        scannerDialogLayout.setHorizontalGroup(
            scannerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scannerDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(scannerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scannerDialogLayout.createSequentialGroup()
                        .addComponent(scannerDialogAddressLabel)
                        .addGap(18, 18, 18)
                        .addComponent(scannerDialogAddressTextField))
                    .addComponent(scannerDialogSeparator)
                    .addComponent(scannerDialogScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scannerDialogLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(scannerDialogSelectButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scannerDialogCancelButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scannerDialogLayout.createSequentialGroup()
                        .addComponent(scannerDialogStatusLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(scannerDialogSearchButton)))
                .addContainerGap())
        );
        scannerDialogLayout.setVerticalGroup(
            scannerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scannerDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(scannerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scannerDialogAddressTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scannerDialogAddressLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(scannerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scannerDialogSearchButton)
                    .addComponent(scannerDialogStatusLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scannerDialogSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scannerDialogScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(scannerDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scannerDialogCancelButton)
                    .addComponent(scannerDialogSelectButton))
                .addContainerGap())
        );

        databaseDialog.setTitle(DocumentScanner.generateApplicationWindowTitle("Connect to database"));
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

        oCRDialog.setModal(true);

        oCRDialogEngineComboBox.setModel(oCREngineComboBoxModel);

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
        setTitle(String.format("%s %s", APP_NAME, APP_VERSION));
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));

        javax.swing.GroupLayout statusBarLayout = new javax.swing.GroupLayout(statusBar);
        statusBar.setLayout(statusBarLayout);
        statusBarLayout.setHorizontalGroup(
            statusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 428, Short.MAX_VALUE)
        );
        statusBarLayout.setVerticalGroup(
            statusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );

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

        setJMenuBar(mainMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mainTabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mainTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(statusBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        this.shutdownHook();
        this.setVisible(false);
        this.dispose();
        System.exit(EXIT_SUCCESS);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void selectScannerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectScannerButtonActionPerformed

    }//GEN-LAST:event_selectScannerButtonActionPerformed

    private void scanMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanMenuItemActionPerformed
        this.scan();
    }//GEN-LAST:event_scanMenuItemActionPerformed

    private void selectScannerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectScannerMenuItemActionPerformed
        this.scannerDialog.pack();
        this.scannerDialog.setLocationRelativeTo(this);
        this.scannerDialog.setVisible(true);
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
        } catch(ODatabaseException | OSecurityAccessException ex) {
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

    private void scannerDialogCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scannerDialogCancelButtonActionPerformed
        this.scannerDialog.setVisible(false);
    }//GEN-LAST:event_scannerDialogCancelButtonActionPerformed

    private void scannerDialogSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scannerDialogSelectButtonActionPerformed
        this.selectScanner();
        this.scannerDialog.setVisible(false);
    }//GEN-LAST:event_scannerDialogSelectButtonActionPerformed

    private void scannerDialogSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scannerDialogSearchButtonActionPerformed
        this.searchScanner();
    }//GEN-LAST:event_scannerDialogSearchButtonActionPerformed

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
        Class<? extends StorageConf<?>> clazz = this.storageCreateDialogTypeComboBox.getItemAt(this.storageCreateDialogTypeComboBox.getSelectedIndex());
        StorageConfPanel<?> responsibleStorageConfPanel = this.storageConfPanelMap.get(clazz);
        StorageConf<?> createdStorageConf = responsibleStorageConfPanel.getStorageConf();
        this.storageListModel.addElement(createdStorageConf);
        this.conf.setStorageConf(createdStorageConf);
    }//GEN-LAST:event_storageCreateDialogSaveButtonActionPerformed

    private void storageCreateDialogCancelDialogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageCreateDialogCancelDialogActionPerformed
        this.storageCreateDialog.setVisible(false);
    }//GEN-LAST:event_storageCreateDialogCancelDialogActionPerformed

    private void storageDialogCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageDialogCancelButtonActionPerformed
        this.storageDialog.setVisible(false);
    }//GEN-LAST:event_storageDialogCancelButtonActionPerformed

    private void storageDialogSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageDialogSelectButtonActionPerformed
        StorageConf<?> selectedStorage = this.storageList.getSelectedValue();
        assert selectedStorage != null;
        this.conf.setStorageConf(selectedStorage);
    }//GEN-LAST:event_storageDialogSelectButtonActionPerformed

    private void storageDialogNewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageDialogNewButtonActionPerformed
        this.storageCreateDialog.pack();
        this.storageCreateDialog.setLocationRelativeTo(this);
        this.storageCreateDialog.setVisible(true);
    }//GEN-LAST:event_storageDialogNewButtonActionPerformed

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        InputStream pdfInputStream = null;
        try {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "PDF files", "pdf");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(this);
            File selectedFile = chooser.getSelectedFile();
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                pdfInputStream = new FileInputStream(selectedFile);
            }
            PDDocument document = PDDocument.load(pdfInputStream);
            @SuppressWarnings("unchecked")
            List<PDPage> pages = document.getDocumentCatalog().getAllPages();
            List<OCRSelectPanel> panels = new LinkedList<>();
            for(PDPage page : pages) {
                BufferedImage image = page.convertToImage();
                @SuppressWarnings("serial")
                OCRSelectPanel panel = new OCRSelectPanel(image) {
                    @Override
                    public void mouseReleased(MouseEvent evt) {
                        super.mouseReleased(evt);
                        if(this.getDragStart() != null && !this.getDragStart().equals(this.getDragEnd())) {
                            DocumentScanner.this.handleOCRSelection();
                        }
                    }
                };
                panels.add(panel);
            }
            document.close();
            OCRSelectComponent oCRSelectComponent = new OCRSelectComponent(panels);
            OCREngine oCREngine = this.retrieveOCREninge();
            if(oCREngine == null) {
                //a warning in form of a dialog has been given
                return;
            }
            OCRResultPanelFetcher oCRResultPanelFetcher = new DocumentTabOCRResultPanelFetcher(oCRSelectComponent, oCREngine);
            ScanResultPanelFetcher scanResultPanelFetcher = new DocumentTabScanResultPanelFetcher(oCRSelectComponent);
            List<Pair<Class<? extends Annotation>, FieldAnnotationHandler>> fieldAnnotationMapping = new LinkedList<>(JPAReflectionFormBuilder.JPA_FIELD_ANNOTATION_MAPPING_DEFAULT);
            fieldAnnotationMapping.add(new ImmutablePair<Class<? extends Annotation>, FieldAnnotationHandler>(OCRResult.class, new OCRResultFieldAnnotationHandler(oCRResultPanelFetcher)));
            fieldAnnotationMapping.add(new ImmutablePair<Class<? extends Annotation>, FieldAnnotationHandler>(ScanResult.class, new ScanResultFieldAnnotationHandler(scanResultPanelFetcher)));
            List<Pair<Class<? extends Annotation>, ClassAnnotationHandler>> classAnnotationMapping = new LinkedList<>(JPAReflectionFormBuilder.CLASS_ANNOTATION_MAPPING_DEFAULT);
            DocumentTab documentTab = new DocumentTab(selectedFile.getName(),
                    oCRSelectComponent,
                    oCREngine,
                    DocumentScanner.ENTITY_CLASSES,
                    this.entityManager,
                    fieldAnnotationMapping,
                    classAnnotationMapping,
                    oCRResultPanelFetcher,
                    scanResultPanelFetcher);
            this.mainTabbedPane.add(selectedFile.getName(), documentTab);
            this.validate();
        }catch(HeadlessException | IOException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
            this.handleException(ex);
        }
    }//GEN-LAST:event_openMenuItemActionPerformed

    /**
     * for code reusage in {@link #searchScanner() }
     * @param ex
     * @param additional
     */
    private void handleSearchScannerException(Exception ex, String additional) {
        String message = ex.getMessage();
        if(ex.getCause() != null) {
            message = String.format("%s (caused by '%s')", message, ex.getCause().getMessage());
        }
        this.scannerDialogStatusLabel.setText(String.format("<html>The search at the specified address failed with the following error: %s%s</html>", message, additional));
    }

    private void searchScanner() {
        String addressString = this.scannerDialogAddressTextField.getText();
        InetAddress address;
        this.scannerDialogStatusLabel.setText("Searching...");
        try {
            address = InetAddress.getByName(addressString);
            this.scannerDialogSaneSession = SaneSession.withRemoteSane(address);
            List<SaneDevice> availableDevices = this.scannerDialogSaneSession.listDevices();
            for(SaneDevice available : availableDevices) {
                this.scannerDialogTableModel.addRow(new Object[] {available.getName(), available.getModel(), available.getType(), available.getVendor()});
            }
            this.scannerDialogStatusLabel.setText(" ");
        } catch(ConnectException ex) {
            String additional = "<br/>You might suffer from a saned bug, try <tt>/usr/sbin/saned -d -s -a saned</tt> with appropriate privileges in order to restart saned and try again</html>";
            this.handleSearchScannerException(ex, additional);
        } catch (IOException | SaneException ex) {
            this.handleSearchScannerException(ex, "");
        }
    }

    private void selectScanner() {
        String selectedName = (String) ((List)this.scannerDialogTableModel.getDataVector().get(this.scannerDialogTable.getSelectedRow())).get(0);
        try {
            this.device = this.scannerDialogSaneSession.getDevice(selectedName);
        } catch (IOException ex) {
            this.handleException(ex);
        }
        String scannerAddress = this.scannerDialogAddressTextField.getText();
        String scannerName = this.device.getName();
        this.conf.setScannerName(scannerName);
        this.conf.setScannerSaneAddress(scannerAddress);
        this.scanMenuItem.setEnabled(true);
        this.scanMenuItem.getParent().revalidate();
    }

    /**
     * connects to an OrientDB database using the current values of the text
     * properties of the text inputs in the database connection dialog. Expect a
     * {@link ODatabaseException} if the connection doesn't succeed due to an
     * errornous URL or wrong credentials. Excepts the remote engine to be on
     * the classpath (usually provided by orientdb-client module (in doubt check
     * with {@code Orient.instance().registerEngine(new OEngineRemote());}).
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

    private void scan() {
        assert this.device != null;
        try {
            this.device.open();
            //for a list of SANE options see http://www.sane-project.org/html/doc014.html
            SaneOption deviceResolutionOption = this.device.getOption("resolution");
            if(!deviceResolutionOption.isWriteable()) {
                LOGGER.warn("SANE option \"resolution\" isn't writable in this SANE frontend. There's nothing you can do. Scanning with {} DPI only", deviceResolutionOption.getType().equals(OptionValueType.INT) ? deviceResolutionOption.getIntegerValue() : deviceResolutionOption.getFixedValue());
            }else {
                if(deviceResolutionOption.getType().equals(OptionValueType.INT)) {
                    deviceResolutionOption.setIntegerValue(RESOLUTION_DEFAULT);
                }else if(deviceResolutionOption.getType().equals(OptionValueType.FIXED)) {
                    deviceResolutionOption.setFixedValue(RESOLUTION_DEFAULT);
                }else {
                    throw new IllegalStateException("type of SANE option \"resolution\" is neither int nor fixed");
                }
                LOGGER.debug("set SANE option \"resolution\" to {}", RESOLUTION_DEFAULT);
            }
            BufferedImage image = this.device.acquireImage();
            @SuppressWarnings("serial")
            OCRSelectPanel panel = new OCRSelectPanel(image) {
                @Override
                public void mouseReleased(MouseEvent evt) {
                    super.mouseReleased(evt);
                    if(this.getDragStart() != null && !this.getDragStart().equals(this.getDragEnd())) {
                        DocumentScanner.this.handleOCRSelection();
                    }
                }
            };
            OCRSelectComponent oCRSelectComponent = new OCRSelectComponent(panel);
            OCREngine oCREngine = this.retrieveOCREninge();
            if(oCREngine == null) {
                //a warning in form of a dialog has been given
                return;
            }
            OCRResultPanelFetcher oCRResultPanelFetcher = new DocumentTabOCRResultPanelFetcher(oCRSelectComponent, oCREngine);
            ScanResultPanelFetcher scanResultPanelFetcher = new DocumentTabScanResultPanelFetcher(oCRSelectComponent);
            List<Pair<Class<? extends Annotation>, FieldAnnotationHandler>> fieldAnnotationMapping = new LinkedList<>(JPAReflectionFormBuilder.JPA_FIELD_ANNOTATION_MAPPING_DEFAULT);
            fieldAnnotationMapping.add(new ImmutablePair<Class<? extends Annotation>, FieldAnnotationHandler>(OCRResult.class, new OCRResultFieldAnnotationHandler(oCRResultPanelFetcher)));
            fieldAnnotationMapping.add(new ImmutablePair<Class<? extends Annotation>, FieldAnnotationHandler>(ScanResult.class, new ScanResultFieldAnnotationHandler(scanResultPanelFetcher)));
            List<Pair<Class<? extends Annotation>, ClassAnnotationHandler>> classAnnotationMapping = new LinkedList<>(JPAReflectionFormBuilder.CLASS_ANNOTATION_MAPPING_DEFAULT);
            DocumentTab newTab = new DocumentTab(UNSAVED_NAME,
                    oCRSelectComponent,
                    oCREngine,
                    ENTITY_CLASSES,
                    this.entityManager,
                    fieldAnnotationMapping,
                    classAnnotationMapping,
                    oCRResultPanelFetcher,
                    scanResultPanelFetcher);
            this.mainTabbedPane.add(UNSAVED_NAME, newTab);
            this.invalidate();
        } catch (SaneException | IOException | IllegalAccessException | IllegalArgumentException | IllegalStateException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
            this.handleException(ex);
        } finally {
            if(this.device != null) {
                try {
                    this.device.close();
                } catch (IOException ex) {
                    this.handleException(ex);
                }
            }
        }
    }

    private void handleOCRSelection() {
        DocumentTab selectedComponent = (DocumentTab) this.mainTabbedPane.getSelectedComponent();
        OCRSelectComponent oCRSelectComponent = selectedComponent.getoCRSelectComponent();
        BufferedImage imageSelection = oCRSelectComponent.getSelection();
        OCREngine oCREngine = this.retrieveOCREninge();
        if(oCREngine == null) {
            //a warning in form of a dialog has been given
            return;
        }
        String oCRResult = oCREngine.recognizeImage(imageSelection);
        selectedComponent.getDocumentForm().getoCRResultTextArea().setText(oCRResult);
    }

    private void handleException(Throwable ex) {
        LOGGER.info("handling exception {}", ex);
        MessagePanelEntry entry = new MessagePanelEntry(new JLabel(ex.getMessage()));
        GroupLayout layout = new GroupLayout(this.statusBar);
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
        hGroup.addGroup(layout.createParallelGroup().
                 addComponent(entry));
        layout.setHorizontalGroup(hGroup);
        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
        vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
                 addComponent(entry));
        layout.setVerticalGroup(vGroup);
        this.statusBar.setLayout(layout);
        this.pack();
        this.invalidate();
    }

    private OCREngine retrieveOCREninge() {
        if(this.conf.getoCREngineConf() == null) {
            JOptionPane.showMessageDialog(this, "OCREngine isn't set up", DocumentScanner.generateApplicationWindowTitle("Warning"), JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return this.conf.getoCREngineConf().getOCREngine();
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
                try {
                    new DocumentScanner().setVisible(true);
                } catch (TesseractNotFoundException ex) {
                    JOptionPane.showConfirmDialog(null, "The tesseract binary isn't available. Install it on your system and make sure it's executable (in doubt check if tesseract runs on the console)", generateApplicationWindowTitle("tesserate binary missing"), JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private class DocumentTabOCRResultPanelFetcher implements OCRResultPanelFetcher {
        private final List<Double> stringBufferLengths = new ArrayList<>();
        private final OCRSelectComponent oCRSelectComponent;
        private final OCREngine oCREngine;

        DocumentTabOCRResultPanelFetcher(OCRSelectComponent oCRSelectComponent, OCREngine oCREngine) {
            this.oCRSelectComponent = oCRSelectComponent;
            this.oCREngine = oCREngine;
        }

        @Override
        public String fetch() {
            //estimate the initial StringBuilder size based on the median
            //of all prior OCR results (string length) (and 1000 initially)
            int stringBufferLengh;
            if (this.stringBufferLengths.isEmpty()) {
                stringBufferLengh = 1_000;
            } else {
                DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(this.stringBufferLengths.toArray(new Double[this.stringBufferLengths.size()]));
                stringBufferLengh = ((int) descriptiveStatistics.getPercentile(.5)) + 1;
            }
            this.stringBufferLengths.add((double) stringBufferLengh);
            StringBuilder retValueBuilder = new StringBuilder(stringBufferLengh);
            for (OCRSelectPanel imagePanel : this.oCRSelectComponent.getImagePanels()) {
                String oCRResult = this.oCREngine.recognizeImage(imagePanel.getImage());
                retValueBuilder.append(oCRResult);
            }
            String retValue = retValueBuilder.toString();
            return retValue;
        }
    }

    private class DocumentTabScanResultPanelFetcher implements ScanResultPanelFetcher {
        private final OCRSelectComponent oCRSelectComponent;

        DocumentTabScanResultPanelFetcher(OCRSelectComponent oCRSelectComponent) {
            this.oCRSelectComponent = oCRSelectComponent;
        }

        @Override
        public byte[] fetch() {
            ByteArrayOutputStream retValueStream = new ByteArrayOutputStream();
            for (OCRSelectPanel imagePanel : this.oCRSelectComponent.getImagePanels()) {
                try {
                    if (!ImageIO.write(imagePanel.getImage(), "png", retValueStream)) {
                        throw new IllegalStateException("writing image data to output stream failed");
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return retValueStream.toByteArray();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
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
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JPopupMenu.Separator exitMenuItemSeparator;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPopupMenu.Separator knownScannersMenuItemSeparator;
    private javax.swing.JPopupMenu.Separator knownStoragesMenuItemSeparartor;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JTabbedPane mainTabbedPane;
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
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenuItem scanMenuItem;
    private javax.swing.JDialog scannerDialog;
    private javax.swing.JLabel scannerDialogAddressLabel;
    private javax.swing.JTextField scannerDialogAddressTextField;
    private javax.swing.JButton scannerDialogCancelButton;
    private javax.swing.JScrollPane scannerDialogScrollPane;
    private javax.swing.JButton scannerDialogSearchButton;
    private javax.swing.JButton scannerDialogSelectButton;
    private javax.swing.JSeparator scannerDialogSeparator;
    private javax.swing.JLabel scannerDialogStatusLabel;
    private javax.swing.JTable scannerDialogTable;
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
    private javax.swing.JComboBox<Class<? extends StorageConf<?>>> storageCreateDialogTypeComboBox;
    private javax.swing.JLabel storageCreateDialogTypeLabel;
    private javax.swing.JDialog storageDialog;
    private javax.swing.JButton storageDialogCancelButton;
    private javax.swing.JButton storageDialogDeleteButton;
    private javax.swing.JButton storageDialogEditButton;
    private javax.swing.JButton storageDialogNewButton;
    private javax.swing.JButton storageDialogSelectButton;
    private javax.swing.JLabel storageLabel;
    private javax.swing.JList<StorageConf<?>> storageList;
    private javax.swing.JScrollPane storageListScrollPane;
    private javax.swing.JMenu storageSelectionMenu;
    private javax.swing.JMenuItem storageSelectionMenuItem;
    // End of variables declaration//GEN-END:variables
}
