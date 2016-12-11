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
import richtercloud.reflection.form.builder.storage.StorageConf;

/**
 *
 * @author richter
 * @param <C> the type of the {@link StorageConf} to use for saving and loading
 */
/*
internal implementation notes:
- threre's few sense in making the panel reusable, so don't do it
*/
public abstract class StorageConfPanel<C extends StorageConf> extends JPanel {
    private static final long serialVersionUID = 1L;

    public abstract C getStorageConf();

    /**
     * Stores value of the GUI components on the passed {@link StorageConf}.
     * Validation should occur with {@link StorageConf#validate() }.
     */
    public abstract void save();

    public abstract void cancel();
}
