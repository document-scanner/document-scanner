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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math4.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.conf.OCREngineConf;
import richtercloud.document.scanner.ifaces.OCRSelectPanel;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanel;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanelFetcher;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanelFetcherProgressEvent;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanelFetcherProgressListener;
import richtercloud.document.scanner.ocr.OCREngine;
import richtercloud.document.scanner.ocr.OCREngineFactory;

/**
 * This could be in {@link OCRSelectPanelPanel} as well, but has been once moved
 * out of the class (for reusage which then didn't apply), so move it back in
 * whenever it makes sense or improves understanding.
 * @author richter
 */
public class DefaultOCRSelectPanelPanelFetcher implements OCRSelectPanelPanelFetcher {
    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultOCRSelectPanelPanelFetcher.class);
    private final List<Double> stringBufferLengths = new ArrayList<>();
    private final OCRSelectPanelPanel oCRSelectPanelPanel;
    private final Set<OCRSelectPanelPanelFetcherProgressListener> progressListeners = new HashSet<>();
    private boolean cancelRequested = false;
    /**
     * Since {@link OCRSelectPanel} has an immutable {@code image} property
     * it can be used well as cache map key.
     */
    private final Map<OCRSelectPanel, String> fetchCache = new HashMap<>();
    /**
     * Record all used {@link OCREngine}s in order to be able to cancel if
     * {@link #cancelFetch() } is invoked.
     */
    /*
    internal implementation notes:
    - is a Queue in order to be able to cancel as fast as possible
    */
    private Queue<OCREngine> usedEngines = new LinkedList<>();
    /**
     * The factory to create one or multiple {@link OCREngine}s for linear
     * or parallel fetching.
     */
    private final OCREngineFactory oCREngineFactory;
    private final OCREngineConf oCREngineConf;

    public DefaultOCRSelectPanelPanelFetcher(OCRSelectPanelPanel oCRSelectPanelPanel,
            OCREngineFactory oCREngineFactory,
            OCREngineConf oCREngineConf) {
        this.oCRSelectPanelPanel = oCRSelectPanelPanel;
        this.oCREngineFactory = oCREngineFactory;
        this.oCREngineConf = oCREngineConf;
    }

    @Override
    public void addProgressListener(OCRSelectPanelPanelFetcherProgressListener progressListener) {
        this.progressListeners.add(progressListener);
    }

    @Override
    public void removeProgressListener(OCRSelectPanelPanelFetcherProgressListener progressListener) {
        this.progressListeners.remove(progressListener);
    }

    @Override
    public String fetch() {
        //estimate the initial StringBuilder size based on the median
        //of all prior OCR results (string length) (and 1000 initially)
        int stringBufferLengh;
        cancelRequested = false;
        if (this.stringBufferLengths.isEmpty()) {
            stringBufferLengh = 1_000;
        } else {
            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(this.stringBufferLengths.toArray(new Double[this.stringBufferLengths.size()]));
            stringBufferLengh = ((int) descriptiveStatistics.getPercentile(.5)) + 1;
        }
        this.stringBufferLengths.add((double) stringBufferLengh);
        StringBuilder retValueBuilder = new StringBuilder(stringBufferLengh);
        int i=0;
        List<OCRSelectPanel> imagePanels = oCRSelectPanelPanel.getoCRSelectPanels();
        Queue<Pair<OCRSelectPanel, FutureTask<String>>> threadQueue = new LinkedList<>();
        Executor executor = Executors.newCachedThreadPool();
        //check in loop whether cache can be used, otherwise enqueue started
        //SwingWorkers; after loop wait for SwingWorkers until queue is
        //empty and append to retValueBuilder (if cache has been used
        //(partially) queue will be empty)
        for (final OCRSelectPanel imagePanel : imagePanels) {
            if(cancelRequested) {
                //no need to notify progress listener
                break;
            }
            String oCRResult = fetchCache.get(imagePanel);
            if(oCRResult != null) {
                LOGGER.info(String.format("using cached OCR result for image %d of current OCR select component", i));
                retValueBuilder.append(oCRResult);
                for(OCRSelectPanelPanelFetcherProgressListener progressListener: progressListeners) {
                    progressListener.onProgressUpdate(new OCRSelectPanelPanelFetcherProgressEvent(oCRResult, i/imagePanels.size()));
                }
                i += 1;
            }else {
                FutureTask<String> worker = new FutureTask<>(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        OCREngine oCREngine = oCREngineFactory.create(oCREngineConf);
                        usedEngines.add(oCREngine);
                        String oCRResult = oCREngine.recognizeImage(imagePanel.getImage());
                        if(oCRResult == null) {
                            //indicates that the OCREngine.recognizeImage has been aborted
                            if(cancelRequested) {
                                //no need to notify progress listener
                                return null;
                            }
                        }
                        return oCRResult;
                    }
                });
                executor.execute(worker);
                threadQueue.add(new ImmutablePair<>(imagePanel, worker));
            }
        }
        while(!threadQueue.isEmpty()) {
            Pair<OCRSelectPanel, FutureTask<String>> threadQueueHead = threadQueue.poll();
            String oCRResult;
            try {
                oCRResult = threadQueueHead.getValue().get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
            retValueBuilder.append(oCRResult);
            fetchCache.put(threadQueueHead.getKey(), oCRResult);
            for(OCRSelectPanelPanelFetcherProgressListener progressListener: progressListeners) {
                progressListener.onProgressUpdate(new OCRSelectPanelPanelFetcherProgressEvent(oCRResult, i/imagePanels.size()));
            }
            i += 1;
        }
        String retValue = retValueBuilder.toString();
        return retValue;
    }

    @Override
    public void cancelFetch() {
        this.cancelRequested = true;
        while(!usedEngines.isEmpty()) {
            OCREngine usedEngine = usedEngines.poll();
            usedEngine.cancelRecognizeImage();
        }
    }
}
