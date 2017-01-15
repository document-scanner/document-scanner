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

import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneException;
import java.awt.Dimension;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.DocumentScanner;
import richtercloud.document.scanner.ifaces.ImageWrapper;
import richtercloud.message.handler.JavaFXDialogMessageHandler;
import richtercloud.message.handler.Message;

/**
 * Allows associating scan results, i.e. images, with documents which group
 * scan results. Callers can open a document tag for each document.
 *
 * Documents are organized in a {@link DocumentPane}.
 *
 * Zoom function controls both document and scan result zoom since there's no
 * imaginable use case where zoom ought to be applied to only one of them.
 *
 * @author richter
 */
public class ScannerResultDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(ScannerResultDialog.class);
    /**
     * Add scan results in the order of selection.
     */
    public final static int SCAN_RESULT_ADD_MODE_SELECTION_ORDER = 1;
    /**
     * Add scan results in the order of scanning. This makes it impossible to
     * change the order of pages inside a document.
     */
    public final static int SCAN_RESULT_ADD_MODE_SCAN_ORDER = 2;
    private float zoomLevel = 1.0f;
    private float zoomMultiplicator = 0.3f;
    private int initialWidth = 600;
    private int initialHeight = 400;
    /**
     * The current desired with including zoom level.
     */
    private int panelWidth;
    /**
     * The height of empty document panels which are added to {@code leftPane}
     * whose relation can't already be known. Scan result objects' height is
     * calculated based on the image width-height relation and
     * {@code panelWidth}.
     *
     * The default value is one calculated based on {@code panelWidth} and the
     * relation of DIN A4/ISO 216 paper with 210 mm × 297 mm.
     */
    private int panelHeight;
    private int centralPanelPadding = 15;
    private final JFXPanel mainPanel = new JFXPanel();
    private final JButton openButton = new JButton("Open documents");
    private final JButton cancelButton = new JButton("Cancel");
    private final Button addImagesButton = new Button("Add to document");
    /**
     * The result of the dialog. {@code null} indicates that the dialog has been
     * canceled.
     */
    private List<List<ImageWrapper>> sortedDocuments = null;
    private final DocumentPane documentPane;
    private final ScrollPane documentPaneScrollPane;
    private final SplitPane splitPane;
    /**
     * How selected scan result ought to be added.
     * @see #SCAN_RESULT_ADD_MODE_SCAN_ORDER
     * @see #SCAN_RESULT_ADD_MODE_SELECTION_ORDER
     */
    private int scanResultAddMode = SCAN_RESULT_ADD_MODE_SCAN_ORDER;
    /**
     * A device used to eventually scan more images.
     */
    private final SaneDevice scannerDevice;
    private final File imageWrapperStorageDir;
    private final JavaFXDialogMessageHandler messageHandler;

    public ScannerResultDialog(Window owner,
            List<ImageWrapper> initialScanResultImages,
            int preferredScanResultPanelWidth,
            SaneDevice scannerDevice,
            File imageWrapperStorageDir,
            JavaFXDialogMessageHandler messageHandler) throws IOException {
        super(owner,
                ModalityType.APPLICATION_MODAL);
        this.panelWidth = preferredScanResultPanelWidth;
        this.panelHeight = panelWidth * 297 / 210;
        if(scannerDevice == null) {
            throw new IllegalArgumentException("scannerDevice mustn't be null");
        }
        this.scannerDevice = scannerDevice;
        if(imageWrapperStorageDir == null) {
            throw new IllegalArgumentException("imageWrapperStorageDir mustn't be null");
        }
        this.imageWrapperStorageDir = imageWrapperStorageDir;
        if(messageHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.messageHandler = messageHandler;

        mainPanel.setPreferredSize(new Dimension(initialWidth, initialHeight));

        setTitle(DocumentScanner.generateApplicationWindowTitle("Scanner result",
                DocumentScanner.APP_NAME,
                DocumentScanner.APP_VERSION));

        this.documentPane = new DocumentPane();
        this.documentPaneScrollPane = new ScrollPane(documentPane);
        documentPaneScrollPane.setFitToWidth(true);
        this.splitPane = new SplitPane();
        Platform.runLater(() -> {
            // Create the username and password labels and fields.
            documentPane.setHgap(10);
            documentPane.setVgap(10);

            splitPane.setOrientation(Orientation.HORIZONTAL);
            ScanResultPane scanResultPane = new ScanResultPane(Orientation.HORIZONTAL,
                        //orientation needs to be horizontal for
                        //scanResultPaneScrollPane.setFitToWidth(true) to have
                        //any effect
                    centralPanelPadding,
                    centralPanelPadding);
            ScrollPane scanResultPaneScrollPane = new ScrollPane(scanResultPane);
            scanResultPaneScrollPane.setFitToWidth(true);
            scanResultPane.setPadding(new Insets(10));
            BorderPane leftPane = new BorderPane();
                //GridPane doesn't allow sufficient control over resizing
            leftPane.setPadding(new Insets(10));
            Button addDocumentButton = new Button("New document");
            Button removeDocumentButton = new Button("Remove document");
            leftPane.setCenter(documentPaneScrollPane);
            GridPane buttonPaneTop = new GridPane();
            GridPane buttonPaneLeft = new GridPane();
            buttonPaneLeft.setHgap(5);
            buttonPaneLeft.setPadding(new Insets(5));
            buttonPaneLeft.add(addDocumentButton, 0, 0);
            buttonPaneLeft.add(removeDocumentButton, 1, 0);
            buttonPaneLeft.add(addImagesButton, 2, 0);
            leftPane.setBottom(buttonPaneLeft);
            addImagesButton.setDisable(true);
            addDocumentButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    DocumentViewPane addedDocument = addNewDocument(documentPane,
                            panelWidth,
                            panelHeight);
                    //always select the newly added document because chances are
                    //high that the user wants to proceed with the newly added
                    //document
                    handleScanResultSelection(new LinkedList<>(Arrays.asList(addedDocument)),
                            documentPane.getDocumentNodes(),
                            false //enableAddImagesButton
                    );
                    documentPane.setSelectedDocument(addedDocument);
                }
            });
            removeDocumentButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    //enqueue the scan results which were grouped in the document
                    //back into the scan result pane...
                    for(ImageWrapper selectedDocumentScanResult : documentPane.getSelectedDocument().getImageWrappers()) {
                        try {
                            addScanResult(selectedDocumentScanResult,
                                    scanResultPane,
                                    scanResultPane.getSelectedScanResults());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    //...and remove the document
                    documentPane.getChildren().remove(documentPane.getChildren().size()-1);
                }
            });
            addImagesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
                if(documentPane.getChildrenUnmodifiable().isEmpty()) {
                    DocumentViewPane addedDocument = addNewDocument(documentPane,
                            panelWidth,
                            panelHeight);
                    documentPane.setSelectedDocument(addedDocument);
                    addedDocument.setSelected(true);
                }
                assert documentPane.getSelectedDocument() != null;
                assert documentPane.containsDocumentNode(documentPane.getSelectedDocument());
                List<ScanResultViewPane> selectedScanResults;
                assert scanResultAddMode == SCAN_RESULT_ADD_MODE_SCAN_ORDER
                        || scanResultAddMode == SCAN_RESULT_ADD_MODE_SELECTION_ORDER;
                if(scanResultAddMode == SCAN_RESULT_ADD_MODE_SELECTION_ORDER) {
                    selectedScanResults = scanResultPane.getSelectedScanResults();
                }else if(scanResultAddMode == SCAN_RESULT_ADD_MODE_SCAN_ORDER) {
                    selectedScanResults = new LinkedList<>();
                    for(Node scanResultPaneChild : scanResultPane.getChildrenUnmodifiable()) {
                        if(scanResultPane.getSelectedScanResults().contains(scanResultPaneChild)) {
                            selectedScanResults.add((ScanResultViewPane) scanResultPaneChild);
                        }
                    }
                }else {
                    throw new IllegalStateException(String.format(
                            "scanResultAddMode has to be %d or %d",
                            SCAN_RESULT_ADD_MODE_SCAN_ORDER,
                            SCAN_RESULT_ADD_MODE_SELECTION_ORDER));
                }
                assert !selectedScanResults.isEmpty();
                    //should be avoided by dis- and enabling of addImagesButton
                for(ScanResultViewPane selectedScanResult : selectedScanResults) {
                    //...and add to selected document in document pane
                    try {
                        //ImageViewPanes in the scan result pane only have one
                        //ScanResult
                        documentPane.getSelectedDocument().addScanResult(selectedScanResult.getImageWrapper(),
                                panelWidth);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                int selectedScanResultOffsetX = scanResultPane.getSelectedScanResults().stream().mapToInt((value) -> (int)value.getLayoutX()).min().getAsInt();
                int selectedScanResultOffsetY = scanResultPane.getSelectedScanResults().stream().mapToInt((value) -> (int)value.getLayoutY()).min().getAsInt();
                scanResultPane.removeScanResultPanes(scanResultPane.getSelectedScanResults());
                scanResultPane.getSelectedScanResults().clear();
                scanResultPaneScrollPane.layout();
                //the following assertions make the life easier and there's no
                //assumption that they will change
                assert scanResultPaneScrollPane.getHmax() == 1.0;
                assert scanResultPaneScrollPane.getHmin() == 0.0;
                assert scanResultPaneScrollPane.getVmax() == 1.0;
                assert scanResultPaneScrollPane.getVmin() == 0.0;
                double scanResultPaneScrollPaneHValue = selectedScanResultOffsetX/scanResultPane.getWidth();
                double scanResultPaneScrollPaneVValue = selectedScanResultOffsetY/scanResultPane.getHeight();
                scanResultPaneScrollPane.setHvalue(scanResultPaneScrollPaneHValue);
                scanResultPaneScrollPane.setVvalue(scanResultPaneScrollPaneVValue);
                    //scroll to the beginning of the first added document
                scanResultPaneScrollPane.layout();

                //scanResultPane.getSelectedScanResults() is empty now
                addImagesButton.setDisable(true);
            });

            for(ImageWrapper scanResultImage : initialScanResultImages) {
                try {
                    addScanResult(scanResultImage,
                            scanResultPane,
                            scanResultPane.getSelectedScanResults());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            BorderPane rightPane = new BorderPane();
            rightPane.setCenter(scanResultPaneScrollPane);
            GridPane buttonPaneRight = new GridPane();
            buttonPaneRight.setHgap(5);
            buttonPaneRight.setPadding(new Insets(5));
            Button zoomInButton = new Button("+");
            Button zoomOutButton = new Button("-");
            Button scanMoreButton = new Button("Scan more");
            Button deletePageButton = new Button("Delete page");
            Button selectAllButton = new Button("Select all");
            Button turnRightButton = new Button("Turn right");
            Button turnLeftButton = new Button("Turn left");
            ComboBox scanResultAddModeComboBox = new ComboBox(FXCollections.observableArrayList(
                SCAN_RESULT_ADD_MODE_SCAN_ORDER,
                SCAN_RESULT_ADD_MODE_SELECTION_ORDER
            ));
            scanResultAddModeComboBox.setCellFactory(new Callback<ListView<Integer>, ListCell<Integer>>() {
                @Override
                public ListCell<Integer> call(ListView<Integer> param) {
                    return new ScanResultAddModeComboBoxCell();
                }
            });
            scanResultAddModeComboBox.setButtonCell(new ScanResultAddModeComboBoxCell());
            scanResultAddModeComboBox.valueProperty().setValue(scanResultAddMode);
            scanResultAddModeComboBox.valueProperty().addListener((event) -> {
                scanResultAddMode = (int) scanResultAddModeComboBox.valueProperty().get();
            });
            buttonPaneTop.setHgap(5);
            buttonPaneTop.setPadding(new Insets(5));
            buttonPaneTop.add(zoomInButton,
                    0, //columnIndex
                    0 //rowIndex
            );
            buttonPaneTop.add(zoomOutButton,
                    1, //columnIndex
                    0 //rowIndex
            );
            buttonPaneTop.add(scanMoreButton,
                    2, //columnIndex
                    0 //rowIndex
            );
            buttonPaneRight.add(deletePageButton,
                    2, //columnIndex
                    0 //rowIndex
            );
            buttonPaneRight.add(selectAllButton,
                    3, //columnIndex
                    0 //rowIndex
            );
            buttonPaneRight.add(turnRightButton,
                    4, //columnIndex
                    0 //rowIndex
            );
            buttonPaneRight.add(turnLeftButton,
                    5, //columnIndex
                    0 //rowIndex
            );
            buttonPaneRight.add(scanResultAddModeComboBox,
                    6, //columnIndex
                    0 //rowIndex
            );
            zoomInButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    float oldZoomLevel = ScannerResultDialog.this.zoomLevel;
                    ScannerResultDialog.this.zoomLevel = ScannerResultDialog.this.zoomLevel*(1+ScannerResultDialog.this.zoomMultiplicator);
                    handleZoomChange(scanResultPane.getScanResultPanes(),
                            oldZoomLevel,
                            ScannerResultDialog.this.zoomLevel);
                }
            });
            zoomOutButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    float oldZoomLevel = zoomLevel;
                    ScannerResultDialog.this.zoomLevel = ScannerResultDialog.this.zoomLevel*(1-ScannerResultDialog.this.zoomMultiplicator);
                    handleZoomChange(scanResultPane.getScanResultPanes(),
                            oldZoomLevel,
                            ScannerResultDialog.this.zoomLevel);
                }
            });
            scanMoreButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
                try {
                    List<ImageWrapper> newImages = DocumentScanner.retrieveImages(scannerDevice,
                            this,
                            imageWrapperStorageDir,
                            messageHandler);
                    for(ImageWrapper newImage : newImages) {
                        try {
                            addScanResult(newImage,
                                    scanResultPane,
                                    scanResultPane.getSelectedScanResults());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                } catch (SaneException | IOException ex) {
                    messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
                }
            });
            deletePageButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    scanResultPane.removeScanResultPanes(scanResultPane.getSelectedScanResults());
                }
            });
            selectAllButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    handleScanResultSelection(scanResultPane.getScanResultPanes(),
                            scanResultPane.getScanResultPanes(),
                            true //enableAddImagesButton
                    );
                    scanResultPane.getSelectedScanResults().clear();
                    scanResultPane.getSelectedScanResults().addAll(scanResultPane.getScanResultPanes());
                }
            });
            turnRightButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
                for(ImageViewPane selectedScanResult : scanResultPane.getSelectedScanResults()) {
                    selectedScanResult.turnRight(panelWidth);
                }
            });
            turnLeftButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
                for(ImageViewPane selectedScanResult : scanResultPane.getSelectedScanResults()) {
                    selectedScanResult.turnLeft(panelWidth);
                }
            });
            rightPane.setBottom(buttonPaneRight);
            splitPane.getItems().addAll(leftPane,
                    rightPane);
            BorderPane mainPane = new BorderPane();
            mainPane.setTop(buttonPaneTop);
            mainPane.setCenter(splitPane);
            Scene  scene  =  new  Scene(mainPane, Color.ALICEBLUE);
            //putting splitPane inside a Group causes JFXPanel to be not resized
            //<ref>http://stackoverflow.com/questions/41034366/how-to-provide-javafx-components-in-a-dialog-in-a-swing-application/41047426#41047426</ref>
            mainPanel.setScene(scene);
        });

        GroupLayout layout = new GroupLayout(this.getContentPane());
        setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(mainPanel,
                        0,
                        GroupLayout.PREFERRED_SIZE,
                        Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(cancelButton)
                        .addComponent(openButton)));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(mainPanel,
                        0,
                        GroupLayout.PREFERRED_SIZE,
                        Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup()
                        .addComponent(cancelButton)
                        .addComponent(openButton)));
        pack();

        this.cancelButton.addActionListener((event) -> this.setVisible(false));
        this.openButton.addActionListener((event) -> {
            this.sortedDocuments = new LinkedList<>();
            this.documentPane.getDocumentNodes().forEach((DocumentViewPane imageViewPane) -> this.sortedDocuments.add(imageViewPane.getImageWrappers()));
            this.setVisible(false);
        });
    }

    /**
     *
     * @param documentPane the pane containing the document collection elements
     * @param documentImagePane the currently selected document in
     * {@code documentPane}
     */
    private void handleScanResultSelection(List<? extends ImageViewPane> selectedPanes,
            List<? extends ImageViewPane> toChecks,
            boolean enableAddImagesButton) {
        for(ImageViewPane toCheck : toChecks) {
            if(selectedPanes.contains(toCheck)) {
                toCheck.setSelected(true);
            }else {
                toCheck.setSelected(false);
            }
        }
        if(enableAddImagesButton) {
            addImagesButton.setDisable(false);
        }
    }

    /**
     * The panel width resulting after zoom. Can be used after closing the
     * dialog to store for restauration after application restart.
     *
     * @return the current panel width
     */
    public int getPanelWidth() {
        return panelWidth;
    }

    public List<List<ImageWrapper>> getSortedDocuments() {
        return sortedDocuments;
    }

    private void handleZoomChange(List<? extends ImageViewPane> imageViewPanes,
            float oldZoomLevel,
            float newZoomLevel) {
        int newWidth = (int) (panelWidth/oldZoomLevel*newZoomLevel);
        LOGGER.debug(String.format("resizing from fit width %d to %d after zoom change",
                panelWidth,
                newWidth));
        for(ImageViewPane imageViewPane : imageViewPanes) {
            imageViewPane.changeZoom(newWidth);
        }
        for(ImageViewPane documentNode : documentPane.getDocumentNodes()) {
            documentNode.changeZoom(newWidth);
        }
        panelWidth = newWidth;
        panelHeight = (int)(panelHeight/oldZoomLevel*newZoomLevel);
    }

    private void addScanResult(ImageWrapper scanResult,
            ScanResultPane scanResultPane,
            List<ScanResultViewPane> selectedScanResults) throws IOException {
        ScanResultViewPane scanResultImageViewPane = new ScanResultViewPane(scanResult,
                panelWidth);
        scanResultPane.addScanResultPane(scanResultImageViewPane);
        scanResultImageViewPane.getImageView().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!event.isControlDown()) {
                    selectedScanResults.clear();
                }
                selectedScanResults.add(scanResultImageViewPane);
                handleScanResultSelection(selectedScanResults,
                        scanResultPane.getScanResultPanes(),
                        true //enableAddImagesButton
                );
            }
        });
    }

    /**
     * Adds a document node (in form of a {@link ImageViewPane}) to be displayed
     * in the document pane.
     * @param centralPane
     * @param documentPane
     * @param panelWidth
     * @param panelHeight
     * @param selectedDocument
     * @return the added document image pane
     */
    private DocumentViewPane addNewDocument(DocumentPane documentPane,
            int panelWidth,
            int panelHeight) {
        DocumentViewPane retValue = new DocumentViewPane(panelWidth,
                panelHeight
        );
        retValue.addEventHandler(MouseEvent.MOUSE_CLICKED,
            (MouseEvent event) -> {
                handleScanResultSelection(new LinkedList<>(Arrays.asList(retValue)),
                        documentPane.getDocumentNodes(),
                        false //enableAddImagesButton (no need to enable if a
                            //document is selected)
                );
                documentPane.setSelectedDocument(retValue);
            });
            //if ImageViewPane is created with empty WritableImage, the listener
            //has to be added to the containing pane rather than the ImageView
        documentPane.addDocumentNode(retValue);
        //scroll
        //the following assertions make the life easier and there's no
        //assumption that they will change
        assert documentPaneScrollPane.getHmax() == 1.0;
        assert documentPaneScrollPane.getHmin() == 0.0;
        assert documentPaneScrollPane.getVmax() == 1.0;
        assert documentPaneScrollPane.getVmin() == 0.0;
        documentPaneScrollPane.layout();
            //documentPane.layout causes documentPaneScrollPane.setVvalue to
            //have no effect
        //there's supposed to be no horizontal scrolling in documentPane
        documentPaneScrollPane.setVvalue(1.0);
            //KISS: the panel is always added at the end of documentPane, so
            //always scroll
        return retValue;
    }

    /**
     * Can't use the same instance in cell factory and for button cell.
     */
    private class ScanResultAddModeComboBoxCell extends ListCell<Integer> {
        @Override
        public void updateItem(Integer item,
                boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                if(item == SCAN_RESULT_ADD_MODE_SCAN_ORDER) {
                    setText("scan order (ignore order of selection)");
                }else if(item == SCAN_RESULT_ADD_MODE_SELECTION_ORDER) {
                    setText("selection order");
                }else {
                    throw new IllegalStateException();
                }
            } else {
                setText(null);
            }
        }
    }
}
