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
import richtercloud.reflection.form.builder.components.money.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.money.AmountMoneyCurrencyStorageException;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetriever;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetrieverException;
import richtercloud.reflection.form.builder.components.money.MemoryAmountMoneyCurrencyStorage;

/**
 *
 * @author richter
 */
public class CurrencyFormatAutoOCRValueDetectionService2Test {

    /**
     * Test of fetchResults0 method, of class CurrencyFormatAutoOCRValueDetectionService2.
     */
    @Test
    public void testFetchResults0() throws AmountMoneyCurrencyStorageException, AmountMoneyExchangeRateRetrieverException {
        //test with currency symbol (without space)
        String input = "jfklds jklfd jklds jkldfs fjkdls jkdflss fdjskl f jklfds fkd 5€ jkfdls fkldfsjklf  fdjklf sjklfds f jkldslskd ";
        AmountMoneyCurrencyStorage amountMoneyCurrencyStorage = new MemoryAmountMoneyCurrencyStorage();
        amountMoneyCurrencyStorage.saveCurrency(Currency.EUR);
        AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever = mock(AmountMoneyExchangeRateRetriever.class);
        when(amountMoneyExchangeRateRetriever.getSupportedCurrencies()).thenReturn(new HashSet<>(Arrays.asList(Currency.EUR)));
        CurrencyFormatAutoOCRValueDetectionService2 instance = new CurrencyFormatAutoOCRValueDetectionService2(amountMoneyCurrencyStorage,
                amountMoneyExchangeRateRetriever);
        LinkedHashSet<AutoOCRValueDetectionResult<Amount<Money>>> result = instance.fetchResults0(input);
        assertTrue(result.contains(new AutoOCRValueDetectionResult<>("5€",
                Amount.valueOf(5.0d, Currency.EUR)
        )));

        //test with currency symbol (with space)
        input = "jfklds jklfd jklds jkldfs fjkdls jkdflss fdjskl f jklfds fkd 5 € jkfdls fkldfsjklf  fdjklf sjklfds f jkldslskd ";
        result = instance.fetchResults0(input);
        assertTrue(result.contains(new AutoOCRValueDetectionResult<>("5 €",
                Amount.valueOf(5.0d, Currency.EUR)
        )));

        //test with currency name (without space)
        input = "jfklds jklfd jklds jkldfs fjkdls jkdflss fdjskl f jklfds fkd 5EUR jkfdls fkldfsjklf  fdjklf sjklfds f jkldslskd ";
        result = instance.fetchResults0(input);
        assertTrue(result.contains(new AutoOCRValueDetectionResult<>("5EUR",
                Amount.valueOf(5.0d, Currency.EUR)
        )));

        input = "jfklds jklfd jklds jkldfs fjkdls jkdflss fdjskl f jklfds fkd 5 EUR jkfdls fkldfsjklf  fdjklf sjklfds f jkldslskd ";
        result = instance.fetchResults0(input);
        assertTrue(result.contains(new AutoOCRValueDetectionResult<>("5 EUR",
                Amount.valueOf(5.0d, Currency.EUR)
        )));

        //test currency symbol before value (without space)
        input = "jfklds jklfd jklds jkldfs fjkdls jkdflss fdjskl f jklfds fkd EUR5 jkfdls fkldfsjklf  fdjklf sjklfds f jkldslskd ";
        result = instance.fetchResults0(input);
        assertTrue(result.contains(new AutoOCRValueDetectionResult<>("EUR5",
                Amount.valueOf(5.0d, Currency.EUR)
        )));

        input = "jfklds jklfd jklds jkldfs fjkdls jkdflss fdjskl f jklfds fkd EUR 5 jkfdls fkldfsjklf  fdjklf sjklfds f jkldslskd ";
        result = instance.fetchResults0(input);
        assertTrue(result.contains(new AutoOCRValueDetectionResult<>("EUR 5",
                Amount.valueOf(5.0d, Currency.EUR)
        )));
    }
}
