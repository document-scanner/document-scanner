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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.components.ValueDetectionPanel;
import richtercloud.document.scanner.gui.ocrresult.OCRResult;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.message.handler.ExceptionMessage;
import richtercloud.message.handler.IssueHandler;
import richtercloud.message.handler.Message;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.FieldInfo;
import richtercloud.reflection.form.builder.ReflectionFormPanel;
import richtercloud.reflection.form.builder.TransformationException;
import richtercloud.validation.tools.FieldRetrievalException;
import richtercloud.validation.tools.FieldRetriever;

/**
 * A {@link JMenu} which permits lazy loading of contained items representing
 * fields of the class represented by this menu.
 * @author richter
 */
public class EntityClassMenu extends JMenu {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(EntityClassMenu.class);
    private final IssueHandler issueHandler;

    public EntityClassMenu(Class<?> entityClass,
            FieldRetriever fieldRetriever,
            ReflectionFormPanelTabbedPane reflectionFormPanelTabbedPane,
            Map<Class<? extends JComponent>, ValueSetter<?, ?>> valueSetterMapping,
            AbstractFieldPopupMenuFactory fieldPopupMenuFactory,
            IssueHandler issueHandler) {
        super(findClassName(entityClass));
        this.issueHandler = issueHandler;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                ReflectionFormPanel reflectionFormPanel;
                try {
                    reflectionFormPanel = reflectionFormPanelTabbedPane.getReflectionFormPanel(entityClass);
                } catch (TransformationException | FieldRetrievalException ex) {
                    EntityClassMenu.this.issueHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
                    return;
                }
                List<Field> relevantFields;
                try {
                    relevantFields = fieldRetriever.retrieveRelevantFields(entityClass);
                } catch (FieldRetrievalException ex) {
                    LOGGER.error("unexpected exception during retrieval of fields",
                            ex);
                    issueHandler.handleUnexpectedException(new ExceptionMessage(ex));
                    return;
                }
                for(Field relevantField : relevantFields) {
                    JComponent relevantFieldComponent = reflectionFormPanel.getComponentByField(relevantField);
                    assert relevantFieldComponent instanceof ValueDetectionPanel;
                    ValueDetectionPanel valueDetectionPanel = (ValueDetectionPanel) relevantFieldComponent;
                    JComponent valueDetectionPanelComponent = valueDetectionPanel.getClassComponent();
                    ValueSetter relevantFieldValueSetter = valueSetterMapping.get(valueDetectionPanelComponent.getClass());
                    if(relevantFieldValueSetter == null) {
                        LOGGER.debug(String.format("skipping field %s because it doesn't have a %s mapped",
                                relevantField,
                                ValueSetter.class));
                        continue;
                    }
                    if(!relevantFieldValueSetter.isSupportsOCRResultSetting()) {
                        LOGGER.debug(String.format("skipping field %s because its %s doesn't support setting %ss",
                                relevantField,
                                ValueSetter.class,
                                OCRResult.class));
                        continue;
                    }
                    String fieldName; //it's fine to use FieldInfo.name only
                        //since there're no queries involved
                    FieldInfo fieldInfo = relevantField.getAnnotation(FieldInfo.class);
                    if(fieldInfo != null) {
                        fieldName = fieldInfo.name();
                    }else {
                        fieldName = relevantField.getName();
                    }
                    JMenuItem relevantFieldMenuItem = new JMenuItem(fieldName);
                    relevantFieldMenuItem.addActionListener(fieldPopupMenuFactory.createFieldActionListener(relevantField,
                            reflectionFormPanel));
                    EntityClassMenu.this.add(relevantFieldMenuItem);
                }
            }
        });
    }

    private static String findClassName(Class<?> entityClass) {
        String className;
        ClassInfo classInfo = entityClass.getAnnotation(ClassInfo.class);
        if(classInfo != null) {
            className = classInfo.name();
        }else {
            className = entityClass.getSimpleName();
        }
        return className;
    }
}
