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
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.apache.commons.collections4.OrderedMap;
import richtercloud.document.scanner.ifaces.ImageWrapper;
import richtercloud.document.scanner.ifaces.OCREngine;
import richtercloud.document.scanner.ifaces.OCREngineConf;
import richtercloud.document.scanner.ifaces.OCREngineProgressEvent;
import richtercloud.document.scanner.ifaces.OCREngineProgressListener;
import richtercloud.document.scanner.ifaces.OCREngineRecognitionException;

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
- expressing cancelation by returning null from thread workers avoids a boolean
flag in ParallelOCREngine
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

    private String recognizeTasks(List<FutureTask<String>> tasks) throws OCREngineRecognitionException {
        StringBuilder retValueBuilder = new StringBuilder(1000);
        //check in loop whether cache can be used, otherwise enqueue started
        //SwingWorkers; after loop wait for SwingWorkers until queue is
        //empty and append to retValueBuilder (if cache has been used
        //(partially) queue will be empty)
        Queue<FutureTask<String>> threadQueue = new LinkedList<>();
            //the Queue preserves the order to workers
        ExecutorService executor = Executors.newCachedThreadPool();
        for(FutureTask<String> task : tasks) {
            executor.submit(task);
            threadQueue.add(task);
        }
        executor.shutdown();
        int i=0;
        boolean aborted = false;
        while(!threadQueue.isEmpty()) {
            FutureTask<String> threadQueueHead = threadQueue.poll();
            String oCRResult;
            try {
                oCRResult = threadQueueHead.get();
                if(oCRResult == null) {
                    aborted = true;
                    //still need to wait for all tasks to finish because it's up
                    //to implementations to finished them properly
                }
            } catch (InterruptedException | ExecutionException ex) {
                throw new OCREngineRecognitionException(ex);
            }
            if(!aborted) {
                retValueBuilder.append(oCRResult);
                for(OCREngineProgressListener progressListener: progressListeners) {
                    progressListener.onProgressUpdate(new OCREngineProgressEvent(oCRResult, i/tasks.size()));
                }
                i += 1;
            }
        }
        if(aborted) {
            return null;
        }
        String retValue = retValueBuilder.toString();
        return retValue;
    }

    @Override
    public String recognizeImageStreams(OrderedMap<ImageWrapper, InputStream> imageStreams) throws OCREngineRecognitionException {
        List<FutureTask<String>> tasks = new LinkedList<>();
        for(Map.Entry<ImageWrapper, InputStream> imageStream : imageStreams.entrySet()) {
            FutureTask<String> task = new FutureTask<>(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String oCRResult = recognizeImageStream(imageStream.getKey(),
                            imageStream.getValue());
                    return oCRResult;
                }
            });
            tasks.add(task);
        }
        String retValue = recognizeTasks(tasks);
        return retValue;
    }

    @Override
    public String recognizeImages(List<BufferedImage> images) throws OCREngineRecognitionException {
        List<FutureTask<String>> tasks = new LinkedList<>();
        for(BufferedImage image : images) {
            FutureTask<String> task = new FutureTask<>(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String oCRResult = recognizeImage(image);
                    return oCRResult;
                }
            });
            tasks.add(task);
        }
        String retValue = recognizeTasks(tasks);
        return retValue;
    }

    protected abstract String recognizeImage(BufferedImage image) throws OCREngineRecognitionException;

    protected abstract String recognizeImageStream(ImageWrapper image, InputStream imageStream) throws OCREngineRecognitionException;

    @Override
    public void addProgressListener(OCREngineProgressListener progressListener) {
        this.progressListeners.add(progressListener);
    }

    @Override
    public void removeProgressListener(OCREngineProgressListener progressListener) {
        this.progressListeners.remove(progressListener);
    }
}
