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
package richtercloud.document.scanner.components;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.document.scanner.valuedetectionservice.ValueDetectionResult;
import richtercloud.message.handler.ConfirmMessageHandler;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.fieldhandler.FieldHandler;
import richtercloud.reflection.form.builder.fieldhandler.FieldHandlingException;
import richtercloud.reflection.form.builder.jpa.JPAFieldRetriever;
import richtercloud.reflection.form.builder.jpa.JPAReflectionFormBuilder;
import richtercloud.reflection.form.builder.jpa.WarningHandler;
import richtercloud.reflection.form.builder.jpa.idapplier.IdApplier;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;

/**
 * The value of the Auto OCR value detection could be set on the field, but then
 * the question is how to notify the component about the change since
 * - there's no way to listen to field changes
 * - there's no way to force components to have an interface implemented since
 * JComponent is not an interface (and wrapping the component is a tremendous
 * piece work)
 * -> @TODO
 * @author richter
 */
public class AutoOCRValueDetectionReflectionFormBuilder extends JPAReflectionFormBuilder {
    /**
     * The association of the field of each class (used in order to avoid
     * confusion between the same field used in (super) classes and subclasses)
     * and the {@link DefaultComboBoxModel} to represent detected values for
     * selection.
     */
    private final Map<Pair<Class, Field>, DefaultComboBoxModel<ValueDetectionResult<?>>> comboBoxModelMap = new HashMap<>();
    private final Map<Class<? extends JComponent>, ValueSetter<?,?>> valueSetterMapping;
    private final Set<JPanel> autoOCRValueDetectionPanels = new HashSet<>();

    public AutoOCRValueDetectionReflectionFormBuilder(PersistenceStorage storage,
            String fieldDescriptionDialogTitle,
            MessageHandler messageHandler,
            ConfirmMessageHandler confirmMessageHandler,
            JPAFieldRetriever fieldRetriever,
            IdApplier idApplier,
            Map<Class<?>, WarningHandler<?>> warningHandlers,
            Map<Class<? extends JComponent>, ValueSetter<?,?>> valueSetterMapping) {
        super(storage,
                fieldDescriptionDialogTitle,
                messageHandler,
                confirmMessageHandler,
                fieldRetriever,
                idApplier,
                warningHandlers);
        this.valueSetterMapping = valueSetterMapping;
    }

    public Map<Pair<Class, Field>, DefaultComboBoxModel<ValueDetectionResult<?>>> getComboBoxModelMap() {
        return Collections.unmodifiableMap(comboBoxModelMap);
    }

    public Set<JPanel> getAutoOCRValueDetectionPanels() {
        return Collections.unmodifiableSet(autoOCRValueDetectionPanels);
    }

    @Override
    protected JComponent getClassComponent(final Field field,
            Class<?> entityClass,
            final Object instance,
            FieldHandler fieldHandler) throws IllegalAccessException, FieldHandlingException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        final JComponent classComponent = super.getClassComponent(field,
                entityClass,
                instance,
                fieldHandler);
        //put label and combobox in a separate panel in order to be able to
        //trigger visibility
        JPanel autoOCRValueDetectionPanel = new JPanel();
        GroupLayout autoOCRValueDetectionPanelLayout = new GroupLayout(autoOCRValueDetectionPanel);
        autoOCRValueDetectionPanel.setLayout(autoOCRValueDetectionPanelLayout);
        autoOCRValueDetectionPanelLayout.setAutoCreateGaps(true);
        JLabel label = new JLabel("Auto OCR detection values:");
        JComboBox<ValueDetectionResult<?>> comboBox = new JComboBox<>();
        final DefaultComboBoxModel<ValueDetectionResult<?>> comboBoxModel = new DefaultComboBoxModel<>();
        comboBox.setModel(comboBoxModel);
        comboBox.setRenderer(new ListCellRenderer<ValueDetectionResult<?>>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends ValueDetectionResult<?>> list,
                    ValueDetectionResult<?> value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                if(value == null) {
                    return new JLabel(" " //need one space in order to not make
                        //the bar ComboBox appear pressed flat
                    ); // value null is passed to
                        //getListCellRendererComponent if model is empty
                }
//                JPanel retValue = new JPanel();
//                GroupLayout layout = new GroupLayout(retValue);
//                retValue.setLayout(layout);
//                layout.setAutoCreateContainerGaps(true);
//                layout.setAutoCreateGaps(true);
//                JLabel valueLabel = new JLabel(String.format("value: %s", value.getValue().toString()));
//                JLabel oCRSourceLabel = new JLabel(String.format("OCR source: %s", value.getoCRSource()));
//                layout.setVerticalGroup(layout.createParallelGroup()
//                        .addComponent(valueLabel)
//                        .addGap(GroupLayout.DEFAULT_SIZE)
//                        .addComponent(oCRSourceLabel));
//                layout.setHorizontalGroup(layout.createSequentialGroup()
//                        .addComponent(valueLabel)
//                        .addGap(GroupLayout.DEFAULT_SIZE)
//                        .addComponent(oCRSourceLabel));
//                return retValue;
                return new JLabel(String.format("Value: %s    OCR source: %s",
                            //\t instead of 4 space doesn't work
                        value.getValue().toString(),
                        value.getoCRSource()));
            }
        });
        comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    //other state changes aren't interesting
                    ValueDetectionResult<?> detectionResult = (ValueDetectionResult<?>) e.getItem();
                    ValueSetter valueSetter = valueSetterMapping.get(classComponent.getClass());
                    if(valueSetter == null) {
                        throw new IllegalArgumentException(String.format("no %s mapped to component class %s", ValueSetter.class, classComponent.getClass()));
                    }
                    valueSetter.setValue(detectionResult.getValue(), classComponent);

                    //set the combobox back to null in order to avoid the
                    //impression that it reflects the state of the component
                    //(it's just used to select auto detection values and copy
                    //them onto the component)
                    comboBoxModel.setSelectedItem(null);
                }
            }
        });
        autoOCRValueDetectionPanelLayout.setVerticalGroup(autoOCRValueDetectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(label)
                .addComponent(comboBox));
        autoOCRValueDetectionPanelLayout.setHorizontalGroup(autoOCRValueDetectionPanelLayout.createSequentialGroup()
                .addComponent(label)
                .addComponent(comboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        AutoOCRValueDetectionPanel retValue = new AutoOCRValueDetectionPanel(classComponent,
                autoOCRValueDetectionPanel);

        Pair<Class, Field> pair = new ImmutablePair<>(entityClass, field);
        comboBoxModelMap.put(pair, comboBoxModel);
        autoOCRValueDetectionPanels.add(autoOCRValueDetectionPanel);
        return retValue;
    }

}
