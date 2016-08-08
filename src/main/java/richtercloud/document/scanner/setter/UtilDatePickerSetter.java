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
package richtercloud.document.scanner.setter;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.FormatOCRResult;
import richtercloud.document.scanner.gui.OCRPanel;
import richtercloud.reflection.form.builder.components.UtilDatePicker;

/**
 * Taken from http://stackoverflow.com/questions/9288350/adding-vertical-scroll-to-a-jpopupmenu/14167008#14167008 under CC-by-SA.
 *
 * @author richter
 */
public class UtilDatePickerSetter implements ValueSetter<FormatOCRResult, UtilDatePicker> {
    private final static UtilDatePickerSetter INSTANCE = new UtilDatePickerSetter();
    private final static Logger LOGGER = LoggerFactory.getLogger(UtilDatePickerSetter.class);

    public static UtilDatePickerSetter getInstance() {
        return INSTANCE;
    }

    @Override
    public void setValue(FormatOCRResult value, UtilDatePicker comp) {
        Date date = null;
        //order date time, date and time (see OCRPanel) and the fact that one
        //of them is automatic or not in the chain doesn't matter (see OCRPanel)
        if(value.getDateTimeFormat() == null) {
            //automatic
            for(Locale locale : Locale.getAvailableLocales()) {
                for(int formatInt : OCRPanel.DATE_FORMAT_INTS) {
                    for(int formatInt1 : OCRPanel.DATE_FORMAT_INTS) {
                        try {
                            date = DateFormat.getDateTimeInstance(formatInt, formatInt1, locale).parse(value.getoCRResult());
                            break; //first match is the chosen one
                        }catch(ParseException ex) {
                            //skip to next locale
                        }
                    }
                }
            }
            if(date == null) {
                if(value.getDateFormat() == null) {
                    //automatic
                    for(Locale locale : Locale.getAvailableLocales()) {
                        for(int formatInt : OCRPanel.DATE_FORMAT_INTS) {
                            try {
                                date = DateFormat.getDateInstance(formatInt, locale).parse(value.getoCRResult());
                            }catch(ParseException ex) {
                                //skip to next locale
                            }
                        }
                    }
                    if(date == null) {
                        if(value.getTimeFormat() == null) {
                            //automatic
                            for(Locale locale : Locale.getAvailableLocales()) {
                                for(int formatInt : OCRPanel.DATE_FORMAT_INTS) {
                                    try {
                                        date = DateFormat.getTimeInstance(formatInt, locale).parse(value.getoCRResult());
                                    }catch(ParseException ex) {
                                        //skip to next locale
                                    }
                                }
                            }
                            if(date == null) {
                                //all three automatic formats failed
                                throw new IllegalArgumentException(String.format("No date, time and date-time format of any locale succeeds to parse the OCR result '%s'",
                                        value.getoCRResult()));
                            }
                        } else {
                            try {
                                date = value.getTimeFormat().parse(value.getoCRResult());
                            }catch(ParseException ex) {
                                throw new IllegalArgumentException(String.format("Failed to parse OCR result '%s'",
                                        value.getoCRResult()));
                            }
                        }
                    }
                }else {
                    try {
                        date = value.getDateFormat().parse(value.getoCRResult());
                    }catch(ParseException ex) {
                        throw new IllegalArgumentException(String.format("Failed to parse OCR result '%s'",
                                value.getoCRResult()));
                    }
                }
            }
        }else {
            try {
                date = value.getDateTimeFormat().parse(value.getoCRResult());
            } catch (ParseException ex) {
                throw new IllegalArgumentException(String.format("Failed to parse OCR result '%s'",
                        value.getoCRResult()));
            }
        }
        LOGGER.debug(String.format("Setting OCR result '%s' as date '%s'",
                value.getoCRResult(),
                date));
        comp.getModel().setValue(date); //correctly updates the displayed value
    }
}