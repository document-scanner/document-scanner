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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.LinkedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.ifaces.ImageWrapper;
import richtercloud.document.scanner.ifaces.OCREngine;
import richtercloud.document.scanner.ifaces.OCREngineProgressEvent;
import richtercloud.document.scanner.ifaces.OCREngineProgressListener;
import richtercloud.document.scanner.ifaces.OCREngineRecognitionException;
import richtercloud.document.scanner.ifaces.OCRSelectPanel;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanel;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanelFetcher;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanelFetcherProgressEvent;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanelFetcherProgressListener;

/**
 * This could be in {@link OCRSelectPanelPanel} as well, but has been once moved
 * out of the class (for reusage which then didn't apply), so move it back in
 * whenever it makes sense or improves understanding.
 * @author richter
 */
public class DefaultOCRSelectPanelPanelFetcher implements OCRSelectPanelPanelFetcher {
    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultOCRSelectPanelPanelFetcher.class);
    private final OCRSelectPanelPanel oCRSelectPanelPanel;
    private final Set<OCRSelectPanelPanelFetcherProgressListener> progressListeners = new HashSet<>();
    private final DocumentScannerConf documentScannerConf;
    private final OCREngine oCREngine;

    public DefaultOCRSelectPanelPanelFetcher(OCRSelectPanelPanel oCRSelectPanelPanel,
            OCREngine oCREngine,
            DocumentScannerConf documentScannerConf) {
        this.oCRSelectPanelPanel = oCRSelectPanelPanel;
        this.oCREngine = oCREngine;
        this.documentScannerConf = documentScannerConf;
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
    public String fetch() throws OCREngineRecognitionException {
        int i=0;
        List<OCRSelectPanel> imagePanels = oCRSelectPanelPanel.getoCRSelectPanels();
        OrderedMap<ImageWrapper, InputStream> imageStreams = new LinkedMap<>();
        for (final OCRSelectPanel imagePanel : imagePanels) {
            try {
                InputStream imageStream = imagePanel.getImage().getOriginalImageStream();
                imageStreams.put(imagePanel.getImage(),
                        imageStream);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        oCREngine.addProgressListener(new OCREngineProgressListener() {
            @Override
            public void onProgressUpdate(OCREngineProgressEvent progressEvent) {
                for(OCRSelectPanelPanelFetcherProgressListener progressListener : progressListeners) {
                    progressListener.onProgressUpdate(new OCRSelectPanelPanelFetcherProgressEvent(progressEvent.getNewValue(), progressEvent.getProgress()));
                }
            }
        });
        String oCRResult;
        oCRResult = oCREngine.recognizeImageStreams(imageStreams);
            //need to operate on original image in order to get
            //acceptable OCR results
        if(oCRResult == null) {
            //indicates that the OCREngine.recognizeImage has been aborted
            return null;
        }
        return oCRResult;
    }

    @Override
    public void cancelFetch() {
        this.oCREngine.cancelRecognizeImages();
    }
}
