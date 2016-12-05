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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.tika.io.IOUtils;
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
public class MainPanelScanResultPanelRecreatorTest {

    /**
     * Test of recreate method, of class MainPanelScanResultPanelRecreator.
     */
    @Test
    public void testRecreate() throws IOException {
        ByteArrayOutputStream imageFileBytesOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(MainPanelScanResultPanelFetcherTest.class.getResourceAsStream("/File_CC-BY-SA_3_icon_88x31.png"),
                imageFileBytesOutputStream);
        byte[] imageFileBytes = imageFileBytesOutputStream.toByteArray();
        ByteArrayOutputStream imageBytesOutputStream = new ByteArrayOutputStream();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageFileBytes));
        ImageIO.write(image,
                "png",
                imageBytesOutputStream);
        byte[] imageBytes = imageBytesOutputStream.toByteArray();
        File tmpFile = File.createTempFile("prefix", "suffix");
        MainPanelScanResultPanelRecreator instance = new MainPanelScanResultPanelRecreator(tmpFile);
        List<ImageWrapper> result = instance.recreate(imageBytes);
        assertEquals(1,
                result.size());
        ByteArrayOutputStream resultOutputStream = new ByteArrayOutputStream();
        ImageIO.write(result.get(0).getOriginalImage(),
                "png",
                resultOutputStream);
        assertImagesEquals(image,
                result.get(0).getOriginalImage());

        //test in conjunction with MainPanelScanResultFetcher
        OCRSelectPanelPanel oCRSelectPanelPanel = mock(OCRSelectPanelPanel.class);
        OCRSelectPanel oCRSelectPanel1 = mock(OCRSelectPanel.class);
        OCRSelectPanel oCRSelectPanel2 = mock(OCRSelectPanel.class);
        image = ImageIO.read(MainPanelScanResultPanelFetcherTest.class.getResource("/File_CC-BY-SA_3_icon_88x31.png"));
        ImageWrapper imageWrapper = mock(ImageWrapper.class);
        when(imageWrapper.getOriginalImage()).thenReturn(image);
        when(oCRSelectPanel1.getImage()).thenReturn(imageWrapper);
        when(oCRSelectPanel2.getImage()).thenReturn(imageWrapper);
        List<OCRSelectPanel> oCRSelectPanels = new LinkedList<>(Arrays.asList(oCRSelectPanel1,
                oCRSelectPanel2));
        when(oCRSelectPanelPanel.getoCRSelectPanels()).thenReturn(oCRSelectPanels);
        MainPanelScanResultPanelFetcher mainPanelScanResultPanelFetcher = new MainPanelScanResultPanelFetcher(oCRSelectPanelPanel);
        byte[] mainPanelScanResultPanelFetcherBytes = mainPanelScanResultPanelFetcher.fetch();
        List<ImageWrapper> mainPanelScanResultPanelFetcherImages = instance.recreate(mainPanelScanResultPanelFetcherBytes);
        assertEquals(mainPanelScanResultPanelFetcherImages.size(),
                2);
        for(int i=0; i<2; i++) {
            assertImagesEquals(mainPanelScanResultPanelFetcherImages.get(i).getOriginalImage(),
                    image);
        }
    }

    protected static void assertImagesEquals(BufferedImage image,
            BufferedImage image1) {
        //bytes aren't equals, but have the same head, and don't provide an
        //equals method
        assertEquals(image.getType(), image1.getType());
        assertEquals(image.getColorModel(), image1.getColorModel());
        assertEquals(image.getNumXTiles(), image1.getNumXTiles());
        assertEquals(image.getNumYTiles(), image1.getNumYTiles());
        assertEquals(image.getTransparency(), image1.getTransparency());
        assertEquals(image.isAlphaPremultiplied(), image1.isAlphaPremultiplied());
        assertEquals(image.getWidth(), image1.getWidth());
        assertEquals(image.getHeight(), image1.getHeight());
        //data is different for equal image apparently
    }
}
