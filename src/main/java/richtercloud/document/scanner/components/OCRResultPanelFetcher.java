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

import richtercloud.document.scanner.ifaces.OCREngineRecognitionException;

/**
 *
 * @author richter
 */
/*
internal implementation notes:
- Don't use a built-in cancelable Future or FutureTask because it's unclear how
it handles resources like started OS processes -> implement manually with
return values or exceptions (use exceptions only if necessary, i.e. if return
value null is needed for something else)
- Although function (even implementations) has been moved to OCRSelectPanelPanel
it's fine to keep OCRResultPanelFetcher in order to match with the symmetry of
ScanResultPanelFetcher and ScanResultPanelRecreator (even though
OCRResultPanelFetcher doesn't have a symmetric counterpart)
- makes sense to once remove OCRResultPanelFetcherProgressListener because do
nothing, but delegate to OCRSelectPanelPanelFetcherProgressListener
*/
public interface OCRResultPanelFetcher {

    /**
     * Fetches the result
     * @return the OCR result or {@code null} if {@link #fetch() } has been
     * canceled with {@link #cancelFetch() }
     */
    String fetch() throws OCREngineRecognitionException;

    /**
     * Allows cancelation of a (potentially time taking) {@link #fetch() } from
     * another thread.
     *
     * @throws UnsupportedOperationException if the
     * {@code OCRResultPanelFetcher} doesn't support cancelation
     */
    /*
    internal implementation notes:
    - canceling from the same thread doesn't make sense because fetch must
    return first and it return when it's completed only
    */
    void cancelFetch() throws UnsupportedOperationException;

    void addProgressListener(OCRResultPanelFetcherProgressListener progressListener);

    void removeProgressListener(OCRResultPanelFetcherProgressListener progressListener);
}
