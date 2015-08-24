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

import java.util.Properties;
import javax.swing.JPanel;
import richtercloud.document.scanner.storage.Storage;

/**
 *
 * @author richter
 */
public abstract class StorageConfPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    public abstract Storage getStorage();
    
    public abstract void save(Properties conf);
    
    public abstract void load(Properties conf);
}
