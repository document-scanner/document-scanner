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

/**
 *
 * @author richter
 */
public interface OCREngine {

    /**
     * recognizes the characters of {@code image}
     * @param image
     * @return the recognized characters
     */
    String recognizeImage(BufferedImage image);

    /**
     * Allows cancelation of a (potentially time taking) {@link #recognizeImage(java.awt.image.BufferedImage) } from
     * another thread.
     */
    /*
    internal implementation notes:
    - canceling from the same thread doesn't make sense because recognizeImage must
    return first and it return when it's completed only
    */
    void cancelRecognizeImage();
}
