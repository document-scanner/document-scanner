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
import net.sourceforge.javaocr.ocrPlugins.mseOCR.OCRScanner;
import net.sourceforge.javaocr.scanner.PixelImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link OCREngine} implementation which uses javaocr - which needs a lot of
 * training data and initially does nothing - which is what this implementation
 * consequently does currently.
 * @author richter
 */
public class JavaocrOCREngine implements OCREngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaocrOCREngine.class);
    private final OCRScanner scanner = new OCRScanner();

    @Override
    public String recognizeImage(BufferedImage image) {
        PixelImage pixelImage = new PixelImage(image);
        pixelImage.toGrayScale(true);
        pixelImage.filter();
        String text = this.scanner.scan(image, 0, 0, 0, 0, null);
        LOGGER.debug("OCR result: {}", text);
        return text;
    }
    
}
