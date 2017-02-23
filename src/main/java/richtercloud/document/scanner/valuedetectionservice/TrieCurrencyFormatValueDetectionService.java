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

import com.googlecode.concurrenttrees.common.Iterables;
import com.googlecode.concurrenttrees.radix.node.NodeFactory;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharSequenceNodeFactory;
import com.googlecode.concurrenttrees.suffix.ConcurrentSuffixTree;
import com.googlecode.concurrenttrees.suffix.SuffixTree;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
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
 * {@link Amount} which splits input on whitespace into tokens and searches for
 * currency symbols. On occurance of currency symbols tried to parse all
 * combinations {@link CurrencyFormatValueDetectionService#MAX_FORMAT_WORDS}
 * tokens before and after the occurance of a currency symbol with all supported
 * currency {@link NumberFormat}s. Uses a trie (suffix tree) for the search.
 *
 * @author richter
 */
public class TrieCurrencyFormatValueDetectionService extends AbstractValueDetectionService<Amount<Money>> {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrieCurrencyFormatValueDetectionService.class);

    private final AmountMoneyCurrencyStorage amountMoneyCurrencyStorage;
    /**
     * A reference to a {@link AmountMoneyExchangeRateRetriever} to check for
     * support of eventually found currencies. Assume that this check doesn't
     * have a severe impact because exchange rate retrievers are only checked if
     * the exchange rate is unknown, i.e. hasn't been set by the user.
     */
    private final AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever;

    public TrieCurrencyFormatValueDetectionService(AmountMoneyCurrencyStorage amountMoneyCurrencyStorage,
            AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever) {
        this.amountMoneyCurrencyStorage = amountMoneyCurrencyStorage;
        this.amountMoneyExchangeRateRetriever = amountMoneyExchangeRateRetriever;
    }

    @Override
    protected LinkedHashSet<ValueDetectionResult<Amount<Money>>> fetchResults0(String input) {
        final LinkedHashSet<ValueDetectionResult<Amount<Money>>> retValue = new LinkedHashSet<>();
        final List<String> tokens = new LinkedList<>();
        StringTokenizer tokenizer = new StringTokenizer(input);
        NodeFactory nodeFactory = new DefaultCharSequenceNodeFactory();
        SuffixTree<Integer> suffixTree = new ConcurrentSuffixTree<>(nodeFactory);
        int index = 0;
        while(tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            tokens.add(token);
            suffixTree.put(token, index);
            index += 1;
        }
        for(final Map.Entry<NumberFormat, Set<Locale>> currencyFormat : FormatUtils.getDisjointCurrencyFormatsEntySet()) {
            //completely unclear why ConcurrentModificationException occurs when
            //reading unmodifiable
            //FormatUtils.getDisjointCurrencyFormats.entrySet -> copy as a
            //workaround
            final String currencyCode = currencyFormat.getKey().getCurrency().getCurrencyCode();
            final String currencySymbol = currencyFormat.getKey().getCurrency().getSymbol();
            //- checking tokens.indexOf(currencyCode) isn't sufficient because
            //the currency code doesn't have to be separated by whitespace and
            //represent a token alone
            //- search for currencyCode and currencySymbol and add the results
            //since they will be disjoint and need the same treatment
            Iterable<CharSequence> tokensContainingCurrencyCode = suffixTree.getKeysContaining(currencyCode);
            Iterable<CharSequence> tokensContainingCurrencySymbol = suffixTree.getKeysContaining(currencySymbol);
            Set<CharSequence> relevantTokens = new HashSet<>();
            relevantTokens.addAll(Iterables.toSet(tokensContainingCurrencyCode));
            relevantTokens.addAll(Iterables.toSet(tokensContainingCurrencySymbol));
            for(CharSequence token : relevantTokens) {
                String tokenString = token.toString();
                Integer tokenIndex = suffixTree.getValueForExactKey(token);
                //take a sublist of n positions before and n positions after
                //the occurance (with
                //n=CurrencyFormatValueDetectionService.MAX_FORMAT_WORDS)
                //and shift it to the left. This also ensures that longest
                //matches are detected first
                int subListFromIndex = Math.max(0, tokenIndex-CurrencyFormatValueDetectionService.MAX_FORMAT_WORDS);
                int subListToIndex = Math.min(tokens.size()-1, tokenIndex+CurrencyFormatValueDetectionService.MAX_FORMAT_WORDS);
                List<String> subList = tokens.subList(subListFromIndex,
                        subListToIndex);
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
                                LOGGER.trace(String.format("attempting to parse substring '%s'", subListString));
                                currencyValue = currencyFormat.getKey().parse(subListString);
                            }catch(ParseException ex) {
                                try {
                                    String subListStringSpace;
                                    if(tokenString.contains(currencySymbol)) {
                                        subListStringSpace = subListString.replace(currencySymbol, " "+currencySymbol+" ");
                                    }else {
                                        subListStringSpace = subListString.replace(currencyCode, " "+currencyCode+" ");
                                    }
                                    LOGGER.trace(String.format("attempting to parse substring '%s'", subListString));
                                    currencyValue = currencyFormat.getKey().parse(subListStringSpace);
                                }catch(ParseException ex1) {
                                    return;
                                }
                            }
                            Currency currency = TrieCurrencyFormatValueDetectionService.this.amountMoneyCurrencyStorage.translate(currencyFormat.getKey().getCurrency());
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
                                TrieCurrencyFormatValueDetectionService.this.amountMoneyExchangeRateRetriever.retrieveExchangeRate(currency);
                            }
                            Amount<Money> value = Amount.<Money>valueOf(currencyValue.doubleValue(), currency);
                            ValueDetectionResult<Amount<Money>> autoOCRValueDetectionResult = new ValueDetectionResult<>(subListString,
                                    value
                            );
                            //not sufficient to check whether result
                            //is already contained because the same date
                            //might be retrieved from a longer and a
                            //shorter substring of a substring
                            retValue.add(autoOCRValueDetectionResult);
                            for(ValueDetectionServiceUpdateListener<Amount<Money>> listener : getListeners()) {
                                listener.onUpdate(new ValueDetectionServiceUpdateEvent<>(new LinkedList<>(retValue),
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
                        return CurrencyFormatValueDetectionService.MAX_FORMAT_WORDS;
                    }
                };
                inputSplitHandler.handle(subList);
            }
        }
        return retValue;
    }
}
