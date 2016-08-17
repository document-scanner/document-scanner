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

import java.util.Objects;

/**
 *
 * @author richter
 */
public class AutoOCRValueDetectionResult<T> {
    private final String oCRSource;
    private final T result;

    public AutoOCRValueDetectionResult(String oCRSource, T result) {
        this.oCRSource = oCRSource;
        this.result = result;
    }

    public T getResult() {
        return result;
    }

    public String getoCRSource() {
        return oCRSource;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.oCRSource);
        hash = 29 * hash + Objects.hashCode(this.result);
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
        return Objects.equals(this.result, other.result);
    }

}
