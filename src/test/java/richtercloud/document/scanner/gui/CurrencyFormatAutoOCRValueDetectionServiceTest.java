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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import richtercloud.reflection.form.builder.components.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.MemoryAmountMoneyCurrencyStorage;

/**
 *
 * @author richter
 */
public class CurrencyFormatAutoOCRValueDetectionServiceTest {

    /**
     * Test of getMaxWords method, of class CurrencyFormatAutoOCRValueDetectionService.
     */
    @Test
    public void testGetMaxWords() {
        System.out.println("getMaxWords");
        AmountMoneyCurrencyStorage amountMoneyCurrencyStorage = mock(AmountMoneyCurrencyStorage.class);
        CurrencyFormatAutoOCRValueDetectionService instance = new CurrencyFormatAutoOCRValueDetectionService(amountMoneyCurrencyStorage);
        int expResult = 2; //the only thing that makes sense with currency
            //formats
        int result = instance.getMaxWords();
        assertEquals(expResult, result);
    }

    /**
     * Test of checkResult method, of class CurrencyFormatAutoOCRValueDetectionService.
     */
    @Test
    public void testCheckResult() {
        String inputSub = "1.2 €";
        List<AutoOCRValueDetectionResult<?>> retValues = new LinkedList<>();
        List<String> inputSplits = new LinkedList<>(Arrays.asList(inputSub));
        int i = 0;
        AmountMoneyCurrencyStorage amountMoneyCurrencyStorage = new MemoryAmountMoneyCurrencyStorage();
        CurrencyFormatAutoOCRValueDetectionService instance = new CurrencyFormatAutoOCRValueDetectionService(amountMoneyCurrencyStorage);
        instance.checkResult(inputSub, retValues, inputSplits, i);
        assertTrue(retValues.contains(new AutoOCRValueDetectionResult<>(inputSub, 1.2)));
    }
}
