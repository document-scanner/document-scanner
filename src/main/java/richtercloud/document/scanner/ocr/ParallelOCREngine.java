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
package richtercloud.document.scanner.ocr;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import richtercloud.document.scanner.ifaces.OCREngine;
import richtercloud.document.scanner.ifaces.OCREngineConf;
import richtercloud.document.scanner.ifaces.OCREngineProgressEvent;
import richtercloud.document.scanner.ifaces.OCREngineProgressListener;

/**
 *
 * @author richter
 */
/*
internal implementation notes:
- it's impossible to handle parallelization in multiple parallel tasks (e.g.
FutureTasks), keep caching on a per-image level (caching results for a list of
images seems pointless) and avoiding unnecessary creation of task instances
when retrieving cached results -> don't care about creation of task instances.
*/
public abstract class ParallelOCREngine<C extends OCREngineConf> implements OCREngine<C> {
    private final Set<OCREngineProgressListener> progressListeners = new HashSet<>();
    private final C oCREngineConf;

    public ParallelOCREngine(C oCREngineConf) {
        this.oCREngineConf = oCREngineConf;
    }

    @Override
    public C getoCREngineConf() {
        return oCREngineConf;
    }

    @Override
    public String recognizeImages(List<BufferedImage> images) {
        StringBuilder retValueBuilder = new StringBuilder(1000);
        //check in loop whether cache can be used, otherwise enqueue started
        //SwingWorkers; after loop wait for SwingWorkers until queue is
        //empty and append to retValueBuilder (if cache has been used
        //(partially) queue will be empty)
        Queue<Pair<BufferedImage, FutureTask<String>>> threadQueue = new LinkedList<>();
        ExecutorService executor = Executors.newCachedThreadPool();
        for(BufferedImage image : images) {
            FutureTask<String> worker = new FutureTask<>(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String oCRResult = recognizeImage(image);
                    return oCRResult;
                }
            });
            executor.execute(worker);
            threadQueue.add(new ImmutablePair<>(image, worker));
        }
        executor.shutdown();
        int i=0;
        while(!threadQueue.isEmpty()) {
            Pair<BufferedImage, FutureTask<String>> threadQueueHead = threadQueue.poll();
            String oCRResult;
            try {
                oCRResult = threadQueueHead.getValue().get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
            retValueBuilder.append(oCRResult);
            for(OCREngineProgressListener progressListener: progressListeners) {
                progressListener.onProgressUpdate(new OCREngineProgressEvent(oCRResult, i/images.size()));
            }
            i += 1;
        }
        String retValue = retValueBuilder.toString();
        return retValue;
    }

    protected abstract String recognizeImage(BufferedImage image);

    @Override
    public void addProgressListener(OCREngineProgressListener progressListener) {
        this.progressListeners.add(progressListener);
    }

    @Override
    public void removeProgressListener(OCREngineProgressListener progressListener) {
        this.progressListeners.remove(progressListener);
    }
}
