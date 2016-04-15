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
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.EmptyMultipleCDockableFactory;
import bibliothek.gui.dock.common.MultipleCDockable;
import bibliothek.gui.dock.common.MultipleCDockableFactory;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.event.CVetoFocusListener;
import bibliothek.gui.dock.common.intern.CDockable;
import com.thoughtworks.xstream.core.ReferenceByIdMarshaller;
import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math4.stat.descriptive.DescriptiveStatistics;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.components.OCRResultPanelFetcher;
import richtercloud.document.scanner.components.OCRResultPanelFetcherProgressEvent;
import richtercloud.document.scanner.components.OCRResultPanelFetcherProgressListener;
import richtercloud.document.scanner.components.ScanResultPanelFetcher;
import static richtercloud.document.scanner.gui.DocumentScanner.APP_NAME;
import static richtercloud.document.scanner.gui.DocumentScanner.APP_VERSION;
import static richtercloud.document.scanner.gui.DocumentScanner.BIDIRECTIONAL_HELP_DIALOG_TITLE;
import static richtercloud.document.scanner.gui.DocumentScanner.INITIAL_QUERY_LIMIT_DEFAULT;
import static richtercloud.document.scanner.gui.DocumentScanner.generateApplicationWindowTitle;
import richtercloud.document.scanner.gui.conf.OCREngineConf;
import richtercloud.document.scanner.idgenerator.EntityIdGenerator;
import richtercloud.document.scanner.model.Document;
import richtercloud.document.scanner.ocr.OCREngine;
import richtercloud.document.scanner.ocr.OCREngineFactory;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.reflection.form.builder.FieldRetriever;
import richtercloud.reflection.form.builder.ReflectionFormBuilder;
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
*/
public class MainPanel extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(MainPanel.class);
    private final CControl dockingControl;
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
    private Map<CDockable, Pair<DefaultMultipleCDockable, DefaultMultipleCDockable>> documentSwitchingMap = new HashMap<>();
    private Map<Class<?>, ReflectionFormPanel> reflectionFormPanelMap;
    private final Set<Class<?>> entityClasses;
    private final Class<?> primaryClassSelection;
    private final Map<Class<? extends JComponent>, ValueSetter<?>> valueSetterMapping;
    private final EntityManager entityManager;
    private final MessageHandler messageHandler;
    private final ReflectionFormBuilder reflectionFormBuilder;
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
    /*
    internal implementation notes:
    - has to be a DefaultMultipleCDockable instead of a MultipleCDockable in
    order to have getContentPane method available -> in case this field is
    exposed consider managing components in a separate mapping of
    MultipleCDockable -> triple of all involved components
    */
    private DefaultMultipleCDockable oCRSelectComponentDockable;
    private final OCREngineFactory oCREngineFactory;
    private final FieldRetriever fieldRetriever = new JPACachedFieldRetriever();
    private final Map<java.lang.reflect.Type, TypeHandler<?, ?,?, ?>> typeHandlerMapping;
    private final OCREngineConf oCREngineConf;

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
            Map<java.lang.reflect.Type, TypeHandler<?, ?,?, ?>> typeHandlerMapping) {
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
                typeHandlerMapping);
    }

    public MainPanel(Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            Map<Class<? extends JComponent>, ValueSetter<?>> valueSetterMapping,
            EntityManager entityManager,
            AmountMoneyUsageStatisticsStorage amountMoneyUsageStatisticsStorage,
            AmountMoneyCurrencyStorage amountMoneyCurrencyStorage,
            AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever,
            MessageHandler messageHandler,
            JFrame dockingControlFrame,
            OCREngineFactory oCREngineFactory,
            OCREngineConf oCREngineConf,
            Map<java.lang.reflect.Type, TypeHandler<?, ?,?, ?>> typeHandlerMapping) {
        if(messageHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.entityClasses = entityClasses;
        this.primaryClassSelection = primaryClassSelection;
        this.valueSetterMapping = valueSetterMapping;
        this.entityManager = entityManager;
        this.messageHandler = messageHandler;
        this.amountMoneyUsageStatisticsStorage = amountMoneyUsageStatisticsStorage;
        this.amountMoneyCurrencyStorage = amountMoneyCurrencyStorage;
        this.amountMoneyExchangeRateRetriever = amountMoneyExchangeRateRetriever;
        this.typeHandlerMapping = typeHandlerMapping;
        this.dockingControl = new CControl(dockingControlFrame);
        this.dockingControl.addVetoFocusListener(new CVetoFocusListener() {
            /**
             * Reference to the {@link CDockable} which will lose the focus.
             */
            /*
            internal implementation notes:
            - not too elegant, but since it's kept in this CVetoFocusListener
            it's ok, otherwise DockingFrames ought to be investigated for a more
            elegant solution for replacement of dockables
            */
            private CDockable willLoseFocus;
            @Override
            public boolean willGainFocus(CDockable cd) {
                if(this.willLoseFocus == null) {
                    //not switching from a OCRSelectComponent dockable
                    return true;
                }
                if(cd.equals(willLoseFocus)) {
                    //no switch of documents
                    return true;
                }
                Pair<DefaultMultipleCDockable, DefaultMultipleCDockable> documentSwitchingPair = documentSwitchingMap.get(cd);
                if(documentSwitchingPair != null) {
                    MultipleCDockable oCRResultPanelDockable = documentSwitchingPair.getLeft();
                    MultipleCDockable entityPanelDockable = documentSwitchingPair.getRight();
                    Pair<DefaultMultipleCDockable, DefaultMultipleCDockable> documentSwitchingPairOld = documentSwitchingMap.get(willLoseFocus);
                    MultipleCDockable oCRPanelDockableOld = documentSwitchingPairOld.getLeft();
                    MultipleCDockable entityPanelDockableOld = documentSwitchingPairOld.getRight();
                    dockingControl.replace(oCRPanelDockableOld, oCRResultPanelDockable);
                    dockingControl.replace(entityPanelDockableOld, entityPanelDockable);
                }
                return true;
            }

            @Override
            public boolean willLoseFocus(CDockable cd) {
                if(documentSwitchingMap.keySet().contains(cd)) {
                    this.willLoseFocus = cd;
                }
                return true;
            }
        });
        this.reflectionFormBuilder = new JPAReflectionFormBuilder(entityManager,
                DocumentScanner.generateApplicationWindowTitle("Field description",
                        DocumentScanner.APP_NAME,
                        DocumentScanner.APP_VERSION),
                messageHandler,
                new JPACachedFieldRetriever());
        this.initComponents();
        this.add(dockingControl.getContentArea(), BorderLayout.CENTER);
        this.oCREngineFactory = oCREngineFactory;
        this.oCREngineConf = oCREngineConf;
    }

    public List<BufferedImage> retrieveImages(final File documentFile) throws DocumentAddException, InterruptedException, ExecutionException {
        if(documentFile == null) {
            throw new IllegalArgumentException("documentFile mustn't be null");
        }
        final ProgressMonitor progressMonitor = new ProgressMonitor(this,
                "Generating new document tab", //message
                null, //note
                0,
                100);
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
                        if(progressMonitor.isCanceled()) {
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
                progressMonitor.setProgress(100);
            }
        };
        worker.execute();
        progressMonitor.setProgress(1);
        List<BufferedImage> retValue = worker.get();
        return retValue;
    }

    public void addDocument (final List<BufferedImage> images, final String title) throws DocumentAddException {
        if(images == null) {
            throw new IllegalArgumentException("images mustn't be null");
        }
        final ProgressMonitor progressMonitor = new ProgressMonitor(this,
                "Generating new document tab", //message
                null, //note
                0,
                100);
        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    List<OCRSelectPanel> panels = new LinkedList<>();
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

                    OCRSelectComponent oCRSelectComponent = new OCRSelectComponent(panels);

                    OCRResultPanelFetcher oCRResultPanelFetcher = new DocumentTabOCRResultPanelFetcher(oCRSelectComponent,
                            oCREngineFactory);
                    ScanResultPanelFetcher scanResultPanelFetcher = new DocumentTabScanResultPanelFetcher(oCRSelectComponent);

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
                            scanResultPanelFetcher);

                    if(MainPanel.this.reflectionFormPanelMap == null) {
                        MainPanel.this.reflectionFormPanelMap = new HashMap<>();
                        for(Class<?> entityClass : entityClasses) {
                            ReflectionFormPanel reflectionFormPanel;
                            try {
                                reflectionFormPanel = reflectionFormBuilder.transformEntityClass(entityClass,
                                        null, //entityToUpdate
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
                    }

                    JScrollPane oCRSelectComponentScrollPane = new JScrollPane(oCRSelectComponent);
                    OCRPanel oCRPanel = new OCRPanel(entityClasses,
                            reflectionFormPanelMap,
                            valueSetterMapping,
                            entityManager,
                            messageHandler,
                            reflectionFormBuilder);
                    EntityPanel entityPanel = new EntityPanel(entityClasses,
                            primaryClassSelection,
                            reflectionFormPanelMap,
                            fieldHandler,
                            valueSetterMapping,
                            entityManager,
                            oCRResultPanelFetcher,
                            scanResultPanelFetcher,
                            amountMoneyUsageStatisticsStorage,
                            amountMoneyCurrencyStorage,
                            messageHandler);
                    MainPanel.this.oCRSelectComponentDockable = new DefaultMultipleCDockable(null,
                            title,
                            oCRSelectComponentScrollPane);
                    DefaultMultipleCDockable oCRPanelDockable = new DefaultMultipleCDockable (null,
                            "OCR result",
                            oCRPanel);
                    DefaultMultipleCDockable entityPanelDockable = new DefaultMultipleCDockable(null,
                            "Entity editing",
                            entityPanel);
                    if(!progressMonitor.isCanceled()) {
                        dockingControl.addDockable(oCRSelectComponentDockable);
                        dockingControl.addDockable(oCRPanelDockable);
                        dockingControl.addDockable(entityPanelDockable);
                        documentSwitchingMap.put(oCRSelectComponentDockable,
                                new ImmutablePair<>(oCRPanelDockable, entityPanelDockable));
                            //needs to be called before setVisible
                        oCRSelectComponentDockable.setLocation(CLocation.base().normalWest(0.4));
                        oCRPanelDockable.setLocation(CLocation.base().normalEast(0.6));
                        entityPanelDockable.setLocation(CLocation.base().normalSouth(0.6));
                        //invoke CDockable.setVisible in done in order to avoid
                        //ConcurrentModificationException
                    }
                } catch (HeadlessException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException ex) {
                    progressMonitor.close();
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
                    progressMonitor.close();
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
                return null;
            }

            @Override
            protected void done() {
                progressMonitor.close();
                //invoke CDockable.setVisible in done in order to avoid
                //ConcurrentModificationException
                Pair<DefaultMultipleCDockable, DefaultMultipleCDockable> documentSwitchingPair = documentSwitchingMap.get(oCRSelectComponentDockable);
                MultipleCDockable oCRPanelDockable = documentSwitchingPair.getLeft();
                MultipleCDockable entityPanelDockable = documentSwitchingPair.getRight();
                oCRSelectComponentDockable.setVisible(true);
                oCRPanelDockable.setVisible(true);
                entityPanelDockable.setVisible(true);
            }
        };
        worker.execute();
        progressMonitor.setProgress(1); //ProgressMonitor dialog blocks until SwingWorker.done
            //is invoked
    }

    private void handleOCRSelection() {
        JScrollPane oCRSelectComponentScrollPane = (JScrollPane) oCRSelectComponentDockable.getContentPane().getComponents()[0];
        OCRSelectComponent oCRSelectComponent = (OCRSelectComponent) oCRSelectComponentScrollPane.getViewport().getComponents()[0];
        BufferedImage imageSelection = oCRSelectComponent.getSelection();
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
        OCRPanel oCRPanel = (OCRPanel) documentSwitchingMap.get(oCRSelectComponentDockable).getLeft().getContentPane().getComponents()[0];
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
        private final List<Double> stringBufferLengths = new ArrayList<>();
        private final OCRSelectComponent oCRSelectComponent;
        private final Set<OCRResultPanelFetcherProgressListener> progressListeners = new HashSet<>();
        private boolean cancelRequested = false;
        /**
         * Since {@link OCRSelectPanel} has an immutable {@code image} property
         * it can be used well as cache map key.
         */
        private final Map<OCRSelectPanel, String> fetchCache = new HashMap<>();
        /**
         * Record all used {@link OCREngine}s in order to be able to cancel if
         * {@link #cancelFetch() } is invoked.
         */
        /*
        internal implementation notes:
        - is a Queue in order to be able to cancel as fast as possible
        */
        private Queue<OCREngine> usedEngines = new LinkedList<>();
        /**
         * The factory to create one or multiple {@link OCREngine}s for linear
         * or parallel fetching.
         */
        private final OCREngineFactory oCREngineFactory;

        DocumentTabOCRResultPanelFetcher(OCRSelectComponent oCRSelectComponent,
                OCREngineFactory oCREngineFactory) {
            this.oCRSelectComponent = oCRSelectComponent;
            this.oCREngineFactory = oCREngineFactory;
        }

        @Override
        public String fetch() {
            //estimate the initial StringBuilder size based on the median
            //of all prior OCR results (string length) (and 1000 initially)
            int stringBufferLengh;
            cancelRequested = false;
            if (this.stringBufferLengths.isEmpty()) {
                stringBufferLengh = 1_000;
            } else {
                DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(this.stringBufferLengths.toArray(new Double[this.stringBufferLengths.size()]));
                stringBufferLengh = ((int) descriptiveStatistics.getPercentile(.5)) + 1;
            }
            this.stringBufferLengths.add((double) stringBufferLengh);
            StringBuilder retValueBuilder = new StringBuilder(stringBufferLengh);
            int i=0;
            List<OCRSelectPanel> imagePanels = oCRSelectComponent.getImagePanels();
            Queue<Pair<OCRSelectPanel, FutureTask<String>>> threadQueue = new LinkedList<>();
            Executor executor = Executors.newCachedThreadPool();
            //check in loop whether cache can be used, otherwise enqueue started
            //SwingWorkers; after loop wait for SwingWorkers until queue is
            //empty and append to retValueBuilder (if cache has been used
            //(partially) queue will be empty)
            for (final OCRSelectPanel imagePanel : imagePanels) {
                if(cancelRequested) {
                    //no need to notify progress listener
                    break;
                }
                String oCRResult = fetchCache.get(imagePanel);
                if(oCRResult != null) {
                    LOGGER.info(String.format("using cached OCR result for image %d of current OCR select component", i));
                    retValueBuilder.append(oCRResult);
                    for(OCRResultPanelFetcherProgressListener progressListener: progressListeners) {
                        progressListener.onProgressUpdate(new OCRResultPanelFetcherProgressEvent(oCRResult, i/imagePanels.size()));
                    }
                    i += 1;
                }else {
                    FutureTask<String> worker = new FutureTask<>(new Callable<String>() {
                        @Override
                        public String call() throws Exception {
                            OCREngine oCREngine = oCREngineFactory.create(oCREngineConf);
                            usedEngines.add(oCREngine);
                            String oCRResult = oCREngine.recognizeImage(imagePanel.getImage());
                            if(oCRResult == null) {
                                //indicates that the OCREngine.recognizeImage has been aborted
                                if(cancelRequested) {
                                    //no need to notify progress listener
                                    return null;
                                }
                            }
                            return oCRResult;
                        }
                    });
                    executor.execute(worker);
                    threadQueue.add(new ImmutablePair<>(imagePanel, worker));
                }
            }
            while(!threadQueue.isEmpty()) {
                Pair<OCRSelectPanel, FutureTask<String>> threadQueueHead = threadQueue.poll();
                String oCRResult;
                try {
                    oCRResult = threadQueueHead.getValue().get();
                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
                retValueBuilder.append(oCRResult);
                fetchCache.put(threadQueueHead.getKey(), oCRResult);
                for(OCRResultPanelFetcherProgressListener progressListener: progressListeners) {
                    progressListener.onProgressUpdate(new OCRResultPanelFetcherProgressEvent(oCRResult, i/imagePanels.size()));
                }
                i += 1;
            }
            String retValue = retValueBuilder.toString();
            return retValue;
        }

        @Override
        public void cancelFetch() {
            this.cancelRequested = true;
            while(!usedEngines.isEmpty()) {
                OCREngine usedEngine = usedEngines.poll();
                usedEngine.cancelRecognizeImage();
            }
        }

        @Override
        public void addProgressListener(OCRResultPanelFetcherProgressListener progressListener) {
            this.progressListeners.add(progressListener);
        }

        @Override
        public void removeProgressListener(OCRResultPanelFetcherProgressListener progressListener) {
            this.progressListeners.remove(progressListener);
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
    // End of variables declaration//GEN-END:variables

}
