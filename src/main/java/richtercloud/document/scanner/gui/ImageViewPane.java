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
package richtercloud.document.scanner.gui;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * A wrapper around {@link ImageView} which provides a {@link Pane} to be
 * able to set a border around (to show selection). {@link MouseEvent}
 * listeners have to be added to the contained {@link ImageView}.
 *
 * Image scaling routine for {@code scanResult} inspired by
 * http://stackoverflow.com/a/4216635/1797006 licensed under cc by-sa 3.0
 * with attribution required.
 *
 * Don't provide a pane for multiple images with an offset arragement
 * because it doesn't scale (e.g. 1000 image would require 10000 pixel) or
 * scaling is hard to program. Instead provide the topmost image and a
 * number in the lower right korner which is displayed if more than one
 * image is added (which makes it more reusable).
 */
/*
internal implementation notes:
- extending BorderPane instead of Pane causes border to be not changed
 */
public class ImageViewPane extends GridPane {
    /**
     * The topmost {@link ImageView}.
     */
    private OriginalImageView imageView;
    private final List<BufferedImage> scanResults = new LinkedList<>();
    private float zoomLevel = 1.0f;

    public ImageViewPane(int imageWidth, int imageHeight) {
        this();
        addScanResult(new OriginalImageView(new WritableImage(imageWidth, imageHeight)));
    }

    private ImageViewPane() {
        this.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }

    /**
     * Creates a {@code ImageViewPane} containing {@code scanResult} on top
     * which is scaled down first.
     * @param scanResult if {@code null} creates an empty {@link ImageView}
     * which allows it to be selectable
     * @param imageWidth the preferred width of the image which is used to
     * calculate the height while preserving width-height ratio
     */
    public ImageViewPane(BufferedImage scanResult, int imageWidth) {
        this();
        addScanResult(scanResult, imageWidth);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public List<BufferedImage> getScanResults() {
        return scanResults;
    }

    public void changeZoom(float zoomLevel) {
        imageView.setFitWidth(imageView.getFitWidth()/this.zoomLevel*zoomLevel);
        this.zoomLevel = zoomLevel;
    }

    private void addScanResult(OriginalImageView scanResultImageView) {
        this.imageView = scanResultImageView;
        GridPane.setMargin(this.imageView, new Insets(5));
        getChildren().clear();
        getChildren().add(this.imageView);
        StackPane.setMargin(this.imageView, new Insets(2, 2, 2, 2));
        if (this.scanResults.size() > 1) {
            Text numberText = new Text(String.valueOf(this.scanResults.size()));
            getChildren().add(numberText);
        }
        this.setPadding(Insets.EMPTY);
    }

    /**
     * Need a method to be able to add original image (scan results)
     * @param scanResult
     */
    public void addScanResult(BufferedImage scanResult, int imageWidth) {
        addScanResult(new OriginalImageView(scanResult, imageWidth));
        this.scanResults.add(scanResult);
    }

    private class OriginalImageView extends ImageView {
        private final BufferedImage originalImage;

        OriginalImageView(BufferedImage originalImage,
                int imageWidth) {
            //scale BufferedImage down before passing it to
            //SwingFXUtils.toFXImage because the latter performs badly on large
            //images (which ought to be transformed into small ones anyway)
            super(SwingFXUtils.toFXImage(originalImage,
                    null //wimg
            ));
            this.originalImage = originalImage;
            setPreserveRatio(true);
            setFitWidth(imageWidth);
        }

        OriginalImageView(Image image) {
            super(image);
            this.originalImage = null;
        }

        public BufferedImage getOriginalImage() {
            return originalImage;
        }
    }
}
