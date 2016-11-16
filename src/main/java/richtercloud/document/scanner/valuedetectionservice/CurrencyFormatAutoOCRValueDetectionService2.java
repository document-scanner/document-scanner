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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
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
 * An {@link AutoOCRValueDetectionService} for currencies in form of a
 * {@link Amount} which splits input on whitespace, searches for currency
 * symbols, stores
 * {@link CurrencyFormatAutoOCRValueDetectionService#MAX_FORMAT_WORDS} tokens
 * behind and after the occurance of a currency symbol and tries parsing with
 * all supported currency {@link NumberFormat}.
 * @author richter
 */
public class CurrencyFormatAutoOCRValueDetectionService2 extends AbstractAutoOCRValueDetectionService<Amount<Money>> {
    private final static Logger LOGGER = LoggerFactory.getLogger(CurrencyFormatAutoOCRValueDetectionService2.class);

    private final AmountMoneyCurrencyStorage amountMoneyCurrencyStorage;
    /**
     * A reference to a {@link AmountMoneyExchangeRateRetriever} to check for
     * support of eventually found currencies. Assume that this check doesn't
     * have a severe impact because exchange rate retrievers are only checked if
     * the exchange rate is unknown, i.e. hasn't been set by the user.
     */
    private final AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever;

    public CurrencyFormatAutoOCRValueDetectionService2(AmountMoneyCurrencyStorage amountMoneyCurrencyStorage,
            AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever) {
        this.amountMoneyCurrencyStorage = amountMoneyCurrencyStorage;
        this.amountMoneyExchangeRateRetriever = amountMoneyExchangeRateRetriever;
    }

    @Override
    protected LinkedHashSet<AutoOCRValueDetectionResult<Amount<Money>>> fetchResults0(String input) {
        final LinkedHashSet<AutoOCRValueDetectionResult<Amount<Money>>> retValue = new LinkedHashSet<>();
        final List<String> tokens = new LinkedList<>();
        StringTokenizer tokenizer = new StringTokenizer(input);
        while(tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            tokens.add(token);
        }
        for(final Map.Entry<NumberFormat, Set<Locale>> currencyFormat : FormatUtils.getDisjointCurrencyFormats().entrySet()) {
            final String currencyCode = currencyFormat.getKey().getCurrency().getCurrencyCode();
            final String currencySymbol = currencyFormat.getKey().getCurrency().getSymbol();
            //checking tokens.indexOf(currencyCode) isn't sufficient because
            //the currency code doesn't have to be separated by whitespace and
            //represent a token alone
            ListIterator<String> tokensItr = tokens.listIterator();
            while(tokensItr.hasNext()) {
                final String token = tokensItr.next();
                int index = tokensItr.nextIndex();
                if(token.contains(currencySymbol) || token.contains(currencyCode)) {
                    //take a sublist of n positions before and n positions after
                    //the occurance (with
                    //n=CurrencyFormatAutoOCRValueDetectionService.MAX_FORMAT_WORDS)
                    //and shift it to the left. This also ensures that longest
                    //matches are detected first
                    List<String> subList = tokens.subList(index-CurrencyFormatAutoOCRValueDetectionService.MAX_FORMAT_WORDS-1,
                            index+CurrencyFormatAutoOCRValueDetectionService.MAX_FORMAT_WORDS+1);
                        //add 1 since it's possible that the value and the
                        //currency symbol are separated with a space
                    InputSplitHandler inputSplitHandler = new InputSplitHandler() {
                        @Override
                        protected void handle0(List<String> inputSplitsSubs, List<String> inputSplits, int index) {
                            String subListString = String.join(" ", inputSplitsSubs);
                            try  {
                                //Since currency values seem to be parsed only
                                //if there's a space between the currency symbol
                                //and the value
                                Number currencyValue;
                                try {
                                    currencyValue = currencyFormat.getKey().parse(subListString);
                                }catch(ParseException ex) {
                                    try {
                                        String subListStringSpace;
                                        if(token.contains(currencySymbol)) {
                                            subListStringSpace = subListString.replace(currencySymbol, " "+currencySymbol+" ");
                                        }else {
                                            subListStringSpace = subListString.replace(currencyCode, " "+currencyCode+" ");
                                        }
                                        currencyValue = currencyFormat.getKey().parse(subListStringSpace);
                                    }catch(ParseException ex1) {
                                        return;
                                    }
                                }
                                Currency currency = CurrencyFormatAutoOCRValueDetectionService2.this.amountMoneyCurrencyStorage.translate(currencyFormat.getKey().getCurrency());
                                if(currency == null) {
                                    //Currency is not supported by JScience and plainly
                                    //creating it with Currency code (passed to constructor)
                                    //gets the application in a state where a missing
                                    //exchange rate which cannot be retrieved has to be
                                    //handled manually; currencies should only be created
                                    //manually in dialog
                                    return;
                                }
                                if(!amountMoneyExchangeRateRetriever.getSupportedCurrencies().contains(currency)) {
                                    LOGGER.debug(String.format("skipping eventual currency '%s' which isn't supported by exchange rate retriever", currency.getCode()));
                                    return;
                                }
                                try {
                                    currency.getExchangeRate();
                                }catch(ConversionException ex) {
                                    CurrencyFormatAutoOCRValueDetectionService2.this.amountMoneyExchangeRateRetriever.retrieveExchangeRate(currency);
                                }
                                Amount<Money> value = Amount.<Money>valueOf(currencyValue.doubleValue(), currency);
                                AutoOCRValueDetectionResult<Amount<Money>> autoOCRValueDetectionResult = new AutoOCRValueDetectionResult<>(subListString,
                                        value
                                );
                                //not sufficient to check whether result
                                //is already contained because the same date
                                //might be retrieved from a longer and a
                                //shorter substring of a substring
                                retValue.add(autoOCRValueDetectionResult);
                                for(AutoOCRValueDetectionServiceUpdateListener<Amount<Money>> listener : getListeners()) {
                                    listener.onUpdate(new AutoOCRValueDetectionServiceUpdateEvent<>(new LinkedList<>(retValue),
                                            tokens.size(),
                                            index //lastIndex is closest to current progress
                                    ));
                                }
                            } catch (AmountMoneyCurrencyStorageException | AmountMoneyExchangeRateRetrieverException ex) {
                                throw new RuntimeException(ex);
                            }
                        }

                        @Override
                        protected int getMaxWords() {
                            return CurrencyFormatAutoOCRValueDetectionService.MAX_FORMAT_WORDS;
                        }
                    };
                    inputSplitHandler.handle(subList);
                }
            }
        }
        return retValue;
    }
}
