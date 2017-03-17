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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author richter
 */
public class DelegatingValueDetectionService extends AbstractValueDetectionService {
    private final Set<ValueDetectionService<?>> valueDetectionServices;

    public DelegatingValueDetectionService(Set<ValueDetectionService<?>> valueDetectionServices) {
        this.valueDetectionServices = valueDetectionServices;
    }

    @Override
    public LinkedHashSet<ValueDetectionResult<?>> fetchResults0(final String input) {
        final LinkedHashSet<ValueDetectionResult<?>> retValue = new LinkedHashSet<>();
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for(final ValueDetectionService valueDetectionService : valueDetectionServices) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    List<ValueDetectionResult<?>> serviceResults = valueDetectionService.fetchResults(input);
                    //not necessary to prevent values to get added to retValue because
                    //retValue after cancelation isn't specified
                    synchronized(retValue) {
                        retValue.addAll(serviceResults);
                    }
                }
            };
            executorService.submit(runnable);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        return retValue;
    }

    @Override
    public void cancelFetch() {
        super.cancelFetch();
        for(ValueDetectionService<?> valueDetectionService : valueDetectionServices) {
            valueDetectionService.cancelFetch();
        }
    }
}
