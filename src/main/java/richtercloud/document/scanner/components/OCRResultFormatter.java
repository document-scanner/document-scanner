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
package richtercloud.document.scanner.components;

import java.io.Serializable;

/**
 * Allows to specify how to format an auto-OCR-value-detection result when
 * displayed in {@link ValueDetectionPanel}'s dropdown menu.
 *
 * @author richter
 * @param <T> the type of objects to format
 */
/*
internal implementation notes:
- is not a functional interface in order to make serialization more easy
(lambdas contain a lot of overhead)
*/
public interface OCRResultFormatter<T> extends Serializable {

    String format(T object);
}
