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
package richtercloud.document.scanner.valuedetectionservice;

import java.util.List;

/**
 * Abstraction for different implementations for auto-OCR-value-detection.
 *
 * @author richter
 * @param <T> the type of values to detect
 */
public interface AutoOCRValueDetectionService<T> {

    /**
     * Fetches results in the form of {@link AutoOCRValueDetectionResult}s from
     * {@code input}.
     * @param input
     * @return the fetched results
     */
    List<AutoOCRValueDetectionResult<T>> fetchResults(String input);

    /**
     * Cancels a previously started {@link #fetchResults(java.lang.String) }.
     */
    void cancelFetch();

    void addUpdateListener(AutoOCRValueDetectionServiceUpdateListener listener);

    void removeUpdateListener(AutoOCRValueDetectionServiceUpdateListener listener);
}