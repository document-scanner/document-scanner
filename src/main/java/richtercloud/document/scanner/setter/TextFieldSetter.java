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
package richtercloud.document.scanner.setter;

import javax.swing.JTextField;
import richtercloud.document.scanner.gui.StringOCRResult;

/**
 *
 * @author richter
 */
public class TextFieldSetter implements ValueSetter<StringOCRResult, JTextField> {
    private final static TextFieldSetter INSTANCE = new TextFieldSetter();

    public static TextFieldSetter getInstance() {
        return INSTANCE;
    }

    public TextFieldSetter() {
    }

    @Override
    public void setValue(StringOCRResult value, JTextField comp) {
        comp.setText(value.getoCRResult());
    }

}
