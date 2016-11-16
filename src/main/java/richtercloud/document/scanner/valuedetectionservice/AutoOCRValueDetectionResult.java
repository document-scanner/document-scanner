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

import java.util.Objects;

/**
 * A result of Auto OCR value detection contains the OCR source for displaying,
 * the recognized value for displaying and a set of callback components which
 * allow the user to choose actions in difficult situations like parsing contact
 * information and an existing contact entry in the database, but a possibly new
 * address.
 *
 * @author richter
 */
public class AutoOCRValueDetectionResult<T> {
    private final String oCRSource;
    private final T value;
    /**
     * One parent component which can contain and manage other callback
     * components (e.g. in a dropdown menu).
     */

    public AutoOCRValueDetectionResult(String oCRSource,
            T result) {
        this.oCRSource = oCRSource;
        this.value = result;
    }

    public T getValue() {
        return value;
    }

    public String getoCRSource() {
        return oCRSource;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.oCRSource);
        hash = 29 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AutoOCRValueDetectionResult<?> other = (AutoOCRValueDetectionResult<?>) obj;
        if (!Objects.equals(this.oCRSource, other.oCRSource)) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }
}