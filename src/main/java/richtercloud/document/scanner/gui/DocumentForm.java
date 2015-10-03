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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Metamodel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.commons.lang3.tuple.Pair;
import richtercloud.document.scanner.components.OCRResultPanelFetcher;
import richtercloud.document.scanner.components.ScanResultPanelFetcher;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.reflection.form.builder.ClassAnnotationHandler;
import richtercloud.reflection.form.builder.FieldAnnotationHandler;
import richtercloud.reflection.form.builder.FieldHandler;
import richtercloud.reflection.form.builder.FieldUpdateEvent;
import richtercloud.reflection.form.builder.ReflectionFormBuilder;
import richtercloud.reflection.form.builder.ReflectionFormPanel;
import richtercloud.reflection.form.builder.jpa.JPAReflectionFormBuilder;
import richtercloud.reflection.form.builder.jpa.panels.QueryPanel;
import richtercloud.reflection.form.builder.jpa.panels.QueryPanelUpdateEvent;
import richtercloud.reflection.form.builder.jpa.panels.QueryPanelUpdateListener;
import richtercloud.reflection.form.builder.retriever.ValueRetriever;

/**
 *
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
public class DocumentForm extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;
    private DefaultComboBoxModel<Class<?>> entityEditingClassComboBoxModel = new DefaultComboBoxModel<>();
    private EntityManager entityManager;
    private ReflectionFormBuilder reflectionFormBuilder;
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

    /**
     * Creates new form DocumentForm
     */
    private DocumentForm() {
        this.initComponents();
        this.entityEditingClassComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                //don't recreate QueryPanel instances, but cache them in order
                //to keep custom queries managed in QueryPanel
                if(DocumentForm.this.entityEditingQueryPanel != null) {
                    Class<?> selectedEntityClass = (Class<?>) e.getItem();
                    handleEntityEditingQueryPanelUpdate(selectedEntityClass);
                }
            }
        });
    }

    public DocumentForm(Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            EntityManager entityManager,
            List<Pair<Class<? extends Annotation>,FieldAnnotationHandler>> fieldAnnotationMapping,
            List<Pair<Class<? extends Annotation>,ClassAnnotationHandler<Object,FieldUpdateEvent<Object>>>> classAnnotationMapping,
            final OCRResultPanelFetcher oCRResultPanelRetriever,
            final ScanResultPanelFetcher scanResultPanelRetriever,
            Set<Type> ignoresFieldAnnotationMapping,
            Set<Type> ignoresClassAnnotationMapping,
            Set<Type> ignoresPrimitiveMapping) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        this(entityClasses,
                primaryClassSelection,
                DocumentScanner.CLASS_MAPPING_DEFAULT,
                DocumentScanner.PRIMITIVE_MAPPING_DEFAULT,
                DocumentScanner.VALUE_RETRIEVER_MAPPING_DEFAULT,
                DocumentScanner.VALUE_SETTER_MAPPING_DEFAULT,
                entityManager,
                fieldAnnotationMapping,
                classAnnotationMapping,
                oCRResultPanelRetriever,
                scanResultPanelRetriever,
                ignoresFieldAnnotationMapping,
                ignoresClassAnnotationMapping,
                ignoresPrimitiveMapping);
    }

    /**
     *
     * @param entityClasses
     * @param primaryClassSelection the entry in the edit and create class choise combo box which is initially selected in a freshly created DocumentForm
     * @param classMapping
     * @param primitiveMapping
     * @param valueRetrieverMapping
     * @param valueSetterMapping
     * @param entityManager
     * @param fieldAnnotationMapping
     * @param classAnnotationMapping
     * @param oCRResultPanelRetriever
     * @param scanResultPanelRetriever
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public DocumentForm(Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            Map<Type, FieldHandler<?,?>> classMapping,
            Map<Class<?>, FieldHandler<?,?>> primitiveMapping,
            Map<Class<? extends JComponent>, ValueRetriever<?, ?>> valueRetrieverMapping,
            Map<Class<? extends JComponent>, ValueSetter<?>> valueSetterMapping,
            EntityManager entityManager,
            List<Pair<Class<? extends Annotation>,FieldAnnotationHandler>> fieldAnnotationMapping,
            List<Pair<Class<? extends Annotation>,ClassAnnotationHandler<Object,FieldUpdateEvent<Object>>>> classAnnotationMapping,
            final OCRResultPanelFetcher oCRResultPanelRetriever,
            final ScanResultPanelFetcher scanResultPanelRetriever,
            Set<Type> ignoresFieldAnnotationMapping,
            Set<Type> ignoresClassAnnotationMapping,
            Set<Type> ignoresPrimitiveMapping) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        this();
        reflectionFormBuilder = new JPAReflectionFormBuilder(classMapping,
                primitiveMapping,
                valueRetrieverMapping,
                fieldAnnotationMapping,
                classAnnotationMapping,
                ignoresFieldAnnotationMapping,
                ignoresClassAnnotationMapping,
                ignoresPrimitiveMapping,
                entityManager,
                ReflectionFormPanel.generateApplicationWindowTitle("Persistence failure", DocumentScanner.APP_NAME, DocumentScanner.APP_VERSION));
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
        List<Class<?>> entityClassesSort = new LinkedList<>(entityClasses);
        Collections.sort(entityClassesSort, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getSimpleName().compareTo(o2.getSimpleName());
            }
        });
        for(Class<?> entityClass : entityClassesSort) {
            ReflectionFormPanel reflectionFormPanel = reflectionFormBuilder.transform(entityClass,
                    null //entityToUpdate
            );
            this.entityCreationTabbedPane.add(createClassTabTitle(entityClass), new JScrollPane(reflectionFormPanel));
            List<Field> relevantFields = reflectionFormBuilder.retrieveRelevantFields(entityClass);
            JMenu entityClassMenu = new JMenu(entityClass.getSimpleName());
            for(Field relevantField : relevantFields) {
                JMenuItem relevantFieldMenuItem = new JMenuItem(relevantField.getName());
                relevantFieldMenuItem.addActionListener(new FieldActionListener(reflectionFormPanel, relevantField, valueSetterMapping));
                entityClassMenu.add(relevantFieldMenuItem);
            }
            this.oCRResultPopup.add(entityClassMenu);
        }
        this.entityCreationTabbedPane.setSelectedIndex(this.entityCreationTabbedPane.indexOfTab(createClassTabTitle(primaryClassSelection)));
        this.entityCreationTabbedPane.validate();
        this.validate();
        for(Class<?> entityClass : entityClassesSort) {
            this.entityEditingClassComboBoxModel.addElement(entityClass);
        }
        this.entityEditingClassComboBox.setSelectedItem(primaryClassSelection);
    }

    private String createClassTabTitle(Class<?> entityClass) {
        String retValue = entityClass.getSimpleName();
        return retValue;
    }

    private class FieldActionListener implements ActionListener {
        private final Field field;
        private final ReflectionFormPanel reflectionFormPanel;
        private final Map<Class<? extends JComponent>, ValueSetter<?>> valueSetterMapping;

        FieldActionListener(ReflectionFormPanel reflectionFormPanel, Field field, Map<Class<? extends JComponent>, ValueSetter<?>> valueSetterMapping) {
            this.field = field;
            this.reflectionFormPanel = reflectionFormPanel;
            this.valueSetterMapping = valueSetterMapping;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JComponent comp = this.reflectionFormPanel.getComponentByField(this.field);
            String oCRSelection = DocumentForm.this.oCRResultTextArea.getSelectedText();
            ValueSetter valueSetter = this.valueSetterMapping.get(comp.getClass());
            if(valueSetter != null) {
                valueSetter.setValue(oCRSelection, comp);
            }else {
                //@TODO: handle feedback
            }
        }

    }

    public JTextArea getoCRResultTextArea() {
        return this.oCRResultTextArea;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        oCRResultPopup = new javax.swing.JPopupMenu();
        oCRResultPopupHintMenuItem = new javax.swing.JMenuItem();
        oCRResultPopupSeparator = new javax.swing.JPopupMenu.Separator();
        entityCreationTabbedPane = new javax.swing.JTabbedPane();
        entityEditingPanel = new javax.swing.JPanel();
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
        splitPane = new javax.swing.JSplitPane();
        entityPanel = new javax.swing.JPanel();
        entityContentPanel = new javax.swing.JPanel();
        creationModeButton = new javax.swing.JRadioButton();
        editingModeButton = new javax.swing.JRadioButton();
        oCRResultPanel = new javax.swing.JPanel();
        oCRResultLabel = new javax.swing.JLabel();
        oCRResultTextAreaScrollPane = new javax.swing.JScrollPane();
        oCRResultTextArea = new javax.swing.JTextArea();

        oCRResultPopupHintMenuItem.setText("Paste into");
        oCRResultPopupHintMenuItem.setEnabled(false);
        oCRResultPopup.add(oCRResultPopupHintMenuItem);
        oCRResultPopup.add(oCRResultPopupSeparator);

        entityEditingClassComboBox.setModel(entityEditingClassComboBoxModel);
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
            .addComponent(entityEditingReflectionFormPanelScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 698, Short.MAX_VALUE)
        );
        entityEditingSplitPaneLeftPanelLayout.setVerticalGroup(
            entityEditingSplitPaneLeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(entityEditingReflectionFormPanelScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
        );

        entityEditingSplitPane.setRightComponent(entityEditingSplitPaneLeftPanel);

        javax.swing.GroupLayout entityEditingSplitPaneRightPanelLayout = new javax.swing.GroupLayout(entityEditingSplitPaneRightPanel);
        entityEditingSplitPaneRightPanel.setLayout(entityEditingSplitPaneRightPanelLayout);
        entityEditingSplitPaneRightPanelLayout.setHorizontalGroup(
            entityEditingSplitPaneRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(entityEditingQueryPanelScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 698, Short.MAX_VALUE)
        );
        entityEditingSplitPaneRightPanelLayout.setVerticalGroup(
            entityEditingSplitPaneRightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(entityEditingQueryPanelScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
        );

        entityEditingSplitPane.setLeftComponent(entityEditingSplitPaneRightPanel);

        javax.swing.GroupLayout entityEditingPanelLayout = new javax.swing.GroupLayout(entityEditingPanel);
        entityEditingPanel.setLayout(entityEditingPanelLayout);
        entityEditingPanelLayout.setHorizontalGroup(
            entityEditingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, entityEditingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(entityEditingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(entityEditingSplitPane)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, entityEditingPanelLayout.createSequentialGroup()
                        .addComponent(entityEditingClassComboBoxLabel)
                        .addGap(18, 18, 18)
                        .addComponent(entityEditingClassComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(entityEditingClassSeparator, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        entityEditingPanelLayout.setVerticalGroup(
            entityEditingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(entityEditingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(entityEditingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(entityEditingClassComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(entityEditingClassComboBoxLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(entityEditingClassSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(entityEditingSplitPane)
                .addContainerGap())
        );

        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        entityContentPanel.setLayout(new java.awt.BorderLayout());

        modeSelectionButtonGroup.add(creationModeButton);
        creationModeButton.setText("Creation mode");
        creationModeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                creationModeButtonActionPerformed(evt);
            }
        });

        modeSelectionButtonGroup.add(editingModeButton);
        editingModeButton.setText("Editing mode");
        editingModeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editingModeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout entityPanelLayout = new javax.swing.GroupLayout(entityPanel);
        entityPanel.setLayout(entityPanelLayout);
        entityPanelLayout.setHorizontalGroup(
            entityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(entityPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(entityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(entityContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(entityPanelLayout.createSequentialGroup()
                        .addComponent(creationModeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(editingModeButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        entityPanelLayout.setVerticalGroup(
            entityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, entityPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(entityPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(creationModeButton)
                    .addComponent(editingModeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(entityContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        splitPane.setRightComponent(entityPanel);

        oCRResultLabel.setText("OCR result");

        oCRResultTextArea.setColumns(20);
        oCRResultTextArea.setRows(5);
        oCRResultTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                oCRResultTextAreaMouseClicked(evt);
            }
        });
        oCRResultTextAreaScrollPane.setViewportView(oCRResultTextArea);

        javax.swing.GroupLayout oCRResultPanelLayout = new javax.swing.GroupLayout(oCRResultPanel);
        oCRResultPanel.setLayout(oCRResultPanelLayout);
        oCRResultPanelLayout.setHorizontalGroup(
            oCRResultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(oCRResultPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(oCRResultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(oCRResultTextAreaScrollPane)
                    .addGroup(oCRResultPanelLayout.createSequentialGroup()
                        .addComponent(oCRResultLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        oCRResultPanelLayout.setVerticalGroup(
            oCRResultPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, oCRResultPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(oCRResultLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(oCRResultTextAreaScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        splitPane.setTopComponent(oCRResultPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 677, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void oCRResultTextAreaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_oCRResultTextAreaMouseClicked
        if(evt.getButton() == MouseEvent.BUTTON3) {
            //right click
            this.oCRResultPopup.show(this.oCRResultTextArea, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_oCRResultTextAreaMouseClicked

    private void creationModeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_creationModeButtonActionPerformed
        entityContentPanel.removeAll();
        entityContentPanel.add(entityCreationTabbedPane);
        entityContentPanel.validate();
    }//GEN-LAST:event_creationModeButtonActionPerformed

    private void editingModeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editingModeButtonActionPerformed
        Class<?> selectedEntityClass = (Class<?>) entityEditingClassComboBox.getSelectedItem();
        handleEntityEditingQueryPanelUpdate(selectedEntityClass);
    }//GEN-LAST:event_editingModeButtonActionPerformed

    private void entityEditingClassComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entityEditingClassComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_entityEditingClassComboBoxActionPerformed

    private void handleEntityEditingQueryPanelUpdate(Class<?> selectedEntityClass) {
        entityContentPanel.removeAll();
        entityEditingQueryPanel = this.entityEditingQueryPanelCache.get(selectedEntityClass);
        if(entityEditingQueryPanel == null) {
            try {
                this.entityEditingQueryPanel = new QueryPanel<>(entityManager,
                        selectedEntityClass,
                        reflectionFormBuilder,
                        null //initialValue
                );
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
            entityEditingQueryPanelCache.put(selectedEntityClass, entityEditingQueryPanel);
            this.entityEditingQueryPanel.addUpdateListener(new QueryPanelUpdateListener() {
                @Override
                public void onUpdate(QueryPanelUpdateEvent event) {
                    try {
                        Object selectEntity = DocumentForm.this.entityEditingQueryPanel.getSelectedObject();
                        ReflectionFormPanel reflectionFormPanel = DocumentForm.this.reflectionFormBuilder.transform(event.getSource().getEntityClass(),
                                selectEntity);
                        DocumentForm.this.entityEditingReflectionFormPanelScrollPane.setViewportView(reflectionFormPanel);
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }
        entityEditingQueryPanelScrollPane.setViewportView(entityEditingQueryPanel);
        entityContentPanel.add(entityEditingPanel);
        entityContentPanel.validate();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton creationModeButton;
    private javax.swing.JRadioButton editingModeButton;
    private javax.swing.JPanel entityContentPanel;
    private javax.swing.JTabbedPane entityCreationTabbedPane;
    private javax.swing.JComboBox<Class<?>> entityEditingClassComboBox;
    private javax.swing.JLabel entityEditingClassComboBoxLabel;
    private javax.swing.JSeparator entityEditingClassSeparator;
    private javax.swing.JPanel entityEditingPanel;
    private javax.swing.JScrollPane entityEditingQueryPanelScrollPane;
    private javax.swing.JScrollPane entityEditingReflectionFormPanelScrollPane;
    private javax.swing.JSplitPane entityEditingSplitPane;
    private javax.swing.JPanel entityEditingSplitPaneLeftPanel;
    private javax.swing.JPanel entityEditingSplitPaneRightPanel;
    private javax.swing.JPanel entityPanel;
    private javax.swing.ButtonGroup modeSelectionButtonGroup;
    private javax.swing.JLabel oCRResultLabel;
    private javax.swing.JPanel oCRResultPanel;
    private javax.swing.JPopupMenu oCRResultPopup;
    private javax.swing.JMenuItem oCRResultPopupHintMenuItem;
    private javax.swing.JPopupMenu.Separator oCRResultPopupSeparator;
    private javax.swing.JTextArea oCRResultTextArea;
    private javax.swing.JScrollPane oCRResultTextAreaScrollPane;
    private javax.swing.JSplitPane splitPane;
    // End of variables declaration//GEN-END:variables
}
