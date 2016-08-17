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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author richter
 */
public abstract class ProcessOCREngine extends CachedOCREngine {
    private final static Logger LOGGER = LoggerFactory.getLogger(ProcessOCREngine.class);

    /**
     * checks whether the specified {@code tesseract} command is available and
     * accessible/executable
     * @param binary the command to check
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
    public static IOException checkBinaryAvailable(String binary) throws InterruptedException {
        try {
            new ProcessBuilder(binary).start().waitFor();
            return null;
        }catch(IOException ex) {
            return ex;
        }
    }

    public static void checkBinaryAvailableExceptions(String binary) throws BinaryNotFoundException {
        IOException exception;
        try {
            exception = checkBinaryAvailable(binary);
        } catch (InterruptedException ex) {
            throw new RuntimeException(String.format("An unexpected exception occured during the search of the binary '%s' because the process has been interrupted (see nested exception for details)", binary), ex);
        }
        if(exception != null) {
            throw new BinaryNotFoundException(binary, exception);
        }
    }

    private final String binary;
    private Process binaryProcess;

    public ProcessOCREngine(String binary) {
        this.binary = binary;
    }

    public String getBinary() {
        return binary;
    }

    public Process getBinaryProcess() {
        return binaryProcess;
    }

    public void setBinaryProcess(Process binaryProcess) {
        this.binaryProcess = binaryProcess;
    }

    @Override
    public String recognizeImage0(BufferedImage image) throws IllegalStateException {
        if(image == null) {
            throw new IllegalArgumentException("image mustn't be null");
        }
        try {
            checkBinaryAvailableExceptions(this.binary);
        }catch(BinaryNotFoundException ex) {
            throw new RuntimeException("tesseract not available (see nested exception for details)", ex);
        }
        LOGGER.debug("tesseract binary '{}' found and executable", this.binary);
        return recognizeImage1(image);
    }

    protected abstract String recognizeImage1(BufferedImage image) throws IllegalStateException;

    @Override
    public void cancelRecognizeImage() {
        if(this.binaryProcess != null) {
            this.binaryProcess.destroy(); // there's no way of cleanly shutting
                //down a process in Java process API<ref>http://stackoverflow.com/questions/6339861/how-to-pass-sigint-to-a-process-created-in-java</ref>
                //(something less severe than SIGTERM). It shouldn't matter
                //because tesseract propably won't do more than opening some
                // process pipes
        }
    }
}
