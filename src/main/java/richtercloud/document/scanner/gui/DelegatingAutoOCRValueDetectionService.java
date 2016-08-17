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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import richtercloud.reflection.form.builder.components.AmountMoneyCurrencyStorage;

/**
 *
 * @author richter
 */
public class DelegatingAutoOCRValueDetectionService extends AbstractAutoOCRValueDetectionService {
    private final DateFormatAutoOCRValueDetectionService dateFormatAutoOCRValueDetectionService = new DateFormatAutoOCRValueDetectionService();
    private final CurrencyFormatAutoOCRValueDetectionService currencyFormatAutoOCRValueDetectionService;
    private final Set<AutoOCRValueDetectionService> autoOCRValueDetectionServices;

    public DelegatingAutoOCRValueDetectionService(AmountMoneyCurrencyStorage amountMoneyCurrencyStorage) {
        this.currencyFormatAutoOCRValueDetectionService = new CurrencyFormatAutoOCRValueDetectionService(amountMoneyCurrencyStorage);
        this.autoOCRValueDetectionServices = new HashSet<AutoOCRValueDetectionService>(Arrays.asList(dateFormatAutoOCRValueDetectionService, currencyFormatAutoOCRValueDetectionService));
    }

    @Override
    public List<AutoOCRValueDetectionResult<?>> fetchResults0(String input) {
        List<AutoOCRValueDetectionResult<?>> retValue = new LinkedList<>();
        for(AutoOCRValueDetectionService autoOCRValueDetectionService : autoOCRValueDetectionServices) {
            List<AutoOCRValueDetectionResult<?>> serviceResults = autoOCRValueDetectionService.fetchResults(input);
            retValue.addAll(serviceResults);
        }
        return retValue;
    }

    @Override
    public void cancelFetch() {
        for(AutoOCRValueDetectionService autoOCRValueDetectionService : autoOCRValueDetectionServices) {
            autoOCRValueDetectionService.cancelFetch();
        }
    }
}
