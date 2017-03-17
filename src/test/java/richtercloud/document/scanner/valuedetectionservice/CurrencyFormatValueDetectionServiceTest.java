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
import java.util.LinkedList;
import java.util.List;
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
public class CurrencyFormatValueDetectionServiceTest {

    /**
     * Test of getMaxWords method, of class CurrencyFormatValueDetectionService.
     */
    @Test
    public void testGetMaxWords() {
        System.out.println("getMaxWords");
        AmountMoneyCurrencyStorage amountMoneyCurrencyStorage = mock(AmountMoneyCurrencyStorage.class);
        AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever = mock(AmountMoneyExchangeRateRetriever.class);
        CurrencyFormatValueDetectionService instance = new CurrencyFormatValueDetectionService(amountMoneyCurrencyStorage,
                amountMoneyExchangeRateRetriever);
        int expResult = 2; //the only thing that makes sense with currency
            //formats
        int result = instance.getMaxWords();
        assertEquals(expResult, result);
    }

    /**
     * Test of checkResult method, of class CurrencyFormatValueDetectionService.
     */
    @Test
    public void testCheckResult() throws AmountMoneyCurrencyStorageException, AmountMoneyExchangeRateRetrieverException {
        String inputSub = "1,2 €";
        List<String> inputSplits = new LinkedList<>(Arrays.asList(inputSub));
        int i = 0;
        AmountMoneyCurrencyStorage amountMoneyCurrencyStorage = new MemoryAmountMoneyCurrencyStorage();
        amountMoneyCurrencyStorage.saveCurrency(Currency.EUR);
        AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever = mock(AmountMoneyExchangeRateRetriever.class);
        when(amountMoneyExchangeRateRetriever.getSupportedCurrencies()).thenReturn(new HashSet<>(Arrays.asList(Currency.EUR)));
        CurrencyFormatValueDetectionService instance = new CurrencyFormatValueDetectionService(amountMoneyCurrencyStorage,
                amountMoneyExchangeRateRetriever);
        List<ValueDetectionResult<Amount<Money>>> retValues = instance.checkResult(inputSub, inputSplits, i);
        assertTrue(retValues.contains(new ValueDetectionResult<>(inputSub, Amount.valueOf(1.2d, Currency.EUR))));
    }
}
