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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import richtercloud.document.scanner.ifaces.OCREngineConfValidationException;

/**
 *
 * @author richter
 */
@OCREngineConfInfo(name = "Tesseract OCR")
public class TesseractOCREngineConf extends ProcessOCREngineConf {
    private static final long serialVersionUID = 1L;
    private final static List<String> SELECTED_LANGUAGES_DEFAULT = Collections.unmodifiableList(new LinkedList<>(Arrays.asList("deu")));
    /**
     * the default name of the tesseract binary
     */
    public final static String TESSERACT_DEFAULT = "tesseract";
    private List<String> selectedLanguages = new LinkedList<>(SELECTED_LANGUAGES_DEFAULT);

    public TesseractOCREngineConf() {
        this(TESSERACT_DEFAULT,
                SELECTED_LANGUAGES_DEFAULT);
    }

    protected TesseractOCREngineConf(String binary,
            List<String> selectedLanguages) {
        super(binary);
        this.selectedLanguages = selectedLanguages;
    }

    /**
     * Cloning constructor of {@code TesseractOCREngineConf}.
     * @param conf the {@link TesseractOCREngineConf} to clone
     */
    public TesseractOCREngineConf(TesseractOCREngineConf conf) {
        this(conf.getBinary(),
                conf.getSelectedLanguages());
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

    /**
     *
     * @return
     * @throws TesseractOCREngineAvailableLanguageRetrievalException if
     * {@code tesseract} binary invoked with {@code --list-langs} returns a code
     * {@code != 0} or empty output of both {@code stdout} and {@code stderr} or
     * output on both {@code stdout} and {@code stderr}
     */
    public List<String> getAvailableLanguages() throws TesseractOCREngineAvailableLanguageRetrievalException, IOException, InterruptedException {
        ProcessBuilder tesseractProcessBuilder = new ProcessBuilder(this.getBinary(), "--list-langs");
        Process tesseractProcess = tesseractProcessBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE).start();
        int tesseractProcessReturnCode = tesseractProcess.waitFor();
        String tesseractProcessStdout = IOUtils.toString(tesseractProcess.getInputStream());
        String tesseractProcessStderr = IOUtils.toString(tesseractProcess.getErrorStream());
        if(tesseractProcessReturnCode != 0) {
            throw new TesseractOCREngineAvailableLanguageRetrievalException(String.format("The tesseract process '%s' unexpectedly returned with non-zero return code %d and output '%s' (stdout) and '%s' (stderr).", this.getBinary(), tesseractProcessReturnCode, tesseractProcessStdout, tesseractProcessStderr));
        }
        //tesseract --list-langs prints to stderr, reported as https://bugs.launchpad.net/ubuntu/+source/tesseract/+bug/1481015
        List<String> langs = new LinkedList<>();
        String relevantOutput;
            //Some version print result of --list-langs to stderr, some to
            //stderr (see https://bugs.launchpad.net/ubuntu/+source/tesseract/+bug/1481015
            //for details and issue state). Since tesseract also has a useless output when invoked
            //with --version, do the following naive test
        if(tesseractProcessStdout.isEmpty() && tesseractProcessStderr.isEmpty()) {
            throw new TesseractOCREngineAvailableLanguageRetrievalException("both stdout and stderr output is empty");
        }
        if(!tesseractProcessStdout.isEmpty() && !tesseractProcessStderr.isEmpty()) {
            throw new TesseractOCREngineAvailableLanguageRetrievalException("both stdout and stderr contain output");
        }
        if(!tesseractProcessStdout.isEmpty()) {
            relevantOutput = tesseractProcessStdout;
        }else {
            relevantOutput = tesseractProcessStderr;
        }
        for(String lang : relevantOutput.split("\n")) {
            if(!lang.startsWith("List of available languages")) {
                langs.add(lang);
            }
        }
        Collections.sort(langs, String.CASE_INSENSITIVE_ORDER);
        return langs;
    }

    @Override
    public void validate() throws OCREngineConfValidationException {
        try {
            ProcessOCREngine.checkBinaryAvailableExceptions(this.getBinary());
        } catch (BinaryNotFoundException ex) {
            throw new OCREngineConfValidationException(ex);
        }
        try {
            getAvailableLanguages();
            if(getAvailableLanguages().isEmpty()) {
                throw new OCREngineConfValidationException("list of available languages mustn't be empty");
            }
        } catch (IOException | InterruptedException | IllegalStateException | TesseractOCREngineAvailableLanguageRetrievalException ex) {
            throw new OCREngineConfValidationException(String.format("retrieval of available languages of tesseract binary '%s' failed (see nested exception for details)", getBinary()), ex);
        }
        if(getSelectedLanguages().isEmpty()) {
            throw new OCREngineConfValidationException("list of selected languages mustn't be empty");
        }
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 17 * hash + Objects.hashCode(this.selectedLanguages);
        return hash;
    }

    protected boolean equalsTransitive(TesseractOCREngineConf other) {
        if(!super.equalsTransitive(other)) {
            return false;
        }
        if (!Objects.equals(this.selectedLanguages, other.selectedLanguages)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TesseractOCREngineConf other = (TesseractOCREngineConf) obj;
        return equalsTransitive(other);
    }
}
