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

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import richtercloud.document.scanner.components.OCRResultPanelFetcher;
import richtercloud.document.scanner.components.ScanResultPanelFetcher;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.ReflectionFormPanel;
import richtercloud.reflection.form.builder.ReflectionFormPanelUpdateEvent;
import richtercloud.reflection.form.builder.ReflectionFormPanelUpdateListener;
import richtercloud.reflection.form.builder.components.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.AmountMoneyUsageStatisticsStorage;
import richtercloud.reflection.form.builder.fieldhandler.FieldHandler;
import richtercloud.reflection.form.builder.fieldhandler.FieldHandlingException;
import richtercloud.reflection.form.builder.jpa.JPACachedFieldRetriever;
import richtercloud.reflection.form.builder.jpa.JPAReflectionFormBuilder;
import richtercloud.reflection.form.builder.jpa.panels.BidirectionalControlPanel;
import richtercloud.reflection.form.builder.jpa.panels.QueryPanel;
import richtercloud.reflection.form.builder.jpa.panels.QueryPanelUpdateEvent;
import richtercloud.reflection.form.builder.jpa.panels.QueryPanelUpdateListener;
import richtercloud.reflection.form.builder.message.MessageHandler;

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
    private DefaultComboBoxModel<Class<?>> entityEditingClassComboBoxModel = new DefaultComboBoxModel<>();
    private ListCellRenderer<Object> entityEditingClassComboBoxRenderer = new DefaultListCellRenderer() {
        private static final long serialVersionUID = 1L;
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Class<?> valueCast = (Class<?>) value;
            String value0;
            ClassInfo classInfo = valueCast.getAnnotation(ClassInfo.class);
            if(classInfo != null) {
                value0 = classInfo.name();
            }else {
                value0 = valueCast.getSimpleName();
            }
            return super.getListCellRendererComponent(list, value0, index, isSelected, cellHasFocus);
        }
    };
    private EntityManager entityManager;
    private JPAReflectionFormBuilder reflectionFormBuilder;
    private QueryPanel<Object> entityEditingQueryPanel;
    /**
     * A cache to keep custom queries when changing the query class (would be
     * overwritten at recreation. This also avoid extranous recreations.
     */
    /*
    internal implementation notes:
    - for necessity for instance creation see class comment
    */
    private Map<Class<?>, QueryPanel<Object>> entityEditingQueryPanelCache = new HashMap<>();
    private final static int DEFAULT_SCROLL_INTERVAL = 24;
    private final FieldHandler fieldHandler;
    private final MessageHandler messageHandler;

    /**
     * Creates new form EntityPanel
     * @param reflectionFormPanelMap allows sharing of already generated
     * {@link ReflectionFormPanel}s
     */
    public EntityPanel(Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            Map<Class<?>, ReflectionFormPanel> reflectionFormPanelMap,
            FieldHandler fieldHandler,
            Map<Class<? extends JComponent>, ValueSetter<?>> valueSetterMapping,
            EntityManager entityManager,
            final OCRResultPanelFetcher oCRResultPanelRetriever,
            final ScanResultPanelFetcher scanResultPanelRetriever,
            AmountMoneyUsageStatisticsStorage amountMoneyUsageStatisticsStorage,
            AmountMoneyCurrencyStorage amountMoneyAdditionalCurrencyStorage,
            MessageHandler messageHandler) throws InstantiationException,
            IllegalAccessException,
            IllegalArgumentException,
            InvocationTargetException,
            NoSuchMethodException {
        this.initComponents();
        this.entityEditingClassComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                //don't recreate QueryPanel instances, but cache them in order
                //to keep custom queries managed in QueryPanel
                if(EntityPanel.this.entityEditingQueryPanel != null) {
                    Class<?> selectedEntityClass = (Class<?>) e.getItem();
                    handleEntityEditingQueryPanelUpdate(selectedEntityClass);
                }
            }
        });
        if(messageHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.messageHandler = messageHandler;
        reflectionFormBuilder = new JPAReflectionFormBuilder(entityManager,
                DocumentScanner.generateApplicationWindowTitle("Field description", DocumentScanner.APP_NAME, DocumentScanner.APP_VERSION),
                messageHandler,
                new JPACachedFieldRetriever());
        if(entityManager == null) {
            throw new IllegalArgumentException("entityManager mustn't be null");
        }
        this.entityManager = entityManager;
        if(entityClasses == null) {
            throw new IllegalArgumentException("entityClasses mustn't be null");
        }
        if(entityClasses.isEmpty()) {
            throw new IllegalArgumentException("entityClass mustn't be empty");
        }
        if(!entityClasses.contains(primaryClassSelection)) {
            throw new IllegalArgumentException(String.format("primaryClassSelection '%s' has to be contained in entityClasses", primaryClassSelection));
        }
        List<Class<?>> entityClassesSort = sortEntityClasses(entityClasses);
        for(Class<?> entityClass : entityClassesSort) {
            ReflectionFormPanel reflectionFormPanel = reflectionFormPanelMap.get(entityClass);
            if(reflectionFormPanel == null) {
                throw new IllegalArgumentException(String.format("entityClass %s has no %s mapped in reflectionFormPanelMap",
                        entityClass,
                        ReflectionFormPanel.class));
            }
            JScrollPane reflectionFormPanelScrollPane = new JScrollPane(reflectionFormPanel);
            reflectionFormPanelScrollPane.getVerticalScrollBar().setUnitIncrement(DEFAULT_SCROLL_INTERVAL);
            reflectionFormPanelScrollPane.getHorizontalScrollBar().setUnitIncrement(DEFAULT_SCROLL_INTERVAL);
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
        for(Class<?> entityClass : entityClassesSort) {
            this.entityEditingClassComboBoxModel.addElement(entityClass);
        }
        this.entityEditingClassComboBox.setSelectedItem(primaryClassSelection);
        this.fieldHandler = fieldHandler;
        this.modeSelectionButtonGroup.add(creationModeButton);
        this.modeSelectionButtonGroup.add(editingModeButton);
        this.creationModeButtonSetup(); //JButton.setSelected doesn't trigger
            //action listener
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
        editingPanel = new javax.swing.JPanel();
        entityEditingClassComboBox = new JComboBox<>();
        ;
        entityEditingClassComboBoxLabel = new javax.swing.JLabel();
        entityEditingClassSeparator = new javax.swing.JSeparator();
        entityEditingSplitPane = new javax.swing.JSplitPane();
        entityEditingSplitPaneLeftPanel = new javax.swing.JPanel();
        entityEditingReflectionFormPanelScrollPane = new javax.swing.JScrollPane();
        entityEditingSplitPaneRightPanel = new javax.swing.JPanel();
        entityEditingQueryPanelScrollPane = new javax.swing.JScrollPane();
        modeSelectionButtonGroup = new javax.swing.ButtonGroup();
        entityContentPanel = new javax.swing.JPanel();
        creationModeButton = new javax.swing.JRadioButton();
        editingModeButton = new javax.swing.JRadioButton();

        entityEditingClassComboBox.setModel(entityEditingClassComboBoxModel);
        entityEditingClassComboBox.setRenderer(entityEditingClassComboBoxRenderer);
        entityEditingClassComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                entityEditingClassComboBoxActionPerformed(evt);
            }
        });

        entityEditingClassComboBoxLabel.setText("Query class:");

        entityEditingSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        javax.swing.GroupLayout entityEditingSplitPaneLeftPanelLayout = new javax.swing.GroupLayout(entityEditingSplitPaneLeftPanel);
        entityEditingSplitPaneLeftPanel.setLayout(entityEditingSplitPaneLeftPanelLayout);
        entityEditingSplitPaneLeftPanelLayout.setHorizontalGroup(
            entityEditingSplitPaneLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(entityEditingReflectionFormPanelScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
        );
        entityEditingSplitPaneLeftPanelLayout.setVerticalGroup(
            entityEditingSplitPaneLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(entityEditingReflectionFormPanelScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
        );

        entityEditingSplitPane.setRightComponent(entityEditingSplitPaneLeftPanel);

        javax.swing.GroupLayout entityEditingSplitPaneRightPanelLayout = new javax.swing.GroupLayout(entityEditingSplitPaneRightPanel);
        entityEditingSplitPaneRightPanel.setLayout(entityEditingSplitPaneRightPanelLayout);
        entityEditingSplitPaneRightPanelLayout.setHorizontalGroup(
            entityEditingSplitPaneRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(entityEditingQueryPanelScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
        );
        entityEditingSplitPaneRightPanelLayout.setVerticalGroup(
            entityEditingSplitPaneRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(entityEditingQueryPanelScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 19, Short.MAX_VALUE)
        );

        entityEditingSplitPane.setLeftComponent(entityEditingSplitPaneRightPanel);

        javax.swing.GroupLayout editingPanelLayout = new javax.swing.GroupLayout(editingPanel);
        editingPanel.setLayout(editingPanelLayout);
        editingPanelLayout.setHorizontalGroup(
            editingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(editingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(entityEditingSplitPane)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, editingPanelLayout.createSequentialGroup()
                        .addComponent(entityEditingClassComboBoxLabel)
                        .addGap(18, 18, 18)
                        .addComponent(entityEditingClassComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(entityEditingClassSeparator, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        editingPanelLayout.setVerticalGroup(
            editingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(editingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(entityEditingClassComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(entityEditingClassComboBoxLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(entityEditingClassSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(entityEditingSplitPane)
                .addContainerGap())
        );

        javax.swing.GroupLayout entityContentPanelLayout = new javax.swing.GroupLayout(entityContentPanel);
        entityContentPanel.setLayout(entityContentPanelLayout);
        entityContentPanelLayout.setHorizontalGroup(
            entityContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 376, Short.MAX_VALUE)
        );
        entityContentPanelLayout.setVerticalGroup(
            entityContentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 246, Short.MAX_VALUE)
        );

        creationModeButton.setSelected(true);
        creationModeButton.setText("Creation mode");
        creationModeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                creationModeButtonActionPerformed(evt);
            }
        });

        editingModeButton.setText("Editing mode");
        editingModeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editingModeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(entityContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(creationModeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(editingModeButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(creationModeButton)
                    .addComponent(editingModeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(entityContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void entityEditingClassComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entityEditingClassComboBoxActionPerformed
        Class<?> selectedEntityClass = (Class<?>) entityEditingClassComboBox.getSelectedItem();
        handleEntityEditingQueryPanelUpdate(selectedEntityClass);
    }//GEN-LAST:event_entityEditingClassComboBoxActionPerformed

    private void creationModeButtonSetup() {
        entityContentPanel.removeAll();
        //GroupLayout seems not necessary, but doesn't hurt
        GroupLayout entityContentPanelLayout = (GroupLayout) entityContentPanel.getLayout();
        entityContentPanelLayout.setHorizontalGroup(entityContentPanelLayout.createParallelGroup().addComponent(entityCreationTabbedPane,
                0, //min (0 necessary to make the scroll pane to appear)
                GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE));
        entityContentPanelLayout.setVerticalGroup(entityContentPanelLayout.createSequentialGroup().addComponent(entityCreationTabbedPane,
                0, //min (0 necessary to make the scroll pane to appear)
                GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE));
        entityContentPanel.validate();
    }

    private void creationModeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_creationModeButtonActionPerformed
        creationModeButtonSetup();
    }//GEN-LAST:event_creationModeButtonActionPerformed

    private void editingModeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editingModeButtonActionPerformed
        entityContentPanel.removeAll();
        //GroupLayout seems not necessary, but doesn't hurt
        GroupLayout entityContentPanelLayout = (GroupLayout) entityContentPanel.getLayout();
        entityContentPanelLayout.setHorizontalGroup(entityContentPanelLayout.createParallelGroup().addComponent(editingPanel,
                0, //min (0 necessary to make the scroll pane to appear)
                GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE));
        entityContentPanelLayout.setVerticalGroup(entityContentPanelLayout.createSequentialGroup().addComponent(editingPanel,
                0, //min (0 necessary to make the scroll pane to appear)
                GroupLayout.DEFAULT_SIZE,
                Short.MAX_VALUE));
        entityContentPanel.validate();
        entityEditingSplitPane.setDividerLocation(0.66); //invoke after
        //entityContentPanel.validate
    }//GEN-LAST:event_editingModeButtonActionPerformed

    private void handleEntityEditingQueryPanelUpdate(Class<?> selectedEntityClass) {
        entityEditingQueryPanel = this.entityEditingQueryPanelCache.get(selectedEntityClass);
        if(entityEditingQueryPanel == null) {
            try {
                List<Field> entityClassFields = reflectionFormBuilder.getFieldRetriever().retrieveRelevantFields(selectedEntityClass);
                Set<Field> mappedFieldCandidates = QueryPanel.retrieveMappedFieldCandidates(selectedEntityClass,
                                entityClassFields,
                                reflectionFormBuilder.getFieldRetriever());
                BidirectionalControlPanel bidirectionalControlPanel = new BidirectionalControlPanel(selectedEntityClass,
                        DocumentScanner.BIDIRECTIONAL_HELP_DIALOG_TITLE, //bidirectionalHelpDialogTitle
                        QueryPanel.retrieveMappedByField(entityClassFields),
                        mappedFieldCandidates);
                this.entityEditingQueryPanel = new QueryPanel<>(entityManager,
                        selectedEntityClass,
                        messageHandler,
                        reflectionFormBuilder,
                        null, //initialValue
                        bidirectionalControlPanel
                );
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
            entityEditingQueryPanelCache.put(selectedEntityClass, entityEditingQueryPanel);
            this.entityEditingQueryPanel.addUpdateListener(new QueryPanelUpdateListener() {
                @Override
                public void onUpdate(QueryPanelUpdateEvent event) {
                    try {
                        Object selectEntity = EntityPanel.this.entityEditingQueryPanel.getSelectedObject();
                        ReflectionFormPanel reflectionFormPanel = EntityPanel.this.reflectionFormBuilder.transformEntityClass(event.getSource().getEntityClass(),
                                selectEntity,
                                true, //editingMode
                                fieldHandler
                        );
                        reflectionFormPanel.addUpdateListener(new ReflectionFormPanelUpdateListener() {
                            @Override
                            public void onUpdate(ReflectionFormPanelUpdateEvent reflectionFormPanelUpdateEvent) {
                                if(reflectionFormPanelUpdateEvent.getType() == ReflectionFormPanelUpdateEvent.INSTANCE_DELETED) {
                                    entityEditingQueryPanel.getQueryComponent().repeatLastQuery();
                                    if(entityEditingQueryPanel.getQueryResults().isEmpty()) {
                                        EntityPanel.this.entityEditingReflectionFormPanelScrollPane.setViewportView(null);
                                    }
                                }
                            }
                        });
                        EntityPanel.this.entityEditingReflectionFormPanelScrollPane.setViewportView(reflectionFormPanel);
                        EntityPanel.this.entityEditingReflectionFormPanelScrollPane.getVerticalScrollBar().setUnitIncrement(DEFAULT_SCROLL_INTERVAL);
                        EntityPanel.this.entityEditingReflectionFormPanelScrollPane.getHorizontalScrollBar().setUnitIncrement(DEFAULT_SCROLL_INTERVAL);
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | FieldHandlingException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }
        this.entityEditingQueryPanelScrollPane.setViewportView(entityEditingQueryPanel);
        this.entityEditingQueryPanelScrollPane.getVerticalScrollBar().setUnitIncrement(DEFAULT_SCROLL_INTERVAL);
        this.entityEditingQueryPanelScrollPane.getHorizontalScrollBar().setUnitIncrement(DEFAULT_SCROLL_INTERVAL);
        this.entityEditingReflectionFormPanelScrollPane.setViewportView(null);
        this.entityEditingQueryPanel.clearSelection();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton creationModeButton;
    private javax.swing.JRadioButton editingModeButton;
    private javax.swing.JPanel editingPanel;
    private javax.swing.JPanel entityContentPanel;
    private javax.swing.JTabbedPane entityCreationTabbedPane;
    private javax.swing.JComboBox<Class<?>> entityEditingClassComboBox;
    private javax.swing.JLabel entityEditingClassComboBoxLabel;
    private javax.swing.JSeparator entityEditingClassSeparator;
    private javax.swing.JScrollPane entityEditingQueryPanelScrollPane;
    private javax.swing.JScrollPane entityEditingReflectionFormPanelScrollPane;
    private javax.swing.JSplitPane entityEditingSplitPane;
    private javax.swing.JPanel entityEditingSplitPaneLeftPanel;
    private javax.swing.JPanel entityEditingSplitPaneRightPanel;
    private javax.swing.ButtonGroup modeSelectionButtonGroup;
    // End of variables declaration//GEN-END:variables
}
