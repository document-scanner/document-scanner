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

import java.lang.reflect.Field;
import javax.swing.JComponent;
import richtercloud.document.scanner.components.OCRResultPanel;
import richtercloud.document.scanner.components.OCRResultPanelFetcher;
import richtercloud.document.scanner.components.OCRResultPanelUpdateEvent;
import richtercloud.document.scanner.components.OCRResultPanelUpdateListener;
import richtercloud.reflection.form.builder.fieldhandler.FieldAnnotationHandler;
import richtercloud.reflection.form.builder.fieldhandler.FieldUpdateEvent;
import richtercloud.reflection.form.builder.fieldhandler.FieldUpdateListener;
import richtercloud.reflection.form.builder.ReflectionFormBuilder;
import richtercloud.reflection.form.builder.message.MessageHandler;

/**
 *
 * @author richter
 */
public class OCRResultFieldAnnotationHandler implements FieldAnnotationHandler<String, FieldUpdateEvent<String>, ReflectionFormBuilder> {
    private final OCRResultPanelFetcher oCRResultPanelFetcher;
    private final MessageHandler messageHandler;

    public OCRResultFieldAnnotationHandler(OCRResultPanelFetcher oCRResultPanelFetcher,
            MessageHandler messageHandler) {
        this.oCRResultPanelFetcher = oCRResultPanelFetcher;
        this.messageHandler = messageHandler;
    }

    @Override
    public JComponent handle(Field field,
            Object instance,
            final FieldUpdateListener<FieldUpdateEvent<String>> fieldUpdateListener,
            ReflectionFormBuilder reflectionFormBuilder) throws IllegalAccessException {
        if(field == null) {
            throw new IllegalArgumentException("field mustn't be null");
        }
        String fieldValue = (String) field.get(instance);
        OCRResultPanel retValue = new OCRResultPanel(oCRResultPanelFetcher,
                fieldValue,
                messageHandler);
        retValue.addUpdateListener(new OCRResultPanelUpdateListener() {
            @Override
            public void onUpdate(OCRResultPanelUpdateEvent event) {
                fieldUpdateListener.onUpdate(new FieldUpdateEvent<>(event.getNewValue()));
            }
        });
        return retValue;
    }

}
