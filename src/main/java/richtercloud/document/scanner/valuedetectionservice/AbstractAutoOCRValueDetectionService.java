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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author richter
 * @param <T> the type of values to detect
 */
public abstract class AbstractAutoOCRValueDetectionService<T> implements AutoOCRValueDetectionService<T> {
    /**
     * The comparator used for sorting the results in the table of the result
     * dialog
     */
    /*
    internal implementation notes:
    - @TODO: try to pass T instead of ?
    */
    private final static Comparator<AutoOCRValueDetectionResult<?>> AUTO_OCR_VALUE_DETECTION_RESULT_COMPARATOR = new Comparator<AutoOCRValueDetectionResult<?>>() {
        @Override
        public int compare(AutoOCRValueDetectionResult<?> o1, AutoOCRValueDetectionResult<?> o2) {
            //Provide stable sorting first on type, then on value
            if(!o1.getValue().getClass().equals(o2.getValue())) {
                return o1.getValue().getClass().getSimpleName().compareTo(o2.getValue().getClass().getSimpleName());
            }
            if(o1.getValue() instanceof Comparable) {
                return ((Comparable)o1.getValue()).compareTo(o2.getValue());
            }else {
                //not comparable
                return -1;
            }
        }
    };
    private Set<AutoOCRValueDetectionServiceUpdateListener<T>> listeners = new HashSet<>();
    /**
     * Indicates that ongoing actions ought to be canceled (synchronized
     * accross threads with {@code volatile} keyword).
     */
    private volatile boolean canceled = false;

    @Override
    public void cancelFetch() {
        canceled = true;
    }

    protected boolean isCanceled() {
        return this.canceled;
    }

    /**
     * Retrieves and returns the auto value detection results in the order of
     * detection (i.e. earlier results have lower indices in the list).
     * @param input
     * @return list of results
     */
    @Override
    public final List<AutoOCRValueDetectionResult<T>> fetchResults(String input) {
        this.canceled = false;
        List<AutoOCRValueDetectionResult<T>> retValue = new LinkedList<>(fetchResults0(input));
        Collections.sort(retValue, AUTO_OCR_VALUE_DETECTION_RESULT_COMPARATOR);
            //sort internally in order to improve sorting performance of table
            //providing a Comparator is necessary in order to avoid making
            //entities Comparables which is rather bad style and might have
            //unwanted results
        return retValue;
    }

    /**
     * Retrieves and returns the auto value detection results.
     * @param input
     * @return list of results
     */
    /*
    internal implementation notes:
    - enforces return type LinkedHashSet which avoids duplicates early and keeps
    order in order to avoid each implementation to use it internally only (which
    would require code to be managed at multiple locations)
    */
    protected abstract LinkedHashSet<AutoOCRValueDetectionResult<T>> fetchResults0(String input);

    @Override
    public void addUpdateListener(AutoOCRValueDetectionServiceUpdateListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeUpdateListener(AutoOCRValueDetectionServiceUpdateListener listener) {
        listeners.remove(listener);
    }

    protected Set<AutoOCRValueDetectionServiceUpdateListener<T>> getListeners() {
        return Collections.unmodifiableSet(listeners);
    }
}