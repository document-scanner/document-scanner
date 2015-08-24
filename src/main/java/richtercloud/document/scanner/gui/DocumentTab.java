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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.swing.JComponent;
import org.apache.commons.math4.stat.descriptive.DescriptiveStatistics;
import richtercloud.document.scanner.ocr.OCREngine;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.reflection.form.builder.components.OCRResultPanelRetriever;
import richtercloud.reflection.form.builder.components.ScanResultPanelRetriever;
import richtercloud.reflection.form.builder.retriever.ValueRetriever;

/**
 *
 * @author richter
 */
public class DocumentTab extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;
    private String title;
    private DocumentForm documentForm;
    private OCRSelectComponent oCRSelectComponent;
    private OCREngine oCREngine;
    
    public DocumentTab(String title, OCRSelectComponent oCRSelectComponent, OCREngine oCREngine, Set<Class<?>> entityClasses, EntityManager entityManager) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        this.title = title;
        this.oCRSelectComponent = oCRSelectComponent;
        this.oCREngine = oCREngine;
        OCRResultPanelRetriever oCRResultPanelRetriever = new OCRResultPanelRetriever() {
            private final List<Double> stringBufferLengths = new ArrayList<>();
            @Override
            public String retrieve() {
                //estimate the initial StringBuilder size based on the median
                //of all prior OCR results (string length) (and 1000 initially)
                int stringBufferLengh;
                if(stringBufferLengths.isEmpty()) {
                    stringBufferLengh = 1000;
                }else {
                    DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(stringBufferLengths.toArray(new Double[stringBufferLengths.size()]));
                    stringBufferLengh = ((int)descriptiveStatistics.getPercentile(.5))+1;
                }
                stringBufferLengths.add((double)stringBufferLengh);
                StringBuilder retValueBuilder = new StringBuilder(stringBufferLengh);
                for(OCRSelectPanel oCRSelectComponent : DocumentTab.this.oCRSelectComponent.getImagePanels()) {
                    String oCRResult = DocumentTab.this.oCREngine.recognizeImage(oCRSelectComponent.getImage());
                    retValueBuilder.append(oCRResult);
                }
                String retValue = retValueBuilder.toString();
                return retValue;
            }
        };
        ScanResultPanelRetriever scanResultPanelRetriever = new ScanResultPanelRetriever() {
            @Override
            public byte[] retrieve() {
                ByteArrayOutputStream retValueStream = new ByteArrayOutputStream();
                for(OCRSelectPanel oCRSelectComponent : DocumentTab.this.oCRSelectComponent.getImagePanels()) {
                    try {
                        if(!ImageIO.write( oCRSelectComponent.getImage(), "png", retValueStream )) {
                            throw new IllegalStateException("writing image data to output stream failed");
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                return retValueStream.toByteArray();
            }
        };
        this.documentForm = new DocumentForm(entityClasses, entityManager, oCRResultPanelRetriever, scanResultPanelRetriever);
        this.initComponents();
        imageScrollPane.getViewport().setView(oCRSelectComponent);
        this.splitPane.setRightComponent(this.documentForm);
    }
    
    public DocumentTab(String title, 
            OCRSelectComponent oCRSelectComponent, 
            OCREngine oCREngine, 
            Set<Class<?>> entityClasses, Map<Class<?>, Class<? extends JComponent>> classMapping, 
            Map<Class<? extends JComponent>, ValueRetriever<?,?>> valueRetrieverMapping, 
            Map<Class<? extends JComponent>, ValueSetter<?>> valueSetterMapping, 
            EntityManager entityManager, 
            OCRResultPanelRetriever oCRResultPanelRetriever, 
            ScanResultPanelRetriever scanResultPanelRetriever) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        this(title, oCRSelectComponent, oCREngine, entityClasses, entityManager);
        this.documentForm = new DocumentForm(entityClasses, 
                classMapping, 
                valueRetrieverMapping, 
                valueSetterMapping, 
                entityManager, 
                oCRResultPanelRetriever, 
                scanResultPanelRetriever);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitPane = new javax.swing.JSplitPane();
        imageScrollPane = new javax.swing.JScrollPane();

        splitPane.setDividerLocation(350);
        splitPane.setLeftComponent(imageScrollPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    public String getTitle() {
        return this.title;
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    public DocumentForm getDocumentForm() {
        return this.documentForm;
    }

    public OCRSelectComponent getoCRSelectComponent() {
        return oCRSelectComponent;
    }

    public void setoCRSelectComponent(OCRSelectComponent oCRSelectComponent) {
        this.oCRSelectComponent = oCRSelectComponent;
        this.imageScrollPane.getViewport().setView(this.oCRSelectComponent);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane imageScrollPane;
    private javax.swing.JSplitPane splitPane;
    // End of variables declaration//GEN-END:variables
}
