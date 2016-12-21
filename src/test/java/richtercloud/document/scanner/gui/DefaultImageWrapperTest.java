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

import richtercloud.document.scanner.gui.imagewrapper.DefaultImageWrapper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author richter
 */
public class DefaultImageWrapperTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultImageWrapperTest.class);

    /**
     * Test of getOriginalImageStream method, of class DefaultImageWrapper.
     */
    @Test
    public void testGetOriginalImageStream() throws Exception {
        System.out.println("getOriginalImageStream");
        DefaultImageWrapper instance = null;
        FileInputStream expResult = null;
        InputStream result = instance.getOriginalImageStream();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOriginalImage method, of class DefaultImageWrapper.
     */
    @Test
    public void testGetOriginalImage() throws Exception {
        System.out.println("getOriginalImage");
        DefaultImageWrapper instance = null;
        BufferedImage expResult = null;
        BufferedImage result = instance.getOriginalImage();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getImagePreview method, of class DefaultImageWrapper.
     */
    @Test
    public void testGetImagePreview() throws Exception {
        System.out.println("getImagePreview");
        int width = 0;
        DefaultImageWrapper instance = null;
        BufferedImage expResult = null;
        BufferedImage result = instance.getImagePreview(width);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getImagePreviewFX method, of class DefaultImageWrapper.
     */
    @Test
    public void testGetImagePreviewFX() throws Exception {
        System.out.println("getImagePreviewFX");
        int width = 0;
        DefaultImageWrapper instance = null;
        WritableImage expResult = null;
        WritableImage result = instance.getImagePreviewFX(width);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getImageHeightScaled method, of class DefaultImageWrapper.
     */
    @Test
    public void testGetImageHeightScaled() {
        System.out.println("getImageHeightScaled");
        int width = 0;
        DefaultImageWrapper instance = null;
        int expResult = 0;
        int result = instance.getImageHeightScaled(width);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getInitialWidth method, of class DefaultImageWrapper.
     */
    @Test
    public void testGetInitialWidth() {
        System.out.println("getInitialWidth");
        DefaultImageWrapper instance = null;
        int expResult = 0;
        int result = instance.getInitialWidth();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getInitialHeight method, of class DefaultImageWrapper.
     */
    @Test
    public void testGetInitialHeight() {
        System.out.println("getInitialHeight");
        DefaultImageWrapper instance = null;
        int expResult = 0;
        int result = instance.getInitialHeight();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStorageFile method, of class DefaultImageWrapper.
     */
    @Test
    public void testGetStorageFile() {
        System.out.println("getStorageFile");
        DefaultImageWrapper instance = null;
        File expResult = null;
        File result = instance.getStorageFile();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testSerialize() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        InputStream instanceInputStream = DefaultImageWrapperTest.class.getResourceAsStream("/File_CC-BY-SA_3_icon_88x31.png");
        BufferedImage instanceImage = ImageIO.read(instanceInputStream);
        File storageDir = File.createTempFile(DefaultImageWrapperTest.class.getSimpleName(),
                "storage-dir" //suffix
        );
        storageDir.delete();
        storageDir.mkdirs();
        DefaultImageWrapper instance = new DefaultImageWrapper(storageDir,
                instanceImage);
        objectOutputStream.writeObject(instance);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        DefaultImageWrapper result = (DefaultImageWrapper) objectInputStream.readObject();
        File controlFile = File.createTempFile(DefaultImageWrapperTest.class.getSimpleName(),
                "result" //suffix
        );
        ImageIO.write(result.getOriginalImage(), "png", controlFile);
        LOGGER.info(String.format("control file is %s", controlFile.getAbsolutePath()));
    }
}
