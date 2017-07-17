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
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
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
import richtercloud.message.handler.Message;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.ReflectionFormPanel;
import richtercloud.reflection.form.builder.TransformationException;
import richtercloud.reflection.form.builder.fieldhandler.FieldHandler;
import richtercloud.reflection.form.builder.jpa.JPAReflectionFormBuilder;
import richtercloud.validation.tools.FieldRetrievalException;

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
    private final MessageHandler messageHandler;
    private final Set<ReflectionFormPanelTabbedPaneLister> listeners = new HashSet<>();

    /**
     * Creates a {@code ReflectionFormPanelTabbedPane} with lazy loading tabs
     * for all classes in {@code entityClasses}.
     * @param entityClasses
     * @param primaryClassSelection determines which tab for which entity class
     * is selected after creation if {@code entityToEdit} is unspecified.
     * @param entityToEdit the entity to retrieve initial values for the tab of
     * the class of {@code entityToEdit}. If another instance of the class of
     * {@code entityToEdit} is supposed to be saved, the
     * {@code ReflectionFormPanel} can be reset.
     * @param reflectionFormBuilder
     * @param fieldHandler
     */
    public ReflectionFormPanelTabbedPane(Set<Class<?>> entityClasses,
            Class<?> primaryClassSelection,
            Object entityToEdit,
            JPAReflectionFormBuilder reflectionFormBuilder,
            FieldHandler fieldHandler,
            MessageHandler messageHandler) throws TransformationException, FieldRetrievalException {
        this.entityClasses = entityClasses;
        this.primaryClassSelection = primaryClassSelection;
        this.reflectionFormBuilder = reflectionFormBuilder;
        this.fieldHandler = fieldHandler;
        this.messageHandler = messageHandler;
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
            Component tabComponent;
            if(entityToEdit == null || !entityToEdit.getClass().equals(entityClass)) {
                tabComponent = new JPanel(); //placeholder until tab is selected
            }else {
                ReflectionFormPanel reflectionFormPanel = reflectionFormBuilder.transformEntityClass(entityToEdit.getClass(),
                        entityToEdit,
                        true, //editingMode
                        fieldHandler
                );
                tabComponent = createReflectionFormPanelScrollPane(reflectionFormPanel);
            }
            this.insertTab(createClassTabTitle(entityClass),
                    newTabIcon,
                    tabComponent,
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
            try {
                selectedTabPanel = getReflectionFormPanel(entityClass);
            } catch (TransformationException | FieldRetrievalException ex) {
                String message = String.format("An exception during creation of components occured (details: %s)",
                        ex.getMessage());
                LOGGER.error(message, ex);
                messageHandler.handle(new Message(message,
                        JOptionPane.ERROR_MESSAGE,
                        "Component creation failed"));
                throw new RuntimeException(ex);
            }
            JScrollPane reflectionFormPanelScrollPane = createReflectionFormPanelScrollPane(selectedTabPanel);
            this.setComponentAt(selectedIndex,
                    reflectionFormPanelScrollPane);
                //@TODO: recreation of JScrollPane at every tab switch not
                //necessary
        });
        this.setSelectedIndex(this.indexOfTab(createClassTabTitle(primaryClassSelection)));
    }

    private JScrollPane createReflectionFormPanelScrollPane(ReflectionFormPanel reflectionFormPanel) {
        JScrollPane reflectionFormPanelScrollPane = new JScrollPane(reflectionFormPanel);
        reflectionFormPanelScrollPane.getVerticalScrollBar().setUnitIncrement(Constants.DEFAULT_SCROLL_INTERVAL);
        reflectionFormPanelScrollPane.getHorizontalScrollBar().setUnitIncrement(Constants.DEFAULT_SCROLL_INTERVAL);
        return reflectionFormPanelScrollPane;
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

    public ReflectionFormPanel getReflectionFormPanel(Class<?> entityClass) throws TransformationException, FieldRetrievalException {
        ReflectionFormPanel reflectionFormPanel = classPanelMap.get(entityClass);
        if(reflectionFormPanel == null) {
            reflectionFormPanel = reflectionFormBuilder.transformEntityClass(entityClass,
                    null, //entityToUpdate
                    false, //editingMode
                    fieldHandler
            );
            classPanelMap.put(entityClass,
                    reflectionFormPanel);
            for(ReflectionFormPanelTabbedPaneLister listener : listeners) {
                listener.onReflectionFormPanelLazilyCreated(reflectionFormPanel);
            }
        }
        return reflectionFormPanel;
    }

    public void addReflectionFormPanelTabbedPaneListener(ReflectionFormPanelTabbedPaneLister listener) {
        listeners.add(listener);
    }

    public void removeReflectionFormPanelTabbedPaneListener(ReflectionFormPanelTabbedPaneLister listener) {
        listeners.remove(listener);
    }
}
