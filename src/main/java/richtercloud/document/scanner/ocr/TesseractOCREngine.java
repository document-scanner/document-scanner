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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link OCREngine} which uses tesseract in inter-process communication
 * because no Java-bindings exist.
 *
 * Prior to processing the image is passed through {@code unpaper} which is the
 * idea of {@code pdfsandwich} which sadly doesn't provide text output (only
 * PDF).
 *
 * @author richter
 */
public class TesseractOCREngine extends ProcessOCREngine {
    /**
     * the default name of the tesseract binary
     */
    public final static String TESSERACT_DEFAULT = "tesseract";
    private static final Logger LOGGER = LoggerFactory.getLogger(TesseractOCREngine.class);

    private final Lock lock = new ReentrantLock();
    private List<String> languages;

    public TesseractOCREngine(List<String> languages) {
        this(TESSERACT_DEFAULT, languages);
    }

    public TesseractOCREngine(String tesseractCmd, List<String> languages) {
        super(tesseractCmd);
        if(languages == null || languages.isEmpty()) {
            throw new IllegalArgumentException("languages mustn't be null or empty");
        }
        this.languages = languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public List<String> getLanguages() {
        return Collections.unmodifiableList(this.languages);
    }

    /**
     * Don't invoke {@code recognizeImage} from multiple threads.
     *
     * @param image
     * @throws IllegalArgumentException if {@code image} is {@code null}
     * @throws IllegalStateException if another recognition is currently running
     * @return {@code null} if the recognition has been canceled using {@link #cancelRecognizeImage() } or the recognition process crashed or the recognition result otherwise
     */
    @Override
    public String recognizeImage0(BufferedImage image) throws IllegalStateException {
        if(!lock.tryLock()) {
            throw new IllegalStateException("This tesseract OCR engine is already used from another thread.");
        }
        try {
            Iterator<String> languagesItr = this.languages.iterator();
            String lanuguageString = languagesItr.next();
            while(languagesItr.hasNext()) {
                lanuguageString += "+"+languagesItr.next();
            }
            ProcessBuilder tesseractProcessBuilder = new ProcessBuilder(this.getBinary(), "-l", lanuguageString, "stdin", "stdout")
                    .redirectOutput(ProcessBuilder.Redirect.PIPE);
            Process tesseractProcess = tesseractProcessBuilder.start();
            setBinaryProcess(tesseractProcess);
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
        }finally {
            lock.unlock();
        }
    }
}
