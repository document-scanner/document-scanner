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

import richtercloud.document.scanner.ocr.OCREngine;

/**
 * Both a data container and factory for instance of {@code E}.
 * @author richter
 * @param <E> the type of the OCR engine managed by this configuration
 */
public interface OCREngineConf<E extends OCREngine> {
    E getOCREngine();
}
