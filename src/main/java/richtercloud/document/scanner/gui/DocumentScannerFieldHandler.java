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

import java.awt.Window;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.swing.JComponent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import richtercloud.document.scanner.components.OCRResultPanel;
import richtercloud.document.scanner.components.OCRResultPanelFetcher;
import richtercloud.document.scanner.components.OCRResultPanelUpdateEvent;
import richtercloud.document.scanner.components.OCRResultPanelUpdateListener;
import richtercloud.document.scanner.components.ScanResultPanel;
import richtercloud.document.scanner.components.ScanResultPanelFetcher;
import richtercloud.document.scanner.components.ScanResultPanelUpdateEvent;
import richtercloud.document.scanner.components.ScanResultPanelUpdateListener;
import richtercloud.document.scanner.components.WorkflowItemTreePanel;
import richtercloud.document.scanner.components.WorkflowItemTreePanelUpdateEvent;
import richtercloud.document.scanner.components.WorkflowItemTreePanelUpdateListener;
import richtercloud.document.scanner.components.annotations.CommunicationTree;
import richtercloud.document.scanner.components.annotations.OCRResult;
import richtercloud.document.scanner.components.annotations.ScanResult;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.model.WorkflowItem;
import richtercloud.document.scanner.components.tag.TagComponent;
import richtercloud.document.scanner.components.tag.TagComponentUpdateEvent;
import richtercloud.document.scanner.components.tag.TagComponentUpdateListener;
import richtercloud.document.scanner.components.tag.TagStorage;
import richtercloud.document.scanner.components.annotations.Tags;
import richtercloud.document.scanner.ifaces.MainPanel;
import richtercloud.message.handler.ConfirmMessageHandler;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.ComponentHandler;
import richtercloud.reflection.form.builder.FieldRetriever;
import richtercloud.reflection.form.builder.components.money.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetriever;
import richtercloud.reflection.form.builder.components.money.AmountMoneyUsageStatisticsStorage;
import richtercloud.reflection.form.builder.fieldhandler.FieldHandler;
import richtercloud.reflection.form.builder.fieldhandler.FieldHandlingException;
import richtercloud.reflection.form.builder.fieldhandler.FieldUpdateEvent;
import richtercloud.reflection.form.builder.fieldhandler.FieldUpdateListener;
import richtercloud.reflection.form.builder.fieldhandler.MappedFieldUpdateEvent;
import richtercloud.reflection.form.builder.fieldhandler.MappingFieldHandler;
import richtercloud.reflection.form.builder.fieldhandler.factory.AmountMoneyMappingFieldHandlerFactory;
import richtercloud.reflection.form.builder.jpa.JPAReflectionFormBuilder;
import richtercloud.reflection.form.builder.jpa.WarningHandler;
import richtercloud.reflection.form.builder.jpa.fieldhandler.JPAMappingFieldHandler;
import richtercloud.reflection.form.builder.jpa.fieldhandler.factory.JPAAmountMoneyMappingFieldHandlerFactory;
import richtercloud.reflection.form.builder.jpa.idapplier.IdApplier;
import richtercloud.reflection.form.builder.jpa.typehandler.ElementCollectionTypeHandler;
import richtercloud.reflection.form.builder.jpa.typehandler.ToManyTypeHandler;
import richtercloud.reflection.form.builder.jpa.typehandler.ToOneTypeHandler;
import richtercloud.reflection.form.builder.typehandler.TypeHandler;

/**
 *
 * @author richter
 */
public class DocumentScannerFieldHandler extends JPAMappingFieldHandler<Object, FieldUpdateEvent<Object>> {
    private final static ComponentHandler<OCRResultPanel> OCR_RESULT_PANEL_COMPONENT_RESETTABLE = new ComponentHandler<OCRResultPanel>() {
        @Override
        public void reset(OCRResultPanel component) {
            component.reset();
        }
    };
    private final static ComponentHandler<ScanResultPanel> SCAN_RESULT_PANEL_COMPONENT_RESETTABLE = new ComponentHandler<ScanResultPanel>() {
        @Override
        public void reset(ScanResultPanel component) {
            component.reset();
        }
    };
    private final static ComponentHandler<WorkflowItemTreePanel> COMMUNICATION_TREE_PANEL_COMPONENT_HANDLER = new ComponentHandler<WorkflowItemTreePanel>() {
        @Override
        public void reset(WorkflowItemTreePanel component) {
            component.reset();
        }
    };
    private final static ComponentHandler<TagComponent> TAG_COMPONENT_HANDLER = new ComponentHandler<TagComponent>() {
        @Override
        public void reset(TagComponent component) {
            component.reset();
        }
    };
    private final OCRResultPanelFetcher oCRResultPanelFetcher;
    private final ScanResultPanelFetcher scanResultPanelFetcher;
    private final DocumentScannerConf documentScannerConf;
    private final Window oCRProgressMonitorParent;
    private final EntityManager entityManager;
    private final Set<Class<?>> entityClasses;
    private final Class<?> primaryClassSelection;
    private final MainPanel mainPanel;
    private final TagStorage tagStorage;
    private final ConfirmMessageHandler confirmMessageHandler;
    private final Map<Class<?>, WarningHandler<?>> warningHandlers;

    /**
     * A factory method which avoid creation of some type handlers by callers.
     * @param classMapping
     * @param embeddableMapping
     * @param primitiveMapping
     * @param amountMoneyUsageStatisticsStorage
     * @param amountMoneyCurrencyStorage
     * @param amountMoneyExchangeRateRetriever
     * @param messageHandler
     * @param confirmMessageHandler
     * @param typeHandlerMapping
     * @param entityManager
     * @param fieldRetriever
     * @param oCRResultPanelFetcher
     * @param scanResultPanelFetcher
     * @param documentScannerConf
     * @param oCRProgressMonitorParent
     * @param entityClasses
     * @param primaryClassSelection
     * @param mainPanel
     * @param tagStorage
     * @param idApplier
     * @param warningHandlers
     * @param initialQueryLimit
     * @param bidirectionalHelpDialogTitle
     * @return
     */
    public static DocumentScannerFieldHandler create(AmountMoneyUsageStatisticsStorage amountMoneyUsageStatisticsStorage,
            AmountMoneyCurrencyStorage amountMoneyCurrencyStorage,
            AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever,
            MessageHandler messageHandler,
            ConfirmMessageHandler confirmMessageHandler,
            Map<java.lang.reflect.Type, TypeHandler<?, ?,?, ?>> typeHandlerMapping,
            EntityManager entityManager,
            FieldRetriever fieldRetriever,
            OCRResultPanelFetcher oCRResultPanelFetcher,
            ScanResultPanelFetcher scanResultPanelFetcher,
            DocumentScannerConf documentScannerConf,
            Window oCRProgressMonitorParent,
            Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            MainPanel mainPanel,
            TagStorage tagStorage,
            IdApplier idApplier,
            Map<Class<?>, WarningHandler<?>> warningHandlers,
            int initialQueryLimit,
            String bidirectionalHelpDialogTitle) {
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
                initialQueryLimit,
                messageHandler,
                amountMoneyUsageStatisticsStorage,
                amountMoneyCurrencyStorage,
                amountMoneyExchangeRateRetriever,
                bidirectionalHelpDialogTitle);
        ToManyTypeHandler toManyTypeHandler = new ToManyTypeHandler(entityManager,
                messageHandler,
                typeHandlerMapping,
                typeHandlerMapping,
                bidirectionalHelpDialogTitle);
        ToOneTypeHandler toOneTypeHandler = new ToOneTypeHandler(entityManager,
                messageHandler,
                bidirectionalHelpDialogTitle);
        DocumentScannerFieldHandler retValue = new DocumentScannerFieldHandler(jPAAmountMoneyMappingFieldHandlerFactory.generateClassMapping(),
                embeddableFieldHandlerFactory.generateClassMapping(),
                embeddableFieldHandlerFactory.generatePrimitiveMapping(),
                elementCollectionTypeHandler,
                toManyTypeHandler,
                toOneTypeHandler,
                messageHandler,
                confirmMessageHandler,
                fieldRetriever,
                oCRResultPanelFetcher,
                scanResultPanelFetcher,
                documentScannerConf,
                oCRProgressMonitorParent,
                entityManager,
                entityClasses,
                primaryClassSelection,
                mainPanel,
                tagStorage,
                idApplier,
                warningHandlers);
        return retValue;
    }

    public DocumentScannerFieldHandler(Map<Type, FieldHandler<?, ?, ?, ?>> classMapping,
            Map<Type, FieldHandler<?, ?, ?, ?>> embeddableMapping,
            Map<Class<?>, FieldHandler<?, ?, ?, ?>> primitiveMapping,
            ElementCollectionTypeHandler elementCollectionTypeHandler,
            ToManyTypeHandler toManyTypeHandler,
            ToOneTypeHandler toOneTypeHandler,
            MessageHandler messageHandler,
            ConfirmMessageHandler confirmMessageHandler,
            FieldRetriever fieldRetriever,
            OCRResultPanelFetcher oCRResultPanelFetcher,
            ScanResultPanelFetcher scanResultPanelFetcher,
            DocumentScannerConf documentScannerConf,
            Window oCRProgressMonitorParent,
            EntityManager entityManager,
            Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            MainPanel mainPanel,
            TagStorage tagStorage,
            IdApplier idApplier,
            Map<Class<?>, WarningHandler<?>> warningHandlers) {
        super(classMapping,
                embeddableMapping,
                primitiveMapping,
                elementCollectionTypeHandler,
                toManyTypeHandler,
                toOneTypeHandler,
                messageHandler,
                fieldRetriever,
                idApplier);
        this.oCRResultPanelFetcher = oCRResultPanelFetcher;
        this.scanResultPanelFetcher = scanResultPanelFetcher;
        this.documentScannerConf = documentScannerConf;
        this.oCRProgressMonitorParent = oCRProgressMonitorParent;
        if(entityManager == null) {
            throw new IllegalArgumentException("entityManager mustn't be null");
        }
        this.entityManager = entityManager;
        this.entityClasses = entityClasses;
        this.primaryClassSelection = primaryClassSelection;
        this.mainPanel = mainPanel;
        this.tagStorage = tagStorage;
        this.confirmMessageHandler = confirmMessageHandler;
        this.warningHandlers = warningHandlers;
    }

    @Override
    protected Pair<JComponent, ComponentHandler<?>> handle0(Field field,
            Object instance,
            final FieldUpdateListener updateListener,
            JPAReflectionFormBuilder reflectionFormBuilder) throws IllegalArgumentException,
            IllegalAccessException,
            FieldHandlingException,
            InvocationTargetException,
            NoSuchMethodException,
            InstantiationException {
        if(field == null) {
            throw new IllegalArgumentException("field mustn't be null");
        }
        if(field.getAnnotation(OCRResult.class) != null) {
            String fieldValue = (String) field.get(instance);
            OCRResultPanel retValue = new OCRResultPanel(oCRResultPanelFetcher,
                    fieldValue,
                    getMessageHandler(),
                    oCRProgressMonitorParent);
            retValue.addUpdateListener(new OCRResultPanelUpdateListener() {
                @Override
                public void onUpdate(OCRResultPanelUpdateEvent event) {
                    updateListener.onUpdate(new FieldUpdateEvent<>(event.getNewValue()));
                }
            });
            if(this.documentScannerConf.isAutoSaveOCRData()
                    && fieldValue == null) {
                //if fieldValue != null there's no need to start OCR (i.e. if
                //an invalid value has been persisted it's up to the user to
                //(re)start OCR manually
                retValue.startOCR();
            }
            return new ImmutablePair<JComponent, ComponentHandler<?>>(retValue,
                    OCR_RESULT_PANEL_COMPONENT_RESETTABLE);
        }
        if(field.getAnnotation(ScanResult.class) != null) {
            byte[] fieldValue = (byte[]) field.get(instance);
            ScanResultPanel retValue = new ScanResultPanel(scanResultPanelFetcher,
                    fieldValue);
            retValue.addUpdateListerner(new ScanResultPanelUpdateListener() {
                @Override
                public void onUpdate(ScanResultPanelUpdateEvent event) {
                    updateListener.onUpdate(new FieldUpdateEvent<>(event.getNewValue()));
                }
            });
            if(documentScannerConf.isAutoSaveImageData()
                    && fieldValue == null) {
                //if fieldValue != null there's no need to save the image data
                retValue.save();
            }
            return new ImmutablePair<JComponent, ComponentHandler<?>>(retValue,
                    SCAN_RESULT_PANEL_COMPONENT_RESETTABLE);
        }
        if(field.getAnnotation(CommunicationTree.class) != null) {
            List<WorkflowItem> fieldValue = (List<WorkflowItem>) field.get(instance);
            WorkflowItemTreePanel retValue = new WorkflowItemTreePanel(entityManager,
                    fieldValue,
                    getMessageHandler(),
                    confirmMessageHandler,
                    reflectionFormBuilder,
                    entityClasses,
                    primaryClassSelection,
                    mainPanel,
                    getIdApplier(),
                    warningHandlers);
            retValue.addUpdateListener(new WorkflowItemTreePanelUpdateListener() {
                @Override
                public void onUpdate(WorkflowItemTreePanelUpdateEvent event) {
                    updateListener.onUpdate(new MappedFieldUpdateEvent<>(event.getNewValue(),
                            event.getMappedField() //mappedField
                    ));
                }
            });
            return new ImmutablePair<JComponent, ComponentHandler<?>>(retValue,
                    COMMUNICATION_TREE_PANEL_COMPONENT_HANDLER);
        }
        if(field.getAnnotation(Tags.class) != null) {
            Set<String> fieldValue = (Set<String>) field.get(instance);
            TagComponent retValue = new TagComponent(tagStorage, fieldValue);
            retValue.addUpdateListener(new TagComponentUpdateListener() {
                @Override
                public void onUpdate(TagComponentUpdateEvent updateEvent) {
                    updateListener.onUpdate(new FieldUpdateEvent(updateEvent.getNewValue()));
                }
            });
            return new ImmutablePair<JComponent, ComponentHandler<?>>(retValue,
                    TAG_COMPONENT_HANDLER);
        }
        return super.handle0(field, instance, updateListener, reflectionFormBuilder);
    }

}
