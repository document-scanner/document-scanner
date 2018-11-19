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
package de.richtercloud.document.scanner.valuedetectionservice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.message.handler.ExceptionMessage;
import richtercloud.message.handler.IssueHandler;

/**
 * Allows to execute a set of {@link ValueDetectionService}s and track their
 * common, i.e. average, progress.
 *
 * @author richter
 * @param <T> the type of {@link ValueDetectionResult} to enforce
 */
/*
internal implementation notes:
- This class was a ValueDetectionService before called
DelegatingValueDetectionService which allowed to reuse the progress listener
interface, but caused trouble when ValueDetectionService.supportsField was
introduced because it callers might want to know which value detection service
detected the value and this requires unintuitive changes to the interface.
*/
public class DefaultValueDetectionServiceExecutor<T> implements ValueDetectionServiceExecutor<T> {
    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultValueDetectionServiceExecutor.class);
    private final Set<ValueDetectionService<T>> valueDetectionServices;
    /**
     * Used for mapping {@code wordCount} and {@code wordNumber} properties of
     * {@link ValueDetectionServiceUpdateEvent}.
     */
    private final Map<ValueDetectionService<?>, Integer> progressWordCountMap = new HashMap<>();
    private final Map<ValueDetectionService<?>, Integer> progressWordNumberMap = new HashMap<>();
    private final List<ValueDetectionResult<T>> progressResults = new LinkedList<>();
    private final Map<ValueDetectionService<?>, Boolean> progressFinishedMap = new HashMap<>();
    private final IssueHandler issueHandler;
    private final Set<ValueDetectionServiceExecutorListener<T>> listeners = new HashSet<>();

    public DefaultValueDetectionServiceExecutor(Set<ValueDetectionService<T>> valueDetectionServices,
            IssueHandler issueHandler) {
        this.valueDetectionServices = valueDetectionServices;
        this.issueHandler = issueHandler;
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
                    listeners.forEach(listener -> {
                        listener.onUpdate(new ValueDetectionServiceUpdateEvent<>(progressResults,
                                wordCount,
                                wordNumber));
                    });
                }

                @Override
                public void onFinished() {
                    progressFinishedMap.put(valueDetectionService, true);
                    for(ValueDetectionService<?> valueDetectionService : DefaultValueDetectionServiceExecutor.this.valueDetectionServices) {
                        if(!progressFinishedMap.containsKey(valueDetectionService)
                            //check whether key is contained is sufficient
                            //because only true is ever put in the map
                        ) {
                            return;
                        }
                    }
                    listeners.forEach(listener -> {
                        listener.onFinished();
                    });
                }
            });
        });
    }

    @Override
    public void addListener(ValueDetectionServiceExecutorListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ValueDetectionServiceExecutorListener<T> listener) {
        listeners.remove(listener);
    }

    protected Set<ValueDetectionServiceExecutorListener<T>> getListeners() {
        return listeners;
    }

    @Override
    public Map<ValueDetectionService<T>, List<ValueDetectionResult<T>>> execute(final String input,
            String languageIdentifier) throws ResultFetchingException {
        progressWordCountMap.clear();
        progressWordNumberMap.clear();
        progressFinishedMap.clear();
        progressResults.clear();
        final Map<ValueDetectionService<T>, List<ValueDetectionResult<T>>> retValue = new HashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for(final ValueDetectionService<T> valueDetectionService : valueDetectionServices) {
            Callable<Void> runnable = () -> {
                List<ValueDetectionResult<T>> serviceResults = valueDetectionService.fetchResults(input,
                        languageIdentifier);
                //not necessary to prevent values to get added to retValue because
                //retValue after cancelation isn't specified
                synchronized(retValue) {
                    retValue.put(valueDetectionService,
                            serviceResults);
                }
                return null;
            };
            executorService.submit(runnable);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ex) {
            issueHandler.handleUnexpectedException(new ExceptionMessage(ex));
            throw new ResultFetchingException(ex);
        }
        return retValue;
    }

    @Override
    public void cancelExecute() {
        for(ValueDetectionService<?> valueDetectionService : valueDetectionServices) {
            valueDetectionService.cancelFetch();
        }
    }
}
