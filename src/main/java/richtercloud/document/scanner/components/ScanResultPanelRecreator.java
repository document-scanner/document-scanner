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
 * Undoes {@link ScanResultPanelFetcher#fetch() }.
 * @author richter
 */
public interface ScanResultPanelRecreator {

    /**
     * Recreates all images from once fetched binary data to appear on
     * internally managed references.
     * @param data
     * @return the recreation result
     */
    List<ImageWrapper> recreate(byte[] data);
}
