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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.ifaces.Constants;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.ReflectionFormPanel;
import richtercloud.reflection.form.builder.fieldhandler.FieldHandler;
import richtercloud.reflection.form.builder.fieldhandler.FieldHandlingException;
import richtercloud.reflection.form.builder.jpa.JPAReflectionFormBuilder;

/**
 * Used as {@code JTabbedPane} and factory for all {@link ReflectionFormPanel}s
 * through {@link #getReflectionFormPanel(java.lang.Class) }.
 *
 * @author richter
 */
public class ReflectionFormPanelTabbedPane extends JTabbedPane {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(ReflectionFormPanelTabbedPane.class);
    /**
     * Maps requested classes to created {@link ReflectionFormPanel}s.
     */
    /*
    internal implementation notes:
    - mapping indices to classes avoids one Map.get for retrieval of the class
    based on index, but costs one more at storage
    */
    private final Map<Class<?>, ReflectionFormPanel> classPanelMap = new HashMap<>();
    private final Map<Integer, Class<?>> indexClassMap = new HashMap<>();
    private final Set<Class<?>> entityClasses;
    private final Class<?> primaryClassSelection;
    private final JPAReflectionFormBuilder reflectionFormBuilder;
    private final FieldHandler fieldHandler;

    public ReflectionFormPanelTabbedPane(Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            JPAReflectionFormBuilder reflectionFormBuilder,
            FieldHandler fieldHandler) {
        this.entityClasses = entityClasses;
        this.primaryClassSelection = primaryClassSelection;
        this.reflectionFormBuilder = reflectionFormBuilder;
        this.fieldHandler = fieldHandler;
        int i=0;
        for(Class<?> entityClass : Tools.sortEntityClasses(entityClasses)) {
            indexClassMap.put(i,
                    entityClass);
            i += 1;
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
            this.insertTab(createClassTabTitle(entityClass),
                    newTabIcon,
                    new JPanel(), //component (placeholder until tab is
                        //selected)
                    newTabTip,
                    this.getTabCount()
            );
        }
        addChangeListener((ChangeEvent e) -> {
            int selectedIndex = ReflectionFormPanelTabbedPane.this.getSelectedIndex();
            assert selectedIndex >= 0;
            Class<?> entityClass = indexClassMap.get(selectedIndex);
            assert entityClass != null;
            ReflectionFormPanel selectedTabPanel = classPanelMap.get(entityClass);
            selectedTabPanel = getReflectionFormPanel(entityClass);
            JScrollPane reflectionFormPanelScrollPane = new JScrollPane(selectedTabPanel);
            reflectionFormPanelScrollPane.getVerticalScrollBar().setUnitIncrement(Constants.DEFAULT_SCROLL_INTERVAL);
            reflectionFormPanelScrollPane.getHorizontalScrollBar().setUnitIncrement(Constants.DEFAULT_SCROLL_INTERVAL);
            this.setComponentAt(selectedIndex,
                    reflectionFormPanelScrollPane);
                //@TODO: recreation of JScrollPane at every tab switch not
                //necessary
        });
        this.setSelectedIndex(this.indexOfTab(createClassTabTitle(primaryClassSelection)));
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

    public ReflectionFormPanel getReflectionFormPanel(Class<?> entityClass) {
        ReflectionFormPanel reflectionFormPanel = classPanelMap.get(entityClass);
        if(reflectionFormPanel == null) {
            try {
                reflectionFormPanel = reflectionFormBuilder.transformEntityClass(entityClass,
                        null, //entityToUpdate
                        false, //editingMode
                        fieldHandler
                );
                classPanelMap.put(entityClass,
                        reflectionFormPanel);
            } catch (FieldHandlingException ex) {
                String message = String.format("An exception during creation of components occured (details: %s)",
                        ex.getMessage());
                JOptionPane.showMessageDialog(ReflectionFormPanelTabbedPane.this,
                        message,
                        DocumentScanner.generateApplicationWindowTitle("Exception",
                                DocumentScanner.APP_NAME,
                                DocumentScanner.APP_VERSION),
                        JOptionPane.WARNING_MESSAGE);
                LOGGER.error(message, ex);
                throw new RuntimeException(ex);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            }
        }
        return reflectionFormPanel;
    }
}
