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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.ocrresult.OCRResult;
import richtercloud.reflection.form.builder.TransformationException;
import richtercloud.reflection.form.builder.jpa.panels.EmbeddableListPanel;
import richtercloud.validation.tools.FieldRetrievalException;

/**
 *
 * @author richter
 */
public class EmbeddableListPanelSetter implements ValueSetter<Object, EmbeddableListPanel> {
    private final static EmbeddableListPanelSetter INSTANCE = new EmbeddableListPanelSetter();
    private final static Logger LOGGER = LoggerFactory.getLogger(EmbeddableListPanelSetter.class);

    public static EmbeddableListPanelSetter getInstance() {
        return INSTANCE;
    }

    @Override
    public void setValue(Object value,
            EmbeddableListPanel comp) throws TransformationException, FieldRetrievalException {
        comp.addValue(value);
    }

    @Override
    public void setOCRResult(OCRResult oCRResult, EmbeddableListPanel comp) {
        //There's no easy way to set multiple values in form of a list
        //-> better to be handled in auto OCR value detection
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSupportsOCRResultSetting() {
        return false;
    }
}
