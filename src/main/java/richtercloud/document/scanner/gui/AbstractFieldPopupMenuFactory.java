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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.message.handler.IssueHandler;
import richtercloud.reflection.form.builder.ReflectionFormPanel;
import richtercloud.validation.tools.FieldRetriever;

/**
 * Creates {@link JMenuItem}s to be added to {@link JPopupMenu}s or
 * {@link JMenu}s (or {@link JMenuItem}s).
 *
 * @author richter
 */
/*
internal implementation notes:
- This class is slightly overkill, but there's no way around it and not have
duplicate code to maintain or overly complex and unelegant code in the form of
static methods.
- Since JPopupMenu doesn't extend JMenu and doesn't implement any shared
interfaces which allow adding of menu items it's necessary that this factory
returns the menu items in order to allow the caller to process them.
*/
public abstract class AbstractFieldPopupMenuFactory {
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractFieldPopupMenuFactory.class);
    private final Map<Class<? extends JComponent>, ValueSetter<?, ?>> valueSetterMapping;
    private final IssueHandler issueHandler;

    public AbstractFieldPopupMenuFactory(Map<Class<? extends JComponent>, ValueSetter<?, ?>> valueSetterMapping,
            IssueHandler issueHandler) {
        this.valueSetterMapping = valueSetterMapping;
        this.issueHandler = issueHandler;
    }

    public Map<Class<? extends JComponent>, ValueSetter<?, ?>> getValueSetterMapping() {
        return valueSetterMapping;
    }

    protected abstract AbstractFieldActionListener createFieldActionListener(Field field,
            ReflectionFormPanel reflectionFormPanel);

    /**
     * Creates a set of {@link JMenuItem}s for each class in
     * {@code entityClassesSort} each containing menu items with every field of
     * the class.
     * @param entityClassesSort sorting can be done with
     * {@link EntityPanel#sortEntityClasses(java.util.Set) }
     * @return the generated menu items
     */
    public List<JMenuItem> createFieldPopupMenuItems(List<Class<?>> entityClassesSort,
            ReflectionFormPanelTabbedPane reflectionFormPanelMap,
            FieldRetriever fieldRetriever) {
        List<JMenuItem> retValue = new LinkedList<>();
        for(Class<?> entityClass : entityClassesSort) {
            JMenu entityClassMenu = new EntityClassMenu(entityClass,
                    fieldRetriever,
                    reflectionFormPanelMap,
                    valueSetterMapping,
                    this,
                    issueHandler);
            retValue.add(entityClassMenu);
        }
        return retValue;
    }
}
