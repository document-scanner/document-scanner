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

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;

/**
 * Allows associating scan results, i.e. images, with documents which group
 * scan results. Callers can open a document tag for each document.
 * @author richter
 */
public class ScannerResultDialog extends Dialog<List<List<BufferedImage>>> {
    private final static Logger LOGGER = LoggerFactory.getLogger(ScannerResultDialog.class);
    private final static Border BORDER_UNSELECTED = new Border(new BorderStroke(Color.BLACK,
            BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY,
            BorderWidths.DEFAULT));
    private final static Border BORDER_SELECTED = new Border(new BorderStroke(Color.RED,
            BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY,
            BorderWidths.DEFAULT));
    /**
     *
     * @param documentPane the pane containing the document collection elements
     * @param documentImagePane the currently selected document in
     * {@code documentPane}
     */
    private static void handleScanResultSelection(List<ImageViewPane> selectedPanes,
            List<ImageViewPane> toChecks) {
        for(Node toCheck : toChecks) {
            Pane toCheckPane = (Pane) toCheck;
            if(selectedPanes.contains(toCheckPane)) {
                toCheckPane.setBorder(BORDER_SELECTED);
            }else {
                toCheckPane.setBorder(BORDER_UNSELECTED);
            }
        }
    }
    private float zoomLevel = 1.0f;
    private float zoomMultiplicator = 0.3f;
    private int initialWidth = 600;
    private int initialHeight = 400;
    private final int panelWidth;
    /**
     * The height of empty document panels which are added to {@code leftPane}
     * whose relation can't already be known. Scan result objects' height is
     * calculated based on the image width-height relation and
     * {@code panelWidth}.
     *
     * The default value is one calculated based on {@code panelWidth} and the
     * relation of DIN A4/ISO 216 paper with 210 mm × 297 mm.
     */
    private final int panelHeight;
    private int centralPanelPadding = 15;

    public ScannerResultDialog(List<BufferedImage> scanResultImages,
            DocumentScannerConf documentScannerConf) {
        this.panelWidth = documentScannerConf.getPreferredWidth();
        this.panelHeight = panelWidth * 297 / 210;
        setResizable(true);
        getDialogPane().setPrefSize(initialWidth, initialHeight);

        setTitle(DocumentScanner.generateApplicationWindowTitle("Scanner result",
                DocumentScanner.APP_NAME,
                DocumentScanner.APP_VERSION));
        setHeaderText("Sort scanned pages into documents");

        // Set the icon (must be included in the project).
        //setGraphic(new ImageView(this.getClass().getResource("login.png").toString()));

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Open documents", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        DocumentPane documentPane = new DocumentPane();
        documentPane.setHgap(10);
        documentPane.setVgap(10);
        documentPane.setPadding(new Insets(10, 10, 10, 10));
        documentPane.setPrefHeight(Short.MAX_VALUE);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        ScanResultPane scanResultPane = new ScanResultPane(Orientation.VERTICAL,
                centralPanelPadding,
                centralPanelPadding);
        ScrollPane scanResultPaneScrollPane = new ScrollPane(scanResultPane);
        scanResultPaneScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scanResultPane.setPadding(new Insets(10));
        BorderPane leftPane = new BorderPane();
            //GridPane doesn't allow sufficient control over resizing
        leftPane.setPadding(new Insets(10));
        Button addDocumentButton = new Button("New document");
        Button removeDocumentButton = new Button("Remove document");
        Button addImagesButton = new Button("Add to document");
        ScrollPane documentPaneScrollPane = new ScrollPane(documentPane);
        leftPane.setCenter(documentPaneScrollPane);
        GridPane buttonPaneLeft = new GridPane();
        buttonPaneLeft.setHgap(5);
        buttonPaneLeft.setPadding(new Insets(5));
        buttonPaneLeft.add(addDocumentButton, 0, 0);
        buttonPaneLeft.add(removeDocumentButton, 1, 0);
        buttonPaneLeft.add(addImagesButton, 2, 0);
        leftPane.setBottom(buttonPaneLeft);
        ReturnValue<ImageViewPane> selectedDocument = new ReturnValue<>();
            //Shouldn't be a ReturnValue<ScanResult> because that requires
            //searching in ScanResults of ImageViewPanes
        List<ImageViewPane> selectedScanResults = new LinkedList<>();
        addDocumentButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ImageViewPane addedDocument = addNewDocument(scanResultPane,
                        documentPane,
                        panelWidth,
                        panelHeight,
                        selectedDocument);
                //always select the newly added document because chances are
                //high that the user wants to proceed with the newly added
                //document
                handleScanResultSelection(new LinkedList<>(Arrays.asList(addedDocument)),
                        documentPane.getDocumentNodes());
                selectedDocument.setValue(addedDocument);
            }
        });
        removeDocumentButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                //enqueue the scan results which were grouped in the document
                //back into the scan result pane...
                for(BufferedImage selectedDocumentScanResult : selectedDocument.getValue().getScanResults()) {
                    addScanResult(selectedDocumentScanResult, scanResultPane, selectedScanResults);
                }
                //...and remove the document
                documentPane.getChildren().remove(documentPane.getChildren().size()-1);
            }
        });
        addImagesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            if(documentPane.getChildrenUnmodifiable().isEmpty()) {
                ImageViewPane addedDocument = addNewDocument(scanResultPane,
                        documentPane,
                        panelWidth,
                        panelHeight,
                        selectedDocument);
                selectedDocument.setValue(addedDocument);
            }
            assert selectedDocument.getValue() != null;
            assert documentPane.containsDocumentNode(selectedDocument.getValue());
            for(ImageViewPane selectedScanResult : selectedScanResults) {
                assert selectedDocument.getValue() != null;
                //...and add to selected document in document pane
                assert selectedScanResult.getScanResults().size() == 1;
                    //ImageViewPanes in the scan result pane only have one
                    //ScanResult
                selectedDocument.getValue().addScanResult(selectedScanResult.getScanResults().get(0),
                        panelWidth);
            }
            scanResultPane.removeScanResultPanes(selectedScanResults);
            selectedScanResults.clear();
        });

        for(BufferedImage scanResultImage : scanResultImages) {
            addScanResult(scanResultImage,
                    scanResultPane,
                    selectedScanResults);
        }

        BorderPane rightPane = new BorderPane();
        rightPane.setCenter(scanResultPaneScrollPane);
        GridPane buttonPaneRight = new GridPane();
        buttonPaneRight.setHgap(5);
        buttonPaneRight.setPadding(new Insets(5));
        Button zoomInButton = new Button("+");
        Button zoomOutButton = new Button("-");
        Button deletePageButton = new Button("Delete page");
        Button selectAllButton = new Button("Select all");
        buttonPaneRight.add(zoomInButton,
                0, //columnIndex
                0 //rowIndex
        );
        buttonPaneRight.add(zoomOutButton,
                1, //columnIndex
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
        deletePageButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                scanResultPane.removeScanResultPanes(selectedScanResults);
            }
        });
        selectAllButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleScanResultSelection(scanResultPane.getScanResultPanes(),
                        scanResultPane.getScanResultPanes());
                selectedScanResults.clear();
                selectedScanResults.addAll(scanResultPane.getScanResultPanes());
            }
        });
        rightPane.setBottom(buttonPaneRight);
        splitPane.getItems().addAll(leftPane,
                rightPane);
        getDialogPane().setPadding(Insets.EMPTY); //set padding in items of
            //splitPane in order to avoid divider to be displayed with padding
        getDialogPane().setContent(splitPane);
        setResultConverter((ButtonType param) -> {
            if(param.getButtonData().isCancelButton()) {
                return null;
            }
            List<List<BufferedImage>> retValue = new LinkedList<>();
            documentPane.getDocumentNodes().forEach((ImageViewPane imageViewPane) -> retValue.add(imageViewPane.getScanResults()));
            return retValue;
        });
    }

    private void handleZoomChange(List<ImageViewPane> imageViewPanes,
            float oldZoomLevel,
            float newZoomLevel) {
        for(ImageViewPane imageViewPane : imageViewPanes) {
            double newWidth = imageViewPane.getImageView().getFitWidth()/oldZoomLevel*newZoomLevel;
            LOGGER.debug(String.format("resizing from fit width %f to %f after zoom change",
                    imageViewPane.getImageView().getFitWidth(),
                    newWidth));
            imageViewPane.changeZoom(newZoomLevel);
        }
    }

    private void addScanResult(BufferedImage scanResult,
            ScanResultPane scanResultPane,
            List<ImageViewPane> selectedScanResults) {
        ImageViewPane scanResultImageViewPane = new ImageViewPane(scanResult,
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
                        scanResultPane.getScanResultPanes());
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
    private ImageViewPane addNewDocument(Pane centralPane,
            DocumentPane documentPane,
            int panelWidth,
            int panelHeight,
            ReturnValue<ImageViewPane> selectedDocument) {
        ImageViewPane retValue = new ImageViewPane(panelWidth,
                panelHeight
        );
        retValue.addEventHandler(MouseEvent.MOUSE_CLICKED,
            (MouseEvent event) -> {
                handleScanResultSelection(new LinkedList<>(Arrays.asList(retValue)),
                        documentPane.getDocumentNodes());
                selectedDocument.setValue(retValue);
            });
            //if ImageViewPane is created with empty WritableImage, the listener
            //has to be added to the containing pane rather than the ImageView
        documentPane.addDocumentNode(retValue);
        return retValue;
    }

    private class ScanResultPane extends FlowPane {
        private final List<ImageViewPane> scanResultPanes = new LinkedList<>();

        ScanResultPane(Orientation orientation,
                double hgap,
                double vgap) {
            super(orientation, hgap, vgap);
        }

        public void addScanResultPane(ImageViewPane scanResultPane) {
            getChildren().add(scanResultPane);
            scanResultPanes.add(scanResultPane);
        }

        public void removeScanResultPanes(List<ImageViewPane> scanResultPanes) {
            getChildren().removeAll(scanResultPanes);
            this.scanResultPanes.removeAll(scanResultPanes);
        }

        public List<ImageViewPane> getScanResultPanes() {
            return scanResultPanes;
        }
    }
}
