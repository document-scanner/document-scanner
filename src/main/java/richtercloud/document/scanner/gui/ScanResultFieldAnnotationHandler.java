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
import richtercloud.document.scanner.components.ScanResultFieldUpdateEvent;
import richtercloud.document.scanner.components.ScanResultPanel;
import richtercloud.document.scanner.components.ScanResultPanelFetcher;
import richtercloud.document.scanner.components.ScanResultPanelUpdateEvent;
import richtercloud.document.scanner.components.ScanResultPanelUpdateListener;
import richtercloud.reflection.form.builder.FieldAnnotationHandler;
import richtercloud.reflection.form.builder.FieldUpdateListener;
import richtercloud.reflection.form.builder.ReflectionFormBuilder;

/**
 *
 * @author richter
 */
public class ScanResultFieldAnnotationHandler implements FieldAnnotationHandler<byte[], ScanResultFieldUpdateEvent> {
    private final ScanResultPanelFetcher scanResultPanelFetcher;

    public ScanResultFieldAnnotationHandler(ScanResultPanelFetcher scanResultPanelFetcher) {
        this.scanResultPanelFetcher = scanResultPanelFetcher;
    }

    @Override
    public JComponent handle(Type fieldClass,
            byte[] fieldValue,
            Object entity,
            final FieldUpdateListener<ScanResultFieldUpdateEvent> fieldUpdateListener,
            ReflectionFormBuilder reflectionFormBuilder) {
        if(fieldClass == null) {
            throw new IllegalArgumentException("fieldClass mustn't be null");
        }
        ScanResultPanel retValue = new ScanResultPanel(scanResultPanelFetcher, fieldValue);
        retValue.addUpdateListerner(new ScanResultPanelUpdateListener() {
            @Override
            public void onUpdate(ScanResultPanelUpdateEvent event) {
                fieldUpdateListener.onUpdate(new ScanResultFieldUpdateEvent(event.getNewValue()));
            }
        });
        return retValue;
    }

}
