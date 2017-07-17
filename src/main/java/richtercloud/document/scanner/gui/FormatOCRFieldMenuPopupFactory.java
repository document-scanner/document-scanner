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
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.message.handler.IssueHandler;
import richtercloud.reflection.form.builder.ReflectionFormPanel;

/**
 *
 * @author richter
 */
public class FormatOCRFieldMenuPopupFactory extends OCRFieldPopupMenuFactory {
    private final ButtonGroup numberFormatPopupButtonGroup;
    private final ButtonGroup percentFormatPopupButtonGroup;
    private final ButtonGroup currencyFormatPopupButtonGroup;
    private final ButtonGroup dateFormatPopupButtonGroup;
    private final ButtonGroup timeFormatPopupButtonGroup;
    private final ButtonGroup dateTimeFormatPopupButtonGroup;

    public FormatOCRFieldMenuPopupFactory(ButtonGroup numberFormatPopupButtonGroup,
            ButtonGroup percentFormatPopupButtonGroup,
            ButtonGroup currencyFormatPopupButtonGroup,
            ButtonGroup dateFormatPopupButtonGroup,
            ButtonGroup timeFormatPopupButtonGroup,
            ButtonGroup dateTimeFormatPopupButtonGroup,
            JTextArea oCRResultTextArea,
            IssueHandler issueHandler,
            Map<Class<? extends JComponent>, ValueSetter<?, ?>> valueSetterMapping) {
        super(oCRResultTextArea,
                issueHandler,
                valueSetterMapping);
        this.numberFormatPopupButtonGroup = numberFormatPopupButtonGroup;
        this.percentFormatPopupButtonGroup = percentFormatPopupButtonGroup;
        this.currencyFormatPopupButtonGroup = currencyFormatPopupButtonGroup;
        this.dateFormatPopupButtonGroup = dateFormatPopupButtonGroup;
        this.timeFormatPopupButtonGroup = timeFormatPopupButtonGroup;
        this.dateTimeFormatPopupButtonGroup = dateTimeFormatPopupButtonGroup;
    }

    @Override
    protected AbstractFieldActionListener createFieldActionListener(Field field,
            ReflectionFormPanel reflectionFormPanel) {
        return new FormatOCRFieldActionListener(numberFormatPopupButtonGroup,
                        percentFormatPopupButtonGroup,
                        currencyFormatPopupButtonGroup,
                        dateFormatPopupButtonGroup,
                        timeFormatPopupButtonGroup,
                        dateTimeFormatPopupButtonGroup,
                        reflectionFormPanel,
                        field,
                        getValueSetterMapping(),
                        getoCRResultTextArea(),
                        getMessageHandler());
    }
}
