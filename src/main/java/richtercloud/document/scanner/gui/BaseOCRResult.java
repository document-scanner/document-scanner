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
package richtercloud.document.scanner.gui;

/**
 * OCR results might not always be strings, but more concrete, e.g. after auto
 * OCR value detection.
 *
 * @author richter
 */
public class BaseOCRResult<T> implements OCRResult {
    private final T oCRResult;

    public BaseOCRResult(T oCRResult) {
        this.oCRResult = oCRResult;
    }

    public T getoCRResult() {
        return oCRResult;
    }
}
