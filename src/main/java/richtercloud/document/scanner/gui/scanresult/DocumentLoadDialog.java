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

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.ifaces.DocumentItem;
import richtercloud.message.handler.ExceptionMessage;
import richtercloud.message.handler.IssueHandler;

/**
 * Allows to select a {@link DocumentItem} passed in constructor (most likely
 * one which is loaded in the main panel) or to cancel the selection.
 *
 * The dialog can't be closed with the window close button because that requires
 * extra implementation in Java FX and there's a cancel button.
 *
 * @author richter
 */
public class DocumentLoadDialog extends Dialog<DocumentViewPane> {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentLoadDialog.class);
    private final Button loadButton;
    private final IssueHandler issuehandler;
    private final int panelWidth;
    private final ListView<DocumentItem> documentItemList = new ListView<>();

    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public DocumentLoadDialog(List<DocumentItem> documentItems,
            int panelWidth,
            IssueHandler issueHandler) {
        super();
        this.initModality(Modality.APPLICATION_MODAL);
        this.panelWidth = panelWidth;
        this.issuehandler = issueHandler;
        ButtonType loadButtonType = new ButtonType("Load", ButtonData.FINISH);
        ButtonType cancelButtonType = ButtonType.CANCEL;
        this.getDialogPane().getButtonTypes().addAll(loadButtonType,
                cancelButtonType);
        this.loadButton = (Button) this.getDialogPane().lookupButton(loadButtonType);
        this.loadButton.setDisable(true);
        documentItemList.getSelectionModel().selectedItemProperty().addListener(observable -> {
            loadButton.setDisable(documentItemList.getSelectionModel().getSelectedItems().isEmpty());
        });
        ObservableList<DocumentItem> items =FXCollections.observableArrayList(documentItems);
        documentItemList.setItems(items);
        documentItemList.setCellFactory(param -> new ListCell<DocumentItem>() {
            @Override
            protected void updateItem(DocumentItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getSelectedFile() == null) {
                    setText(null);
                } else {
                    setText(item.getSelectedFile().getName());
                }
            }
        });
        BorderPane mainPane = new BorderPane(documentItemList);
        this.getDialogPane().setContent(mainPane);
        this.setResultConverter((ButtonType buttonType) -> {
            try {
                if (buttonType == loadButtonType) {
                    assert documentItemList.getSelectionModel().getSelectedItems().size() == 1;
                    DocumentItem selectedDocumentItem = documentItemList.getSelectionModel().getSelectedItem();
                    assert selectedDocumentItem != null;
                    WritableImage topMostScanResultScaled = selectedDocumentItem.getImages().get(0).getImagePreviewFX(this.panelWidth);
                    if(topMostScanResultScaled == null) {
                        //cache has been shut down
                        return null;
                    }
                    DocumentViewPane loadedDocument = new DocumentViewPane(topMostScanResultScaled,
                            selectedDocumentItem);
                    return loadedDocument;
                }
            }catch(Throwable ex) {
                LOGGER.error("unexpected exception during laoding of document",
                        ex);
                DocumentLoadDialog.this.issuehandler.handleUnexpectedException(new ExceptionMessage(ex));
            }
            return null;
        });
    }
}
