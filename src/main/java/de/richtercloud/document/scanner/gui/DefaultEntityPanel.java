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
package de.richtercloud.document.scanner.gui;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import de.richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.richtercloud.document.scanner.components.ValueDetectionReflectionFormBuilder;
import de.richtercloud.document.scanner.ifaces.EntityPanel;
import de.richtercloud.document.scanner.ifaces.OCREngineRecognitionException;
import de.richtercloud.document.scanner.ifaces.OCRSelectPanelPanelFetcher;
import de.richtercloud.document.scanner.valuedetectionservice.DefaultValueDetectionServiceExecutor;
import de.richtercloud.document.scanner.valuedetectionservice.DelegatingValueDetectionServiceFactory;
import de.richtercloud.document.scanner.valuedetectionservice.ResultFetchingException;
import de.richtercloud.document.scanner.valuedetectionservice.ValueDetectionResult;
import de.richtercloud.document.scanner.valuedetectionservice.ValueDetectionService;
import de.richtercloud.document.scanner.valuedetectionservice.ValueDetectionServiceConf;
import de.richtercloud.document.scanner.valuedetectionservice.ValueDetectionServiceCreationException;
import de.richtercloud.document.scanner.valuedetectionservice.ValueDetectionServiceFactory;
import richtercloud.message.handler.ExceptionMessage;
import richtercloud.message.handler.IssueHandler;
import richtercloud.message.handler.Message;
import richtercloud.reflection.form.builder.components.money.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetriever;
import richtercloud.reflection.form.builder.fieldhandler.FieldHandler;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;

/**
 *
 * @author richter
 */
public class DefaultEntityPanel extends EntityPanel {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultEntityPanel.class);
    private DefaultValueDetectionServiceExecutor valueDetectionServiceExecutor;
    private final ValueDetectionReflectionFormBuilder reflectionFormBuilder;
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
    private Map<ValueDetectionService, List<ValueDetectionResult>> detectionResults;
    /**
     * The Apache Tika language detector used for value detection.
     */
    /*
    internal implementation notes:
    - @TODO: figure out whether it's better in terms of memory requirement and
    performance to have one instance per EntityPanel or one static instance
    whose access needs to be synchronized since there's no info about eventual
    thread-safety of LanguageDetector
    */
    private final LanguageDetector languageDetector;
    private final PersistenceStorage<Long> storage;

    /**
     * Creates new form EntityPanel.
     *
     * @throws ValueDetectionServiceCreationException if one happens in
     * {@link #applyValueDetectionServiceSelection() }
     * @throws IOException if one happens during loading of Apache Tika language
     * detector models
     */
    public DefaultEntityPanel(AmountMoneyCurrencyStorage amountMoneyAdditionalCurrencyStorage,
            AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever,
            ValueDetectionReflectionFormBuilder reflectionFormBuilder,
            FieldHandler fieldHandler,
            IssueHandler issueHandler,
            DocumentScannerConf documentScannerConf,
            ReflectionFormPanelTabbedPane reflectionFormPanelTabbedPane,
            PersistenceStorage<Long> storage) throws InstantiationException,
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            NoSuchMethodException,
            ValueDetectionServiceCreationException,
            IOException {
        if(fieldHandler == null) {
            throw new IllegalArgumentException("fieldHandler mustn't be null");
        }
        entityCreationTabbedPane = reflectionFormPanelTabbedPane;
        this.initComponents();
        if(reflectionFormBuilder == null) {
            throw new IllegalArgumentException("reflectionFormBuilder mustn't be null");
        }
        this.reflectionFormBuilder = reflectionFormBuilder;
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
        this.languageDetector = LanguageDetector.getDefaultLanguageDetector();
        assert languageDetector != null;
        languageDetector.loadModels();
            //- avoids NullPointerException in
            //org.apache.tika.langdetect.OptimaizeLangDetector which is usually
            //returned by LanguageDetector.getDefaultLanguageDetector, requested
            //clarification at https://issues.apache.org/jira/browse/TIKA-2439
            //- takes a long time, so languageDetector should be initialized in
            //constructor
        this.storage = storage;
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
                amountMoneyExchangeRateRetriever,
                issueHandler,
                storage);
        Set<ValueDetectionService<?>> valueDetectionServices = new HashSet<>();
        for(ValueDetectionServiceConf serviceConf : documentScannerConf.getSelectedValueDetectionServiceConfs()) {
            ValueDetectionService<?> valueDetectionService = valueDetectionServiceConfFactory.createService(serviceConf);
            valueDetectionServices.add(valueDetectionService);
        }
        this.valueDetectionServiceExecutor = new DefaultValueDetectionServiceExecutor(valueDetectionServices,
                issueHandler);
    }

    @Override
    public DefaultValueDetectionServiceExecutor<?> getValueDetectionServiceExecutor() {
        return valueDetectionServiceExecutor;
    }

    @Override
    public Map<ValueDetectionService, List<ValueDetectionResult>> getDetectionResults() {
        return Collections.unmodifiableMap(detectionResults);
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public void valueDetection(OCRSelectPanelPanelFetcher oCRSelectPanelPanelFetcher,
            boolean forceRenewal) {
        for(Pair<Class, Field> pair : this.reflectionFormBuilder.getComboBoxModelMap().keySet()) {
            JComboBox<ValueDetectionResult<?>> comboBox = this.reflectionFormBuilder.getComboBoxModelMap().get(pair);
            comboBox.setEnabled(false);
        }
        Thread valueDetectionThread = new Thread(() -> {
            try {
                valueDetectionNonGUI(oCRSelectPanelPanelFetcher, forceRenewal);
                SwingUtilities.invokeLater(() -> {
                    try {
                        valueDetectionGUI();
                    }catch(Throwable ex) {
                        LOGGER.error("unexpected exception during fetching of "
                                + "auto-OCR-detection values", ex);
                        issueHandler.handleUnexpectedException(new ExceptionMessage(ex));
                    }
                });
            }catch(Throwable ex) {
                LOGGER.error("unexpected exception during value detection occured",
                        ex);
                DefaultEntityPanel.this.issueHandler.handleUnexpectedException(new ExceptionMessage(ex));
            }
        },
                "auto-ocr-value-detection-thread");
        valueDetectionThread.start();
    }

    private void valueDetectionNonGUI(OCRSelectPanelPanelFetcher oCRSelectPanelPanelFetcher,
            boolean forceRenewal) throws ResultFetchingException {
        if(detectionResults == null || forceRenewal == true) {
            final String oCRResult;
            try {
                oCRResult = oCRSelectPanelPanelFetcher.fetch();
                if(oCRResult == null) {
                    //cache has been shut down
                    return;
                }
            } catch (OCREngineRecognitionException ex) {
                LOGGER.error("unexpected exception during fetching of "
                        + "auto-OCR-detection values", ex);
                issueHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
                return;
            }
            if(oCRResult != null) {
                //null indicates that the recognition has been aborted
                String languageIdentifier = documentScannerConf.getTextLanguageIdentifier();
                if(languageIdentifier == null) {
                    //indicates that the text language ought to be recognized
                    //automatically
                    List<LanguageResult> languageResults = languageDetector.detectAll(oCRResult);
                    if(languageResults.size() != 1) {
                        //detection result is either empty or has more than one
                        //candidate -> need user input
                        issueHandler.handle(new Message("The language of the "
                                + "OCR result couldn't be detected "
                                + "automatically, please select the text "
                                + "language in the list",
                                JOptionPane.ERROR_MESSAGE,
                                "Language detection failed"));
                        return;
                    }
                    languageIdentifier = languageResults.get(0).getLanguage();
                    assert languageIdentifier != null && !languageIdentifier.isEmpty();
                }
                detectionResults = valueDetectionServiceExecutor.execute(oCRResult,
                        languageIdentifier);
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
                for(ValueDetectionService valueDetectionService : detectionResults.keySet()) {
                    if(!valueDetectionService.supportsField(field)) {
                        continue;
                    }
                    for(ValueDetectionResult detectionResult : detectionResults.get(valueDetectionService)) {
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
