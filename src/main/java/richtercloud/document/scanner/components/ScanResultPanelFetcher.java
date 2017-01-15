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

import java.util.List;
import richtercloud.document.scanner.ifaces.ImageWrapper;

/**
 *
 * @author richter
 */
/*
internal implementation notes:
- Keeping fetch and recreate action in different interfaces in order to keep
code of MainPanel as untouched as possible (implementations necessarily need a
reference to OCRSelectPanelPanel which makes creation of
ScanResultPanelRecreator inconvenient (because the reference can't be immutable)
and separating the function is most elegant.
- Since this interface is only used in DocumentScanner it's
ok to define it with reference to resources (OCRSelectPanelPanel) which need to
be defined properly by implementors.
*/
public interface ScanResultPanelFetcher {

    /**
     * Fetches binary data from a {@link ScanResultPanel} which is retrieved
     * from internal references (e.g. to {@link OCRSelectPanelPanel}).
     * @return the fetched binary data, never {@code null}
     */
    List<ImageWrapper> fetch();

    void cancelFetch();
}
