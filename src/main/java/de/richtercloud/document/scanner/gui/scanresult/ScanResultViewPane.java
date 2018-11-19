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
package de.richtercloud.document.scanner.gui.scanresult;

import javafx.scene.image.WritableImage;
import de.richtercloud.document.scanner.ifaces.ImageWrapper;
import de.richtercloud.document.scanner.ifaces.ImageWrapperException;

/**
 * Wraps an {@link ImageWrapper} and uses selection highlighting and zoom code
 * from {@link ImageViewPane}.
 *
 * @author richter
 */
public class ScanResultViewPane extends ImageViewPane {
    private final ImageWrapper imageWrapper;

    public ScanResultViewPane(ImageWrapper scanResult,
            WritableImage scanResultScaled) throws ImageWrapperException {
        super(scanResultScaled);
        this.imageWrapper = scanResult;
    }

    public ImageWrapper getImageWrapper() {
        return imageWrapper;
    }

    @Override
    protected ImageWrapper getTopMostImageWrapper() {
        return this.imageWrapper;
    }
}
