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

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import javax.swing.JComponent;
import richtercloud.document.scanner.components.OCRResultFieldUpdateEvent;
import richtercloud.document.scanner.components.OCRResultPanel;
import richtercloud.document.scanner.components.OCRResultPanelFetcher;
import richtercloud.document.scanner.components.OCRResultPanelUpdateEvent;
import richtercloud.document.scanner.components.OCRResultPanelUpdateListener;
import richtercloud.reflection.form.builder.FieldAnnotationHandler;
import richtercloud.reflection.form.builder.FieldUpdateListener;
import richtercloud.reflection.form.builder.ReflectionFormBuilder;

/**
 *
 * @author richter
 */
public class OCRResultFieldAnnotationHandler implements FieldAnnotationHandler<String, OCRResultFieldUpdateEvent> {
    private final OCRResultPanelFetcher oCRResultPanelFetcher;

    public OCRResultFieldAnnotationHandler(OCRResultPanelFetcher oCRResultPanelFetcher) {
        this.oCRResultPanelFetcher = oCRResultPanelFetcher;
    }

    @Override
    public JComponent handle(Type fieldClass,
            String fieldValue,
            Object entity,
            final FieldUpdateListener<OCRResultFieldUpdateEvent> fieldUpdateListener,
            ReflectionFormBuilder reflectionFormBuilder) {
        if(fieldClass == null) {
            throw new IllegalArgumentException("fieldClass mustn't be null");
        }
        OCRResultPanel retValue = new OCRResultPanel(oCRResultPanelFetcher, fieldValue);
        retValue.addUpdateListener(new OCRResultPanelUpdateListener() {
            @Override
            public void onUpdate(OCRResultPanelUpdateEvent event) {
                fieldUpdateListener.onUpdate(new OCRResultFieldUpdateEvent(event.getNewValue()));
            }
        });
        return retValue;
    }

}
