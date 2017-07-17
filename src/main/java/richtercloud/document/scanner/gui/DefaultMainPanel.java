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

import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.components.MainPanelScanResultPanelFetcher;
import richtercloud.document.scanner.components.OCRResultPanelFetcher;
import richtercloud.document.scanner.components.OCRResultPanelFetcherProgressEvent;
import richtercloud.document.scanner.components.OCRResultPanelFetcherProgressListener;
import richtercloud.document.scanner.components.ValueDetectionReflectionFormBuilder;
import richtercloud.document.scanner.components.annotations.ScanResult;
import richtercloud.document.scanner.components.tag.TagStorage;
import richtercloud.document.scanner.flexdock.MainPanelDockingManagerFlexdock;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.ifaces.DocumentAddException;
import richtercloud.document.scanner.ifaces.EntityPanel;
import richtercloud.document.scanner.ifaces.ImageWrapper;
import richtercloud.document.scanner.ifaces.MainPanel;
import richtercloud.document.scanner.ifaces.MainPanelDockingManager;
import richtercloud.document.scanner.ifaces.OCREngine;
import richtercloud.document.scanner.ifaces.OCREngineRecognitionException;
import richtercloud.document.scanner.ifaces.OCRPanel;
import richtercloud.document.scanner.ifaces.OCRSelectComponent;
import richtercloud.document.scanner.ifaces.OCRSelectPanel;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanel;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanelFetcher;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanelFetcherProgressEvent;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanelFetcherProgressListener;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.document.scanner.valuedetectionservice.ValueDetectionServiceCreationException;
import richtercloud.message.handler.ConfirmMessageHandler;
import richtercloud.message.handler.ExceptionMessage;
import richtercloud.message.handler.IssueHandler;
import richtercloud.message.handler.Message;
import richtercloud.reflection.form.builder.components.money.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetriever;
import richtercloud.reflection.form.builder.components.money.AmountMoneyUsageStatisticsStorage;
import richtercloud.reflection.form.builder.fieldhandler.FieldHandler;
import richtercloud.reflection.form.builder.fieldhandler.MappingFieldHandler;
import richtercloud.reflection.form.builder.fieldhandler.factory.AmountMoneyMappingFieldHandlerFactory;
import richtercloud.reflection.form.builder.jpa.IdGenerator;
import richtercloud.reflection.form.builder.jpa.JPACachedFieldRetriever;
import richtercloud.reflection.form.builder.jpa.JPAFieldRetriever;
import richtercloud.reflection.form.builder.jpa.WarningHandler;
import richtercloud.reflection.form.builder.jpa.fieldhandler.factory.JPAAmountMoneyMappingFieldHandlerFactory;
import richtercloud.reflection.form.builder.jpa.idapplier.IdApplier;
import richtercloud.reflection.form.builder.jpa.panels.QueryHistoryEntryStorage;
import richtercloud.reflection.form.builder.jpa.storage.FieldInitializer;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;
import richtercloud.reflection.form.builder.jpa.typehandler.ElementCollectionTypeHandler;
import richtercloud.reflection.form.builder.jpa.typehandler.ToManyTypeHandler;
import richtercloud.reflection.form.builder.jpa.typehandler.ToOneTypeHandler;
import richtercloud.reflection.form.builder.typehandler.TypeHandler;
import richtercloud.validation.tools.FieldRetrievalException;
import richtercloud.validation.tools.FieldRetriever;

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
- Adding the first OCRSelectComponent inside a dockable
doesn't trigger the CVetoFocusListener added to control, but the second does!
- After adding a second OCRSelectComponent inside dockable in
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
- zooming: There's been a method adjustZoomLevels in 24bb703 which calculated
the number of zoom steps in order to approach a specific width as close as
possible. It has been removed because calculating the width is easier to manage.
- The ReflectionFormPanel generation can't be handled on a separate thread
(SwingWorker or else) because it involves adding GUI components and thus needs
be done on the EDT. If cancelation ever becomes an absolute must-have, non-GUI
and GUI actions have to be strictly separated (eventually in
reflection-form-builder as well) and be run in workers and on EDT.
*/
public class DefaultMainPanel extends MainPanel {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultMainPanel.class);
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
    private PersistenceStorage storage;
    private final IssueHandler issueHandler;
    private final ConfirmMessageHandler confirmMessageHandler;
    private final AmountMoneyUsageStatisticsStorage amountMoneyUsageStatisticsStorage;
    private final AmountMoneyCurrencyStorage amountMoneyCurrencyStorage;
    private final AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever;
    /**
     * The dockable which contains the currently focused
     * {@link OCRSelectComponent} and its surrounding components (most likely a
     * {@link JScrollPane}). More than one dockable with
     * {@link OCRSelectComponent}s and surrounding components can be visible,
     * but only one focused.
     */
    private OCRSelectComponent oCRSelectComponent;
    private final FieldRetriever fieldRetriever = new JPACachedFieldRetriever();
    private final Map<java.lang.reflect.Type, TypeHandler<?, ?,?, ?>> typeHandlerMapping;
    private final DocumentScannerConf documentScannerConf;
    private final Window oCRProgressMonitorParent;
    private final TagStorage tagStorage;
    private final IdApplier idApplier;
    private final Map<Class<?>, WarningHandler<?>> warningHandlers;
    private final MainPanelDockingManager mainPanelDockingManager;
    private final GroupLayout layout;
    private int documentCount = 0;
    private OCREngine oCREngine;
    private final FieldInitializer queryComponentFieldInitializer;
    private final QueryHistoryEntryStorage entryStorage;
    private final JPAFieldRetriever reflectionFormBuilderFieldRetriever;
    private final FieldRetriever readOnlyFieldRetriever;
    private final IdGenerator idGenerator;

    public DefaultMainPanel(Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            PersistenceStorage storage,
            AmountMoneyUsageStatisticsStorage amountMoneyUsageStatisticsStorage,
            AmountMoneyCurrencyStorage amountMoneyAdditionalCurrencyStorage,
            AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever,
            IssueHandler issueHandler,
            ConfirmMessageHandler confirmMessageHandler,
            JFrame dockingControlFrame,
            OCREngine oCREngine,
            Map<java.lang.reflect.Type, TypeHandler<?, ?,?, ?>> typeHandlerMapping,
            DocumentScannerConf documentScannerConf,
            Window oCRProgressMonitorParent,
            TagStorage tagStorage,
            IdApplier idApplier,
            IdGenerator idGenerator,
            Map<Class<?>, WarningHandler<?>> warningHandlers,
            FieldInitializer queryComponentFieldInitializer,
            QueryHistoryEntryStorage entryStorage,
            JPAFieldRetriever reflectionFormBuilderFieldRetriever,
            FieldRetriever readOnlyFieldRetriever) {
        this(entityClasses,
                primaryClassSelection,
                DocumentScanner.VALUE_SETTER_MAPPING_DEFAULT,
                storage,
                amountMoneyUsageStatisticsStorage,
                amountMoneyAdditionalCurrencyStorage,
                amountMoneyExchangeRateRetriever,
                issueHandler,
                confirmMessageHandler,
                dockingControlFrame,
                oCREngine,
                typeHandlerMapping,
                documentScannerConf,
                oCRProgressMonitorParent,
                tagStorage,
                idApplier,
                idGenerator,
                warningHandlers,
                queryComponentFieldInitializer,
                entryStorage,
                reflectionFormBuilderFieldRetriever,
                readOnlyFieldRetriever);
    }

    public DefaultMainPanel(Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            Map<Class<? extends JComponent>, ValueSetter<?,?>> valueSetterMapping,
            PersistenceStorage storage,
            AmountMoneyUsageStatisticsStorage amountMoneyUsageStatisticsStorage,
            AmountMoneyCurrencyStorage amountMoneyCurrencyStorage,
            AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever,
            IssueHandler issueHandler,
            ConfirmMessageHandler confirmMessageHandler,
            JFrame dockingControlFrame,
            OCREngine oCREngine,
            Map<java.lang.reflect.Type, TypeHandler<?, ?,?, ?>> typeHandlerMapping,
            DocumentScannerConf documentScannerConf,
            Window oCRProgressMonitorParent,
            TagStorage tagStorage,
            IdApplier idApplier,
            IdGenerator idGenerator,
            Map<Class<?>, WarningHandler<?>> warningHandlers,
            FieldInitializer queryComponentFieldInitializer,
            QueryHistoryEntryStorage entryStorage,
            JPAFieldRetriever reflectionFormBuilderFieldRetriever,
            FieldRetriever readOnlyFieldRetriever) {
        if(issueHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.issueHandler = issueHandler;
        if(documentScannerConf == null) {
            throw new IllegalArgumentException("documentScannerConf mustn't be "
                    + "null");
        }
        this.confirmMessageHandler = confirmMessageHandler;
        this.documentScannerConf = documentScannerConf;
        if(idApplier == null) {
            throw new IllegalArgumentException("idApplier mustn't be null");
        }
        this.idApplier = idApplier;
        this.entityClasses = entityClasses;
        this.primaryClassSelection = primaryClassSelection;
        this.valueSetterMapping = valueSetterMapping;
        this.storage = storage;
        this.amountMoneyUsageStatisticsStorage = amountMoneyUsageStatisticsStorage;
        this.amountMoneyCurrencyStorage = amountMoneyCurrencyStorage;
        this.amountMoneyExchangeRateRetriever = amountMoneyExchangeRateRetriever;
        this.typeHandlerMapping = typeHandlerMapping;
        this.queryComponentFieldInitializer = queryComponentFieldInitializer;
        this.reflectionFormBuilderFieldRetriever = reflectionFormBuilderFieldRetriever;
        this.readOnlyFieldRetriever = readOnlyFieldRetriever;
        this.idGenerator = idGenerator;
        this.entryStorage = entryStorage;
        this.layout = new GroupLayout(this);
        setLayout(layout);
        this.oCREngine = oCREngine;
        this.oCRProgressMonitorParent = oCRProgressMonitorParent;
        if(tagStorage == null) {
            throw new IllegalArgumentException("tagStorage mustn't be null");
        }
        this.tagStorage = tagStorage;
        this.warningHandlers = warningHandlers;
        this.mainPanelDockingManager = new MainPanelDockingManagerFlexdock();
        this.mainPanelDockingManager.init(dockingControlFrame,
                this);
    }

    @Override
    public void applyValueDetectionServiceSelection() throws ValueDetectionServiceCreationException {
        Collection<Pair<OCRPanel, EntityPanel>> pairs = documentSwitchingMap.values();
        for(Pair<OCRPanel, EntityPanel> pair : pairs) {
            EntityPanel entityPanel = pair.getValue();
            entityPanel.applyValueDetectionServiceSelection();
        }
    }

    @Override
    public void setStorage(PersistenceStorage storage) {
        this.storage = storage;
    }

    @Override
    public void setoCREngine(OCREngine oCREngine) {
        this.oCREngine = oCREngine;
    }

    @Override
    public OCRSelectComponent getoCRSelectComponent() {
        return oCRSelectComponent;
    }

    @Override
    public void setoCRSelectComponent(OCRSelectComponent oCRSelectComponent) {
        this.oCRSelectComponent = oCRSelectComponent;
    }

    @Override
    public Map<OCRSelectComponent, Pair<OCRPanel, EntityPanel>> getDocumentSwitchingMap() {
        return documentSwitchingMap;
    }

    @Override
    public int getDocumentCount() {
        return documentCount;
    }

    @Override
    public void exportActiveDocument(OutputStream out,
            int exportFormat) throws IOException {
        if(exportFormat == EXPORT_FORMAT_PDF) {
            //There seems to be no PNG support in Apache PDFBox, but
            //transforming into JPEG isn't too much of an effort and allows to
            //limit dependencies to Apache PDFBox
            PDDocument document = new PDDocument();
            PDRectangle documentRectangle = PDRectangle.A4;
            for(OCRSelectPanel oCRSelectPanel : oCRSelectComponent.getoCRSelectPanelPanel().getoCRSelectPanels()) {
                ImageWrapper imageWrapper = oCRSelectPanel.getImage();
                PDPage page = new PDPage(documentRectangle);
                document.addPage(page);
                //@TODO: figure out how to create PDImageXObject from stream
                //since this was possible in 1.8 and it's unlikely that there's
                //such a severe regression
                BufferedImage awtImage = ImageIO.read(imageWrapper.getOriginalImageStream());
                PDImageXObject  pdImageXObject = LosslessFactory.createFromImage(document, awtImage);
                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.drawImage(pdImageXObject,
                        0,
                        0,
                        documentRectangle.getWidth(),
                        documentRectangle.getHeight()
                );
                    //in case width and height exceed the size of
                    //documentRectangle, the page is empty (or the content might
                    //be placed outside the page which has the same effect)
                contentStream.setFont(PDType1Font.COURIER, 10);
                contentStream.close();
            }
            document.save(out);
            document.close();
            out.flush();
            out.close();
        }else {
            throw new IllegalArgumentException("export format %s isn't supported");
        }
    }

    /**
     * In order to honour the fact that storing scan data (currently only
     * performed with {@link ScanResult} annotation) is optional, check the
     * determinants for storage of scan data and retrieve necessary data from
     * those.
     * @param entityToEdit the instance determining the initial state of
     * components
     */
    @Override
    public void addDocument(Object entityToEdit) throws DocumentAddException, IOException {
        List<Field> entityClassFields;
        try {
            entityClassFields = reflectionFormBuilderFieldRetriever.retrieveRelevantFields(entityToEdit.getClass());
        } catch (FieldRetrievalException ex) {
            LOGGER.error("unexpected exception during retrieval of fields",
                    ex);
            this.issueHandler.handleUnexpectedException(new ExceptionMessage(ex));
            return;
        }
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
        List<ImageWrapper> images = null;
        if(entityToEditScanResultField != null) {
            //restore OCR select component content from persisted data
            Type entityToEditScanResultFieldType = entityToEditScanResultField.getGenericType();
            if(!(entityToEditScanResultFieldType instanceof ParameterizedType)) {
                throw new IllegalArgumentException(String.format("field %s "
                        + "of class %s annotated with %s, but has no generic "
                        + "type",
                        entityToEditScanResultField.getName(),
                        entityToEdit.getClass(),
                        ScanResult.class));
            }
            ParameterizedType entityToEditScanResultFieldParameterizedType = (ParameterizedType) entityToEditScanResultFieldType;
            if(!(entityToEditScanResultFieldParameterizedType.getRawType() instanceof Class)) {
                throw new IllegalArgumentException(String.format("field %s of "
                        + "class %s annotated with %s has no class as raw "
                        + "type",
                        entityToEditScanResultField.getName(),
                        entityToEdit.getClass(),
                        ScanResult.class));
            }
            if(entityToEditScanResultFieldParameterizedType.getActualTypeArguments().length != 1) {
                throw new IllegalArgumentException(String.format("field %s of "
                        + "class %s annotated with %s has more than one "
                        + "generic type",
                        entityToEditScanResultField.getName(),
                        entityToEdit.getClass(),
                        ScanResult.class));
            }
            if(!ImageWrapper.class.isAssignableFrom((Class<?>) entityToEditScanResultFieldParameterizedType.getActualTypeArguments()[0])) {
                throw new IllegalArgumentException(String.format("field %s of "
                        + "class %s annotated with %s has generic type which "
                        + "is not a superclass of/assignable from %s",
                        entityToEditScanResultField.getName(),
                        entityToEdit.getClass(),
                        ScanResult.class,
                        ImageWrapper.class));
            }
            try {
                //Querying the data of entityToEditScanResultField with a JPQL
                //statement doesn't set the data on entityToEdit
                DocumentScannerFieldInitializer scanDataFieldInitializer = new DocumentScannerFieldInitializer(storage);
                scanDataFieldInitializer.initialize(entityToEdit);
                    //custom (de-)serialization in DefaultImageWrapper doesn't
                    //guarantee that scanData is available if field is lazy
                images = (List<ImageWrapper>) entityToEditScanResultField.get(entityToEdit);
                if(images == null) {
                    LOGGER.debug(String.format("scanData of instance of %s "
                            + "is null, assuming that no data has been "
                            + "persisted",
                            entityToEdit.getClass()));
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

    @Override
    public void addDocument (final List<ImageWrapper> images,
            final File documentFile) throws DocumentAddException, IOException {
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
    public void addDocument (final List<ImageWrapper> images,
            final File documentFile,
            final Object entityToEdit) throws DocumentAddException, IOException {
        Pair<OCRSelectComponent, EntityPanel> oCRSelectComponentPair = addDocumentRoutine(images,
                documentFile,
                entityToEdit,
                null //progressMonitor
        );
        addDocumentDone(oCRSelectComponentPair.getKey(),
                oCRSelectComponentPair.getValue());
    }

    /**
     *
     * @param oCRSelectComponent the created {@link OCRSelectComponent} for the
     * new document
     * @param entityPanel the created {@link EntityPanel} for the new document
     */
    private void addDocumentDone(OCRSelectComponent oCRSelectComponent,
            EntityPanel entityPanel) throws IOException {
        mainPanelDockingManager.addDocumentDockable(DefaultMainPanel.this.oCRSelectComponent,
                oCRSelectComponent);
        //collect oCRSelectComponent's images
        List<ImageWrapper> images = new LinkedList<>();
        for(OCRSelectPanel oCRSelectPanel : oCRSelectComponent.getoCRSelectPanelPanel().getoCRSelectPanels()) {
            images.add(oCRSelectPanel.getImage());
        }
        if(this.documentScannerConf.isAutoOCRValueDetection()) {
            oCRSelectComponent.getValueDetectionButton().setEnabled(false);
            entityPanel.valueDetection(new DefaultOCRSelectPanelPanelFetcher(oCRSelectComponent.getoCRSelectPanelPanel(),
                    oCREngine,
                    documentScannerConf),
                    false //forceRenewal (shouldn't matter here since the
                        //initial list of results has to be empty)
            );
        }
        this.documentCount++;
    }

    @Override
    public void removeActiveDocument() {
        this.mainPanelDockingManager.removeDocument(this.getoCRSelectComponent());
        this.documentCount--;
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
     * @return the created pair of {@link OCRSelectComponent} and
     * {@link EntityPanel}
     */
    private Pair<OCRSelectComponent, EntityPanel> addDocumentRoutine(List<ImageWrapper> images,
            File documentFile,
            Object entityToEdit,
            ProgressMonitor progressMonitor) throws DocumentAddException {
        Pair<OCRSelectComponent, EntityPanel> retValue = null;
        try {
            List<OCRSelectPanel> panels = new LinkedList<>();
            if(images != null) {
                for (ImageWrapper image : images) {
                    @SuppressWarnings("serial")
                    OCRSelectPanel panel = new DefaultOCRSelectPanel(image,
                            documentScannerConf.getPreferredOCRSelectPanelWidth()) {
                        @Override
                        public void mouseReleased(MouseEvent evt) {
                            try {
                                super.mouseReleased(evt);
                                if (this.getDragStart() != null && !this.getDragStart().equals(this.getDragEnd())) {
                                    try {
                                        DefaultMainPanel.this.handleOCRSelection();
                                    } catch (IOException ex) {
                                        issueHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
                                    }
                                }
                            }catch(RuntimeException ex) {
                                LOGGER.error("an unexpected exception occured during mouse release", ex);
                                issueHandler.handleUnexpectedException(new ExceptionMessage(ex));
                            }
                        }
                    };
                    panels.add(panel);
                }
            }
            OCRSelectPanelPanel oCRSelectPanelPanel = new DefaultOCRSelectPanelPanel(panels,
                    documentFile,
                    oCREngine,
                    documentScannerConf);

            DocumentTabOCRResultPanelFetcher oCRResultPanelFetcher = new DocumentTabOCRResultPanelFetcher(oCRSelectPanelPanel //oCRSelectPanelPanel
                    );
            MainPanelScanResultPanelFetcher scanResultPanelFetcher = new MainPanelScanResultPanelFetcher(oCRSelectPanelPanel //oCRSelectPanelPanel
                    );

            AmountMoneyMappingFieldHandlerFactory embeddableFieldHandlerFactory = new AmountMoneyMappingFieldHandlerFactory(amountMoneyUsageStatisticsStorage,
                    amountMoneyCurrencyStorage,
                    amountMoneyExchangeRateRetriever,
                    issueHandler);
            FieldHandler embeddableFieldHandler = new MappingFieldHandler(embeddableFieldHandlerFactory.generateClassMapping(),
                    embeddableFieldHandlerFactory.generatePrimitiveMapping());
            ElementCollectionTypeHandler elementCollectionTypeHandler = new ElementCollectionTypeHandler(typeHandlerMapping,
                    typeHandlerMapping,
                    issueHandler,
                    embeddableFieldHandler,
                    readOnlyFieldRetriever);
            JPAAmountMoneyMappingFieldHandlerFactory jPAAmountMoneyMappingFieldHandlerFactory = JPAAmountMoneyMappingFieldHandlerFactory.create(storage,
                    Constants.INITIAL_QUERY_LIMIT_DEFAULT,
                    issueHandler,
                    amountMoneyUsageStatisticsStorage,
                    amountMoneyCurrencyStorage,
                    amountMoneyExchangeRateRetriever,
                    Constants.BIDIRECTIONAL_HELP_DIALOG_TITLE,
                    readOnlyFieldRetriever);
            ToManyTypeHandler toManyTypeHandler = new ToManyTypeHandler(storage,
                    issueHandler,
                    typeHandlerMapping,
                    typeHandlerMapping,
                    Constants.BIDIRECTIONAL_HELP_DIALOG_TITLE,
                    queryComponentFieldInitializer,
                    entryStorage,
                    readOnlyFieldRetriever);
            ToOneTypeHandler toOneTypeHandler = new ToOneTypeHandler(storage,
                    issueHandler,
                    Constants.BIDIRECTIONAL_HELP_DIALOG_TITLE,
                    queryComponentFieldInitializer,
                    entryStorage,
                    readOnlyFieldRetriever);
            FieldHandler fieldHandler = new DocumentScannerFieldHandler(jPAAmountMoneyMappingFieldHandlerFactory.generateClassMapping(),
                    embeddableFieldHandlerFactory.generateClassMapping(),
                    embeddableFieldHandlerFactory.generatePrimitiveMapping(),
                    elementCollectionTypeHandler,
                    toManyTypeHandler,
                    toOneTypeHandler,
                    issueHandler,
                    confirmMessageHandler,
                    fieldRetriever,
                    oCRResultPanelFetcher,
                    scanResultPanelFetcher,
                    this.documentScannerConf,
                    oCRProgressMonitorParent, //oCRProgressMonitorParent
                    storage,
                    entityClasses,
                    primaryClassSelection,
                    this,
                    tagStorage,
                    idApplier,
                    warningHandlers,
                    queryComponentFieldInitializer,
                    entryStorage,
                    readOnlyFieldRetriever
            );

            Set<Class<?>> entityClasses0;
            Class<?> primaryClassSelection0;
            if(entityToEdit == null) {
                entityClasses0 = entityClasses;
                primaryClassSelection0 = this.primaryClassSelection;
            }else {
                entityClasses0 = new HashSet<>(Arrays.asList(entityToEdit.getClass()));
                primaryClassSelection0 = entityToEdit.getClass();
            }
            ValueDetectionReflectionFormBuilder reflectionFormBuilder = new ValueDetectionReflectionFormBuilder(storage,
                    "Field description",
                    issueHandler,
                    confirmMessageHandler,
                    reflectionFormBuilderFieldRetriever,
                    idApplier,
                    idGenerator,
                    warningHandlers,
                    valueSetterMapping,
                    documentScannerConf);
                //we need one reflection form builder per document in order to
                //allow the auto-value-detection values to be stored separately
                //(and not change ReflectionFormBuilder's interface)
            ReflectionFormPanelTabbedPane reflectionFormPanelTabbedPane = reflectionFormPanelTabbedPane = new ReflectionFormPanelTabbedPane(entityClasses0,
                    primaryClassSelection0,
                    entityToEdit,
                    reflectionFormBuilder,
                    fieldHandler,
                    issueHandler);

            OCRPanel oCRPanel = new DefaultOCRPanel(entityClasses0,
                    reflectionFormPanelTabbedPane,
                    valueSetterMapping,
                    storage,
                    issueHandler,
                    reflectionFormBuilderFieldRetriever,
                    documentScannerConf);
            EntityPanel entityPanel = new DefaultEntityPanel(entityClasses0,
                    primaryClassSelection0,
                    valueSetterMapping,
                    oCRResultPanelFetcher,
                    scanResultPanelFetcher,
                    amountMoneyUsageStatisticsStorage,
                    amountMoneyCurrencyStorage,
                    amountMoneyExchangeRateRetriever,
                    reflectionFormBuilder,
                    fieldHandler,
                    issueHandler,
                    documentScannerConf,
                    reflectionFormPanelTabbedPane);
            OCRSelectComponent oCRSelectComponent = new DefaultOCRSelectComponent(oCRSelectPanelPanel,
                    entityPanel,
                    oCREngine,
                    documentScannerConf,
                    reflectionFormBuilder.getValueDetectionPanels(),
                    documentFile);
            reflectionFormPanelTabbedPane.addReflectionFormPanelTabbedPaneListener((reflectionFormPanel) -> {
                entityPanel.valueDetectionGUI();
            });
            retValue = new ImmutablePair<>(oCRSelectComponent, entityPanel);
            if(progressMonitor == null || !progressMonitor.isCanceled()) {
                documentSwitchingMap.put(oCRSelectComponent,
                        new ImmutablePair<>(oCRPanel, entityPanel));
            }
            return retValue;
        } catch (HeadlessException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
            throwDocumentAddException(progressMonitor);
            throw new DocumentAddException(ex);
        } catch(Throwable ex) {
            //This dramatically facilitates debugging since Java
            //debugger has a lot of problems to halt at uncatched
            //exceptions and a lot of JVMs don't provide debugging
            //symbols.
            String message = "An unexpected exception occured "
                            + "during initialization, see nested exception for "
                            + "details";
            LOGGER.error(message, ex);
            if(progressMonitor != null) {
                progressMonitor.close();
            }
            issueHandler.handleUnexpectedException(new ExceptionMessage(ex));
        }
        return retValue;
    }

    private void handleOCRSelection() throws IOException {
        BufferedImage imageSelection = oCRSelectComponent.getoCRSelectPanelPanel().getSelection();
        if(imageSelection == null) {
            //image panels only contain selections of width or height <= 0 -> skip silently
            return;
        }
        String oCRResult;
        try {
            oCRResult = oCREngine.recognizeImages(new LinkedList<>(Arrays.asList(imageSelection)));
        } catch (OCREngineRecognitionException ex) {
            issueHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
            return;
        }
        OCRPanel oCRPanel = documentSwitchingMap.get(oCRSelectComponent).getLeft();
        oCRPanel.getoCRResultTextArea().setText(oCRResult);
    }

    private void throwDocumentAddException(ProgressMonitor progressMonitor) {
        if(progressMonitor != null) {
            progressMonitor.close();
        }
    }

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
            this.oCRSelectPanelPanelFetcher = new DefaultOCRSelectPanelPanelFetcher(oCRSelectPanelPanel,
                    oCREngine,
                    documentScannerConf);
        }

        @Override
        public String fetch() throws OCREngineRecognitionException {
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


}
