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

import javafx.scene.control.ToggleButton;

/**
 *
 * @author richter
 */
public class DocumentJobToggleButton extends ToggleButton {
    private DocumentJob documentJob;

    /**
     * Creates a new document job toggle button.
     *
     * @param documentJob the document job of the toggle button
     */
    public DocumentJobToggleButton(DocumentJob documentJob) {
        super(String.format("#%d",
                documentJob.getJobNumber()));
        this.documentJob = documentJob;
    }

    public DocumentJob getDocumentJob() {
        return documentJob;
    }

    public void setDocumentJob(DocumentJob documentJob) {
        this.documentJob = documentJob;
    }
}
