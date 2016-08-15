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
import java.util.Map;
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
import richtercloud.document.scanner.components.annotations.OCRResult;
import richtercloud.document.scanner.components.annotations.ScanResult;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.reflection.form.builder.ComponentHandler;
import richtercloud.reflection.form.builder.FieldRetriever;
import richtercloud.reflection.form.builder.fieldhandler.FieldHandler;
import richtercloud.reflection.form.builder.fieldhandler.FieldHandlingException;
import richtercloud.reflection.form.builder.fieldhandler.FieldUpdateEvent;
import richtercloud.reflection.form.builder.fieldhandler.FieldUpdateListener;
import richtercloud.reflection.form.builder.jpa.IdGenerator;
import richtercloud.reflection.form.builder.jpa.JPAReflectionFormBuilder;
import richtercloud.reflection.form.builder.jpa.fieldhandler.JPAMappingFieldHandler;
import richtercloud.reflection.form.builder.jpa.typehandler.ElementCollectionTypeHandler;
import richtercloud.reflection.form.builder.jpa.typehandler.ToManyTypeHandler;
import richtercloud.reflection.form.builder.jpa.typehandler.ToOneTypeHandler;
import richtercloud.reflection.form.builder.message.MessageHandler;

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
    private final OCRResultPanelFetcher oCRResultPanelFetcher;
    private final ScanResultPanelFetcher scanResultPanelFetcher;
    private final DocumentScannerConf documentScannerConf;
    private final Window oCRProgressMonitorParent;

    public DocumentScannerFieldHandler(Map<Type, FieldHandler<?, ?, ?, ?>> classMapping,
            Map<Type, FieldHandler<?, ?, ?, ?>> embeddableMapping,
            Map<Class<?>, FieldHandler<?, ?, ?, ?>> primitiveMapping,
            ElementCollectionTypeHandler elementCollectionTypeHandler,
            ToManyTypeHandler toManyTypeHandler,
            ToOneTypeHandler toOneTypeHandler,
            IdGenerator idGenerator,
            MessageHandler messageHandler,
            FieldRetriever fieldRetriever,
            OCRResultPanelFetcher oCRResultPanelFetcher,
            ScanResultPanelFetcher scanResultPanelFetcher,
            DocumentScannerConf documentScannerConf,
            Window oCRProgressMonitorParent) {
        super(classMapping,
                embeddableMapping,
                primitiveMapping,
                elementCollectionTypeHandler,
                toManyTypeHandler,
                toOneTypeHandler,
                idGenerator,
                messageHandler,
                fieldRetriever);
        this.oCRResultPanelFetcher = oCRResultPanelFetcher;
        this.scanResultPanelFetcher = scanResultPanelFetcher;
        this.documentScannerConf = documentScannerConf;
        this.oCRProgressMonitorParent = oCRProgressMonitorParent;
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
        if(field.getAnnotation(OCRResult.class) != null) {
            //keep field == null test here because it's unlikely to be
            //executed very often
            if(field == null) {
                throw new IllegalArgumentException("field mustn't be null");
            }
            String fieldValue = (String) field.get(instance);
            OCRResultPanel retValue = new OCRResultPanel(oCRResultPanelFetcher,
                    fieldValue,
                    getMessageHandler(),
                    this.documentScannerConf.isAutoSaveOCRData(),
                    oCRProgressMonitorParent);
            retValue.addUpdateListener(new OCRResultPanelUpdateListener() {
                @Override
                public void onUpdate(OCRResultPanelUpdateEvent event) {
                    updateListener.onUpdate(new FieldUpdateEvent<>(event.getNewValue()));
                }
            });
            return new ImmutablePair<JComponent, ComponentHandler<?>>(retValue,
                    OCR_RESULT_PANEL_COMPONENT_RESETTABLE);
        }
        if(field.getAnnotation(ScanResult.class) != null) {
            //keep field == null test here because it's unlikely to be
            //executed very often
            if(field == null) {
                throw new IllegalArgumentException("fieldClass mustn't be null");
            }
            byte[] fieldValue = (byte[]) field.get(instance);
            ScanResultPanel retValue = new ScanResultPanel(scanResultPanelFetcher,
                    fieldValue,
                    this.documentScannerConf.isAutoSaveImageData());
            retValue.addUpdateListerner(new ScanResultPanelUpdateListener() {
                @Override
                public void onUpdate(ScanResultPanelUpdateEvent event) {
                    updateListener.onUpdate(new FieldUpdateEvent<>(event.getNewValue()));
                }
            });
            return new ImmutablePair<JComponent, ComponentHandler<?>>(retValue,
                    SCAN_RESULT_PANEL_COMPONENT_RESETTABLE);
        }
        return super.handle0(field, instance, updateListener, reflectionFormBuilder);
    }

}
