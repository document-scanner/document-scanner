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
import javax.swing.JTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.ocrresult.OCRResult;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.ReflectionFormPanel;

/**
 * Handles click on menu items in the OCR text area popup menu which cause
 * values (selected text or the complete text area content if no text is
 * selected) to be set on the field which corresponds to the menu item.
 */
public class OCRFieldActionListener extends AbstractFieldActionListener<OCRResult> {

    private final static Logger LOGGER = LoggerFactory.getLogger(OCRFieldActionListener.class);
    private final JTextArea oCRResultTextArea;

    public OCRFieldActionListener(JTextArea oCRResultTextArea,
            Field field,
            ReflectionFormPanel reflectionFormPanel,
            Map<Class<? extends JComponent>, ValueSetter<?, ?>> valueSetterMapping,
            MessageHandler messageHandler) {
        super(field, reflectionFormPanel, valueSetterMapping, messageHandler);
        this.oCRResultTextArea = oCRResultTextArea;
    }

    @Override
    protected OCRResult retrieveValue() {
        String oCRSelection = this.oCRResultTextArea.getSelectedText();
        if (oCRSelection == null) {
            //if no text is selected use the complete content of the OCR
            //text area
            oCRSelection = this.oCRResultTextArea.getText();
            //leave trimming the text of whitespace to ValueSetters
            //(you might never know what might be needed)
        }
        return new OCRResult(oCRSelection);
    }
}
