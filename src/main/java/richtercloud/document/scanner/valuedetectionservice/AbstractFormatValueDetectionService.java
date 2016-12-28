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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link AutoOCRValueDetectionService} which simply splits input at
 * whitespace, and iterates over a set of {@link DateFormat}s and assumes that
 * only a quite limited number of words {@code n} (e.g. 10) can make up a date.
 * This limits the complexity to {@code #words in input * n}. {@code n} is
 * calculated by formatting a {@link Date} in all available {@link DateFormat}s
 * for all available {@link Locale}s.
 *
 * During recognition attempt words are joined with simple spaces as whitespace
 * between them. The implementation assumes that that doesn't make any
 * difference to other whitespace in any date format that exists.
 *
 * Performance currently is horrible.
 *
 * Multiple invokations of {@link #fetchResults(java.lang.String) } cannot be
 * started from multiple threads, but it's possible to {@link #cancelFetch() }
 * from another thread than the fetch has been started.
 *
 * @author richter
 */
public abstract class AbstractFormatValueDetectionService<T> extends AbstractValueDetectionService<T> {
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractFormatValueDetectionService.class);

    protected abstract int getMaxWords();

    /**
     * Check whether it's possible to create one or more
     * {@link ValueDetectionResult} from {@code inputSub}.
     * @param inputSub
     * @param inputSplits
     * @param i
     * @return a list of created {@link ValueDetectionResult}s or
     * {@code null} if no result could be created
     */
    /*
    internal implementation notes:
    - allowing return value of null avoids creation of a lot of empty lists
    which waste resources
    */
    protected abstract List<ValueDetectionResult<T>> checkResult(String inputSub,
            List<String> inputSplits,
            int i);

    /**
     * Might return different {@link ValueDetectionResult}s with
     * different {@link Date} for the same substring of {@code input}.
     *
     * @param input
     * @return
     */
    @Override
    public LinkedHashSet<ValueDetectionResult<T>> fetchResults0(String input) {
        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() //2 threads per processor cause only 50 % CPU usage, 1 per CPU reaches 80-90 %
                );
            //Executors.newCachedThreadPool() causes OutOfMemeoryException
        final LinkedHashSet<ValueDetectionResult<T>> retValues = new LinkedHashSet<>();
        final List<String> inputSplits = new ArrayList<>(Arrays.asList(input.split("[\\s]+")));
        InputSplitHandler inputSplitHandler = new InputSplitHandler() {
            @Override
            protected void handle0(List<String> inputSplitsSubs,
                    final List<String> inputSplits,
                    final int index) {
                final String inputSub = String.join(" ", inputSplitsSubs);
                Runnable thread = new Runnable() {
                    @Override
                    public void run() {
                        if(isCanceled()) {
                            //not necessary to use canceled in synchronized
                            //block because it doesn't matter if one thread more
                            //or less is started
                            LOGGER.info(String.format("skipping work on input substring '%s' because operation has been canceled", inputSub));
                            return;
                        }
                        LOGGER.trace(String.format("working on input substring '%s'", inputSub));
                        List<ValueDetectionResult<T>> results = checkResult(inputSub, inputSplits, index);
                        if(results != null) {
                            synchronized(retValues) {
                                retValues.addAll(results);
                            }
                        }
                    }
                };
                executor.submit(thread);
            }

            @Override
            protected int getMaxWords() {
                return AbstractFormatValueDetectionService.this.getMaxWords();
            }
        };
        inputSplitHandler.handle(inputSplits);
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        return retValues;
    }
}