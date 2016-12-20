/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package richtercloud.document.scanner.gui.scanresult;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import richtercloud.document.scanner.ifaces.ImageWrapper;

/**
 *
 * @author richter
 */
public class DocumentViewPane extends ImageViewPane {
    private final List<ImageWrapper> imageWrappers;

    public DocumentViewPane(int imageWidth,
            int imageHeight) {
        super(imageWidth,
                imageHeight);
        this.imageWrappers = new LinkedList<>();
    }

    public DocumentViewPane(ImageWrapper scanResult,
            int imageWidth) throws IOException {
        super(scanResult,
                imageWidth);
        this.imageWrappers = new LinkedList<>(Arrays.asList(scanResult));
    }

    public DocumentViewPane(List<ImageWrapper> scanResults,
            int imageWidth) throws IOException {
        super(scanResults.get(0), imageWidth);
        this.imageWrappers = scanResults;
    }

    public List<ImageWrapper> getImageWrappers() {
        return imageWrappers;
    }

    @Override
    protected ImageWrapper getTopMostImageWrapper() {
        if(imageWrappers.isEmpty()) {
            return null;
        }
        return imageWrappers.get(0);
    }

    /**
     * Need a method to be able to add original image (scan results)
     * @param scanResult
     */
    public void addScanResult(ImageWrapper scanResultImageView,
            int imageWidth) throws IOException {
        this.imageWrappers.add(scanResultImageView);
        Image newImage = scanResultImageView.getImagePreviewFX(imageWidth);
        this.getImageView().setImage(newImage);
        GridPane.setMargin(this.getImageView(), new Insets(5));
        getChildren().clear();
        getChildren().add(this.getImageView());
        StackPane.setMargin(this.getImageView(), new Insets(2, 2, 2, 2));
        if (this.imageWrappers.size() > 1) {
            Text numberText = new Text(String.valueOf(this.imageWrappers.size()));
            getChildren().add(numberText);
        }
        this.setPadding(Insets.EMPTY);
        this.setWidth(newImage.getWidth());
        this.setHeight(newImage.getHeight());
            //set width and height based on image because that avoids struggling
            //with rotation
    }
}
