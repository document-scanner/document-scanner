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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import org.jscience.economics.money.Currency;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.reflection.form.builder.components.money.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.money.AmountMoneyCurrencyStorageException;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetriever;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetrieverException;
import richtercloud.reflection.form.builder.components.money.MemoryAmountMoneyCurrencyStorage;

/**
 *
 * @author richter
 */
public class TrieCurrencyFormatValueDetectionServiceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrieCurrencyFormatValueDetectionServiceTest.class);

    /**
     * Test of fetchResults0 method, of class CurrencyFormatValueDetectionService2.
     */
    @Test
    public void testFetchResults0() throws AmountMoneyCurrencyStorageException, AmountMoneyExchangeRateRetrieverException {
        //test with currency symbol (without space)
        String input = "jfklds jklfd jklds jkldfs fjkdls jkdflss fdjskl f jklfds fkd 5€ jkfdls fkldfsjklf  fdjklf sjklfds f jkldslskd ";
        AmountMoneyCurrencyStorage amountMoneyCurrencyStorage = new MemoryAmountMoneyCurrencyStorage();
        amountMoneyCurrencyStorage.saveCurrency(Currency.EUR);
        AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever = mock(AmountMoneyExchangeRateRetriever.class);
        when(amountMoneyExchangeRateRetriever.getSupportedCurrencies()).thenReturn(new HashSet<>(Arrays.asList(Currency.EUR)));
        TrieCurrencyFormatValueDetectionService instance = new TrieCurrencyFormatValueDetectionService(amountMoneyCurrencyStorage,
                amountMoneyExchangeRateRetriever);
        LOGGER.debug(String.format("running with currency symbol without space with input '%s'", input));
        LinkedHashSet<ValueDetectionResult<Amount<Money>>> result = instance.fetchResults0(input);
        LOGGER.debug(String.format("result: %s", result));
        assertTrue(result.contains(new ValueDetectionResult<>("5€",
                Amount.valueOf(5.0d, Currency.EUR)
        )));

        //test with currency symbol (with space)
        input = "jfklds jklfd jklds jkldfs fjkdls jkdflss fdjskl f jklfds fkd 5 € jkfdls fkldfsjklf  fdjklf sjklfds f jkldslskd ";
        LOGGER.debug(String.format("running with currency symbol with space with input '%s'", input));
        result = instance.fetchResults0(input);
        assertTrue(result.contains(new ValueDetectionResult<>("5 €",
                Amount.valueOf(5.0d, Currency.EUR)
        )));

        //test with currency name (without space)
        input = "jfklds jklfd jklds jkldfs fjkdls jkdflss fdjskl f jklfds fkd 5EUR jkfdls fkldfsjklf  fdjklf sjklfds f jkldslskd ";
        LOGGER.debug(String.format("running with currency name without space with input '%s'", input));
        result = instance.fetchResults0(input);
        assertTrue(result.contains(new ValueDetectionResult<>("5EUR",
                Amount.valueOf(5.0d, Currency.EUR)
        )));

        input = "jfklds jklfd jklds jkldfs fjkdls jkdflss fdjskl f jklfds fkd 5 EUR jkfdls fkldfsjklf  fdjklf sjklfds f jkldslskd ";
        LOGGER.debug(String.format("running with currency name with space with input '%s'", input));
        result = instance.fetchResults0(input);
        assertTrue(result.contains(new ValueDetectionResult<>("5 EUR",
                Amount.valueOf(5.0d, Currency.EUR)
        )));

        //test currency symbol before value (without space)
        input = "jfklds jklfd jklds jkldfs fjkdls jkdflss fdjskl f jklfds fkd EUR5 jkfdls fkldfsjklf  fdjklf sjklfds f jkldslskd ";
        LOGGER.debug(String.format("running with currency name before value without space with input '%s'", input));
        result = instance.fetchResults0(input);
        assertTrue(result.contains(new ValueDetectionResult<>("EUR5",
                Amount.valueOf(5.0d, Currency.EUR)
        )));

        input = "jfklds jklfd jklds jkldfs fjkdls jkdflss fdjskl f jklfds fkd EUR 5 jkfdls fkldfsjklf  fdjklf sjklfds f jkldslskd ";
        LOGGER.debug(String.format("running with currency name before value with space with input '%s'", input));
        result = instance.fetchResults0(input);
        assertTrue(result.contains(new ValueDetectionResult<>("EUR 5",
                Amount.valueOf(5.0d, Currency.EUR)
        )));

        //test two occurances (one with and one without space assuming that not
        //all combinations need to be run again)
        input = "jfklds jklfd jklds jkldfs fjkdls jkdflss 4€ fdjskl f jklfds fkd EUR 5 jkfdls fkldfsjklf  fdjklf sjklfds f jkldslskd ";
        LOGGER.debug(String.format("running with currency name before value with space with input '%s'", input));
        result = instance.fetchResults0(input);
        assertTrue(result.contains(new ValueDetectionResult<>("EUR 5",
                Amount.valueOf(5.0d, Currency.EUR)
        )));
        assertTrue(result.contains(new ValueDetectionResult<>("4€",
                Amount.valueOf(4.0d, Currency.EUR))));
    }
}
