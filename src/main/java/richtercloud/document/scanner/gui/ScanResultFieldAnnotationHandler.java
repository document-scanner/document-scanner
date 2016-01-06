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
import java.lang.reflect.Type;
import javax.swing.JComponent;
import richtercloud.document.scanner.components.ScanResultPanel;
import richtercloud.document.scanner.components.ScanResultPanelFetcher;
import richtercloud.document.scanner.components.ScanResultPanelUpdateEvent;
import richtercloud.document.scanner.components.ScanResultPanelUpdateListener;
import richtercloud.reflection.form.builder.fieldhandler.FieldAnnotationHandler;
import richtercloud.reflection.form.builder.fieldhandler.FieldUpdateEvent;
import richtercloud.reflection.form.builder.fieldhandler.FieldUpdateListener;
import richtercloud.reflection.form.builder.ReflectionFormBuilder;

/**
 *
 * @author richter
 */
public class ScanResultFieldAnnotationHandler implements FieldAnnotationHandler<byte[], FieldUpdateEvent<byte[]>, ReflectionFormBuilder> {
    private final ScanResultPanelFetcher scanResultPanelFetcher;

    public ScanResultFieldAnnotationHandler(ScanResultPanelFetcher scanResultPanelFetcher) {
        this.scanResultPanelFetcher = scanResultPanelFetcher;
    }

    @Override
    public JComponent handle(Field field,
            Object instance,
            final FieldUpdateListener<FieldUpdateEvent<byte[]>> fieldUpdateListener,
            ReflectionFormBuilder reflectionFormBuilder) throws IllegalAccessException {
        if(field == null) {
            throw new IllegalArgumentException("fieldClass mustn't be null");
        }
        byte[] fieldValue = (byte[]) field.get(instance);
        ScanResultPanel retValue = new ScanResultPanel(scanResultPanelFetcher, fieldValue);
        retValue.addUpdateListerner(new ScanResultPanelUpdateListener() {
            @Override
            public void onUpdate(ScanResultPanelUpdateEvent event) {
                fieldUpdateListener.onUpdate(new FieldUpdateEvent<>(event.getNewValue()));
            }
        });
        return retValue;
    }

}
