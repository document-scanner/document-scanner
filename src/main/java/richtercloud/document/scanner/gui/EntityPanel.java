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

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.components.OCRResultPanelFetcher;
import richtercloud.document.scanner.components.ScanResultPanelFetcher;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.ReflectionFormBuilder;
import richtercloud.reflection.form.builder.ReflectionFormPanel;
import richtercloud.reflection.form.builder.components.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.AmountMoneyUsageStatisticsStorage;
import richtercloud.reflection.form.builder.message.MessageHandler;
import richtercloud.swing.worker.get.wait.dialog.SwingWorkerCompletionWaiter;
import richtercloud.swing.worker.get.wait.dialog.SwingWorkerGetWaitDialog;

/**
 * Contains the {@link ReflectionFormPanel}s to
 * create or edit entities (including a  {@link JRadioButton} to switch between
 * creation and editing mode).
 * @author richter
 */
/*
internal implementation notes:
- it's legitimate that QueryPanel enforces it's entityClass property to be
non-null -> don't initialize it because it's used in the GUI builder, but add a
placeholder panel entityEditingQueryPanelPanel
- adding JScrollPanes to a JSplitPanel causes trouble with left and right
component -> add two panels as left and right component and move components
between them
*/
public class EntityPanel extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(EntityPanel.class);
    private final DelegatingAutoOCRValueDetectionService autoOCRValueDetectionService;
    private final Set<Class<?>> entityClasses;
    private final ReflectionFormBuilder reflectionFormBuilder;
    private final Map<Class<? extends JComponent>, ValueSetter<?,?>> valueSetterMapping;
    private final MessageHandler messageHandler;
    private final Map<Class<?>, ReflectionFormPanel<?>> reflectionFormPanelMap;

    /**
     * Creates new form EntityPanel
     * @param reflectionFormPanelMap allows sharing of already generated
     * {@link ReflectionFormPanel}s
     */
    public EntityPanel(Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            Map<Class<?>, ReflectionFormPanel<?>> reflectionFormPanelMap,
            Map<Class<? extends JComponent>, ValueSetter<?,?>> valueSetterMapping,
            final OCRResultPanelFetcher oCRResultPanelRetriever,
            final ScanResultPanelFetcher scanResultPanelRetriever,
            AmountMoneyUsageStatisticsStorage amountMoneyUsageStatisticsStorage,
            AmountMoneyCurrencyStorage amountMoneyAdditionalCurrencyStorage,
            ReflectionFormBuilder reflectionFormBuilder,
            MessageHandler messageHandler) throws InstantiationException,
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            NoSuchMethodException {
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
        this.reflectionFormPanelMap = reflectionFormPanelMap;
        this.autoOCRValueDetectionService = new DelegatingAutoOCRValueDetectionService(amountMoneyAdditionalCurrencyStorage);
        List<Class<?>> entityClassesSort = sortEntityClasses(entityClasses);
        for(Class<?> entityClass : entityClassesSort) {
            ReflectionFormPanel reflectionFormPanel = reflectionFormPanelMap.get(entityClass);
            if(reflectionFormPanel == null) {
                throw new IllegalArgumentException(String.format("entityClass %s has no %s mapped in reflectionFormPanelMap",
                        entityClass,
                        ReflectionFormPanel.class));
            }
            JScrollPane reflectionFormPanelScrollPane = new JScrollPane(reflectionFormPanel);
            reflectionFormPanelScrollPane.getVerticalScrollBar().setUnitIncrement(Constants.DEFAULT_SCROLL_INTERVAL);
            reflectionFormPanelScrollPane.getHorizontalScrollBar().setUnitIncrement(Constants.DEFAULT_SCROLL_INTERVAL);
            String newTabTip = null;
            Icon newTabIcon = null;
            ClassInfo entityClassClassInfo = entityClass.getAnnotation(ClassInfo.class);
            if(entityClassClassInfo != null) {
                newTabTip = entityClassClassInfo.description();
                String newTabIconResourcePath = entityClassClassInfo.iconResourcePath();
                if(!newTabIconResourcePath.isEmpty()) {
                    URL newTabIconURL = Thread.currentThread().getContextClassLoader().getResource(newTabIconResourcePath);
                    newTabIcon = new ImageIcon(newTabIconURL);
                }
            }
            this.entityCreationTabbedPane.insertTab(createClassTabTitle(entityClass),
                    newTabIcon,
                    reflectionFormPanelScrollPane,
                    newTabTip,
                    this.entityCreationTabbedPane.getTabCount()
            );
        }
        this.entityCreationTabbedPane.setSelectedIndex(this.entityCreationTabbedPane.indexOfTab(createClassTabTitle(primaryClassSelection)));
    }

    private String createClassTabTitle(Class<?> entityClass) {
        String retValue;
        ClassInfo entityClassClassInfo = entityClass.getAnnotation(ClassInfo.class);
        if(entityClassClassInfo != null) {
            retValue = entityClassClassInfo.name();
        }else {
            retValue = entityClass.getSimpleName();
        }
        return retValue;
    }

    /**
     * Store for the last results of
     * {@link #autoOCRValueDetection(richtercloud.document.scanner.gui.OCRSelectPanelPanelFetcher, boolean) }
     * which one might display without retrieving them again from the OCR
     * result.
     */
    private List<AutoOCRValueDetectionResult<?>> detectionResults;

    public void autoOCRValueDetection(OCRSelectPanelPanelFetcher oCRSelectPanelPanelFetcher,
            boolean forceRenewal) {
        if(detectionResults == null || forceRenewal == true) {
            final String oCRResult = oCRSelectPanelPanelFetcher.fetch();
            final SwingWorkerGetWaitDialog dialog = new SwingWorkerGetWaitDialog(SwingUtilities.getWindowAncestor(this),
                    DocumentScanner.generateApplicationWindowTitle("Auto OCR value detection",
                            DocumentScanner.APP_NAME,
                            DocumentScanner.APP_VERSION),
                    "Auto OCR detection value",
                    "Searching values in input");
            SwingWorkerCompletionWaiter swingWorkerCompletionWaiter = new SwingWorkerCompletionWaiter(dialog);
            SwingWorker<List<AutoOCRValueDetectionResult<?>>, Void> worker = new SwingWorker<List<AutoOCRValueDetectionResult<?>>, Void>() {
                @Override
                protected List<AutoOCRValueDetectionResult<?>> doInBackground() throws Exception {
                    List<AutoOCRValueDetectionResult<?>> retValue = EntityPanel.this.autoOCRValueDetectionService.fetchResults(oCRResult);
                    return retValue;
                }

                @Override
                protected void done() {
                    dialog.setVisible(false);
                }
            };
            this.autoOCRValueDetectionService.addUpdateListener(new AutoOCRValueDetectionServiceUpdateListener() {
                @Override
                public void onUpdate(AutoOCRValueDetectionServiceUpdateEvent event) {
                    LOGGER.debug(String.format("Received update event with values %d/%d", event.getWordNumber(), event.getWordCount()));
                    dialog.getProgressBar().getModel().setValue((int)((double)event.getWordNumber()/event.getWordCount()*100.0));
                }
            });
            worker.addPropertyChangeListener(swingWorkerCompletionWaiter);
            worker.execute();
            //the dialog will be visible until the SwingWorker is done
            dialog.setVisible(true);
            try {
                detectionResults = worker.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }
        if(!detectionResults.isEmpty()) {
            AutoOCRValueDetectionDialog autoOCRValueDetectionDialog = new AutoOCRValueDetectionDialog(SwingUtilities.getWindowAncestor(this),
                    detectionResults,
                    entityClasses,
                    reflectionFormPanelMap,
                    reflectionFormBuilder,
                    valueSetterMapping,
                    messageHandler);
            autoOCRValueDetectionDialog.setVisible(true);
        }
    }

    protected static List<Class<?>> sortEntityClasses(Set<Class<?>> entityClasses) {
        List<Class<?>> entityClassesSort = new LinkedList<>(entityClasses);
        Collections.sort(entityClassesSort, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                String o1Value;
                ClassInfo o1ClassInfo = o1.getAnnotation(ClassInfo.class);
                if(o1ClassInfo != null) {
                    o1Value = o1ClassInfo.name();
                }else {
                    o1Value = o1.getSimpleName();
                }
                String o2Value;
                ClassInfo o2ClassInfo = o2.getAnnotation(ClassInfo.class);
                if(o2ClassInfo != null) {
                    o2Value = o2ClassInfo.name();
                }else {
                    o2Value = o2.getSimpleName();
                }
                return o1Value.compareTo(o2Value);
            }
        });
        return entityClassesSort;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        entityCreationTabbedPane = new javax.swing.JTabbedPane();

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
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane entityCreationTabbedPane;
    // End of variables declaration//GEN-END:variables
}
