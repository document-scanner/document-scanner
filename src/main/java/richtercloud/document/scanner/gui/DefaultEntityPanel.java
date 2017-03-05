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
import richtercloud.document.scanner.components.AutoOCRValueDetectionReflectionFormBuilder;
import richtercloud.document.scanner.components.OCRResultPanelFetcher;
import richtercloud.document.scanner.components.ScanResultPanelFetcher;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.ifaces.AutoOCRValueDetectionListener;
import richtercloud.document.scanner.ifaces.EntityPanel;
import richtercloud.document.scanner.ifaces.OCREngineRecognitionException;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanelFetcher;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.document.scanner.valuedetectionservice.DelegatingValueDetectionService;
import richtercloud.document.scanner.valuedetectionservice.DelegatingValueDetectionServiceConfFactory;
import richtercloud.document.scanner.valuedetectionservice.ValueDetectionResult;
import richtercloud.document.scanner.valuedetectionservice.ValueDetectionService;
import richtercloud.document.scanner.valuedetectionservice.ValueDetectionServiceConf;
import richtercloud.document.scanner.valuedetectionservice.ValueDetectionServiceConfFactory;
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
    private final AutoOCRValueDetectionReflectionFormBuilder reflectionFormBuilder;
    private final Map<Class<? extends JComponent>, ValueSetter<?,?>> valueSetterMapping;
    private final IssueHandler issueHandler;
    private final DocumentScannerConf documentScannerConf;
    private ReflectionFormPanelTabbedPane entityCreationTabbedPane;
    private final AmountMoneyCurrencyStorage amountMoneyAdditionalCurrencyStorage;
    private final AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever;
    private final Set<AutoOCRValueDetectionListener> listeners = new HashSet<>();

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
            IssueHandler issueHandler,
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

    @Override
    public void applyValueDetectionServiceSelection() {
        ValueDetectionServiceConfFactory valueDetectionServiceConfFactory = new DelegatingValueDetectionServiceConfFactory(amountMoneyAdditionalCurrencyStorage,
                amountMoneyExchangeRateRetriever);
        Set<ValueDetectionService<?>> valueDetectionServices = new HashSet<>();
        for(ValueDetectionServiceConf serviceConf : documentScannerConf.getSelectedValueDetectionServiceConfs()) {
            ValueDetectionService<?> valueDetectionService = valueDetectionServiceConfFactory.createService(serviceConf);
            valueDetectionServices.add(valueDetectionService);
        }
        this.valueDetectionService = new DelegatingValueDetectionService(valueDetectionServices);
    }

    /**
     * Store for the last results of
     * {@link #autoOCRValueDetectionNonGUI(richtercloud.document.scanner.gui.OCRSelectPanelPanelFetcher, boolean) }
     * which one might display without retrieving them again from the OCR
     * result.
     */
    private List<ValueDetectionResult<?>> detectionResults;

    @Override
    public List<ValueDetectionResult<?>> getDetectionResults() {
        return Collections.unmodifiableList(detectionResults);
    }

    @Override
    public void autoOCRValueDetection(OCRSelectPanelPanelFetcher oCRSelectPanelPanelFetcher,
            boolean forceRenewal) {
        for(Pair<Class, Field> pair : this.reflectionFormBuilder.getComboBoxModelMap().keySet()) {
            JComboBox<ValueDetectionResult<?>> comboBox = this.reflectionFormBuilder.getComboBoxModelMap().get(pair);
            comboBox.setEnabled(false);
        }
        Thread autoOCRValueDetectionThread = new Thread(() -> {
            autoOCRValueDetectionNonGUI(oCRSelectPanelPanelFetcher, forceRenewal);
            SwingUtilities.invokeLater(() -> {
                try {
                    autoOCRValueDetectionGUI();
                }catch(Exception ex) {
                    issueHandler.handleUnexpectedException(new ExceptionMessage(ex));
                }
            });
            for(AutoOCRValueDetectionListener listener : listeners) {
                listener.onAutoOCRValueDetectionFinished();
            }
        },
                "auto-ocr-value-detection-thread");
        autoOCRValueDetectionThread.start();
    }

    private void autoOCRValueDetectionNonGUI(OCRSelectPanelPanelFetcher oCRSelectPanelPanelFetcher,
            boolean forceRenewal) {
        if(detectionResults == null || forceRenewal == true) {
            final String oCRResult;
            try {
                oCRResult = oCRSelectPanelPanelFetcher.fetch();
            } catch (OCREngineRecognitionException ex) {
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
    public void autoOCRValueDetectionGUI() {
        if(detectionResults != null && !detectionResults.isEmpty()
            //might be null if OCREngineRecognitionException occured in
            //autoOCRValueDetectionNonGUI
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

    @Override
    public void addAutoOCRValueDetectionListener(AutoOCRValueDetectionListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeAutoOCRValueDetectionListener(AutoOCRValueDetectionListener listener) {
        this.listeners.remove(listener);
    }
}
