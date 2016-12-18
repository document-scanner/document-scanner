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
import java.util.List;
import java.util.Map;
import net.sourceforge.javaocr.ocrPlugins.mseOCR.OCRScanner;
import net.sourceforge.javaocr.scanner.PixelImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.ifaces.ImageWrapper;
import richtercloud.document.scanner.ifaces.OCREngine;
import richtercloud.document.scanner.ifaces.OCREngineConf;
import richtercloud.document.scanner.ifaces.OCREngineProgressListener;

/**
 * A {@link OCREngine} implementation which uses javaocr - which needs a lot of
 * training data and initially does nothing - which is what this implementation
 * consequently does currently.
 * @author richter
 */
public class JavaocrOCREngine implements OCREngine<OCREngineConf> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaocrOCREngine.class);
    private final OCRScanner scanner = new OCRScanner();

    @Override
    public String recognizeImages(List<BufferedImage> images) {
        StringBuilder retValueBuilder = new StringBuilder(1000);
        for(BufferedImage image : images) {
            PixelImage pixelImage = new PixelImage(image);
            pixelImage.toGrayScale(true);
            pixelImage.filter();
            String text = this.scanner.scan(image, 0, 0, 0, 0, null);
            retValueBuilder.append(text);
        }
        String retValue = retValueBuilder.toString();
        LOGGER.debug("OCR result: {}", retValue);
        return retValue;
    }

    @Override
    public void cancelRecognizeImages() {
        throw new UnsupportedOperationException("Not supported yet. Figure out how to recognize in parts (e.g. rows or x*y pixel areas) to allow cancelation");
    }

    @Override
    public void addProgressListener(OCREngineProgressListener progressListener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeProgressListener(OCREngineProgressListener progressListener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OCREngineConf getoCREngineConf() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String recognizeImageStreams(Map<ImageWrapper, InputStream> imageStreams) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
