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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.components.AutoOCRValueDetectionReflectionFormBuilder;
import richtercloud.document.scanner.components.OCRResultPanelFetcher;
import richtercloud.document.scanner.components.ScanResultPanelFetcher;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.ifaces.EntityPanel;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanelFetcher;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.document.scanner.valuedetectionservice.AutoOCRValueDetectionResult;
import richtercloud.document.scanner.valuedetectionservice.DelegatingAutoOCRValueDetectionService;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.components.money.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetriever;
import richtercloud.reflection.form.builder.components.money.AmountMoneyUsageStatisticsStorage;
import richtercloud.reflection.form.builder.fieldhandler.FieldHandler;

/**
 *
 * @author richter
 */
public class DefaultEntityPanel extends EntityPanel {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultEntityPanel.class);
    private final DelegatingAutoOCRValueDetectionService autoOCRValueDetectionService;
    private final Set<Class<?>> entityClasses;
    private final AutoOCRValueDetectionReflectionFormBuilder reflectionFormBuilder;
    private final Map<Class<? extends JComponent>, ValueSetter<?,?>> valueSetterMapping;
    private final MessageHandler messageHandler;
    private final DocumentScannerConf documentScannerConf;
    private ReflectionFormPanelTabbedPane entityCreationTabbedPane;

    /**
     * Creates new form EntityPanel
     */
    public DefaultEntityPanel(Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            Map<Class<? extends JComponent>, ValueSetter<?,?>> valueSetterMapping,
            final OCRResultPanelFetcher oCRResultPanelRetriever,
            final ScanResultPanelFetcher scanResultPanelRetriever,
            AmountMoneyUsageStatisticsStorage amountMoneyUsageStatisticsStorage,
            AmountMoneyCurrencyStorage amountMoneyAdditionalCurrencyStorage,
            AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever,
            AutoOCRValueDetectionReflectionFormBuilder reflectionFormBuilder,
            FieldHandler fieldHandler,
            MessageHandler messageHandler,
            DocumentScannerConf documentScannerConf,
            ReflectionFormPanelTabbedPane reflectionFormPanelTabbedPane) throws InstantiationException,
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            NoSuchMethodException {
        if(fieldHandler == null) {
            throw new IllegalArgumentException("fieldHandler mustn't be null");
        }
        entityCreationTabbedPane = reflectionFormPanelTabbedPane;
        this.initComponents();
        if(entityClasses == null) {
            throw new IllegalArgumentException("entityClasses mustn't be null");
        }
        if(entityClasses.isEmpty()) {
            throw new IllegalArgumentException("entityClass mustn't be empty");
        }
        if(!entityClasses.contains(primaryClassSelection)) {
            throw new IllegalArgumentException(String.format("primaryClassSelection '%s' has to be contained in entityClasses", primaryClassSelection));
        }
        this.entityClasses = entityClasses;
        if(reflectionFormBuilder == null) {
            throw new IllegalArgumentException("reflectionFormBuilder mustn't be null");
        }
        this.reflectionFormBuilder = reflectionFormBuilder;
        if(valueSetterMapping == null) {
            throw new IllegalArgumentException("valueSetterMapping mustn't be null");
        }
        this.valueSetterMapping = valueSetterMapping;
        if(messageHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.messageHandler = messageHandler;
        if(documentScannerConf == null) {
            throw new IllegalArgumentException("documentScannerConf mustn't be null");
        }
        this.documentScannerConf = documentScannerConf;
        this.autoOCRValueDetectionService = new DelegatingAutoOCRValueDetectionService(amountMoneyAdditionalCurrencyStorage,
                amountMoneyExchangeRateRetriever);
    }

    public JTabbedPane getEntityCreationTabbedPane() {
        return entityCreationTabbedPane;
    }

    /**
     * Store for the last results of
     * {@link #autoOCRValueDetectionNonGUI(richtercloud.document.scanner.gui.OCRSelectPanelPanelFetcher, boolean) }
     * which one might display without retrieving them again from the OCR
     * result.
     */
    private List<AutoOCRValueDetectionResult<?>> detectionResults;

    @Override
    public void autoOCRValueDetection(OCRSelectPanelPanelFetcher oCRSelectPanelPanelFetcher,
            boolean forceRenewal) {
        Thread autoOCRValueDetectionThread = new Thread(() -> {
            autoOCRValueDetectionNonGUI(oCRSelectPanelPanelFetcher, forceRenewal);
            SwingUtilities.invokeLater(() -> {
                autoOCRValueDetectionGUI();
            });
        },
                "auto-ocr-value-detection-thread");
        autoOCRValueDetectionThread.start();
    }

    private void autoOCRValueDetectionNonGUI(OCRSelectPanelPanelFetcher oCRSelectPanelPanelFetcher,
            boolean forceRenewal) {
        if(detectionResults == null || forceRenewal == true) {
            final String oCRResult = oCRSelectPanelPanelFetcher.fetch();
            detectionResults = autoOCRValueDetectionService.fetchResults(oCRResult);
        }
    }

    private void autoOCRValueDetectionGUI() {
        if(!detectionResults.isEmpty()) {
            for(Pair<Class, Field> pair : this.reflectionFormBuilder.getComboBoxModelMap().keySet()) {
                DefaultComboBoxModel<AutoOCRValueDetectionResult<?>> comboBoxModel = this.reflectionFormBuilder.getComboBoxModelMap().get(pair);
                Field field = pair.getValue();
                comboBoxModel.removeAllElements();
                comboBoxModel.addElement(null);
                for(AutoOCRValueDetectionResult<?> detectionResult : detectionResults) {
                    if(detectionResult.getValue().getClass().isAssignableFrom(field.getType())) {
                        comboBoxModel.addElement(detectionResult);
                    }
                }
                comboBoxModel.setSelectedItem(null);
            }
        }
    }

    private void initComponents() {
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(entityCreationTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(entityCreationTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
        );
    }
}
