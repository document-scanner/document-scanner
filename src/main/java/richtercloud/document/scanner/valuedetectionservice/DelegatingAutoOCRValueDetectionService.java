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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import richtercloud.reflection.form.builder.components.money.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetriever;

/**
 *
 * @author richter
 */
public class DelegatingAutoOCRValueDetectionService extends AbstractAutoOCRValueDetectionService {
    private final CurrencyFormatAutoOCRValueDetectionService2 currencyFormatAutoOCRValueDetectionService;
    private final ContactAutoOCRValueDetectionService addressAutoOCRValueDetectionService;
    private final SUTimeAutoOCRValueDetectionService sUTimeAutoOCRValueDetectionService;
    private final Set<AutoOCRValueDetectionService> autoOCRValueDetectionServices;

    public DelegatingAutoOCRValueDetectionService(AmountMoneyCurrencyStorage amountMoneyCurrencyStorage,
            AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever) {
        this.currencyFormatAutoOCRValueDetectionService = new CurrencyFormatAutoOCRValueDetectionService2(amountMoneyCurrencyStorage,
                amountMoneyExchangeRateRetriever);
        this.addressAutoOCRValueDetectionService = new ContactAutoOCRValueDetectionService();
        this.sUTimeAutoOCRValueDetectionService = new SUTimeAutoOCRValueDetectionService();
        this.autoOCRValueDetectionServices = new HashSet<AutoOCRValueDetectionService>(Arrays.asList(sUTimeAutoOCRValueDetectionService,
                currencyFormatAutoOCRValueDetectionService,
                addressAutoOCRValueDetectionService));
    }

    @Override
    public LinkedHashSet<AutoOCRValueDetectionResult<?>> fetchResults0(final String input) {
        final LinkedHashSet<AutoOCRValueDetectionResult<?>> retValue = new LinkedHashSet<>();
        Queue<Thread> threads = new LinkedList<>();
        for(final AutoOCRValueDetectionService autoOCRValueDetectionService : autoOCRValueDetectionServices) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    List<AutoOCRValueDetectionResult<?>> serviceResults = autoOCRValueDetectionService.fetchResults(input);
                    //not necessary to prevent values to get added to retValue because
                    //retValue after cancelation isn't specified
                    synchronized(retValue) {
                        retValue.addAll(serviceResults);
                    }
                }
            };
            Thread thread = new Thread(runnable,
                    "delegating-auto-value-detection-service-thread");
            thread.start();
            threads.add(thread);
        }
        while(!threads.isEmpty()) {
            Thread threadsHead = threads.poll();
            try {
                threadsHead.join();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        return retValue;
    }

    @Override
    public void cancelFetch() {
        super.cancelFetch();
        for(AutoOCRValueDetectionService autoOCRValueDetectionService : autoOCRValueDetectionServices) {
            autoOCRValueDetectionService.cancelFetch();
        }
    }
}