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
package richtercloud.document.scanner.model.imagewrapper;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.imageio.ImageIO;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author richter
 */
public class DefaultImageWrapperTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultImageWrapperTest.class);

    @Test
    //@Category(JavaFXGUITests.class) //Doesn't work, but should (see pom.xml
    //for details
    @Ignore //needs initialized JavaFX which can't be initialize in a headless
    //environment
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
