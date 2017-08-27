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
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;

/**
 * The pane which contains the documents containing the pages sorted
 * together to a document. Documents are represented by {@link ImageViewPane}s
 * in {@code documentNodes}.
 *
 * @author richter
 */
public class DocumentPane extends FlowPane {
    private final List<DocumentViewPane> documentNodes = new LinkedList<>();
    private DocumentViewPane selectedDocument;

    public DocumentPane() {
        super(Orientation.HORIZONTAL);
    }

    public void addScanResults(List<ScanResultViewPane> scanResults,
            int imageWidth) throws IOException {
        DocumentViewPane newNode = new DocumentViewPane(scanResults.stream().map(p -> p.getImageWrapper()).collect(Collectors.toList()),
                imageWidth);
        getChildren().add(newNode);
        documentNodes.add(newNode);
    }

    public void addDocumentNode(DocumentViewPane node) {
        getChildren().add(getChildrenUnmodifiable().size(), //index
                node
        );
        StackPane.setMargin(node, new Insets(2, 2, 2, 2));
        documentNodes.add(node);
    }

    public List<DocumentViewPane> getDocumentNodes() {
        return documentNodes;
    }

    public boolean containsDocumentNode(DocumentViewPane documentNode) {
        return documentNodes.contains(documentNode);
    }

    public DocumentViewPane getSelectedDocument() {
        return selectedDocument;
    }

    public void setSelectedDocument(DocumentViewPane selectedDocument) {
        this.selectedDocument = selectedDocument;
    }
}
