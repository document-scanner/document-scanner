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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.ifaces.ProgressButton;

/**
 * A button which allows to track the process which has been started by pressing
 * it.
 *
 * @author richter
 */
public class DefaultProgressButton extends ProgressButton {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultProgressButton.class);
    private final static int INSET_DEFAULT = 5;
    private final static Color PROGRESS_COLOR_DEFAULT = Color.GREEN;
    private float progress = 0;
    private final Insets progressInsets = new Insets(INSET_DEFAULT,
            INSET_DEFAULT,
            INSET_DEFAULT,
            INSET_DEFAULT);
    private final Color progressColor;

    public DefaultProgressButton() {
        this(PROGRESS_COLOR_DEFAULT);
    }

    public DefaultProgressButton(String text) {
        this(PROGRESS_COLOR_DEFAULT,
                text);
    }

    public DefaultProgressButton(Color progressColor) {
        this(progressColor,
                "");
    }

    public DefaultProgressButton(Color progressColor,
            String text) {
        super(text);
        this.progressColor = new Color(progressColor.getRed(),
            progressColor.getGreen(),
            progressColor.getBlue(),
            (int)(255*0.8f));
    }

    @Override
    public float getProgress() {
        return progress;
    }

    /**
     * Sets the progress value of the button. Changes are reflected in the paint
     * method and a {@link #repaint() } is requested. If {@code progress} is
     * {@code 1.0f} the value is reset to {@code 0.0f} because the progress is
     * supposed to be shown during the process whose progress it represents
     * only.
     *
     * @param progress the new progress value
     */
    @Override
    public void setProgress(float progress) {
        if(progress < 0) {
            throw new IllegalArgumentException(String.format("progress has to be >= 0, but was %f", progress));
        }
        if(progress > 1) {
            throw new IllegalArgumentException(String.format("progress has to be <= 1, but was %f", progress));
        }
        LOGGER.trace(String.format("progress: %f", progress));
        this.progress = progress < 1.0f ? progress : 0.0f;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(progressColor);
        int width = (int)((getWidth()-progressInsets.left-progressInsets.right)*progress);
        int height = getHeight()-progressInsets.top-progressInsets.bottom;
        g.fillRect(progressInsets.left,
                progressInsets.top,
                width,
                height);
    }
}
