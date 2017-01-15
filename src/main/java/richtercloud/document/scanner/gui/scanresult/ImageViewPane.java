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
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import richtercloud.document.scanner.ifaces.ImageWrapper;

/**
 * A wrapper around {@link ImageView} which provides a {@link Pane} to be
 * able to set a border around (to show selection). It's used in
 * {@link ScanResultPane} to display pages where only one image/page is
 * displayed per result and in {@link DocumentPane} where multiple pages/images
 * are grouped, but only the the topmost image is displayed.
 *
 * {@link MouseEvent} listeners have to be added to the contained
 * {@link ImageView}.
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
- extending BorderPane instead of Pane which causes border to be not changed
 */
public abstract class ImageViewPane extends GridPane {
    private final static Border BORDER_UNSELECTED = new Border(new BorderStroke(Color.BLACK,
            BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY,
            BorderWidths.DEFAULT));
    private final static Border BORDER_SELECTED = new Border(new BorderStroke(Color.RED,
            BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY,
            BorderWidths.DEFAULT));
    /**
     * The topmost {@link ImageView}.
     */
    private final ImageView imageView;

    public ImageViewPane(int imageWidth, int imageHeight) {
        this(new WritableImage(imageWidth, imageHeight));
    }

    /**
     * Creates a {@code ImageViewPane} containing {@code scanResult} on top
     * which is scaled down first.
     * @param scanResult if {@code null} creates an empty {@link ImageView}
     * which allows it to be selectable
     * @param imageWidth the preferred width of the image which is used to
     * calculate the height while preserving width-height ratio
     */
    public ImageViewPane(ImageWrapper scanResult, int imageWidth) throws IOException {
        this(scanResult.getImagePreviewFX(imageWidth));
    }

    private ImageViewPane(WritableImage image) {
        this.imageView = new ImageView(image);
        add(this.imageView,
                0, //columnIndex
                0 //rowIndex
        );
        this.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }

    /**
     * A pointer to the topmost image (or the only image if the implementation
     * only has one topmost image).
     * @return the topmost {@link ImageWrapper} or {@code null} if the
     * {@code ImageViewPane} is empty
     */
    protected abstract ImageWrapper getTopMostImageWrapper();

    public ImageView getImageView() {
        return imageView;
    }

    /**
     * In order to avoid keeping images in memory replace {@code imageWrapper}'s
     * {@code image} property with new preview.
     * @param newWidth the externally calculated width
     */
    public void changeZoom(int newWidth) {
        //Can't set fitWidth on imageView since that causes bad quality image
        //when zooming in -> retrieve "fresh" preview from ImageWrapper
        if(getTopMostImageWrapper() == null) {
            //imageView is empty
            int oldWidth = (int) this.imageView.getImage().getWidth();
            int newHeight = (int) (this.imageView.getImage().getHeight()*newWidth/oldWidth);
            this.imageView.setFitWidth(newWidth);
            this.imageView.setFitHeight(newHeight);
                //need to set fitHeight as well if the ImageView is empty
        }else {
            try {
                this.imageView.setImage(getTopMostImageWrapper().getImagePreviewFX(newWidth));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        int oldWidth = (int) this.getWidth();
        int newHeight = (int) (this.getHeight()*newWidth/oldWidth);
        this.setWidth(newWidth);
        this.setHeight(newHeight);
    }

    private void turn(int rotationDegreesDiff,
            int width) {
        //not necessary to set rotation of ImageView or ImageViewPane because
        //they adjust automatically to the newly set image
        if(getTopMostImageWrapper() == null) {
            this.imageView.setRotate(this.imageView.getRotate()+90);
        }else {
            getTopMostImageWrapper().setRotationDegrees(getTopMostImageWrapper().getRotationDegrees()+rotationDegreesDiff);
            try {
                WritableImage newImage = getTopMostImageWrapper().getImagePreviewFX(width);
                this.imageView.setImage(newImage);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void turnRight(int width) {
        turn(90,
                width);
    }

    public void turnLeft(int width) {
        turn(-90,
                width);
    }

    public void setSelected(boolean selected) {
        if(selected) {
            this.setBorder(BORDER_SELECTED);
        }else {
            this.setBorder(BORDER_UNSELECTED);
        }
    }
}
