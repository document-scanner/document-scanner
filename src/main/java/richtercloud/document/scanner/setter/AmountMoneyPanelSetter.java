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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import org.jscience.economics.money.Currency;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;
import richtercloud.document.scanner.gui.ocrresult.FormatOCRResult;
import richtercloud.document.scanner.gui.ocrresult.OCRResult;
import richtercloud.reflection.form.builder.components.money.AmountMoneyPanel;

/**
 *
 * @author richter
 */
public class AmountMoneyPanelSetter implements ValueSetter<Amount<Money>, AmountMoneyPanel> {
    private final static AmountMoneyPanelSetter INSTANCE = new AmountMoneyPanelSetter();

    public static AmountMoneyPanelSetter getInstance() {
        return INSTANCE;
    }

    @Override
    public void setValue(Amount<Money> value, AmountMoneyPanel comp) {
        comp.setValue(value);
    }

    @Override
    public void setOCRResult(OCRResult value, AmountMoneyPanel comp) {
        if(value instanceof FormatOCRResult) {
            FormatOCRResult formatOCRResult = (FormatOCRResult) value;
            Number number = null;
            //don't parse with percent format because it doesn't make sense
            if(formatOCRResult.getCurrencyFormat() == null) {
                //automatic
                for(Locale locale : Locale.getAvailableLocales()) {
                    try {
                        number = NumberFormat.getCurrencyInstance(locale).parse(formatOCRResult.getoCRResult());
                        break; //first match is the chosen one
                    }catch(ParseException ex) {
                        //skip to next locale
                    }
                }
                if(number == null) {
                    throw new IllegalArgumentException("No number format of any locale succeeds to parse the OCR selection");
                }
            }else {
                try {
                    number = formatOCRResult.getCurrencyFormat().parse(formatOCRResult.getoCRResult());
                } catch (ParseException ex) {
                    try {
                        number = formatOCRResult.getNumberFormat().parse(formatOCRResult.getoCRResult());
                    }catch(ParseException ex1) {
                        throw new IllegalArgumentException(ex);
                    }
                }
            }
            Amount<Money> amountMoney = Amount.valueOf(number.doubleValue(),
                    new Currency(formatOCRResult.getCurrencyFormat().getCurrency().getCurrencyCode()));
            comp.setValue(amountMoney);
        }else {
            //@TODO
        }
    }
}
