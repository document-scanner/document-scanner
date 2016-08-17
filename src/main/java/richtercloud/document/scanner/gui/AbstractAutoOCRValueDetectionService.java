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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author richter
 */
public abstract class AbstractAutoOCRValueDetectionService implements AutoOCRValueDetectionService {
    private boolean canceled = false;
    private Set<AutoOCRValueDetectionServiceUpdateListener> listeners = new HashSet<>();

    @Override
    public void cancelFetch() {
        canceled = true;
    }

    @Override
    public final List<AutoOCRValueDetectionResult<?>> fetchResults(String input) {
        this.canceled = false;
        return fetchResults0(input);
    }

    protected abstract List<AutoOCRValueDetectionResult<?>> fetchResults0(String input);

    protected boolean isCanceled() {
        return canceled;
    }

    @Override
    public void addUpdateListener(AutoOCRValueDetectionServiceUpdateListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeUpdateListener(AutoOCRValueDetectionServiceUpdateListener listener) {
        listeners.remove(listener);
    }

    protected Set<AutoOCRValueDetectionServiceUpdateListener> getListeners() {
        return Collections.unmodifiableSet(listeners);
    }
}
