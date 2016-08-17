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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
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
public abstract class AbstractFormatAutoOCRValueDetectionService extends AbstractAutoOCRValueDetectionService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DateFormatAutoOCRValueDetectionService.class);

    protected abstract int getMaxWords();

    protected abstract void checkResult(String inputSub,
            List<AutoOCRValueDetectionResult<?>> retValues,
            List<String> inputSplits,
            int i);

    /**
     * Might return different {@link AutoOCRValueDetectionResult}s with
     * different {@link Date} for the same substring of {@code input}.
     *
     * @param input
     * @return
     */
    @Override
    public List<AutoOCRValueDetectionResult<?>> fetchResults0(String input) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2 //2 threads per processor seems sane
                );
            //Executors.newCachedThreadPool() causes OutOfMemeoryException
        final List<AutoOCRValueDetectionResult<?>> retValues = new LinkedList<>();
        final List<String> inputSplits = new ArrayList<>(Arrays.asList(input.split("[\\s]+")));
        for(int i=0; i<inputSplits.size() //there's no sense in substracting
                        //MAX_DATE_WORDS because dates in
                        //[inputSplits.size()-MAX_DATE_WORDS;inputSplits.size()]
                        //wouldn't be recognized
                ; i++) {
            final int i0 = i;
            for(int j=Math.min(inputSplits.size(), i+getMaxWords());
                    j>i; //there's no sense in using j>=i because that creates empty lists
                    j--) {
                //start with longer input, then decrease
                List<String> inputSplitsSubs = inputSplits.subList(i, j);
                StringBuilder inputSubBuilder = new StringBuilder(1024);
                for(String inputSplitsSub : inputSplitsSubs) {
                    inputSubBuilder.append(inputSplitsSub);
                    inputSubBuilder.append(" ");
                }
                final String inputSub = inputSubBuilder.toString();
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
                        LOGGER.debug(String.format("working on input substring '%s'", inputSub));
                        checkResult(inputSub, retValues, inputSplits, i0);
                    }
                };
                executor.submit(thread);
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1L, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
//        while(!threadQueue.isEmpty()) {
//            Thread threadHead = threadQueue.poll();
//            try {
//                threadHead.join();
//            } catch (InterruptedException ex) {
//                throw new RuntimeException(ex);
//            }
//        }
        return retValues;
    }
}
