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
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.components.ValueDetectionPanel;
import richtercloud.document.scanner.gui.ocrresult.FormatOCRResult;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.message.handler.Message;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.ReflectionFormPanel;

/**
 * A {@link FieldActionListener} which can deal with predefined formats which
 * are selected in {@link ButtonGroup}s.
 *
 * @author richter
 */
/*
internal implementation notes:
- This is a good abstraction since it allows the trial-error value parsing to be
kept out of ValueSetter and managed here. This requires to check the type of the
field the value is set on which appear unelegant, but isn't so much. This allows
to spare a class to wrap the OCR selection and the selected number, percentage,
date, time, etc. formats (formerly OCRResult and FormatOCRResult) and keeps GUI
operations (figuring out which format is selected in each format's button group)
and model/data operations (setting values on fields and component (models))
separated.
*/
public class FormatOCRFieldActionListener extends OCRFieldActionListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(FormatOCRFieldActionListener.class);
    private final ButtonGroup numberFormatPopupButtonGroup;
    private final ButtonGroup percentFormatPopupButtonGroup;
    private final ButtonGroup currencyFormatPopupButtonGroup;
    private final ButtonGroup dateFormatPopupButtonGroup;
    private final ButtonGroup timeFormatPopupButtonGroup;
    private final ButtonGroup dateTimeFormatPopupButtonGroup;

    public FormatOCRFieldActionListener(ButtonGroup numberFormatPopupButtonGroup,
            ButtonGroup percentFormatPopupButtonGroup,
            ButtonGroup currencyFormatPopupButtonGroup,
            ButtonGroup dateFormatPopupButtonGroup,
            ButtonGroup timeFormatPopupButtonGroup,
            ButtonGroup dateTimeFormatPopupButtonGroup,
            ReflectionFormPanel reflectionFormPanel,
            Field field, Map<Class<? extends JComponent>, ValueSetter<?, ?>> valueSetterMapping,
            JTextArea oCRResultTextArea,
            MessageHandler messageHandler) {
        super(oCRResultTextArea, field, reflectionFormPanel, valueSetterMapping, messageHandler);
        this.numberFormatPopupButtonGroup = numberFormatPopupButtonGroup;
        this.percentFormatPopupButtonGroup = percentFormatPopupButtonGroup;
        this.currencyFormatPopupButtonGroup = currencyFormatPopupButtonGroup;
        this.dateFormatPopupButtonGroup = dateFormatPopupButtonGroup;
        this.timeFormatPopupButtonGroup = timeFormatPopupButtonGroup;
        this.dateTimeFormatPopupButtonGroup = dateTimeFormatPopupButtonGroup;
    }

    @Override
    protected FormatOCRResult retrieveValue() {
        String oCRResult = super.retrieveValue().getoCRResult();
        //cast outside try-catch block in order to cause a
        //ClassCastException in case of concept error which shouldn't be
        //caught
        NumberFormat numberFormat = null;
        NumberFormat percentFormat = null;
        NumberFormat currencyFormat = null;
        DateFormat dateFormat = null;
        DateFormat timeFormat = null;
        DateFormat dateTimeFormat = null;
        //There's no better way to get the selected button from a
        //JButtonGroup
        for (AbstractButton button : Collections.list(numberFormatPopupButtonGroup.getElements())) {
            NumberFormatMenuItem menuItem = (NumberFormatMenuItem) button;
            if (menuItem.isSelected()) {
                numberFormat = menuItem.getNumberFormat();
                break;
            }
        }
        for (AbstractButton button : Collections.list(percentFormatPopupButtonGroup.getElements())) {
            NumberFormatMenuItem menuItem = (NumberFormatMenuItem) button;
            if (menuItem.isSelected()) {
                percentFormat = menuItem.getNumberFormat();
                break;
            }
        }
        for (AbstractButton button : Collections.list(currencyFormatPopupButtonGroup.getElements())) {
            NumberFormatMenuItem menuItem = (NumberFormatMenuItem) button;
            if (menuItem.isSelected()) {
                currencyFormat = menuItem.getNumberFormat();
                break;
            }
        }
        for (AbstractButton button : Collections.list(dateFormatPopupButtonGroup.getElements())) {
            DateFormatMenuItem menuItem = (DateFormatMenuItem) button;
            if (menuItem.isSelected()) {
                dateFormat = menuItem.getDateFormat();
                break;
            }
        }
        for (AbstractButton button : Collections.list(timeFormatPopupButtonGroup.getElements())) {
            DateFormatMenuItem menuItem = (DateFormatMenuItem) button;
            if (menuItem.isSelected()) {
                timeFormat = menuItem.getDateFormat();
                break;
            }
        }
        for (AbstractButton button : Collections.list(dateTimeFormatPopupButtonGroup.getElements())) {
            DateFormatMenuItem menuItem = (DateFormatMenuItem) button;
            if (menuItem.isSelected()) {
                dateTimeFormat = menuItem.getDateFormat();
                break;
            }
        }
        return new FormatOCRResult(numberFormat,
                percentFormat,
                currencyFormat,
                dateFormat,
                timeFormat,
                dateTimeFormat,
                oCRResult);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ValueDetectionPanel comp = (ValueDetectionPanel) retrieveComponent();
            //in document-scanner we can assume that all field components are
            //ValueDetectionPanel
        FormatOCRResult oCRSelection = retrieveValue();
        ValueSetter valueSetter = retrieveValueSetter(comp);

        try {
            valueSetter.setOCRResult(oCRSelection,
                    comp.getClassComponent());
        } catch (Exception ex) {
            LOGGER.error("An exception during setting the OCR value on " + "component occured", ex);
            getMessageHandler().handle(new Message(String.format("The " + "following exception occured while setting the " + "selected value on the field: %s", ExceptionUtils.getRootCauseMessage(ex)), JOptionPane.ERROR_MESSAGE, "Exception occured"));
        }
    }
}
