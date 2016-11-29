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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;

/**
 *
 * @author richter
 */
public class DefaultMainPanelTest {

    /**
     * Test of adjustZoomLevel method, of class DefaultMainPanel. Testing
     * zooming with real images is very hard because the zooming only takes
     * effect in {@link OCRSelectPanel#paintComponent(java.awt.Graphics) } and
     * working with anything Swing-related in tests is annoying and overkill for
     * a unit test -> check that the correct zoom level is set on mocks is
     * sufficient.
     */
    @Test
    public void testAdjustZoomLevel() throws IOException {
        //need real images since mocks won't be resizable
        int imageWidth = 88;
        BufferedImage image1 = mock(BufferedImage.class);
        BufferedImage image2 = mock(BufferedImage.class);
        BufferedImage image3 = mock(BufferedImage.class);
        when(image1.getWidth()).thenReturn(imageWidth);
        when(image2.getWidth()).thenReturn(imageWidth);
        when(image3.getWidth()).thenReturn(imageWidth);
        DocumentScannerConf documentScannerConf = mock(DocumentScannerConf.class);
        float zoomLevelMultiplier = 0.5f;
        when(documentScannerConf.getZoomLevelMultiplier()).thenReturn(zoomLevelMultiplier);
        //test change with one image
        List<BufferedImage> images = new LinkedList<>(Arrays.asList(image1));
        int preferredWidth = 10;
        when(documentScannerConf.getPreferredWidth()).thenReturn(preferredWidth);
        float zoomLevel = DefaultMainPanel.adjustZoomLevel(images,
                documentScannerConf);
        assertEquals(zoomLevel, 0.125f, 0.01f);
        //test no change because current width is closer than zoom result
        images = new LinkedList<>(Arrays.asList(image1, image2, image3));
        preferredWidth = 100;
        when(documentScannerConf.getPreferredWidth()).thenReturn(preferredWidth);
        zoomLevel = DefaultMainPanel.adjustZoomLevel(images,
                documentScannerConf);
        assertEquals(zoomLevel, 1.0f, 0.01f);
        //test zooming in to 22 (== preferredWidth)
        preferredWidth = 22;
        when(documentScannerConf.getPreferredWidth()).thenReturn(preferredWidth);
        zoomLevel = DefaultMainPanel.adjustZoomLevel(images,
                documentScannerConf);
        assertEquals(zoomLevel, 0.25f, 0.01f);
        //test zooming out to 176 (!= preferredWidth == 200)
        preferredWidth = 200;
        when(documentScannerConf.getPreferredWidth()).thenReturn(preferredWidth);
        zoomLevel = DefaultMainPanel.adjustZoomLevel(images,
                documentScannerConf);
        assertEquals(zoomLevel, 2.0f, 0.01f);
    }
}
