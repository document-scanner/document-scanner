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

import richtercloud.document.scanner.ifaces.OCREngineConf;
import java.io.IOException;
import javax.swing.JOptionPane;
import richtercloud.message.handler.Message;
import richtercloud.message.handler.MessageHandler;

/**
 *
 * @author richter
 */
public class DefaultOCREngineConfPanelFactory implements OCREngineConfPanelFactory {
    private final MessageHandler messageHandler;

    public DefaultOCREngineConfPanelFactory(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public OCREngineConfPanel<?> create(OCREngineConf engineConf) {
        if(engineConf instanceof TesseractOCREngineConf) {
            try {
                TesseractOCREngineConfPanel retValue = new TesseractOCREngineConfPanel((TesseractOCREngineConf) engineConf, messageHandler);
                return retValue;
            } catch (IOException | InterruptedException | BinaryNotFoundException ex) {
                messageHandler.handle(new Message(ex, JOptionPane.WARNING_MESSAGE));
            }
        }
        throw new IllegalArgumentException(String.format("OCR engine configuration of type %s not supported", engineConf.getClass()));
    }
}
