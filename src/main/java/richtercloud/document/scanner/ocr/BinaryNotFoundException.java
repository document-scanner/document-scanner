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

/**
 *
 * @author richter
 */
public class BinaryNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public BinaryNotFoundException(String tesseract, IOException cause) {
        super(String.format("The tesseract binary '%s' isn't available or inaccessible (see nested exception for details)", tesseract), cause);
    }

}
