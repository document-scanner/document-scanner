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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;

/**
 * Uses {@link ImageIcon}s to deserialize byte arrays coming out of database
 * storage.
 *
 * @see MainPanelScanResultPanelFetcher
 * @author richter
 */
public class MainPanelScanResultPanelRecreator implements ScanResultPanelRecreator {

    /**
     * licensed CC-by-SA from http://stackoverflow.com/questions/13605248/java-converting-image-to-bufferedimage
     * @param img
     * @return
     */
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    @Override
    public List<BufferedImage> recreate(byte[] data) {
        List<BufferedImage> retValue = new LinkedList<>();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            List<ImageIcon> imageIcons = (List<ImageIcon>) objectInputStream.readObject();
            assert imageIcons != null;
            for(ImageIcon imageIcon : imageIcons) {
                BufferedImage image = toBufferedImage(imageIcon.getImage());
                retValue.add(image);
            }
            //byteArrayInputStream.close not necessary because it's a
            //ByteArrayInputStream
        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        return retValue;
    }
}
