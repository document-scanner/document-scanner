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
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.FieldInfo;
import richtercloud.reflection.form.builder.ReflectionFormBuilder;
import richtercloud.reflection.form.builder.ReflectionFormPanel;
import richtercloud.reflection.form.builder.message.Message;
import richtercloud.reflection.form.builder.message.MessageHandler;

/**
 * The counterpart of every {@link OCRSelectPanel} which contains a text field
 * to display OCR selection results
 * @author richter
 */
/*
internal implementation notes:
- popup menu has a single menu "Paste into" rather than a disabled menu item
which is (mis)used as label for the following menu items because that's more
elegant even if less easy to use (means one click more)
*/
public class OCRPanel extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(OCRPanel.class);
    private final MessageHandler messageHandler;

    /**
     * Creates new form OCRResultPanel
     * @param reflectionFormPanel A reference to the {@link ReflectionFormPanel}
     * which is manipulated by the context menu items
     */
    public OCRPanel(Set<Class<?>> entityClasses,
            Map<Class<?>, ReflectionFormPanel> reflectionFormPanelMap,
            Map<Class<? extends JComponent>, ValueSetter<?>> valueSetterMapping,
            EntityManager entityManager,
            MessageHandler messageHandler,
            ReflectionFormBuilder reflectionFormBuilder) {
        this.initComponents();
        if(messageHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.messageHandler = messageHandler;
        List<Class<?>> entityClassesSort = EntityPanel.sortEntityClasses(entityClasses);
        for(Class<?> entityClass : entityClassesSort) {
            ReflectionFormPanel reflectionFormPanel = reflectionFormPanelMap.get(entityClass);
            if(reflectionFormPanel == null) {
                throw new IllegalArgumentException(String.format("entityClass %s has no %s mapped in reflectionFormPanelMap",
                        entityClass,
                        ReflectionFormPanel.class));
            }
            String className;
            ClassInfo classInfo = entityClass.getAnnotation(ClassInfo.class);
            if(classInfo != null) {
                className = classInfo.name();
            }else {
                className = entityClass.getSimpleName();
            }
            JMenu entityClassMenu = new JMenu(className);
            List<Field> relevantFields = reflectionFormBuilder.getFieldRetriever().retrieveRelevantFields(entityClass);
            for(Field relevantField : relevantFields) {
                String fieldName;
                FieldInfo fieldInfo = relevantField.getAnnotation(FieldInfo.class);
                if(fieldInfo != null) {
                    fieldName = fieldInfo.name();
                }else {
                    fieldName = relevantField.getName();
                }
                JMenuItem relevantFieldMenuItem = new JMenuItem(fieldName);
                relevantFieldMenuItem.addActionListener(new FieldActionListener(reflectionFormPanel,
                        relevantField,
                        valueSetterMapping));
                entityClassMenu.add(relevantFieldMenuItem);
            }
            this.oCRResultPopupPasteIntoMenu.add(entityClassMenu);
        }
    }

    public JTextArea getoCRResultTextArea() {
        return this.oCRResultTextArea;
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
            String oCRSelection = OCRPanel.this.oCRResultTextArea.getSelectedText();
            ValueSetter valueSetter = this.valueSetterMapping.get(comp.getClass());
            if(valueSetter != null) {
                try {
                    valueSetter.setValue(oCRSelection, comp);
                }catch(Exception ex) {
                    LOGGER.error("An exception during setting the OCR value on component occured", ex);
                    messageHandler.handle(new Message(String.format("The following exception occured while setting the selected value on the field: %s",
                            ExceptionUtils.getRootCauseMessage(ex)),
                            JOptionPane.ERROR_MESSAGE));
                }
            }else {
                throw new IllegalArgumentException(String.format("No %s mapped to component %s",
                        ValueSetter.class,
                        comp));
            }
        }
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
        oCRResultPopupPasteIntoMenu = new javax.swing.JMenu();
        numberFormatPopup = new javax.swing.JPopupMenu();
        jRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        oCRResultLabel = new javax.swing.JLabel();
        oCRResultTextAreaScrollPane = new javax.swing.JScrollPane();
        oCRResultTextArea = new javax.swing.JTextArea();

        oCRResultPopupPasteIntoMenu.setText("Paste into");
        oCRResultPopup.add(oCRResultPopupPasteIntoMenu);

        jRadioButtonMenuItem1.setSelected(true);
        jRadioButtonMenuItem1.setText("jRadioButtonMenuItem1");
        numberFormatPopup.add(jRadioButtonMenuItem1);

        oCRResultLabel.setText("OCR result");

        oCRResultTextArea.setColumns(20);
        oCRResultTextArea.setRows(5);
        oCRResultTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                oCRResultTextAreaMouseClicked(evt);
            }
        });
        oCRResultTextAreaScrollPane.setViewportView(oCRResultTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(oCRResultTextAreaScrollPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(oCRResultLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(oCRResultLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(oCRResultTextAreaScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void oCRResultTextAreaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_oCRResultTextAreaMouseClicked
        if(evt.getButton() == MouseEvent.BUTTON3) {
            //right click
            this.oCRResultPopup.show(this.oCRResultTextArea, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_oCRResultTextAreaMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JPopupMenu numberFormatPopup;
    private javax.swing.JLabel oCRResultLabel;
    private javax.swing.JPopupMenu oCRResultPopup;
    private javax.swing.JMenu oCRResultPopupPasteIntoMenu;
    private javax.swing.JTextArea oCRResultTextArea;
    private javax.swing.JScrollPane oCRResultTextAreaScrollPane;
    // End of variables declaration//GEN-END:variables
}
