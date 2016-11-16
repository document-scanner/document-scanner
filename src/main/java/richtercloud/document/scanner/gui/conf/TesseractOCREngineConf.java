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

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import richtercloud.document.scanner.ocr.BinaryNotFoundException;
import richtercloud.document.scanner.ocr.OCREngineConfInfo;
import richtercloud.document.scanner.ocr.ProcessOCREngine;
import richtercloud.document.scanner.ocr.TesseractOCREngine;

/**
 *
 * @author richter
 */
@OCREngineConfInfo(name = "Tesseract OCR")
public class TesseractOCREngineConf implements Serializable, OCREngineConf<TesseractOCREngine> {
    private static final long serialVersionUID = 1L;
    private final static List<String> SELECTED_LANGUAGES_DEFAULT = Collections.unmodifiableList(new LinkedList<>(Arrays.asList("deu")));
    /**
     * The {@link OCREngine} which this configuration manages (initialized
     * lazily).
     */
    private TesseractOCREngine oCREngine;
    private List<String> selectedLanguages = new LinkedList<>(SELECTED_LANGUAGES_DEFAULT);
    /**
     * the {@code tesseract} binary
     */
    private String tesseract = TesseractOCREngine.TESSERACT_DEFAULT;

    public TesseractOCREngineConf() {
    }

    protected TesseractOCREngineConf(String tesseract,
            List<String> selectedLanguages,
            TesseractOCREngine oCREngine) {
        this.tesseract = tesseract;
        this.selectedLanguages = selectedLanguages;
        this.oCREngine = oCREngine;
    }

    /**
     * Cloning constructor of {@code TesseractOCREngineConf}.
     * @param conf the {@link TesseractOCREngineConf} to clone
     */
    public TesseractOCREngineConf(TesseractOCREngineConf conf) {
        this(conf.getTesseract(),
                conf.getSelectedLanguages(),
                conf.getOCREngine());
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
        if(oCREngine == null) {
            oCREngine = new TesseractOCREngine(new LinkedList<>(SELECTED_LANGUAGES_DEFAULT));
        }
        return oCREngine;
    }

    /**
     * The {@code tesseract} binary which is used in this configuration.
     * @return
     */
    public String getTesseract() {
        return this.tesseract;
    }

    public void setTesseract(String tesseract) {
        this.tesseract = tesseract;
    }

    /**
     *
     * @return
     * @throws IllegalStateException if {@code tesseract} binary invoked with
     * {@code --list-langs} returns a code {@code != 0}
     */
    public List<String> getAvailableLanguages() throws IllegalStateException, IOException, InterruptedException {
        ProcessBuilder tesseractProcessBuilder = new ProcessBuilder(this.getTesseract(), "--list-langs");
        Process tesseractProcess = tesseractProcessBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE).start();
        int tesseractProcessReturnCode = tesseractProcess.waitFor();
        String tesseractProcessStdout = IOUtils.toString(tesseractProcess.getInputStream());
        String tesseractProcessStderr = IOUtils.toString(tesseractProcess.getErrorStream());
        if(tesseractProcessReturnCode != 0) {
            throw new IllegalStateException(String.format("The tesseract process '%s' unexpectedly returned with non-zero return code %d and output '%s' (stdout) and '%s' (stderr).", this.getTesseract(), tesseractProcessReturnCode, tesseractProcessStdout, tesseractProcessStderr));
        }
        //tesseract --list-langs prints to stderr, reported as https://bugs.launchpad.net/ubuntu/+source/tesseract/+bug/1481015
        List<String> langs = new LinkedList<>();
        for(String lang : tesseractProcessStderr.split("\n")) {
            if(!lang.startsWith("List of available languages")) {
                langs.add(lang);
            }
        }
        Collections.sort(langs, String.CASE_INSENSITIVE_ORDER);
        return langs;
    }

    public void validate() throws BinaryNotFoundException, IllegalStateException {
        ProcessOCREngine.checkBinaryAvailableExceptions(this.getTesseract());
        try {
            getAvailableLanguages();
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
