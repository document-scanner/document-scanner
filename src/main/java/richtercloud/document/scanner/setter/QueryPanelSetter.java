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
import richtercloud.reflection.form.builder.jpa.panels.QueryPanel;

/**
 *
 * @author richter
 */
public class QueryPanelSetter implements ValueSetter<Object, QueryPanel<?>> {
    private final static QueryPanelSetter INSTANCE = new QueryPanelSetter();

    public static QueryPanelSetter getInstance() {
        return INSTANCE;
    }

    @Override
    public void setOCRResult(OCRResult oCRResult,
            QueryPanel<?> comp) {
        setValue(oCRResult.getoCRResult(),
                comp);
    }

    @Override
    public void setValue(Object value, QueryPanel<?> comp) {
        if(comp.getQueryResultTableModel().getEntities().contains(value)) {
            comp.getSelectedObjects().add(value);
        }
    }

    @Override
    public boolean isSupportsOCRResultSetting() {
        return true;
    }
}
