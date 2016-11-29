/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package richtercloud.document.scanner.setter;

import richtercloud.document.scanner.gui.ocrresult.OCRResult;
import richtercloud.reflection.form.builder.jpa.panels.StringAutoCompletePanel;

/**
 *
 * @author richter
 */
public class StringAutoCompletePanelSetter implements ValueSetter<OCRResult, StringAutoCompletePanel> {
    private final static StringAutoCompletePanelSetter INSTANCE = new StringAutoCompletePanelSetter();

    public static StringAutoCompletePanelSetter getInstance() {
        return INSTANCE;
    }

    @Override
    public void setValue(OCRResult value, StringAutoCompletePanel comp) {
        comp.getComboBox().setSelectedItem(value.getoCRResult());
    }

    @Override
    public void setOCRResult(OCRResult oCRResult, StringAutoCompletePanel comp) {
        comp.getComboBox().setSelectedItem(oCRResult.getoCRResult());
    }

    @Override
    public boolean isSupportsOCRResultSetting() {
        return true;
    }
}
