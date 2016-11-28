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

import richtercloud.document.scanner.gui.ocrresult.OCRResult;
import richtercloud.reflection.form.builder.jpa.panels.LongIdPanel;

/**
 *
 * @author richter
 */
public class LongIdPanelSetter implements ValueSetter<Long, LongIdPanel> {
    private final static LongIdPanelSetter INSTANCE = new LongIdPanelSetter();

    public static LongIdPanelSetter getInstance() {
        return INSTANCE;
    }

    protected LongIdPanelSetter() {
    }

    @Override
    public void setOCRResult(OCRResult oCRResult, LongIdPanel comp) {
        setValue(Long.valueOf(oCRResult.getoCRResult()), comp);
    }

    @Override
    public void setValue(Long value, LongIdPanel comp) {
        comp.setValue(value);
    }

    @Override
    public boolean isSupportsOCRResultSetting() {
        return true;
    }
}
