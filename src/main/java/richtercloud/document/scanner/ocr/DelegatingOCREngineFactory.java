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

import richtercloud.document.scanner.ifaces.OCREngine;
import richtercloud.document.scanner.ifaces.OCREngineConf;

/**
 *
 * @author richter
 */
public class DelegatingOCREngineFactory implements OCREngineFactory<OCREngine, OCREngineConf> {
    private final static TesseractOCREngineFactory TESSERACT_OCRENGINE_FACTORY = new TesseractOCREngineFactory();

    /**
     * Supports {@link TesseractOCREngineConf} only.
     * @param oCREngineConf
     * @throws IllegalArgumentException is {@code oCREngineConf} isn't supported
     * @return the created OCR engine
     */
    @Override
    public OCREngine create(OCREngineConf oCREngineConf) {
        if(oCREngineConf instanceof TesseractOCREngineConf) {
            return TESSERACT_OCRENGINE_FACTORY.create((TesseractOCREngineConf) oCREngineConf);
        }
        throw new IllegalArgumentException(String.format("OCREngineConf of type '%s' isn't supported", oCREngineConf.getClass()));
    }
}
