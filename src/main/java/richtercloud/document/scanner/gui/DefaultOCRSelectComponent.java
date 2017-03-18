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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.ifaces.EntityPanel;
import richtercloud.document.scanner.ifaces.OCREngine;
import richtercloud.document.scanner.ifaces.OCRSelectComponent;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanel;
import richtercloud.document.scanner.ifaces.ProgressButton;
import richtercloud.document.scanner.valuedetectionservice.ValueDetectionServiceListener;
import richtercloud.document.scanner.valuedetectionservice.ValueDetectionServiceUpdateEvent;

/**
 * A container for a {@link OCRSelectPanelPanel} and a toolbar for navigation in
 * the scan (zoom, etc.) as well as buttons to start special functions (OCR
 * value detection, etc.).
 *
 * It'd be nice to use a JavaFX toolbar here since it automatically manages
 * overlapping buttons, but using Platform.runLater requires
 * @author richter
 */
public class DefaultOCRSelectComponent extends OCRSelectComponent {
    private static final long serialVersionUID = 1L;
    private final OCRSelectPanelPanel oCRSelectPanelPanel;
    /*private ToolBar toolbar;
    private Button zoomInButton;
    private Button zoomOutButton;
    private Button valueDetectionResultsButton;
    private Button valueDetectionButton;
    private CheckBox valueDetectionCheckBox;*/
    private final JToolBar toolbar = new JToolBar(SwingConstants.VERTICAL);
    private final JButton zoomInButton = new JButton("+");
    private final JButton zoomOutButton = new JButton("-");
    private final JButton valueDetectionResultsButton = new JButton("Set auto detection results on form");
    private final ProgressButton valueDetectionButton = new DefaultProgressButton("(Re-)Run auto detection");
    private final JCheckBox valueDetectionCheckBox = new JCheckBox("Show auto detection components on form");
    private float zoomLevel = 1;
    private final EntityPanel entityPanel;
    private final OCREngine oCREngine;
    private final DocumentScannerConf documentScannerConf;
    private File file;

    /**
     *
     * @param oCRSelectPanelPanel will be wrapped in a
     * {@link OCRSelectPanelPanelScrollPane}
     */
    public DefaultOCRSelectComponent(OCRSelectPanelPanel oCRSelectPanelPanel,
            EntityPanel entityPanel,
            OCREngine oCREngine,
            DocumentScannerConf documentScannerConf,
            final Set<JPanel> valueDetectionPanels,
            File file) {
        this.oCRSelectPanelPanel = oCRSelectPanelPanel;
        this.entityPanel = entityPanel;
        this.oCREngine = oCREngine;
        this.documentScannerConf = documentScannerConf;
        this.file = file;

        /*zoomInButton = new Button(" + ");
        zoomOutButton = new Button(" - ");
        valueDetectionResultsButton = new Button("Set auto detection results on form");
        valueDetectionButton = new Button("(Re-)Run auto detection");
        valueDetectionCheckBox = new CheckBox("Show auto detection components on form");
        toolbar = new ToolBar(zoomInButton,
                zoomOutButton,
                valueDetectionResultsButton,
                valueDetectionButton,
                valueDetectionCheckBox);
        toolbarPanel.setScene(new Scene(toolbar));
        zoomInButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        OCRSelectComponent.this.zoomLevel *= OCRSelectComponent.this.documentScannerConf.getZoomLevelMultiplier();
                        OCRSelectComponent.this.oCRSelectPanelPanel.setZoomLevels(OCRSelectComponent.this.zoomLevel);
                    }
                });
            }
        });
        zoomOutButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        OCRSelectComponent.this.zoomLevel /= OCRSelectComponent.this.documentScannerConf.getZoomLevelMultiplier();
                        OCRSelectComponent.this.oCRSelectPanelPanel.setZoomLevels(OCRSelectComponent.this.zoomLevel);
                        //zooming out requires a scroll event to occur in order to
                        //paint other pages than the first only; revalidate doesn't
                        //help
                    }
                });
            }
        });
        valueDetectionResultsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        OCRSelectComponent.this.entityPanel.valueDetectionNonGUI(new OCRSelectPanelPanelFetcher(OCRSelectComponent.this.getoCRSelectPanelPanel(),
                                OCRSelectComponent.this.oCREngineFactory,
                                OCRSelectComponent.this.oCREngineConf),
                                false //forceRenewal
                        );
                    }
                });
            }
        });
        valueDetectionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        OCRSelectComponent.this.entityPanel.valueDetectionNonGUI(new OCRSelectPanelPanelFetcher(OCRSelectComponent.this.getoCRSelectPanelPanel(),
                                OCRSelectComponent.this.oCREngineFactory,
                                OCRSelectComponent.this.oCREngineConf),
                                true //forceRenewal
                        );
                    }
                });
            }
        });
        valueDetectionCheckBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        for(JPanel valueDetectionPanel : valueDetectionPanels) {
                            boolean visible = valueDetectionCheckBox.isSelected();
                            valueDetectionPanel.setVisible(visible);
                        }
                    }
                });
            }
        });
        valueDetectionCheckBox.setSelected(true); //should trigger
        //action listener above*/

        //Simulate a multiline toolbar rather than implement something that is
        //already available in JavaFX in which the application will be ported
        //sooner or later anyway
        GroupLayout toolbarLayout = new GroupLayout(toolbar);
        toolbar.setLayout(toolbarLayout);
        JPanel toolbarPanel = new JPanel(new WrapLayout(WrapLayout.LEADING, 5, 5)
                //FlowLayout isn't sufficient
        );
        toolbarPanel.add(zoomInButton);
        toolbarPanel.add(zoomOutButton);
        toolbarPanel.add(valueDetectionResultsButton);
        toolbarPanel.add(valueDetectionButton);
        toolbarPanel.add(valueDetectionCheckBox);
        JScrollPane toolbarPanelScrollPane = new JScrollPane(toolbarPanel);
        toolbarPanelScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        toolbarPanelScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        toolbarLayout.setHorizontalGroup(toolbarLayout.createSequentialGroup().addComponent(toolbarPanelScrollPane));
        toolbarLayout.setVerticalGroup(toolbarLayout.createSequentialGroup().addComponent(toolbarPanelScrollPane));
        toolbar.setFloatable(false);

        OCRSelectPanelPanelScrollPane oCRSelectPanelPanelScrollPane =
                new OCRSelectPanelPanelScrollPane(oCRSelectPanelPanel);
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(oCRSelectPanelPanelScrollPane,
                        0,
                        GroupLayout.PREFERRED_SIZE,
                        Short.MAX_VALUE)
                .addComponent(toolbar,
                        0, //allows toolbar to be resizable horizontally
                        GroupLayout.PREFERRED_SIZE,
                        Short.MAX_VALUE //needs to be able to grow infinitely
                ));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(oCRSelectPanelPanelScrollPane,
                        0,
                        GroupLayout.PREFERRED_SIZE,
                        Short.MAX_VALUE)
                .addComponent(toolbar,
                        GroupLayout.PREFERRED_SIZE, //DEFAULT_SIZE causes following lines to be invisible
                        GroupLayout.PREFERRED_SIZE,
                        Short.MAX_VALUE));

        zoomInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    float zoomLevelOld = DefaultOCRSelectComponent.this.zoomLevel;
                    DefaultOCRSelectComponent.this.zoomLevel /= DefaultOCRSelectComponent.this.documentScannerConf.getZoomLevelMultiplier();
                    DefaultOCRSelectComponent.this.oCRSelectPanelPanel.setZoomLevels(DefaultOCRSelectComponent.this.zoomLevel);
                    if(documentScannerConf.isRememberPreferredOCRSelectPanelWidth()) {
                        int preferredOCRSelectPanelWidthNew = (int) (documentScannerConf.getPreferredOCRSelectPanelWidth()*zoomLevel/zoomLevelOld);
                        documentScannerConf.setPreferredOCRSelectPanelWidth(preferredOCRSelectPanelWidthNew);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        zoomOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    float zoomLevelOld = DefaultOCRSelectComponent.this.zoomLevel;
                    DefaultOCRSelectComponent.this.zoomLevel *= DefaultOCRSelectComponent.this.documentScannerConf.getZoomLevelMultiplier();
                    DefaultOCRSelectComponent.this.oCRSelectPanelPanel.setZoomLevels(DefaultOCRSelectComponent.this.zoomLevel);
                    //zooming out requires a scroll event to occur in order to
                    //paint other pages than the first only; revalidate doesn't
                    //help
                    if(documentScannerConf.isRememberPreferredOCRSelectPanelWidth()) {
                        int preferredOCRSelectPanelWidthNew = (int) (documentScannerConf.getPreferredOCRSelectPanelWidth()*zoomLevel/zoomLevelOld);
                        documentScannerConf.setPreferredOCRSelectPanelWidth(preferredOCRSelectPanelWidthNew);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        entityPanel.getValueDetectionService().addListener(new ValueDetectionServiceListener() {
            @Override
            public void onUpdate(ValueDetectionServiceUpdateEvent updateEvent) {
                float progress = updateEvent.getWordNumber()/(float)updateEvent.getWordCount();
                DefaultOCRSelectComponent.this.getValueDetectionButton().setProgress(progress);
            }

            @Override
            public void onFinished() {
                DefaultOCRSelectComponent.this.valueDetectionButton.setProgress(0.0f);
                    //reset progress in case the last update event has a
                    //progress < 1.0f
                DefaultOCRSelectComponent.this.valueDetectionButton.setEnabled(true);
                DefaultOCRSelectComponent.this.entityPanel.setEnabled(true);
            }
        });
        valueDetectionResultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultOCRSelectComponent.this.entityPanel.setEnabled(false);
                    //will be re-enabled in ValueDetectionListener
                DefaultOCRSelectComponent.this.entityPanel.valueDetection(new DefaultOCRSelectPanelPanelFetcher(DefaultOCRSelectComponent.this.getoCRSelectPanelPanel(),
                        DefaultOCRSelectComponent.this.oCREngine,
                        documentScannerConf),
                        false //forceRenewal
                );
            }
        });
        valueDetectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultOCRSelectComponent.this.valueDetectionButton.setEnabled(false);
                DefaultOCRSelectComponent.this.entityPanel.valueDetection(new DefaultOCRSelectPanelPanelFetcher(DefaultOCRSelectComponent.this.getoCRSelectPanelPanel(),
                        DefaultOCRSelectComponent.this.oCREngine,
                        documentScannerConf),
                        true //forceRenewal
                );
            }
        });
        valueDetectionCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(JPanel valueDetectionPanel : valueDetectionPanels) {
                    valueDetectionPanel.setVisible(valueDetectionCheckBox.isSelected());
                }
            }
        });
        valueDetectionCheckBox.setSelected(true); //should trigger
            //action listener above
    }

    @Override
    public ProgressButton getValueDetectionButton() {
        return valueDetectionButton;
    }

    @Override
    public OCRSelectPanelPanel getoCRSelectPanelPanel() {
        return oCRSelectPanelPanel;
    }

    @Override
    public File getFile() {
        return file;
    }
}
