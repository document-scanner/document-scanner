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
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import richtercloud.document.scanner.ifaces.Constants;
import richtercloud.message.handler.ConfirmMessageHandler;
import richtercloud.message.handler.IssueHandler;
import richtercloud.message.handler.Message;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.jpa.WarningHandler;
import richtercloud.reflection.form.builder.jpa.idapplier.IdApplier;
import richtercloud.reflection.form.builder.jpa.panels.QueryHistoryEntryStorage;
import richtercloud.reflection.form.builder.jpa.panels.QueryPanel;
import richtercloud.reflection.form.builder.jpa.storage.FieldInitializer;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;
import richtercloud.reflection.form.builder.storage.StorageException;
import richtercloud.validation.tools.FieldRetrievalException;
import richtercloud.validation.tools.FieldRetriever;

/**
 * A dialog to select the class and the concrete entity to edit or delete it.
 * @author richter
 */
/*
internal implementation notes:
- There's no sense in providing a reflection form panel for editing since both
the GUI and the code logic only makes sense if there's a OCRSelectPanelPanel
present -> only provide components to query and select to be edited entities and
open them in MainPanel.
*/
public class EntityEditingDialog extends javax.swing.JDialog {
    private static final long serialVersionUID = 1L;
    private final ListCellRenderer<Object> entityEditingClassComboBoxRenderer = new DefaultListCellRenderer() {
        private static final long serialVersionUID = 1L;
        @Override
        public Component getListCellRendererComponent(JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            String value0;
            if(value == null) {
                value0 = null;
            }else {
                //might be null during initialization
                Class<?> valueCast = (Class<?>) value;
                ClassInfo classInfo = valueCast.getAnnotation(ClassInfo.class);
                if(classInfo != null) {
                    value0 = classInfo.name();
                }else {
                    value0 = valueCast.getSimpleName();
                }
            }
            return super.getListCellRendererComponent(list, value0, index, isSelected, cellHasFocus);
        }
    };
    private final DefaultComboBoxModel<Class<?>> entityEditingClassComboBoxModel = new DefaultComboBoxModel<>();
    private final PersistenceStorage storage;
    private final FieldRetriever fieldRetriever;
    private QueryPanel<Object> entityEditingQueryPanel;
    /**
     * A cache to keep custom queries when changing the query class (would be
     * overwritten at recreation. This also avoid extranous recreations.
     */
    /*
    internal implementation notes:
    - for necessity for instance creation see class comment
    */
    private final Map<Class<?>, QueryPanel<Object>> entityEditingQueryPanelCache = new HashMap<>();
    private final IssueHandler issueHandler;
    private final ConfirmMessageHandler confirmMessageHandler;
    private final FieldInitializer fieldInitializer;
    private final QueryHistoryEntryStorage entryStorage;

    public EntityEditingDialog(Window parent,
            Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            PersistenceStorage storage,
            IssueHandler issueHandler,
            ConfirmMessageHandler confirmMessageHandler,
            IdApplier<?> idApplier,
            Map<Class<?>, WarningHandler<?>> warningHandlers,
            FieldInitializer fieldInitializer,
            QueryHistoryEntryStorage entryStorage,
            FieldRetriever fieldRetriever) {
        super(parent,
                ModalityType.APPLICATION_MODAL);
        this.issueHandler = issueHandler;
        this.confirmMessageHandler = confirmMessageHandler;
        this.storage = storage;
        this.fieldInitializer = fieldInitializer;
        this.entryStorage = entryStorage;
        init(entityClasses, primaryClassSelection, storage);
        this.fieldRetriever = fieldRetriever;
        init1(entityClasses, primaryClassSelection);
    }

    private void init(Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            PersistenceStorage storage) {
        initComponents();
        if(issueHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        if(storage == null) {
            throw new IllegalArgumentException("storage mustn't be null");
        }
        if(entityClasses == null) {
            throw new IllegalArgumentException("entityClasses mustn't be null");
        }
        if(entityClasses.isEmpty()) {
            throw new IllegalArgumentException("entityClass mustn't be empty");
        }
        if(!entityClasses.contains(primaryClassSelection)) {
            throw new IllegalArgumentException(String.format("primaryClassSelection '%s' has to be contained in entityClasses", primaryClassSelection));
        }
    }

    private void init1(Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection) {
        this.entityEditingClassComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                //don't recreate QueryPanel instances, but cache them in order
                //to keep custom queries managed in QueryPanel
                if(EntityEditingDialog.this.entityEditingQueryPanel != null) {
                    Class<?> selectedEntityClass = (Class<?>) e.getItem();
                    handleEntityEditingQueryPanelUpdate(selectedEntityClass);
                }
            }
        });
        List<Class<?>> entityClassesSort = Tools.sortEntityClasses(entityClasses);
        for(Class<?> entityClass : entityClassesSort) {
            this.entityEditingClassComboBoxModel.addElement(entityClass);
        }
        this.entityEditingClassComboBox.setSelectedItem(primaryClassSelection);
    }

    private void handleEntityEditingQueryPanelUpdate(Class<?> selectedEntityClass) {
        entityEditingQueryPanel = this.entityEditingQueryPanelCache.get(selectedEntityClass);
        if(entityEditingQueryPanel == null) {
            try {
                this.entityEditingQueryPanel = new QueryPanel(storage,
                        selectedEntityClass,
                        issueHandler,
                        fieldRetriever,
                        null, //initialValue
                        null, //bidirectionalControlPanel (doesn't make sense)
                        ListSelectionModel.MULTIPLE_INTERVAL_SELECTION,
                        fieldInitializer,
                        entryStorage
                );
            } catch (IllegalArgumentException | IllegalAccessException | FieldRetrievalException ex) {
                throw new RuntimeException(ex);
            }
            entityEditingQueryPanelCache.put(selectedEntityClass, entityEditingQueryPanel);
        }
        this.entityEditingQueryPanelScrollPane.setViewportView(entityEditingQueryPanel);
        this.entityEditingQueryPanelScrollPane.getVerticalScrollBar().setUnitIncrement(Constants.DEFAULT_SCROLL_INTERVAL);
        this.entityEditingQueryPanelScrollPane.getHorizontalScrollBar().setUnitIncrement(Constants.DEFAULT_SCROLL_INTERVAL);
        this.entityEditingQueryPanel.clearSelection();
    }

    public List<Object> getSelectedEntities() {
        List<Object> retValue = this.entityEditingQueryPanel.getSelectedObjects();
        return retValue;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        entityEditingClassComboBox = new JComboBox<>();
        ;
        entityEditingClassComboBoxLabel = new javax.swing.JLabel();
        entityEditingClassSeparator = new javax.swing.JSeparator();
        entityEditingQueryPanelScrollPane = new javax.swing.JScrollPane();
        editButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBounds(new java.awt.Rectangle(0, 0, 1024, 768));
        setPreferredSize(new java.awt.Dimension(1024, 768));
        setSize(new java.awt.Dimension(1024, 768));

        entityEditingClassComboBox.setModel(entityEditingClassComboBoxModel);
        entityEditingClassComboBox.setRenderer(entityEditingClassComboBoxRenderer);
        entityEditingClassComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                entityEditingClassComboBoxActionPerformed(evt);
            }
        });

        entityEditingClassComboBoxLabel.setText("Query class:");

        editButton.setText("Edit");
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(entityEditingClassComboBoxLabel)
                        .addGap(18, 18, 18)
                        .addComponent(entityEditingClassComboBox, 0, 606, Short.MAX_VALUE))
                    .addComponent(entityEditingClassSeparator)
                    .addComponent(entityEditingQueryPanelScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 714, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(entityEditingClassComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(entityEditingClassComboBoxLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(entityEditingClassSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(entityEditingQueryPanelScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(editButton)
                    .addComponent(cancelButton)
                    .addComponent(deleteButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void entityEditingClassComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entityEditingClassComboBoxActionPerformed
        Class<?> selectedEntityClass = (Class<?>) entityEditingClassComboBox.getSelectedItem();
        handleEntityEditingQueryPanelUpdate(selectedEntityClass);
    }//GEN-LAST:event_entityEditingClassComboBoxActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.entityEditingQueryPanel.clearSelection(); //causes
            //getSelectedEntities to return an empty list which indicates that
            //the dialog has been canceled
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_editButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        String answer = confirmMessageHandler.confirm(new Message(
                "Do you really want to delete all selected entities?",
                JOptionPane.QUESTION_MESSAGE,
                "Delete entities"),
                "Yes", "No");
        if(answer.equals("Yes")) {
            List<Object> selectedEntities = this.entityEditingQueryPanel.getSelectedObjects();
            for(Object selectedEntity : selectedEntities) {
                try {
                    this.storage.delete(selectedEntity);
                } catch (StorageException ex) {
                    issueHandler.handle(new Message(ex,
                            JOptionPane.ERROR_MESSAGE));
                }
            }
            this.entityEditingQueryPanel.getQueryComponent().repeatLastQuery();
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JComboBox<Class<?>> entityEditingClassComboBox;
    private javax.swing.JLabel entityEditingClassComboBoxLabel;
    private javax.swing.JSeparator entityEditingClassSeparator;
    private javax.swing.JScrollPane entityEditingQueryPanelScrollPane;
    // End of variables declaration//GEN-END:variables
}
