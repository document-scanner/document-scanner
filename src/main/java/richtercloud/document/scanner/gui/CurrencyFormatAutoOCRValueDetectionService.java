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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.jscience.economics.money.Currency;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.reflection.form.builder.components.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.AmountMoneyCurrencyStorageException;

/**
 * Since {@link NumberFormat}s retrieved with
 * {@link NumberFormat#getCurrencyInstance(java.util.Locale) } cannot be used
 * for formatting currency amounts (because Java has no support for it and
 * JScience has to be used) simply use the {@link NumberFormat}s  all currencies retrieved from an
 * {@link AmountMoneyCurrencyStorage} and
 * @author richter
 */
public class CurrencyFormatAutoOCRValueDetectionService extends AbstractFormatAutoOCRValueDetectionService {
    private final static Logger LOGGER = LoggerFactory.getLogger(CurrencyFormatAutoOCRValueDetectionService.class);

    /**
     * The max. number of words a date can be made up from.
     */
    public final static int MAX_DATE_WORDS;
    static {
        int wordsLongest = 0;
        Set<NumberFormat> numberFormats = FormatUtils.getDisjointCurrencyFormats().keySet();
        for(NumberFormat numberFormat : numberFormats) {
            String currencyString = numberFormat.format(FormatUtils.NUMBER_FORMAT_VALUE);
            int words = currencyString.split("[\\s]+",
                    wordsLongest+1 //no need to split after longest
            ).length;
            if(words > wordsLongest) {
                wordsLongest = words;
            }
        }
        MAX_DATE_WORDS = wordsLongest;
        LOGGER.debug(String.format("Max. of words in every currency format of every locale is %d", wordsLongest));
    }
    private final AmountMoneyCurrencyStorage amountMoneyCurrencyStorage;

    public CurrencyFormatAutoOCRValueDetectionService(AmountMoneyCurrencyStorage amountMoneyCurrencyStorage) {
        this.amountMoneyCurrencyStorage = amountMoneyCurrencyStorage;
    }

    @Override
    protected int getMaxWords() {
        return MAX_DATE_WORDS;
    }

    @Override
    protected void checkResult(String inputSub, List<AutoOCRValueDetectionResult<?>> retValues, List<String> inputSplits, int i) {
        for(Map.Entry<NumberFormat, Set<Locale>> currencyFormat : FormatUtils.getDisjointCurrencyFormats().entrySet()) {
            try {
                Number currencyValue = currencyFormat.getKey().parse(inputSub);
                synchronized(retValues) {
                    Currency currency = this.amountMoneyCurrencyStorage.translate(currencyFormat.getKey().getCurrency());
                    AutoOCRValueDetectionResult<?> autoOCRValueDetectionResult = new AutoOCRValueDetectionResult<>(inputSub,
                            Amount.<Money>valueOf(currencyValue.doubleValue(), currency));
                    //not sufficient to check whether result
                    //is already contained because the same date
                    //might be retrieved from a longer and a
                    //shorter substring of a substring
                    retValues.add(autoOCRValueDetectionResult);
                    for(AutoOCRValueDetectionServiceUpdateListener listener : getListeners()) {
                        listener.onUpdate(new AutoOCRValueDetectionServiceUpdateEvent(retValues,
                                inputSplits.size(),
                                i));
                    }
                }
                //don't break, but add all date formats as
                //result for the user to select
            }catch(ParseException ex) {
                //skip to next format
            } catch (AmountMoneyCurrencyStorageException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
