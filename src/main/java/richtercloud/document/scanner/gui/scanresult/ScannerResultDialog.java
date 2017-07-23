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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.Constants;
import richtercloud.document.scanner.gui.DocumentScanner;
import richtercloud.document.scanner.gui.DocumentSourceOptionMissingException;
import richtercloud.document.scanner.gui.Tools;
import richtercloud.document.scanner.gui.scanner.DocumentSource;
import richtercloud.document.scanner.ifaces.ImageWrapper;
import richtercloud.message.handler.BugHandler;
import richtercloud.message.handler.ExceptionMessage;
import richtercloud.message.handler.JavaFXDialogMessageHandler;
import richtercloud.message.handler.Message;

/**
 * Allows associating scan results, i.e. images, with documents which group
 * scan results. Callers can open a document tag for each document.
 *
 * Availble images come from document jobs which are listed in the form of
 * toggle buttons whose activation triggers displaying in the list of available
 * images. This allows to restore overview if a larger set of scan jobs is
 * handled. Once all pages of a scan job have been added to a document, it is
 * automatically removed and images are only moved between the image selection
 * and the document pane in case the image ever is moved back to the image
 * selection pane after removing a document.
 *
 * Documents are organized in a {@link DocumentPane}.
 *
 * Zoom function controls both document and scan result zoom since there's no
 * imaginable use case where zoom ought to be applied to only one of them.
 *
 * @author richter
 */
/*
internal implementation notes:
- @TODO: The toggle buttons for toggling displaying of scan results of each
document job should be disabled as long as the scan is running, but the
re-enabling with ToggleButton.setDisable(false) doesn't work for unknown reasons
although performed on the JavaFX thread with Platform.runLater (search for
`setDisable` in order to investigate; see
https://github.com/krichter722/javafx-toggle-button-demo for a draft for a MCVE
which doesn't exhibit the issue yet -> maybe better to scale down from large
application)
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
    private final BugHandler bugHandler;
    private final Window openDocumentWaitDialogParent;
    private final DocumentController documentController;
    private Map<DocumentJob, List<ImageWrapper>> documentJobImageMapping = new HashMap<>();
    private Map<DocumentJob, DocumentJobToggleButton> documentJobToggleButtonMapping = new HashMap<>();

    /**
     *
     * @param owner
     * @param initialScanResultImages
     * @param preferredScanResultPanelWidth
     * @param scannerDevice the scanner device used in the "Scan more" function
     * (can be {@code null} initially, because a scanner might not be set up and
     * only PDFs be opened, which then causes a warning to be displayed when the
     * "Scan more" button is pressed)
     * @param imageWrapperStorageDir
     * @param messageHandler
     * @param bugHandler
     * @param openDocumentWaitDialogParent
     * @throws IOException
     */
    public ScannerResultDialog(Window owner,
            int preferredScanResultPanelWidth,
            DocumentController documentController,
            SaneDevice scannerDevice,
            File imageWrapperStorageDir,
            JavaFXDialogMessageHandler messageHandler,
            BugHandler bugHandler,
            Window openDocumentWaitDialogParent) throws IOException {
        super(owner,
                ModalityType.APPLICATION_MODAL);
        this.openDocumentWaitDialogParent = openDocumentWaitDialogParent;
        this.panelWidth = preferredScanResultPanelWidth;
        this.panelHeight = panelWidth * 297 / 210;
        this.scannerDevice = scannerDevice;
        if(documentController == null) {
            throw new IllegalArgumentException("documentController mustn't be null");
        }
        this.documentController = documentController;
        if(imageWrapperStorageDir == null) {
            throw new IllegalArgumentException("imageWrapperStorageDir mustn't be null");
        }
        this.imageWrapperStorageDir = imageWrapperStorageDir;
        if(messageHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.messageHandler = messageHandler;
        this.bugHandler = bugHandler;

        mainPanel.setPreferredSize(new Dimension(initialWidth, initialHeight));

        setTitle(DocumentScanner.generateApplicationWindowTitle("Scanner result",
                Constants.APP_NAME,
                Constants.APP_VERSION));

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
            FlowPane documentJobPane = new FlowPane();
            documentJobPane.setOrientation(Orientation.VERTICAL);
            documentJobPane.setColumnHalignment(HPos.RIGHT);
            documentJobPane.setPrefHeight(10);
            documentJobPane.setVgap(5);
            documentJobPane.setHgap(5);
                //change of orientation seems to change sense of vgap and hgap
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
                    List<ImageWrapper> selectedDocumentImageWrappers = documentPane.getSelectedDocument().getImageWrappers();
                    for(ImageWrapper selectedDocumentScanResult : selectedDocumentImageWrappers) {
                        try {
                            addScanResult(selectedDocumentScanResult,
                                    scanResultPane,
                                    scanResultPane.getSelectedScanResults());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    //...remove the document...
                    documentPane.getChildren().remove(documentPane.getChildren().size()-1);
                    //...and create a new scan job for them in order to increase
                    //overview
                    DocumentJob documentJob = documentController.addDocumentJob(selectedDocumentImageWrappers);
                    addDocumentJobToggleButton(documentJob,
                            documentJobPane,
                            scanResultPane);
                }
            });
            addImagesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
                try {
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

                    //handle removal of empty document jobs
                    ListIterator<DocumentJob> documentJobItr = documentController.getDocumentJobs().listIterator();
                    while(documentJobItr.hasNext()) {
                        DocumentJob documentJob = documentJobItr.next();
                        if(documentJob.getImagesUnmodifiable().stream().allMatch(imageWrapper -> documentPane.getDocumentNodes().stream().map(a -> a.getImageWrappers()).anyMatch(b -> b.contains(imageWrapper)))) {
                            documentJobItr.remove();
                            DocumentJobToggleButton documentJobToggleButton = documentJobToggleButtonMapping.remove(documentJob);
                            assert documentJobToggleButton != null;
                            documentJobPane.getChildren().remove(documentJobToggleButton);
                        }
                    }
                }catch(Throwable ex) {
                    LOGGER.error("an unexpected exception during adding of images occured", ex);
                    this.bugHandler.handleUnexpectedException(new ExceptionMessage(ex));
                }
            });

            BorderPane rightPane = new BorderPane();
            rightPane.setCenter(scanResultPaneScrollPane);
            GridPane buttonPaneRight = new GridPane();
            buttonPaneRight.setHgap(5);
            buttonPaneRight.setPadding(new Insets(5));
            Button zoomInButton = new Button("+");
            Button zoomOutButton = new Button("-");
            Button scanMoreButton = new Button("Scan more");
            Button openDocumentButton = new Button("Open scan");
            Label scanResultsLabel = new Label("Scan results: ");
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
            buttonPaneTop.add(openDocumentButton,
                    3, //columnIndex
                    0 //rowIndex
            );
            buttonPaneTop.add(scanResultsLabel,
                    4, //columnIndex
                    0 //rowIndex
            );
            buttonPaneTop.add(documentJobPane,
                    5, //columnIndex
                    0 //rowIndex
            );
            for(DocumentJob documentJob : documentController.getDocumentJobs()) {
                addDocumentJobToggleButton(documentJob,
                        documentJobPane,
                        scanResultPane);
                handleDocumentJobToggleButtonPressed(documentJob,
                        scanResultPane);
                    //- handling event firing programmatically is painful
                    //because the event handler invoked from
                    //documentJobToggleButton.fireEvent doesn't reconize
                    //that the button ought to be pressed after
                    //documentJobToggleButton.fire
                    //- addDocumentJob doesn't add to scanResultPane because
                    //that already happens if a document is removed so that code
                    //reusage is improved
            }
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
                    if(this.scannerDevice == null) {
                        //this is allowed since the dialog ought to be usable to
                        //display opened PDFs without a set up scanner
                        this.messageHandler.handle(new Message("No scanner has been selected and configured yet",
                                JOptionPane.ERROR_MESSAGE,
                                "No scanner selected and configured"));
                        return;
                    }
                    Pair<DocumentSource, Integer> documentSourcePair = DocumentScanner.determineDocumentSource(this.documentController,
                            this.scannerDevice,
                            this);
                    ScanJob scanJob = documentController.addScanJob(this.documentController,
                            this.scannerDevice,
                            documentSourcePair.getKey(),
                            this.imageWrapperStorageDir,
                            documentSourcePair.getValue(),
                            this.messageHandler);
                    final DocumentJobToggleButton scanJobToggleButton = new DocumentJobToggleButton(scanJob //scanJob
                    );
                    ScanJobFinishCallback scanJobFinishCallback = imagesUnmodifiable -> {
                        Platform.runLater(() -> {
                            if(imagesUnmodifiable == null) {
                                //dialog has been canceled
                                buttonPaneTop.getChildren().remove(scanJobToggleButton);
                                    //remove toggle button for canceled job
                                return;
                            }
                            scanJobToggleButton.setDisable(false);
                                //doesn't work, see internal implementation notes of class for
                                //details
                            scanJobToggleButton.setSelected(true);
                                //doesn't work neither
                            for(ImageWrapper newImage : imagesUnmodifiable) {
                                try {
                                    addScanResult(newImage,
                                            scanResultPane,
                                            scanResultPane.getSelectedScanResults());
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        });
                    };
                    scanJob.setFinishCallback(scanJobFinishCallback);
                        //callback can be set safely until thread or other
                        //executor isn't started
                    scanJobToggleButton.setDocumentJob(scanJob);
                    Thread scanJobThread = new Thread(scanJob,
                            String.format("scan-job-thread-%d",
                                    documentController.getDocumentJobCount().intValue()+1));
                    addDocumentJobToggleButton(scanJob,
                            documentJobPane,
                            scanResultPane);
                    scanJobToggleButton.setDisable(true);
                        //re-enabling doesn't work, see internal implementation
                        //notes of class for details
                    scanJobThread.start();
                } catch (SaneException | IOException ex) {
                    messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
                } catch(DocumentSourceOptionMissingException ex) {
                    this.bugHandler.handleUnexpectedException(new ExceptionMessage(ex));
                }
            });
            openDocumentButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "PDF files", "pdf");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(this);
                final File selectedFile = chooser.getSelectedFile();
                if (returnVal != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                List<ImageWrapper> newImages;
                FutureTask<List<ImageWrapper>> swingTask = new FutureTask<>(() -> {
                    List<ImageWrapper> images0 = Tools.retrieveImages(selectedFile,
                            this.openDocumentWaitDialogParent,
                            this.imageWrapperStorageDir);
                    return images0;
                });
                SwingUtilities.invokeLater(swingTask);
                try {
                    newImages = swingTask.get();
                } catch (InterruptedException | ExecutionException ex) {
                    messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
                    return;
                }
                if(newImages == null) {
                    LOGGER.debug("image retrieval has been canceled, discontinuing adding document");
                    return;
                }
                for(ImageWrapper newImage : newImages) {
                    try {
                        addScanResult(newImage,
                                scanResultPane,
                                scanResultPane.getSelectedScanResults());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
            deletePageButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    scanResultPane.removeScanResultPanes(scanResultPane.getSelectedScanResults());
                    if(scanResultPane.getSelectedScanResults().isEmpty()) {
                        //is most likely always empty, but adding this simple
                        //check might avoid trouble in the future
                        addImagesButton.setDisable(true);
                    }
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

    private void handleDocumentJobToggleButtonPressed(DocumentJob documentJob,
            ScanResultPane scanResultPane) {
        for(ImageWrapper scanResultImage : documentJob.getImagesUnmodifiable()) {
            try {
                addScanResult(scanResultImage,
                        scanResultPane,
                        scanResultPane.getSelectedScanResults());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        documentJobImageMapping.put(documentJob,
                new LinkedList<>(documentJob.getImagesUnmodifiable())
                    //needs to be a copy because the original is cleared
        );
    }

    private void addDocumentJobToggleButton(DocumentJob documentJob,
            Pane documentJobPane,
            ScanResultPane scanResultPane) {
        DocumentJobToggleButton documentJobToggleButton = new DocumentJobToggleButton(documentJob);
        documentJobToggleButtonMapping.put(documentJob,
                documentJobToggleButton);
        documentJobPane.getChildren().add(documentJobToggleButton);
        //documentJobToggleButton.setDisable(!documentJob.isFinished());
            //re-enabling doesn't work, see internal implementation notes of
            //class for details
        documentJobToggleButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            //move images from selection pane to job list where they can
            //be moved back from if the button is toggled again
            if(documentJobToggleButton.isSelected()) {
                handleDocumentJobToggleButtonPressed(documentJob,
                        scanResultPane);
            }else {
                //documentJobToggleButton not pressed
                scanResultPane.removeScanResultViewPanesOf(documentJob.getImagesUnmodifiable());
            }
        });
        if(documentJob.isFinished()) {
            documentJobToggleButton.setSelected(true);
                //set selected state visually which doesn't trigger the
                //action events
        }
        LOGGER.debug(String.format("added toggle button for document job %d",
                documentJob.getJobNumber()));
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
