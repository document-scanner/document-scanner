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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.components.OCRResultPanelFetcher;
import richtercloud.document.scanner.components.ScanResultPanelFetcher;
import richtercloud.document.scanner.components.ValueDetectionReflectionFormBuilder;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.ifaces.EntityPanel;
import richtercloud.document.scanner.ifaces.OCREngineRecognitionException;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanelFetcher;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.document.scanner.valuedetectionservice.DelegatingValueDetectionService;
import richtercloud.document.scanner.valuedetectionservice.DelegatingValueDetectionServiceFactory;
import richtercloud.document.scanner.valuedetectionservice.ValueDetectionResult;
import richtercloud.document.scanner.valuedetectionservice.ValueDetectionService;
import richtercloud.document.scanner.valuedetectionservice.ValueDetectionServiceConf;
import richtercloud.document.scanner.valuedetectionservice.ValueDetectionServiceCreationException;
import richtercloud.document.scanner.valuedetectionservice.ValueDetectionServiceFactory;
import richtercloud.message.handler.ExceptionMessage;
import richtercloud.message.handler.IssueHandler;
import richtercloud.message.handler.Message;
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
    private DelegatingValueDetectionService valueDetectionService;
    private final Set<Class<?>> entityClasses;
    private final ValueDetectionReflectionFormBuilder reflectionFormBuilder;
    private final Map<Class<? extends JComponent>, ValueSetter<?,?>> valueSetterMapping;
    private final IssueHandler issueHandler;
    private final DocumentScannerConf documentScannerConf;
    private ReflectionFormPanelTabbedPane entityCreationTabbedPane;
    private final AmountMoneyCurrencyStorage amountMoneyAdditionalCurrencyStorage;
    private final AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever;
    /**
     * Store for the last results of
     * {@link #valueDetectionNonGUI(richtercloud.document.scanner.gui.OCRSelectPanelPanelFetcher, boolean) }
     * which one might display without retrieving them again from the OCR
     * result.
     */
    private List<ValueDetectionResult<?>> detectionResults;

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
            ValueDetectionReflectionFormBuilder reflectionFormBuilder,
            FieldHandler fieldHandler,
            IssueHandler issueHandler,
            DocumentScannerConf documentScannerConf,
            ReflectionFormPanelTabbedPane reflectionFormPanelTabbedPane) throws InstantiationException,
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            NoSuchMethodException,
            ValueDetectionServiceCreationException {
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
        if(issueHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.issueHandler = issueHandler;
        if(documentScannerConf == null) {
            throw new IllegalArgumentException("documentScannerConf mustn't be null");
        }
        this.documentScannerConf = documentScannerConf;
        this.amountMoneyExchangeRateRetriever = amountMoneyExchangeRateRetriever;
        this.amountMoneyAdditionalCurrencyStorage = amountMoneyAdditionalCurrencyStorage;
        applyValueDetectionServiceSelection();
    }

    public JTabbedPane getEntityCreationTabbedPane() {
        return entityCreationTabbedPane;
    }

    /**
     * Re-creates value detection services from {@code documentScannerConf}
     * which resets all added {@link ValueDetectionServiceListener}s.
     */
    @Override
    public void applyValueDetectionServiceSelection() throws ValueDetectionServiceCreationException {
        ValueDetectionServiceFactory valueDetectionServiceConfFactory = new DelegatingValueDetectionServiceFactory(amountMoneyAdditionalCurrencyStorage,
                amountMoneyExchangeRateRetriever);
        Set<ValueDetectionService<?>> valueDetectionServices = new HashSet<>();
        for(ValueDetectionServiceConf serviceConf : documentScannerConf.getSelectedValueDetectionServiceConfs()) {
            ValueDetectionService<?> valueDetectionService = valueDetectionServiceConfFactory.createService(serviceConf);
            valueDetectionServices.add(valueDetectionService);
        }
        this.valueDetectionService = new DelegatingValueDetectionService(valueDetectionServices);
    }

    public ValueDetectionService<?> getValueDetectionService() {
        return valueDetectionService;
    }

    @Override
    public List<ValueDetectionResult<?>> getDetectionResults() {
        return Collections.unmodifiableList(detectionResults);
    }

    @Override
    public void valueDetection(OCRSelectPanelPanelFetcher oCRSelectPanelPanelFetcher,
            boolean forceRenewal) {
        for(Pair<Class, Field> pair : this.reflectionFormBuilder.getComboBoxModelMap().keySet()) {
            JComboBox<ValueDetectionResult<?>> comboBox = this.reflectionFormBuilder.getComboBoxModelMap().get(pair);
            comboBox.setEnabled(false);
        }
        Thread valueDetectionThread = new Thread(() -> {
            valueDetectionNonGUI(oCRSelectPanelPanelFetcher, forceRenewal);
            SwingUtilities.invokeLater(() -> {
                try {
                    valueDetectionGUI();
                }catch(Exception ex) {
                    LOGGER.error("unexpected exception during fetching of "
                            + "auto-OCR-detection values", ex);
                    issueHandler.handleUnexpectedException(new ExceptionMessage(ex));
                }
            });
        },
                "auto-ocr-value-detection-thread");
        valueDetectionThread.start();
    }

    private void valueDetectionNonGUI(OCRSelectPanelPanelFetcher oCRSelectPanelPanelFetcher,
            boolean forceRenewal) {
        if(detectionResults == null || forceRenewal == true) {
            final String oCRResult;
            try {
                oCRResult = oCRSelectPanelPanelFetcher.fetch();
            } catch (OCREngineRecognitionException ex) {
                LOGGER.error("unexpected exception during fetching of "
                        + "auto-OCR-detection values", ex);
                issueHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
                return;
            }
            if(oCRResult != null) {
                //null indicates that the recognition has been aborted
                detectionResults = valueDetectionService.fetchResults(oCRResult);
            }
        }
    }

    @Override
    public void valueDetectionGUI() {
        if(detectionResults != null && !detectionResults.isEmpty()
            //might be null if OCREngineRecognitionException occured in
            //valueDetectionNonGUI
        ) {
            for(Pair<Class, Field> pair : this.reflectionFormBuilder.getComboBoxModelMap().keySet()) {
                JComboBox<ValueDetectionResult<?>> comboBox = this.reflectionFormBuilder.getComboBoxModelMap().get(pair);
                comboBox.setEnabled(true);
                DefaultComboBoxModel<ValueDetectionResult<?>> comboBoxModel = (DefaultComboBoxModel<ValueDetectionResult<?>>) comboBox.getModel();
                Field field = pair.getValue();
                comboBoxModel.removeAllElements();
                comboBoxModel.addElement(null);
                for(ValueDetectionResult<?> detectionResult : detectionResults) {
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
