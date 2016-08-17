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

import java.text.NumberFormat;
import javax.swing.JRadioButtonMenuItem;

/**
 *
 * @author richter
 */
public class NumberFormatMenuItem extends JRadioButtonMenuItem {

    private static final long serialVersionUID = 1L;
    /**
     * The format the user selected in the popup. {@code null} indicates
     * automatic parsing (i.e. try all formats).
     */
    private final NumberFormat numberFormat;

    NumberFormatMenuItem(NumberFormat numberFormat) {
        super(numberFormat != null ? numberFormat.format(FormatUtils.NUMBER_FORMAT_VALUE) : "Automatic");
        this.numberFormat = numberFormat;
    }

    public NumberFormat getNumberFormat() {
        return numberFormat;
    }
}
