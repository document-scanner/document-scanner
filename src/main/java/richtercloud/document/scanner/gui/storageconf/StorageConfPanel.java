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
package richtercloud.document.scanner.gui.storageconf;

import javax.swing.JPanel;
import richtercloud.document.scanner.gui.conf.StorageConf;

/**
 *
 * @author richter
 * @param <C> the type of the {@link StorageConf} to use for saving and loading
 */
/*
internal implementation notes:
- in order to be usable as panel StorageConfPanel needs to expose save and
cancel methods because buttons can not be included in the panel itself in order
to provide sane GUI elements
*/
public abstract class StorageConfPanel<C extends StorageConf<?>> extends JPanel {
    private static final long serialVersionUID = 1L;

    public abstract C getStorageConf();

    public abstract void save();

    public abstract void cancel();
}
