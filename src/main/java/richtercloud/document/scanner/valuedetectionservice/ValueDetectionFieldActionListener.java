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
package richtercloud.document.scanner.valuedetectionservice;

import java.lang.reflect.Field;
import java.util.Map;
import javax.swing.JComponent;
import richtercloud.document.scanner.gui.AbstractFieldActionListener;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.ReflectionFormPanel;

/**
 *
 * @author richter
 */
public class ValueDetectionFieldActionListener extends AbstractFieldActionListener<Object> {
    private final Object value;

    /**
     * Creates a new {@link ValueDetectionFieldActionListener}.
     * @param value
     * @param field
     * @param reflectionFormPanel
     * @param valueSetterMapping
     * @param messageHandler
     */
    /*
    internal implementation notes:
    - it'd be nice to pass a ValueDetectionResult, but that'd require a
    cyclic reference during creation of ValueDetectionResult (which
    requests a list of CallbackActions) which can be dealt with, but is ugly
    */
    public ValueDetectionFieldActionListener(Object value,
            Field field,
            ReflectionFormPanel reflectionFormPanel,
            Map<Class<? extends JComponent>, ValueSetter<?, ?>> valueSetterMapping,
            MessageHandler messageHandler) {
        super(field, reflectionFormPanel, valueSetterMapping, messageHandler);
        this.value = value;
    }

    @Override
    protected Object retrieveValue() {
        return value;
    }
}
