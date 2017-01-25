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
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Iterator;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.ifaces.OCREngineRecognitionException;

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
public class TesseractOCREngine extends ProcessOCREngine<TesseractOCREngineConf> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TesseractOCREngine.class);
    private final TesseractOCREngineConf oCREngineConf;

    public TesseractOCREngine(TesseractOCREngineConf oCREngineConf) {
        super(oCREngineConf);
        if(oCREngineConf == null) {
            throw new IllegalArgumentException("oCREngineConf mustn't be empty");
        }
        this.oCREngineConf = oCREngineConf;
        if(oCREngineConf.getSelectedLanguages() == null || oCREngineConf.getSelectedLanguages().isEmpty()) {
            throw new IllegalArgumentException("languages mustn't be null or empty");
        }
    }

    /**
     *
     * @param image
     * @throws IllegalArgumentException if {@code image} is {@code null}
     * @throws IllegalStateException if another recognition is currently running
     * @return {@code null} if the recognition has been canceled using {@link #cancelRecognizeImage() } or the recognition process crashed or the recognition result otherwise
     */
    @Override
    protected String recognizeImage1(BufferedImage image) throws IllegalStateException, OCREngineRecognitionException {
        String retValue = doRecognizeTask((tesseractProcessStdinStream) -> {
            ImageIO.write(image, "png", tesseractProcessStdinStream);
        });
        return retValue;
    }

    @Override
    protected String recognizeImageStream0(InputStream imageStream) throws OCREngineRecognitionException {
        String retValue = doRecognizeTask((tesseractProcessStdinStream) -> {
            IOUtils.copy(imageStream, tesseractProcessStdinStream);
        });
        return retValue;
    }

    private String doRecognizeTask(RecognizeTask task) throws OCREngineRecognitionException {
        try {
            Iterator<String> languagesItr = this.oCREngineConf.getSelectedLanguages().iterator();
            String lanuguageString = languagesItr.next();
            while(languagesItr.hasNext()) {
                lanuguageString += "+"+languagesItr.next();
            }
            //remember that in the Java Process API stdin is called outputStream
            //and stdout called inputStream
            ProcessBuilder tesseractProcessBuilder = new ProcessBuilder(this.getoCREngineConf().getBinary(), "-l", lanuguageString, "stdin", "stdout")
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectInput(ProcessBuilder.Redirect.PIPE);
            Process tesseractProcess = tesseractProcessBuilder.start();
            getBinaryProcesses().add(tesseractProcess);
            try (OutputStream tesseractProcessStdinStream = new BufferedOutputStream(tesseractProcess.getOutputStream())) {
                task.run(tesseractProcessStdinStream);
                tesseractProcessStdinStream.flush();
                //sending EOF not an option because it's not documented what is expected (sending -1 once or twice doesn't have any effect, also with flush)
            }
            int tesseractProcessExitValue = tesseractProcess.waitFor();
            if(tesseractProcessExitValue != 0) {
                //tesseractProcess.destroy might cause IOException, but
                //termination with exit value != 0 might occur as well
                StringWriter tesseractStderrWriter = new StringWriter();
                IOUtils.copy(tesseractProcess.getErrorStream(), tesseractStderrWriter);
                String tesseractProcessStderr = tesseractStderrWriter.toString();
                String message = String.format("tesseract process '%s' failed with "
                        + "returncode %d and output '%s'",
                        this.getoCREngineConf().getBinary(),
                        tesseractProcessExitValue,
                        tesseractProcessStderr);
                throw new OCREngineRecognitionException(message);
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
            throw new OCREngineRecognitionException(ex);
        }
    }

    @FunctionalInterface
    private interface RecognizeTask {
        void run(OutputStream tesseractProcessStdinStream) throws IOException;
    }
}
