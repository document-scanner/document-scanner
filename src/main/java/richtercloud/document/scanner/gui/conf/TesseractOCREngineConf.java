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
package richtercloud.document.scanner.gui.conf;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import richtercloud.document.scanner.ocr.OCREngineConfInfo;
import richtercloud.document.scanner.ocr.TesseractOCREngine;

/**
 *
 * @author richter
 */
@OCREngineConfInfo(name = "Tesseract OCR")
public class TesseractOCREngineConf implements Serializable, OCREngineConf<TesseractOCREngine> {
    private static final long serialVersionUID = 1L;
    private static TesseractOCREngine instance;
    private final static List<String> SELECTED_LANGUAGES_DEFAULT = Collections.unmodifiableList(new LinkedList<>(Arrays.asList("deu")));
    private List<String> selectedLanguages = SELECTED_LANGUAGES_DEFAULT;
    /**
     * the {@code tesseract} binary
     */
    private String tesseract = TesseractOCREngine.TESSERACT_DEFAULT;

    public TesseractOCREngineConf() {
    }

    /**
     * @return the selectedLanguages
     */
    public List<String> getSelectedLanguages() {
        return Collections.unmodifiableList(this.selectedLanguages);
    }

    /**
     * @param selectedLanguages the selectedLanguages to set
     */
    public void setSelectedLanguages(List<String> selectedLanguages) {
        this.selectedLanguages = selectedLanguages;
    }

    @Override
    public TesseractOCREngine getOCREngine() {
        if(instance == null) {
            instance = new TesseractOCREngine(this.selectedLanguages);
        }
        return instance;
    }

    public String getTesseract() {
        return this.tesseract;
    }

    public void setTesseract(String tesseract) {
        this.tesseract = tesseract;
    }
}
