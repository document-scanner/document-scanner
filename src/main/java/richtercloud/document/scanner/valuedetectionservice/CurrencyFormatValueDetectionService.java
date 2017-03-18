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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.measure.converter.ConversionException;
import org.jscience.economics.money.Currency;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.FormatUtils;
import richtercloud.reflection.form.builder.components.money.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.money.AmountMoneyCurrencyStorageException;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetriever;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetrieverException;

/**
 * Since {@link NumberFormat}s retrieved with
 * {@link NumberFormat#getCurrencyInstance(java.util.Locale) } cannot be used
 * for formatting currency amounts (because Java has no support for it and
 * JScience has to be used) simply use the {@link NumberFormat}s  all currencies retrieved from an
 * {@link AmountMoneyCurrencyStorage} and
 * @author richter
 */
public class CurrencyFormatValueDetectionService extends AbstractFormatValueDetectionService<Amount<Money>> {
    private final static Logger LOGGER = LoggerFactory.getLogger(CurrencyFormatValueDetectionService.class);

    /**
     * The max. number of words a date can be made up from.
     */
    public final static int MAX_FORMAT_WORDS;
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
        MAX_FORMAT_WORDS = wordsLongest;
        LOGGER.debug(String.format("Max. of words in every currency format of every locale is %d", wordsLongest));
    }
    private final AmountMoneyCurrencyStorage amountMoneyCurrencyStorage;
    /**
     * A reference to a {@link AmountMoneyExchangeRateRetriever} to check for
     * support of eventually found currencies. Assume that this check doesn't
     * have a severe impact because exchange rate retrievers are only checked if
     * the exchange rate is unknown, i.e. hasn't been set by the user.
     */
    private final AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever;

    public CurrencyFormatValueDetectionService(AmountMoneyCurrencyStorage amountMoneyCurrencyStorage,
            AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever) {
        this.amountMoneyCurrencyStorage = amountMoneyCurrencyStorage;
        this.amountMoneyExchangeRateRetriever = amountMoneyExchangeRateRetriever;
    }

    @Override
    protected int getMaxWords() {
        return MAX_FORMAT_WORDS;
    }

    /**
     * Returned currencies have their exchange rate set based on the values of
     * {@code amountMoneyExchangeRateRetriever}.
     * @param inputSub
     * @param inputSplits
     * @param index
     */
    @Override
    protected List<ValueDetectionResult<Amount<Money>>> checkResult(String inputSub,
            List<String> inputSplits,
            int index) {
        List<ValueDetectionResult<Amount<Money>>> retValue = new LinkedList<>();
        for(Map.Entry<NumberFormat, Set<Locale>> currencyFormat : FormatUtils.getDisjointCurrencyFormatsEntySet()) {
            try {
                Number currencyValue = currencyFormat.getKey().parse(inputSub);
                Currency currency = this.amountMoneyCurrencyStorage.translate(currencyFormat.getKey().getCurrency());
                if(currency == null) {
                    //Currency is not supported by JScience and plainly
                    //creating it with Currency code (passed to constructor)
                    //gets the application in a state where a missing
                    //exchange rate which cannot be retrieved has to be
                    //handled manually; currencies should only be created
                    //manually in dialog
                    continue;
                }
                if(!amountMoneyExchangeRateRetriever.getSupportedCurrencies().contains(currency)) {
                    LOGGER.debug(String.format("skipping eventual currency '%s' which isn't supported by exchange rate retriever", currency.getCode()));
                    continue;
                }
                try {
                    currency.getExchangeRate();
                }catch(ConversionException ex) {
                    this.amountMoneyExchangeRateRetriever.retrieveExchangeRate(currency);
                }
                ValueDetectionResult<Amount<Money>> valueDetectionResult = new ValueDetectionResult<>(inputSub,
                        Amount.<Money>valueOf(currencyValue.doubleValue(), currency)
                );
                //not sufficient to check whether result
                //is already contained because the same date
                //might be retrieved from a longer and a
                //shorter substring of a substring
                retValue.add(valueDetectionResult);
                //don't break, but add all date formats as
                //result for the user to select
            }catch(ParseException ex) {
                //skip to next format
            } catch (AmountMoneyCurrencyStorageException | AmountMoneyExchangeRateRetrieverException ex) {
                throw new RuntimeException(ex);
            }
        }
        getListeners().stream().forEach((listener) -> {
            listener.onUpdate(new ValueDetectionServiceUpdateEvent<>(new LinkedList<>(retValue),
                    inputSplits.size(),
                    index));
        });
        return retValue;
    }
}
