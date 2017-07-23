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
package richtercloud.document.scanner.gui.scanresult;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import richtercloud.document.scanner.ifaces.ImageWrapper;

/**
 * Base class for a collection of PDF document or scaned pages.
 *
 * @author richter
 */
public class DocumentJob {
    private final List<ImageWrapper> images;
    private boolean finished = true;
    /**
     * The number of the job which was the state of the job counter of the
     * {@link richtercloud.document.scanner.gui.scanresult.DocumentController}
     * when the job was added to it.
     */
    /*
    internal implementation notes:
    - @TODO: make immutable
    */
    private final int jobNumber;

    /**
     * Creates a new document job. This is supposed to be called with a valid
     * job number assigned from {@link DocumentController}.
     *
     * @param images the images of the job
     * @param jobNumber the job number from the {@link DocumentController}
     */
    protected DocumentJob(List<ImageWrapper> images,
            int jobNumber) {
        this.images = images;
        this.jobNumber = jobNumber;
    }

    /**
     * This is used by subclasses which don't want the job to be
     * {@code finished} immediately.
     *
     * @param finished only {@code false} makes sense here
     * @param jobNumber the job number from the {@link DocumentController}
     */
    protected DocumentJob(boolean finished,
            int jobNumber) {
        this.finished = finished;
        this.images = new LinkedList<>();
        this.jobNumber = jobNumber;
    }

    public int getJobNumber() {
        return jobNumber;
    }

    protected List<ImageWrapper> getImages() {
        return images;
    }

    /**
     * An unmodifiable view of the images of this document job. Is unmodifiable
     * in order to ensure that the original state of the job can be compared.
     *
     * @return the unmodifable view of the list of images of this job
     */
    public List<ImageWrapper> getImagesUnmodifiable() {
        return Collections.unmodifiableList(images);
    }

    public boolean isFinished() {
        return finished;
    }

    protected void setFinished(boolean finished) {
        this.finished = finished;
    }
}
