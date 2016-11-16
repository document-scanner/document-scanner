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

import java.util.LinkedList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

/**
 *
 * @author richter
 */
public class DocumentPane extends GridPane {
    private List<ImageViewPane> documentNodes = new LinkedList<>();

    public void addDocumentNode(ImageViewPane node) {
        add(node,
                0, //columnIndex
                getChildrenUnmodifiable().size() //rowIndex
        );
        StackPane.setMargin(node, new Insets(2, 2, 2, 2));
        documentNodes.add(node);
    }

    public List<ImageViewPane> getDocumentNodes() {
        return documentNodes;
    }
}
