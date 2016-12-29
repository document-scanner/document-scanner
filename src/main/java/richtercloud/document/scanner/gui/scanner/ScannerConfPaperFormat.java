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
package richtercloud.document.scanner.gui.scanner;

import java.io.Serializable;

/**
 *
 * @author richter
 */
public class ScannerConfPaperFormat implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * The width of the format in mm.
     */
    private float width;
    /**
     * The height of the format in mm.
     */
    private float height;
    /**
     * The name the format might be know as (e.g. DIN A4).
     */
    private String name;

    public ScannerConfPaperFormat() {
    }

    public ScannerConfPaperFormat(float width, float height, String name) {
        this.width = width;
        this.height = height;
        this.name = name;
    }

    public void validate() throws ScannerConfPaperFormatValidationException {
        if(width <= 0) {
            throw new ScannerConfPaperFormatValidationException("width has to be larger than 0");
        }
        if(height <= 0) {
            throw new ScannerConfPaperFormatValidationException("height has to be larger than 0");
        }
        //name is allowed to be empty
    }

    /**
     * @return the width
     */
    public float getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(float width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public float getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}
