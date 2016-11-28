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

import javax.swing.JSpinner;
import richtercloud.document.scanner.gui.ocrresult.OCRResult;

/**
 *
 * @author richter
 */
public class SpinnerSetter implements ValueSetter<Double, JSpinner> {
    private final static SpinnerSetter INSTANCE = new SpinnerSetter();

    public static SpinnerSetter getInstance() {
        return INSTANCE;
    }

    protected SpinnerSetter() {
    }

    @Override
    public void setOCRResult(OCRResult oCRResult, JSpinner comp) {
        setValue(Double.valueOf(oCRResult.getoCRResult()), comp);
    }

    @Override
    public void setValue(Double value, JSpinner comp) {
        comp.setValue(value);
    }

    @Override
    public boolean isSupportsOCRResultSetting() {
        return true;
    }
}
