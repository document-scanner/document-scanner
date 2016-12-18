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
package richtercloud.document.scanner.components;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import richtercloud.document.scanner.ifaces.OCRSelectPanel;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanel;

/**
 * Uses {@link ImageIcon} and Java built-in binary serialization in order to
 * work around the fact that {@link ImageIO}'s {@code read} and {@code write}
 * methods only seem to be capable of handling one image (at least for reading).
 * @author richter
 */
public class MainPanelScanResultPanelFetcher implements ScanResultPanelFetcher {
    private OCRSelectPanelPanel oCRSelectComponent;

    /**
     * Creates a {@code MainPanelScanResultPanelFetcher}.
     * @param oCRSelectComponent the {@link OCRSelectPanelPanel} where to
     * fetch the OCR results (might be {@code null} in order to avoid
     * cyclic dependencies, but needs to be set up with {@link #setoCRSelectComponent(richtercloud.document.scanner.gui.OCRSelectPanelPanel) }
     * before {@link #fetch() } works.
     */
    public MainPanelScanResultPanelFetcher(OCRSelectPanelPanel oCRSelectComponent) {
        this.oCRSelectComponent = oCRSelectComponent;
    }

    public void setoCRSelectComponent(OCRSelectPanelPanel oCRSelectComponent) {
        this.oCRSelectComponent = oCRSelectComponent;
    }

    /**
     * Uses {@link ImageIO#write(java.awt.image.RenderedImage, java.lang.String, java.io.OutputStream) }
     * assuming that {@link ImageIO#read(java.io.InputStream) } allows
     * re-reading data correctly.
     * @return the fetched binary data
     */
    @Override
    public byte[] fetch() {
        ByteArrayOutputStream retValueStream = new ByteArrayOutputStream();
        List<ImageIcon> imageIcons = new LinkedList<>();
        try {
            for (OCRSelectPanel imagePanel : this.oCRSelectComponent.getoCRSelectPanels()) {
                imageIcons.add(new ImageIcon(imagePanel.getImage().getImagePreview(imagePanel.getImage().getInitialWidth())));
            }
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(retValueStream);
            objectOutputStream.writeObject(imageIcons);
            byte[] retValue = retValueStream.toByteArray();
            return retValue;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
