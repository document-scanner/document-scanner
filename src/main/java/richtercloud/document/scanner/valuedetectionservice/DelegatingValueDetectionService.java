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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the {@link ValueDetectionService} interface around multiple instances
 * which allows to track a common progress of multiple services.
 *
 * @author richter
 * @param <T> the type of {@link ValueDetectionResult} to enforce
 */
public class DelegatingValueDetectionService<T> extends AbstractValueDetectionService<T> {
    private final static Logger LOGGER = LoggerFactory.getLogger(DelegatingValueDetectionService.class);
    private final Set<ValueDetectionService<T>> valueDetectionServices;
    /**
     * Used for mapping {@code wordCount} and {@code wordNumber} properties of
     * {@link ValueDetectionServiceUpdateEvent}.
     */
    private final Map<ValueDetectionService<?>, Integer> progressWordCountMap = new HashMap<>();
    private final Map<ValueDetectionService<?>, Integer> progressWordNumberMap = new HashMap<>();
    private final List<ValueDetectionResult<T>> progressResults = new LinkedList<>();
    private final Map<ValueDetectionService<?>, Boolean> progressFinishedMap = new HashMap<>();

    public DelegatingValueDetectionService(Set<ValueDetectionService<T>> valueDetectionServices) {
        this.valueDetectionServices = valueDetectionServices;
        //simply do like the progress would be the quotient of the sum of all
        //word count and the sum of all word numbers
        valueDetectionServices.forEach(valueDetectionService -> {
            valueDetectionService.addListener(new ValueDetectionServiceListener<T>() {
                @Override
                public void onUpdate(ValueDetectionServiceUpdateEvent<T> event) {
                    progressWordCountMap.put(valueDetectionService, event.getWordCount());
                    progressWordNumberMap.put(valueDetectionService, event.getWordNumber());
                    progressResults.addAll(event.getIntermediateResult());
                    final int wordCount = progressWordCountMap.values().stream().mapToInt(value -> {return value;}).sum();
                    final int wordNumber = progressWordNumberMap.values().stream().mapToInt(value -> {return value;}).sum();
                    if(wordNumber > wordCount) {
                        LOGGER.error(String.format("wordNumber > wordCount for value detection service %s", valueDetectionService));
                        return;
                    }
                    getListeners().forEach(listener -> {
                        listener.onUpdate(new ValueDetectionServiceUpdateEvent<>(progressResults,
                                wordCount,
                                wordNumber));
                    });
                }

                @Override
                public void onFinished() {
                    progressFinishedMap.put(valueDetectionService, true);
                    for(ValueDetectionService<?> valueDetectionService : DelegatingValueDetectionService.this.valueDetectionServices) {
                        if(!progressFinishedMap.containsKey(valueDetectionService)
                            //check whether key is contained is sufficient
                            //because only true is ever put in the map
                        ) {
                            return;
                        }
                    }
                    getListeners().forEach(listener -> {
                        listener.onFinished();
                    });
                }
            });
        });
    }

    @Override
    public LinkedHashSet<ValueDetectionResult<T>> fetchResults0(final String input) {
        progressWordCountMap.clear();
        progressWordNumberMap.clear();
        progressFinishedMap.clear();
        progressResults.clear();
        final LinkedHashSet<ValueDetectionResult<T>> retValue = new LinkedHashSet<>();
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for(final ValueDetectionService<T> valueDetectionService : valueDetectionServices) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    List<ValueDetectionResult<T>> serviceResults = valueDetectionService.fetchResults(input);
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
