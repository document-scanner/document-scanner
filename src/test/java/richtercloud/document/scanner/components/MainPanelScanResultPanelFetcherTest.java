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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import richtercloud.document.scanner.ifaces.ImageWrapper;
import richtercloud.document.scanner.ifaces.OCRSelectPanel;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanel;

/**
 *
 * @author richter
 */
public class MainPanelScanResultPanelFetcherTest {

    /**
     * Test of fetch method, of class MainPanelScanResultPanelFetcher.
     */
    @Test
    public void testFetch() throws IOException {
        OCRSelectPanelPanel oCRSelectPanelPanel = mock(OCRSelectPanelPanel.class);
        OCRSelectPanel oCRSelectPanel1 = mock(OCRSelectPanel.class);
        OCRSelectPanel oCRSelectPanel2 = mock(OCRSelectPanel.class);
        BufferedImage image = ImageIO.read(MainPanelScanResultPanelFetcherTest.class.getResource("/File_CC-BY-SA_3_icon_88x31.png"));
        ImageWrapper imageWrapper = mock(ImageWrapper.class);
        when(imageWrapper.getOriginalImage()).thenReturn(image);
        when(oCRSelectPanel1.getImage()).thenReturn(imageWrapper);
        when(oCRSelectPanel2.getImage()).thenReturn(imageWrapper);
        List<OCRSelectPanel> oCRSelectPanels = new LinkedList<>(Arrays.asList(oCRSelectPanel1,
                oCRSelectPanel2));
        when(oCRSelectPanelPanel.getoCRSelectPanels()).thenReturn(oCRSelectPanels);
        MainPanelScanResultPanelFetcher instance = new MainPanelScanResultPanelFetcher(oCRSelectPanelPanel);
        byte[] imageBytes;
        ByteArrayOutputStream imageBytesOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image,
                "png",
                imageBytesOutputStream);
        imageBytes = imageBytesOutputStream.toByteArray();
        byte[] expResult = new byte[2*imageBytes.length];
        System.arraycopy(imageBytes, 0, expResult, 0, imageBytes.length);
        System.arraycopy(imageBytes, 0, expResult, imageBytes.length, imageBytes.length);
        byte[] result = instance.fetch();
        assertArrayEquals(expResult, result);
    }
}
