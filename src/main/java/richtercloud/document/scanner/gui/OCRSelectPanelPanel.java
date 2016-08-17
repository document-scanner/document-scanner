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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.conf.OCREngineConf;
import richtercloud.document.scanner.ocr.OCREngineFactory;

/**
 * Arranges multiple (or one) images in different selection panel and handles
 * selection on them for OCR.
 *
 * No cross image selection are supported. Starting a selection on one panel
 * removes the selection on another.
 *
 * Auto-OCR-value-detection is handled in this class because it allows handling
 * of values which reach across multiple pages.
 *
 * @author richter
 */
/*
internal implementation notes:
- due to the fact that constructors with List<OCRSelectPanel> and List<BufferedImage>
argument have the same erasure, provide List<OCRSelectPanel> because often
action methods of OCRSelectPanel are adjusted by callers
*/
public class OCRSelectPanelPanel extends JPanel implements Scrollable {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(OCRSelectPanelPanel.class);
    /**
     * The pages of which the drawing area ought to be composed
     */
    /*
    internal implementation notes:
    - initialializing with empty list allows to have zero-argument constructor
    */
    private final List<OCRSelectPanel> oCRSelectPanels = new LinkedList<>();
    private OCRSelectPanel selectedPanel = null;
    private int maxUnitIncrement = 100;
    /**
     * The {@link File} the document is stored in. {@code null} indicates that
     * the document has not been saved yet (e.g. if the
     * {@link OCRSelectComponent} represents scan data).
     */
    private File documentFile;
    private float zoomLevel = 1;

    public OCRSelectPanelPanel(OCRSelectPanel panel,
            File documentFile,
            OCREngineFactory oCREngineFactory,
            OCREngineConf oCREngineConf) {
        this(new LinkedList<>(Arrays.asList(panel)),
                documentFile,
                oCREngineFactory,
                oCREngineConf);
    }

    public OCRSelectPanelPanel(List<OCRSelectPanel> panels,
            File documentFile,
            OCREngineFactory oCREngineFactory,
            OCREngineConf oCREngineConf) {
        this.documentFile = documentFile;
        FlowLayout layout = new FlowLayout(FlowLayout.CENTER, 5, 5);
        this.setLayout(layout);
        for(OCRSelectPanel panel : panels) {
            panel.addSelectionListener(new PanelSelectionListener(panel));
            this.oCRSelectPanels.add(panel);
            this.add(panel);
        }
        updatePreferredSize();
    }

    /**
     * causes pages to be displayed in horizontal arrangement
     */
    private void updatePreferredSize() {
        Dimension newValue;
        if(oCRSelectPanels.isEmpty()) {
            newValue = new Dimension(0, 0);
        }else {
            int preferredWidth = 0, preferredHeight = 0;
            for(OCRSelectPanel panel : oCRSelectPanels) {
                preferredHeight += panel.getImage().getHeight();
                preferredWidth = Math.max(preferredWidth, panel.getImage().getWidth());
            }
            newValue = new Dimension((int)(preferredWidth*zoomLevel),
                    (int)(preferredHeight*zoomLevel));
        }
        this.setPreferredSize(newValue);
    }

    public File getDocumentFile() {
        return documentFile;
    }

    public void setDocumentFile(File documentFile) {
        this.documentFile = documentFile;
    }

    public List<OCRSelectPanel> getoCRSelectPanels() {
        return Collections.unmodifiableList(this.oCRSelectPanels);
    }

    /**
     *
     * @return the selected image or {@code null} if all image panels contain selections with width or height <= 0
     */
    public BufferedImage getSelection() {
        for(OCRSelectPanel panel : this.oCRSelectPanels) {
            if(panel.getDragStart() != null && panel.getDragEnd() != null) {
                int width = panel.dragSelectionWidth();
                if(width <= 0) {
                    //avoid java.awt.image.RasterFormatException: negative or zero height
                    LOGGER.debug(String.format("skipping selection with width %d <= 0", width));
                    continue;
                }
                int height = panel.dragSeletionHeight();
                if(height <= 0) {
                    //avoid java.awt.image.RasterFormatException: negative or zero height
                    LOGGER.debug(String.format("skipping selection with height %d <= 0", height));
                    continue;
                }
                BufferedImage imageSelection = panel.getImage().getSubimage((int)(panel.dragSelectionX()/this.zoomLevel),
                        (int)(panel.dragSelectionY()/this.zoomLevel),
                        (int)(width/this.zoomLevel),
                        (int)(height/this.zoomLevel));
                return imageSelection;
            }
        }
        return null;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return this.getPreferredSize(); //as suggested by
            //https://docs.oracle.com/javase/8/docs/api/javax/swing/Scrollable.html
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        //Get the current position.
        int currentPosition;
        if (orientation == SwingConstants.HORIZONTAL) {
            currentPosition = visibleRect.x;
        } else {
            currentPosition = visibleRect.y;
        }

        //Return the number of pixels between currentPosition
        //and the nearest tick mark in the indicated direction.
        if (direction < 0) {
            int newPosition = currentPosition -
                             (currentPosition / this.maxUnitIncrement)
                              * this.maxUnitIncrement;
            return (newPosition == 0) ? this.maxUnitIncrement : newPosition;
        } else {
            return ((currentPosition / this.maxUnitIncrement) + 1)
                     * this.maxUnitIncrement
                     - currentPosition;
        }
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return visibleRect.width - this.maxUnitIncrement;
        } else {
            return visibleRect.height - this.maxUnitIncrement;
        }
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }


    /**
     * Sets the zoom level {@code zoomLevel} on all {@link OCRSelectPanel}.
     * @param zoomLevel the zoom level
     */
    public void setZoomLevels(float zoomLevel) {
        this.zoomLevel = zoomLevel; //before updatePreferredSize
        for(OCRSelectPanel oCRSelectPanel : this.oCRSelectPanels) {
            oCRSelectPanel.setZoomLevel(zoomLevel);
        }
        updatePreferredSize();
    }

    private class PanelSelectionListener implements OCRSelectPanelSelectionListener {
        private final OCRSelectPanel panel;

        PanelSelectionListener(OCRSelectPanel panel) {
            this.panel = panel;
        }

        @Override
        public void selectionChanged() {
            OCRSelectPanelPanel.this.selectedPanel = this.panel;
            for(OCRSelectPanel panel0: OCRSelectPanelPanel.this.oCRSelectPanels) {
                if(!panel0.equals(OCRSelectPanelPanel.this.selectedPanel)) {
                    panel0.unselect();
                }
            }
        }
    }
}
