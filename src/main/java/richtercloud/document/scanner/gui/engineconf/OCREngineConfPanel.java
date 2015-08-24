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
package richtercloud.document.scanner.gui.engineconf;

import java.util.Properties;
import javax.swing.JPanel;
import richtercloud.document.scanner.ocr.OCREngine;

/**
 * allows management of (eventually completely) different configuration
 * directives and control of different types of {@link OCREngine}s.
 * 
 * Changes only need to be retained if {@link #save() } has been invoked,
 * otherwise the changes can be discarded or kept.
 * @author richter
 */
/*
internal implementation notes:
- needs to be an abstract class extending JPanel in order to be able to work
with instances (adding to container and components - there's no interface in the
JComponent hierarchy)
*/
public abstract class OCREngineConfPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    /**
     * get the instance of {@link OCREngine} this panel manages
     * @return 
     */
    public abstract OCREngine getOCREngine();
    
    /**
     * write the current changes from the panel's components to its properties
     * where they can be retrieved reliably
     * @param conf
     */
    public abstract void save(Properties conf);
    
    /**
     * 
     * @param conf 
     */
    public abstract void load(Properties conf);
}
