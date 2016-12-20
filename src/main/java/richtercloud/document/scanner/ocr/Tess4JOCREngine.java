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
import org.apache.commons.collections4.OrderedMap;
import richtercloud.document.scanner.ifaces.ImageWrapper;
import richtercloud.document.scanner.ifaces.OCREngine;
import richtercloud.document.scanner.ifaces.OCREngineConf;
import richtercloud.document.scanner.ifaces.OCREngineProgressListener;


/**
 *
 * @author richter
 */
public class Tess4JOCREngine implements OCREngine<OCREngineConf> {

    @Override
    public String recognizeImages(List<BufferedImage> image) {
        throw new UnsupportedOperationException("Not supported yet. Figure out maven dependencies");

//        Tesseract instance = Tesseract.getInstance();  // JNA Interface Mapping
//        // Tesseract1 instance = new Tesseract1(); // JNA Direct Mapping
//
//        try {
//            String result = instance.doOCR(imageFile);
//            System.out.println(result);
//        } catch (TesseractException e) {
//            System.err.println(e.getMessage());
//        }
    }

    @Override
    public void cancelRecognizeImages() {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public String recognizeImageStreams(OrderedMap<ImageWrapper, InputStream> imageStreams) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
