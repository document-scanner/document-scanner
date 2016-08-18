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

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultMultipleCDockable;
import bibliothek.gui.dock.common.MultipleCDockable;
import bibliothek.gui.dock.common.event.CVetoFocusListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.util.DockUtilities;
import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.components.OCRResultPanelFetcher;
import richtercloud.document.scanner.components.OCRResultPanelFetcherProgressEvent;
import richtercloud.document.scanner.components.OCRResultPanelFetcherProgressListener;
import richtercloud.document.scanner.components.ScanResultPanelFetcher;
import richtercloud.document.scanner.components.ScanResultPanelRecreator;
import richtercloud.document.scanner.components.annotations.ScanResult;
import static richtercloud.document.scanner.gui.DocumentScanner.APP_NAME;
import static richtercloud.document.scanner.gui.DocumentScanner.APP_VERSION;
import static richtercloud.document.scanner.gui.DocumentScanner.BIDIRECTIONAL_HELP_DIALOG_TITLE;
import static richtercloud.document.scanner.gui.DocumentScanner.INITIAL_QUERY_LIMIT_DEFAULT;
import static richtercloud.document.scanner.gui.DocumentScanner.generateApplicationWindowTitle;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.gui.conf.OCREngineConf;
import richtercloud.document.scanner.idgenerator.EntityIdGenerator;
import richtercloud.document.scanner.ocr.OCREngine;
import richtercloud.document.scanner.ocr.OCREngineFactory;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.reflection.form.builder.FieldRetriever;
import richtercloud.reflection.form.builder.ReflectionFormPanel;
import richtercloud.reflection.form.builder.components.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.AmountMoneyExchangeRateRetriever;
import richtercloud.reflection.form.builder.components.AmountMoneyUsageStatisticsStorage;
import richtercloud.reflection.form.builder.fieldhandler.FieldHandler;
import richtercloud.reflection.form.builder.fieldhandler.FieldHandlingException;
import richtercloud.reflection.form.builder.fieldhandler.MappingFieldHandler;
import richtercloud.reflection.form.builder.fieldhandler.factory.AmountMoneyMappingFieldHandlerFactory;
import richtercloud.reflection.form.builder.jpa.IdGenerator;
import richtercloud.reflection.form.builder.jpa.JPACachedFieldRetriever;
import richtercloud.reflection.form.builder.jpa.JPAReflectionFormBuilder;
import richtercloud.reflection.form.builder.jpa.fieldhandler.factory.JPAAmountMoneyMappingFieldHandlerFactory;
import richtercloud.reflection.form.builder.jpa.typehandler.ElementCollectionTypeHandler;
import richtercloud.reflection.form.builder.jpa.typehandler.ToManyTypeHandler;
import richtercloud.reflection.form.builder.jpa.typehandler.ToOneTypeHandler;
import richtercloud.reflection.form.builder.message.MessageHandler;
import richtercloud.reflection.form.builder.typehandler.TypeHandler;
import richtercloud.swing.worker.get.wait.dialog.SwingWorkerCompletionWaiter;
import richtercloud.swing.worker.get.wait.dialog.SwingWorkerGetWaitDialog;

/**
 * Manages all central windows (see {@link DocumentScanner} for details and
 * concept) which are organized with a docking framework.
 *
 * @author richter
 */
/*
internal implementation notes:
- this class isn't necessary, but keeps DocumentScanner small and comprehendable
- shouldn't manage an OCREngine or OCREngineFactory or AmountMoney related
factories or resources because they're managed in a dialog which MainPanel
doesn't need to know about
- Adding the first OCRSelectComponentScrollPane inside a dockable
doesn't trigger the CVetoFocusListener added to control, but the second does!
- After adding a second OCRSelectComponentScrollPane inside dockable in
addDocumentDockable
  1. CVetoFocusListener.willGainFocus is invoked with the second
dockable as argument
  2. CVetoFocusListener.willLoseFocus is invoked with the second dockable as
argument (which appears unintuitive)
  3. CVetoFocusListener.willGainFocus is invoked with the second dockable as
argument
-> previously focused component can't be retrieved with
CVetoFocusListener.willLoseFocus, but needs to be stored in a variable
- Unclear why `Warning: layout should not be modified by subclasses of bibliothek.gui.dock.event.DockStationListener
 This is only an information, not an exception. If your code is actually safe you can:
 - disabled the warning by calling DockUtilities.disableCheckLayoutLocked() )
 - mark your code as safe by setting the annotation 'LayoutLocked'` is printed
-> using `DockUtilities.disableCheckLayoutLocked()` in static block causes
`java.lang.IllegalStateException: During an operation the framework attempted to
acquire the same lock twice. There are two possible explanations:` -> ignore as
long as no problem occurs
*/
public class MainPanel extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(MainPanel.class);
    /**
     * indicates that {@link #addDocument(java.util.List, java.io.File) } ought
     * to be handled in another thread while a {@link ProgressMonitor} dialog
     * is displayed. If {@code ADD_DOCUMENT_ASYNC} is {@code false}
     * {@link #addDocument(java.util.List, java.io.File) } isn't cancelable and
     * will block the GUI.
     */
    /*
    internal implementation notes:
    - whilst this is used for debugging purposes mostly, it's useful to keep and
    almost costs nothing
    */
    private static final boolean ADD_DOCUMENT_ASYNC = true;
    static {
        /*
        doesn't prevent `java.lang.IllegalStateException: During an operation the framework attempted to acquire the same lock twice. There are two possible explanations:
        1. In a multi-threaded application one or both operations are not executed in the EventDispatchThread, or
        2. The operations are calling each other, which should not happen.
        Please verify that this application is not accessing the framework from different threads, and fill a bugreport if you feel that this exception is not caused by your application.`
        as suggested in the error message
        */
        DockUtilities.disableCheckLayoutLocked();
    }
    /**
     * Holds information which are necessary to adjust docked windows when
     * switching the document in the document tab dock. Store whole components
     * rather than their properties in order to KISS and don't care about
     * extranous memory usage.
     *
     * Maps {@link OCRSelectComponent}'s {@link CDockable} to the corresponding
     * {@link OCRPanel}'s and {@link EntityPanel}'s dockables from which
     * components can be retrieved as well.
     */
    private Map<OCRSelectComponent, Pair<OCRPanel, EntityPanel>> documentSwitchingMap = new HashMap<>();
    private final Set<Class<?>> entityClasses;
    private final Class<?> primaryClassSelection;
    private final Map<Class<? extends JComponent>, ValueSetter<?,?>> valueSetterMapping;
    private final EntityManager entityManager;
    private final MessageHandler messageHandler;
    private final JPAReflectionFormBuilder reflectionFormBuilder;
    private final AmountMoneyUsageStatisticsStorage amountMoneyUsageStatisticsStorage;
    private final AmountMoneyCurrencyStorage amountMoneyCurrencyStorage;
    private final AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever;
    private final IdGenerator idGenerator = EntityIdGenerator.getInstance();
    /**
     * The dockable which contains the currently focused
     * {@link OCRSelectComponent} and its surrounding components (most likely a
     * {@link JScrollPane}). More than one dockable with
     * {@link OCRSelectComponent}s and surrounding components can be visible,
     * but only one focused.
     */
    private OCRSelectComponent oCRSelectComponentScrollPane;
    private final OCREngineFactory oCREngineFactory;
    private final FieldRetriever fieldRetriever = new JPACachedFieldRetriever();
    private final Map<java.lang.reflect.Type, TypeHandler<?, ?,?, ?>> typeHandlerMapping;
    private final OCREngineConf oCREngineConf;
    private final DocumentScannerConf documentScannerConf;
    private final CControl control;
    private final Map<JComponent, MultipleCDockable> dockableMap = new HashMap<>();
    private final Map<CDockable, OCRSelectComponent> componentMap = new HashMap<>();
    private final Window oCRProgressMonitorParent;

    public MainPanel(Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            EntityManager entityManager,
            AmountMoneyUsageStatisticsStorage amountMoneyUsageStatisticsStorage,
            AmountMoneyCurrencyStorage amountMoneyAdditionalCurrencyStorage,
            AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever,
            MessageHandler messageHandler,
            JFrame dockingControlFrame,
            OCREngineFactory oCREngineFactory,
            OCREngineConf oCREngineConf,
            Map<java.lang.reflect.Type, TypeHandler<?, ?,?, ?>> typeHandlerMapping,
            DocumentScannerConf documentScannerConf,
            Window oCRProgressMonitorParent) {
        this(entityClasses,
                primaryClassSelection,
                DocumentScanner.VALUE_SETTER_MAPPING_DEFAULT,
                entityManager,
                amountMoneyUsageStatisticsStorage,
                amountMoneyAdditionalCurrencyStorage,
                amountMoneyExchangeRateRetriever,
                messageHandler,
                dockingControlFrame,
                oCREngineFactory,
                oCREngineConf,
                typeHandlerMapping,
                documentScannerConf,
                oCRProgressMonitorParent);
    }

    public MainPanel(Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            Map<Class<? extends JComponent>, ValueSetter<?,?>> valueSetterMapping,
            EntityManager entityManager,
            AmountMoneyUsageStatisticsStorage amountMoneyUsageStatisticsStorage,
            AmountMoneyCurrencyStorage amountMoneyCurrencyStorage,
            AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever,
            MessageHandler messageHandler,
            JFrame dockingControlFrame,
            OCREngineFactory oCREngineFactory,
            OCREngineConf oCREngineConf,
            Map<java.lang.reflect.Type, TypeHandler<?, ?,?, ?>> typeHandlerMapping,
            DocumentScannerConf documentScannerConf,
            Window oCRProgressMonitorParent) {
        if(messageHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.messageHandler = messageHandler;
        if(documentScannerConf == null) {
            throw new IllegalArgumentException("documentScannerConf mustn't be "
                    + "null");
        }
        this.documentScannerConf = documentScannerConf;
        this.entityClasses = entityClasses;
        this.primaryClassSelection = primaryClassSelection;
        this.valueSetterMapping = valueSetterMapping;
        this.entityManager = entityManager;
        this.amountMoneyUsageStatisticsStorage = amountMoneyUsageStatisticsStorage;
        this.amountMoneyCurrencyStorage = amountMoneyCurrencyStorage;
        this.amountMoneyExchangeRateRetriever = amountMoneyExchangeRateRetriever;
        this.typeHandlerMapping = typeHandlerMapping;
        this.control = new CControl (dockingControlFrame);
        this.reflectionFormBuilder = new JPAReflectionFormBuilder(entityManager,
                DocumentScanner.generateApplicationWindowTitle("Field description",
                        DocumentScanner.APP_NAME,
                        DocumentScanner.APP_VERSION),
                messageHandler,
                new JPACachedFieldRetriever());
        this.initComponents();
        this.add(this.control.getContentArea(),
                BorderLayout.CENTER); //has to be called after initComponents
        this.control.addVetoFocusListener(new CVetoFocusListener() {

            @Override
            public boolean willGainFocus(CDockable dockable) {
                OCRSelectComponent aNew = componentMap.get(dockable);
                if(aNew != null
                        && !aNew.equals(MainPanel.this.oCRSelectComponentScrollPane)
                        //focused component requests focus (e.g. after newly
                        //adding)
                ) {
                    switchDocument(MainPanel.this.oCRSelectComponentScrollPane, aNew);
                    MainPanel.this.oCRSelectComponentScrollPane = aNew;
                }
                return true;
            }

            @Override
            public boolean willLoseFocus(CDockable dockable) {
                return true;
            }
        });
        this.oCREngineFactory = oCREngineFactory;
        this.oCREngineConf = oCREngineConf;
        this.oCRProgressMonitorParent = oCRProgressMonitorParent;
    }

    /**
     * Uses a modal dialog in order to display the progress of the retrieval and
     * make the operation cancelable.
     * @param documentFile
     * @return the retrieved images or {@code null} if the retrieval has been
     * canceled (in dialog)
     * @throws DocumentAddException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    /*
    internal implementation notes:
    - can't use ProgressMonitor without blocking EVT instead of a model dialog
    when using SwingWorker.get
    */
    public List<BufferedImage> retrieveImages(final File documentFile) throws DocumentAddException, InterruptedException, ExecutionException {
        if(documentFile == null) {
            throw new IllegalArgumentException("documentFile mustn't be null");
        }
        final SwingWorkerGetWaitDialog dialog = new SwingWorkerGetWaitDialog(SwingUtilities.getWindowAncestor(this), //owner
                DocumentScanner.generateApplicationWindowTitle("Wait", APP_NAME, APP_VERSION), //dialogTitle
                "Retrieving image data", //labelText
                null //progressBarText
        );
        final SwingWorker<List<BufferedImage>, Void> worker = new SwingWorker<List<BufferedImage>, Void>() {
            @Override
            protected List<BufferedImage> doInBackground() throws Exception {
                List<BufferedImage> retValue = new LinkedList<>();
                try {
                    InputStream pdfInputStream = new FileInputStream(documentFile);
                    PDDocument document = PDDocument.load(pdfInputStream);
                    @SuppressWarnings("unchecked")
                    List<PDPage> pages = document.getDocumentCatalog().getAllPages();
                    for (PDPage page : pages) {
                        if(dialog.isCanceled()) {
                            document.close();
                            MainPanel.LOGGER.debug("tab generation aborted");
                            return null;
                        }
                        BufferedImage image = page.convertToImage();
                        retValue.add(image);
                    }
                    document.close();
                }catch(IOException ex) {
                    throw new DocumentAddException(ex);
                }
                return retValue;
            }

            @Override
            protected void done() {
            }
        };
        worker.addPropertyChangeListener(
            new SwingWorkerCompletionWaiter(dialog));
        worker.execute();
        //the dialog will be visible until the SwingWorker is done
        dialog.setVisible(true);
        List<BufferedImage> retValue = worker.get();
        return retValue;
    }

    /**
     * In order to honour the fact that storing scan data (currently only
     * performed with {@link ScanResult} annotation) is optional, check the
     * determinants for storage of scan data and retrieve necessary data from
     * those.
     * @param entityToEdit the instance determining the initial state of
     * components
     */
    public void addDocument(Object entityToEdit) throws DocumentAddException {
        MainPanelScanResultPanelRecreator mainPanelScanResultPanelRecreator =
                new MainPanelScanResultPanelRecreator();
        List<Field> entityClassFields = reflectionFormBuilder.getFieldRetriever().retrieveRelevantFields(entityToEdit.getClass());
        Field entityToEditScanResultField = null;
        for(Field entityClassField : entityClassFields) {
            ScanResult scanResult = entityClassField.getAnnotation(ScanResult.class);
            if(scanResult != null) {
                if(entityToEditScanResultField != null) {
                    throw new IllegalArgumentException(String.format("class %s "
                            + "of entityToEdit contains more than one field "
                            + "with annotation %s",
                            entityToEdit.getClass(),
                            ScanResult.class));
                }
                entityToEditScanResultField = entityClassField;
            }
        }
        List<BufferedImage> images = null;
        if(entityToEditScanResultField != null) {
            if(!entityToEditScanResultField.getType().equals(byte[].class)) {
                throw new IllegalArgumentException(String.format("field %s "
                        + "of class %s annotated with %s, but not of type "
                        + "%s",
                        entityToEditScanResultField.getName(),
                        entityToEdit.getClass(),
                        ScanResult.class,
                        byte[].class));
            }
            try {
                byte[] scanData = (byte[]) entityToEditScanResultField.get(entityToEdit);
                if(scanData == null) {
                    LOGGER.debug(String.format("scanData of instance of %s "
                            + "is null, assuming that no data has been "
                            + "persisted",
                            entityToEdit.getClass()));
                }else {
                    images = mainPanelScanResultPanelRecreator.recreate(scanData);
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new DocumentAddException(ex);
            }
        }
        addDocument(images,
                null, //documentFile
                entityToEdit
        );
    }

    public void addDocument (final List<BufferedImage> images,
            final File documentFile) throws DocumentAddException {
        addDocument(images,
                documentFile,
                null //entityToEdit
        );
    }

    /**
     *
     * @param images images to be transformed into a {@link OCRSelectPanelPanel}
     * or {@code null} indicating that no scan data was persisted when opening a
     * persisted entry
     * @param documentFile The {@link File} the document is stored in.
     * {@code null} indicates that the document has not been saved yet (e.g. if
     * the {@link OCRSelectComponent} represents scan data).
     * @throws DocumentAddException
     */
    public void addDocument (final List<BufferedImage> images,
            final File documentFile,
            final Object entityToEdit) throws DocumentAddException {
        if(images == null || images.isEmpty()) {
            throw new IllegalArgumentException("images mustn't be null or empty");
        }
        if(ADD_DOCUMENT_ASYNC) {
            final ProgressMonitor progressMonitor = new ProgressMonitor(this, //parent
                    "Generating new document tab", //message
                    null, //note
                    0, //min
                    100 //max
            );
            progressMonitor.setMillisToPopup(0);
            progressMonitor.setMillisToDecideToPopup(0);
            final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                private Pair<OCRSelectComponent, EntityPanel> createdOCRSelectComponentScrollPane;

                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        this.createdOCRSelectComponentScrollPane = addDocumentRoutine(images,
                                documentFile,
                                entityToEdit,
                                progressMonitor);
                    }catch(Exception ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void done() {
                    if(!progressMonitor.isCanceled()) {
                        addDocumentDone(this.createdOCRSelectComponentScrollPane.getKey(),
                                this.createdOCRSelectComponentScrollPane.getValue());
                    }
                    progressMonitor.close(); //- need to close explicitly
                        //because progress isn't set
                        //- only close after document has actually has been
                        //added (pressing the cancel button then has no effect,
                        //but that should be fine)
                }
            };
            worker.execute();
            progressMonitor.setProgress(1); //ProgressMonitor dialog doesn't
                //block the thread, but the GUI until SwingWorker.done is
                //invoked (automatically)
        }else {
            Pair<OCRSelectComponent, EntityPanel> oCRSelectComponentScrollPane = addDocumentRoutine(images,
                    documentFile,
                    entityToEdit,
                    null //progressMonitor
            );
            addDocumentDone(oCRSelectComponentScrollPane.getKey(),
                    oCRSelectComponentScrollPane.getValue());
        }
    }

    private void addDocumentDone(OCRSelectComponent oCRSelectComponentScrollPane,
            EntityPanel entityPanel) {
        addDocumentDockable(MainPanel.this.oCRSelectComponentScrollPane,
                oCRSelectComponentScrollPane);
        if(MainPanel.this.oCRSelectComponentScrollPane == null) {
            //first document added -> CVetoFocusListener methods not
            //triggered
            switchDocument(MainPanel.this.oCRSelectComponentScrollPane,
                    oCRSelectComponentScrollPane);
        }
        if(this.documentScannerConf.isAutoOCRValueDetection()) {
            entityPanel.autoOCRValueDetection(new OCRSelectPanelPanelFetcher(oCRSelectComponentScrollPane.getoCRSelectPanelPanel(),
                    oCREngineFactory,
                    oCREngineConf),
                    false //forceRenewal (shouldn't matter here since the
                        //initial list of results has to be empty)
            );
        }
    }

    /**
     * Exceptions are handled here in order to maximize code reusage although
     * this requires check if {@code progressMonitor} is used (not {@code null})
     * or not.
     * @param images
     * @param documentFile
     * @param entityToEdit {@code null} if setup ought to occur for all entities
     * in {@code entityClasses} or not {@code null} if the setup ought to occur
     * for editing a specified entity
     * @return the created {@link OCRSelectComponentScrollPane}
     */
    private Pair<OCRSelectComponent, EntityPanel> addDocumentRoutine(List<BufferedImage> images,
            File documentFile,
            Object entityToEdit,
            ProgressMonitor progressMonitor) throws DocumentAddException {
        Pair<OCRSelectComponent, EntityPanel> retValue = null;
        try {
            List<OCRSelectPanel> panels = new LinkedList<>();
            if(images != null) {
                for (BufferedImage image : images) {
                    @SuppressWarnings("serial")
                    OCRSelectPanel panel = new OCRSelectPanel(image) {
                        @Override
                        public void mouseReleased(MouseEvent evt) {
                            super.mouseReleased(evt);
                            if (this.getDragStart() != null && !this.getDragStart().equals(this.getDragEnd())) {
                                MainPanel.this.handleOCRSelection();
                            }
                        }
                    };
                    panels.add(panel);
                }
            }
            OCRSelectPanelPanel oCRSelectPanelPanel = new OCRSelectPanelPanel(panels,
                    documentFile,
                    oCREngineFactory,
                    oCREngineConf);

            DocumentTabOCRResultPanelFetcher oCRResultPanelFetcher = new DocumentTabOCRResultPanelFetcher(oCRSelectPanelPanel //oCRSelectPanelPanel
                    );
            MainPanelScanResultPanelFetcher scanResultPanelFetcher = new MainPanelScanResultPanelFetcher(oCRSelectPanelPanel //oCRSelectPanelPanel
                    );

            AmountMoneyMappingFieldHandlerFactory embeddableFieldHandlerFactory = new AmountMoneyMappingFieldHandlerFactory(amountMoneyUsageStatisticsStorage,
                    amountMoneyCurrencyStorage,
                    amountMoneyExchangeRateRetriever,
                    messageHandler);
            FieldHandler embeddableFieldHandler = new MappingFieldHandler(embeddableFieldHandlerFactory.generateClassMapping(),
                    embeddableFieldHandlerFactory.generatePrimitiveMapping());
            ElementCollectionTypeHandler elementCollectionTypeHandler = new ElementCollectionTypeHandler(typeHandlerMapping,
                    typeHandlerMapping,
                    messageHandler,
                    embeddableFieldHandler);
            JPAAmountMoneyMappingFieldHandlerFactory jPAAmountMoneyMappingFieldHandlerFactory = JPAAmountMoneyMappingFieldHandlerFactory.create(entityManager,
                    INITIAL_QUERY_LIMIT_DEFAULT,
                    messageHandler,
                    amountMoneyUsageStatisticsStorage,
                    amountMoneyCurrencyStorage,
                    amountMoneyExchangeRateRetriever,
                    BIDIRECTIONAL_HELP_DIALOG_TITLE);
            ToManyTypeHandler toManyTypeHandler = new ToManyTypeHandler(entityManager,
                    messageHandler,
                    typeHandlerMapping,
                    typeHandlerMapping,
                    BIDIRECTIONAL_HELP_DIALOG_TITLE);
            ToOneTypeHandler toOneTypeHandler = new ToOneTypeHandler(entityManager,
                    messageHandler,
                    BIDIRECTIONAL_HELP_DIALOG_TITLE);
            FieldHandler fieldHandler = new DocumentScannerFieldHandler(jPAAmountMoneyMappingFieldHandlerFactory.generateClassMapping(),
                    embeddableFieldHandlerFactory.generateClassMapping(),
                    embeddableFieldHandlerFactory.generatePrimitiveMapping(),
                    elementCollectionTypeHandler,
                    toManyTypeHandler,
                    toOneTypeHandler,
                    idGenerator,
                    messageHandler,
                    fieldRetriever,
                    oCRResultPanelFetcher,
                    scanResultPanelFetcher,
                    this.documentScannerConf,
                    oCRProgressMonitorParent //oCRProgressMonitorParent
            );

            Map<Class<?>, ReflectionFormPanel<?>> reflectionFormPanelMap = new HashMap<>();
            Set<Class<?>> entityClasses0;
            if(entityToEdit == null) {
                for(Class<?> entityClass : entityClasses) {
                    ReflectionFormPanel reflectionFormPanel;
                    try {
                        reflectionFormPanel = reflectionFormBuilder.transformEntityClass(entityClass,
                                null, //entityToUpdate
                                false, //editingMode
                                fieldHandler
                        );
                        reflectionFormPanelMap.put(entityClass, reflectionFormPanel);
                    } catch (FieldHandlingException ex) {
                        String message = String.format("An exception during creation of components occured (details: %s)",
                                ex.getMessage());
                        JOptionPane.showMessageDialog(MainPanel.this,
                                message,
                                DocumentScanner.generateApplicationWindowTitle("Exception",
                                        DocumentScanner.APP_NAME,
                                        DocumentScanner.APP_VERSION),
                                JOptionPane.WARNING_MESSAGE);
                        LOGGER.error(message, ex);
                        throw ex;
                    }
                }
                entityClasses0 = entityClasses;
            }else {
                ReflectionFormPanel reflectionFormPanel = reflectionFormBuilder.transformEntityClass(entityToEdit.getClass(),
                        entityToEdit,
                        true, //editingMode
                        fieldHandler
                );
                reflectionFormPanelMap.put(entityToEdit.getClass(), reflectionFormPanel);
                entityClasses0 = new HashSet<Class<?>>(Arrays.asList(entityToEdit.getClass()));
            }

            OCRPanel oCRPanel = new OCRPanel(entityClasses0,
                    reflectionFormPanelMap,
                    valueSetterMapping,
                    entityManager,
                    messageHandler,
                    reflectionFormBuilder,
                    documentScannerConf);
            EntityPanel entityPanel = new EntityPanel(entityClasses0,
                    primaryClassSelection,
                    reflectionFormPanelMap,
                    valueSetterMapping,
                    oCRResultPanelFetcher,
                    scanResultPanelFetcher,
                    amountMoneyUsageStatisticsStorage,
                    amountMoneyCurrencyStorage,
                    reflectionFormBuilder,
                    messageHandler,
                    documentScannerConf);
            OCRSelectComponent oCRSelectComponent = new OCRSelectComponent(oCRSelectPanelPanel,
                    entityPanel,
                    oCREngineFactory,
                    oCREngineConf);
            retValue = new ImmutablePair<>(oCRSelectComponent, entityPanel);
            if(progressMonitor == null || !progressMonitor.isCanceled()) {
                documentSwitchingMap.put(oCRSelectComponent,
                        new ImmutablePair<>(oCRPanel, entityPanel));
            }
            return retValue;
        } catch (HeadlessException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
            if(progressMonitor != null) {
                progressMonitor.close();
            }
            throw new DocumentAddException(ex);
        } catch(Throwable ex) {
            //This dramatically facilitates debugging since Java
            //debugger has a lot of problems to halt at uncatched
            //exceptions and a lot of JVMs don't provide debugging
            //symbols.
            String message = "An unexpected exception occured "
                            + "during initialization (see stacktrace "
                            + "for details)";
            LOGGER.error(message, ex);
            if(progressMonitor != null) {
                progressMonitor.close();
            }
            JOptionPane.showMessageDialog(MainPanel.this, //parentComponent
                    String.format("<html>%s. Please consider filing a "
                            + "bug at <a href=\"%s\">%s</a>. Stacktrace: %s</html>",
                            message,
                            DocumentScanner.BUG_URL,
                            DocumentScanner.BUG_URL,
                            ExceptionUtils.getFullStackTrace(ex)),
                    generateApplicationWindowTitle("An exception occured",
                            APP_NAME,
                            APP_VERSION),
                    JOptionPane.ERROR_MESSAGE);
        }
        return retValue;
    }

    /**
     * A method which is called after a new {@link OCRSelectComponentScrollPane}
     * has been created in {@link #addDocument(java.util.List, java.io.File) }
     * which adds the created component to the docking framework which triggers
     * the {@link CVetoFocusListener} added to {@code control}.
     * @param old
     * @param aNew
     */
    private void addDocumentDockable(OCRSelectComponent old,
            OCRSelectComponent aNew) {
        MultipleCDockable aNewDockable = dockableMap.get(aNew);
        if(aNewDockable == null) {
            aNewDockable = new DefaultMultipleCDockable(null,
                    aNew.getoCRSelectPanelPanel().getDocumentFile() != null
                            ? aNew.getoCRSelectPanelPanel().getDocumentFile().getName()
                            : DocumentScanner.UNSAVED_NAME,
                    aNew);
            dockableMap.put(aNew, aNewDockable);
            componentMap.put(aNewDockable, aNew);
        }
        control.addDockable(aNewDockable);
        MultipleCDockable oldDockable = dockableMap.get(old);
        assert oldDockable != null;
        if(old != null) {
            aNewDockable.setLocationsAside(oldDockable);
        }else {
            aNewDockable.setLocation(CLocation.base().normalNorth(0.4));
        }
        aNewDockable.setVisible(true);
    }

    /**
     * Handles both switching documents (if {@code old} and {@code aNew} are not
     * {@code null} and adding the first document (if {@code old} is
     * {@code null}.
     * @param old
     * @param aNew
     */
    /*
    internal implementation notes:
    - handling both switching and adding the first document maximizes code
    reusage
    */
    private void switchDocument(OCRSelectComponent old,
            final OCRSelectComponent aNew) {
        synchronized(aNew.getTreeLock()) {
            Pair<OCRPanel, EntityPanel> newPair = documentSwitchingMap.get(aNew);
            assert newPair != null;
            OCRPanel oCRPanelNew = newPair.getKey();
            EntityPanel entityPanelNew = newPair.getValue();
            assert oCRPanelNew != null;
            assert entityPanelNew != null;
            //check if dockables already exist in order to avoid failure of
            //CControl.replace if dockable is recreated
            MultipleCDockable oCRPanelNewDockable = dockableMap.get(oCRPanelNew);
            if(oCRPanelNewDockable == null) {
                oCRPanelNewDockable = new DefaultMultipleCDockable(null,
                        "OCR result",
                        oCRPanelNew);
                dockableMap.put(oCRPanelNew, oCRPanelNewDockable);
            }
            MultipleCDockable entityPanelNewDockable = dockableMap.get(entityPanelNew);
            if(entityPanelNewDockable == null) {
                entityPanelNewDockable = new DefaultMultipleCDockable(null,
                        "Entities",
                        entityPanelNew);
                dockableMap.put(entityPanelNew, entityPanelNewDockable);
            }
            if(old != null) {
                Pair<OCRPanel, EntityPanel> oldPair = documentSwitchingMap.get(old);
                assert oldPair != null;
                //order doesn't matter
                OCRPanel oCRPanelOld = oldPair.getKey();
                EntityPanel entityPanelOld = oldPair.getValue();
                assert oCRPanelOld != null;
                assert entityPanelOld != null;
                MultipleCDockable oCRPanelOldDockable = dockableMap.get(oCRPanelOld);
                MultipleCDockable entityPanelOldDockable = dockableMap.get(entityPanelOld);
                control.replace(oCRPanelOldDockable, oCRPanelNewDockable);
                    //CControl.replace fails if new dockable is already
                    //registered at CControl
                    //CControl.replace fails if old dockable has already been
                    //removed from CControl
                    //CDockable.setVisible(false) unregisters dockable at
                    //CControl
                oCRPanelNewDockable.setVisible(true);
                control.replace(entityPanelOldDockable, entityPanelNewDockable);
                    //MultipleCDockable.setVisible(true) fails if it's not
                    //registered at a CControl (which has to be done with
                    //CControl.replace (see above))
                entityPanelNewDockable.setVisible(true);
            }else {
                //order matters
                control.addDockable(oCRPanelNewDockable);
                control.addDockable(entityPanelNewDockable);
                oCRPanelNewDockable.setLocation(CLocation.base().normalEast(0.4));
                oCRPanelNewDockable.setVisible(true);
                entityPanelNewDockable.setLocation(CLocation.base().normalSouth(0.4));
                entityPanelNewDockable.setVisible(true);
            }
            this.oCRSelectComponentScrollPane = aNew;
            validate();
        }
    };

    private void handleOCRSelection() {
        BufferedImage imageSelection = oCRSelectComponentScrollPane.getoCRSelectPanelPanel().getSelection();
        if(imageSelection == null) {
            //image panels only contain selections of width or height <= 0 -> skip silently
            return;
        }
        OCREngine oCREngine = oCREngineFactory.create(oCREngineConf);
        if (oCREngine == null) {
            //a warning in form of a dialog has been given
            return;
        }
        String oCRResult = oCREngine.recognizeImage(imageSelection);
        OCRPanel oCRPanel = documentSwitchingMap.get(oCRSelectComponentScrollPane).getLeft();
        oCRPanel.getoCRResultTextArea().setText(oCRResult);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    /**
     * A {@link OCRResultPanelFetcher} which uses an in-memory cache and
     * multiple {@link OCREngine}s if it makes sense in order to speed up
     * fetching of OCR results.
     */
    private class DocumentTabOCRResultPanelFetcher implements OCRResultPanelFetcher {
        private final OCRSelectPanelPanelFetcher oCRSelectPanelPanelFetcher;
        private final Map<OCRResultPanelFetcherProgressListener, OCRSelectPanelPanelFetcherProgressListener> listenerMap = new HashMap<>();

        /**
         * Creates a new {@code DocumentTabOCRResultPanelFetcher}
         * @param oCRSelectPanelPanelFetcher the {@link OCRSelectPanelPanel} where to
         * fetch the OCR results (might be {@code null} in order to avoid
         * cyclic dependencies, but needs to be set up with {@link #setoCRSelectPanelPanelFetcher(richtercloud.document.scanner.gui.OCRSelectPanelPanelFetcher) }
         * before {@link #fetch() } works.
         */
        DocumentTabOCRResultPanelFetcher(OCRSelectPanelPanel oCRSelectPanelPanel) {
            this.oCRSelectPanelPanelFetcher = new OCRSelectPanelPanelFetcher(oCRSelectPanelPanel,
                    oCREngineFactory,
                    oCREngineConf);
        }

        @Override
        public String fetch() {
            return oCRSelectPanelPanelFetcher.fetch();
        }

        @Override
        public void cancelFetch() throws UnsupportedOperationException {
            oCRSelectPanelPanelFetcher.cancelFetch();
        }

        @Override
        public void addProgressListener(final OCRResultPanelFetcherProgressListener progressListener) {
            OCRSelectPanelPanelFetcherProgressListener progressListener0 = new OCRSelectPanelPanelFetcherProgressListener() {
                @Override
                public void onProgressUpdate(OCRSelectPanelPanelFetcherProgressEvent progressEvent) {
                    progressListener.onProgressUpdate(new OCRResultPanelFetcherProgressEvent(progressEvent.getNewValue(),
                            progressEvent.getProgress()));
                }
            };
            this.oCRSelectPanelPanelFetcher.addProgressListener(progressListener0);
            listenerMap.put(progressListener, progressListener0);
        }

        @Override
        public void removeProgressListener(OCRResultPanelFetcherProgressListener progressListener) {
            OCRSelectPanelPanelFetcherProgressListener progressListener0 = listenerMap.get(progressListener);
            this.oCRSelectPanelPanelFetcher.removeProgressListener(progressListener0);
            this.listenerMap.remove(progressListener);
        }
    }

    private class MainPanelScanResultPanelFetcher implements ScanResultPanelFetcher {
        private OCRSelectPanelPanel oCRSelectComponent;

        /**
         * Creates a {@code MainPanelScanResultPanelFetcher}.
         * @param oCRSelectComponent the {@link OCRSelectPanelPanel} where to
         * fetch the OCR results (might be {@code null} in order to avoid
         * cyclic dependencies, but needs to be set up with {@link #setoCRSelectComponent(richtercloud.document.scanner.gui.OCRSelectPanelPanel) }
         * before {@link #fetch() } works.
         */
        MainPanelScanResultPanelFetcher(OCRSelectPanelPanel oCRSelectComponent) {
            this.oCRSelectComponent = oCRSelectComponent;
        }

        public void setoCRSelectComponent(OCRSelectPanelPanel oCRSelectComponent) {
            this.oCRSelectComponent = oCRSelectComponent;
        }

        /**
         * Uses {@link ImageIO#write(java.awt.image.RenderedImage, java.lang.String, java.io.OutputStream) }
         * assuming that {@link ImageIO#read(java.io.InputStream) } allows
         * re-reading data correctly.
         * @return the fetched binary data
         */
        @Override
        public byte[] fetch() {
            ByteArrayOutputStream retValueStream = new ByteArrayOutputStream();
            try {
                for (OCRSelectPanel imagePanel : this.oCRSelectComponent.getoCRSelectPanels()) {
                    if (!ImageIO.write(imagePanel.getImage(), "png", retValueStream)) {
                        throw new IllegalStateException("writing image data to output stream failed");
                    }
                }
                return retValueStream.toByteArray();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private class MainPanelScanResultPanelRecreator implements ScanResultPanelRecreator {

        @Override
        public List<BufferedImage> recreate(byte[] data) {
            List<BufferedImage> retValue = new LinkedList<>();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            try {
                BufferedImage image = ImageIO.read(byteArrayInputStream);
                while(image != null) {
                    retValue.add(image);
                    image = ImageIO.read(byteArrayInputStream);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return retValue;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
