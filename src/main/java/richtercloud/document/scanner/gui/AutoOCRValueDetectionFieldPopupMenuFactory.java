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
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JTable;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.reflection.form.builder.ReflectionFormPanel;
import richtercloud.reflection.form.builder.message.MessageHandler;

/**
 *
 * @author richter
 */
public class AutoOCRValueDetectionFieldPopupMenuFactory extends AbstractFieldPopupMenuFactory {
    private final JTable valueTable;
    private final MessageHandler messageHandler;
    private final Map<Class<? extends JComponent>, ValueSetter<?,?>> valueSetterMapping;

    public AutoOCRValueDetectionFieldPopupMenuFactory(JTable valueTable, MessageHandler messageHandler, Map<Class<? extends JComponent>, ValueSetter<?, ?>> valueSetterMapping) {
        this.valueTable = valueTable;
        this.messageHandler = messageHandler;
        this.valueSetterMapping = valueSetterMapping;
    }

    @Override
    protected AbstractFieldActionListener createFieldActionListener(Field field,
            ReflectionFormPanel reflectionFormPanel) {
        return new AutoOCRValueDetectionFieldActionListener(valueTable,
                field,
                reflectionFormPanel,
                valueSetterMapping,
                messageHandler);
    }
}
