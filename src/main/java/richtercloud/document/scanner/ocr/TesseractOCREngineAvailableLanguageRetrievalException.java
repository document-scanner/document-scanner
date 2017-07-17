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

/**
 * A checked exception in order to indicate that invoking the {@code tesseract}
 * binary with {@code --list-langs} failed for some reason which is described
 * in the cause of the exception.
 *
 * @author richter
 */
public class TesseractOCREngineAvailableLanguageRetrievalException extends Exception {
    private static final long serialVersionUID = 1L;

    public TesseractOCREngineAvailableLanguageRetrievalException(String message) {
        super(message);
    }

    public TesseractOCREngineAvailableLanguageRetrievalException(String message,
            Throwable cause) {
        super(message,
                cause);
    }

    public TesseractOCREngineAvailableLanguageRetrievalException(Throwable cause) {
        super(cause);
    }
}
