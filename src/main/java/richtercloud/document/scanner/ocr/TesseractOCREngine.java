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
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.TesseractNotFoundException;

/**
 * A {@link OCREngine} which uses tesseract in inter-process communication
 * because no Java-bindings exist.
 * @author richter
 */
public class TesseractOCREngine implements OCREngine {
    /**
     * the default name of the tesseract binary
     */
    public final static String TESSERACT_DEFAULT = "tesseract";
    private static final Logger LOGGER = LoggerFactory.getLogger(TesseractOCREngine.class);

    /**
     * checks whether the specified {@code tesseract} command is available and
     * accessible/executable
     * @param tesseract the command to check
     * @return the {@link IOException} which is presumed to have cause the
     * absence of the tesseact binary {@code tesseract}
     * @throws InterruptedException if an {@code InterruptedException} occurs during {@link Runtime#exec(java.lang.String) }
     */
    /*
    internal implementation notes:
    - returns the exception which is presumed to indicate the absense of the
    binary. This allows to examine the exception by callers and eventually to
    distinguish
    IOExceptions which are proof of absense of the binary and unrelated
    IOExceptions which might be thrown and need to be handled by caller
     */
    public static IOException checkTesseractAvailable(String tesseract) throws InterruptedException {
        try {
            new ProcessBuilder(tesseract).start().waitFor();
            return null;
        }catch(IOException ex) {
            return ex;
        }
    }

    public static void checkTesseractAvailableExceptions(String tesseract) throws TesseractNotFoundException {
        IOException exception;
        try {
            exception = checkTesseractAvailable(tesseract);
        } catch (InterruptedException ex) {
            throw new RuntimeException(String.format("An unexpected exception occured during the search of the tesseract binary '%s' because the process has been interrupted (see nested exception for details)", tesseract), ex);
        }
        if(exception != null) {
            throw new TesseractNotFoundException(tesseract, exception);
        }
    }
    private String tesseract = TESSERACT_DEFAULT;
    private List<String> languages;
    private Process tesseractProcess;

    public TesseractOCREngine(List<String> languages) {
        if(languages == null || languages.isEmpty()) {
            throw new IllegalArgumentException("languages mustn't be null or empty");
        }
        this.languages = languages;
    }

    public TesseractOCREngine(String tesseractCmd, List<String> languages) {
        this(languages);
        this.tesseract = tesseractCmd;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public List<String> getLanguages() {
        return Collections.unmodifiableList(this.languages);
    }

    /**
     * Don't invoke {@code recognizeImage} from multiple threads (result would
     * be undefined).
     *
     * @param image
     * @throws IllegalArgumentException if {@code image} is {@code null}
     * @return {@code null} if the recognition has been canceled using {@link #cancelRecognizeImage() } or the recognition process crashed or the recognition result otherwise
     */
    @Override
    public String recognizeImage(BufferedImage image) {
        if(image == null) {
            throw new IllegalArgumentException("image mustn't be null");
        }
        try {
            checkTesseractAvailableExceptions(this.tesseract);
        }catch(TesseractNotFoundException ex) {
            throw new RuntimeException("tesseract not available (see nested exception for details)", ex);
        }
        LOGGER.debug("tesseract binary '{}' found and executable", this.tesseract);
        Iterator<String> languagesItr = this.languages.iterator();
        String lanuguageString = languagesItr.next();
        while(languagesItr.hasNext()) {
            lanuguageString += "+"+languagesItr.next();
        }
        ProcessBuilder tesseractProcessBuilder = new ProcessBuilder(this.tesseract, "-l", lanuguageString, "stdin", "stdout")
                .redirectOutput(ProcessBuilder.Redirect.PIPE);
        try {
            tesseractProcess = tesseractProcessBuilder.start();
            ImageIO.write(image, "png", tesseractProcess.getOutputStream());
            tesseractProcess.getOutputStream().flush();
            tesseractProcess.getOutputStream().close(); //sending EOF not an option because it's not documented what is expected (sending -1 once or twice doesn't have any effect, also with flush)
            int tesseractProcessExitValue = tesseractProcess.waitFor();
            if(tesseractProcessExitValue != 0) {
                //tesseractProcess.destroy might cause IOException, but
                //termination with exit value != 0 might occur as well
                return null;
            }
            StringWriter tesseractResultWriter = new StringWriter();
            IOUtils.copy(tesseractProcess.getInputStream(), tesseractResultWriter);
            String tesseractResult = tesseractResultWriter.toString();
            LOGGER.debug("OCR result: {}", tesseractResult);
            return tesseractResult;
        } catch(InterruptedException ex) {
            //InterruptedException is an IOException
            return null; //might at one point be thrown due to Process.destroy
                    //cancelation
        } catch (IOException ex) {
            if(ex.getMessage().equals("Stream closed")) {
                return null; //result of Process.destroy
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void cancelRecognizeImage() {
        if(this.tesseractProcess != null) {
            this.tesseractProcess.destroy(); // there's no way of cleanly shutting
                //down a process in Java process API<ref>http://stackoverflow.com/questions/6339861/how-to-pass-sigint-to-a-process-created-in-java</ref>
                //(something less severe than SIGTERM). It shouldn't matter
                //because tesseract propably won't do more than opening some
                // process pipes
        }
    }


}
