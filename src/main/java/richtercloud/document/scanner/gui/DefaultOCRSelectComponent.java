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
import richtercloud.document.scanner.gui.conf.OCREngineConf;
import richtercloud.document.scanner.ifaces.EntityPanel;
import richtercloud.document.scanner.ifaces.OCRSelectComponent;
import richtercloud.document.scanner.ifaces.OCRSelectPanelPanel;
import richtercloud.document.scanner.ocr.OCREngineFactory;

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
    private Button autoOCRValueDetectionResultsButton;
    private Button autoOCRValueDetectionButton;
    private CheckBox autoOCRValueDetectionCheckBox;*/
    private final JToolBar toolbar = new JToolBar(SwingConstants.VERTICAL);
    private final JButton zoomInButton = new JButton("+");
    private final JButton zoomOutButton = new JButton("-");
    private final JButton autoOCRValueDetectionResultsButton = new JButton("Set auto detection results on form");
    private final JButton autoOCRValueDetectionButton = new JButton("(Re-)Run auto detection");
    private final JCheckBox autoOCRValueDetectionCheckBox = new JCheckBox("Show auto detection components on form");
    private float zoomLevel = 1;
    private final EntityPanel entityPanel;
    private final OCREngineFactory oCREngineFactory;
    private final OCREngineConf oCREngineConf;
    private final DocumentScannerConf documentScannerConf;

    /**
     *
     * @param oCRSelectPanelPanel will be wrapped in a
     * {@link OCRSelectPanelPanelScrollPane}
     */
    public DefaultOCRSelectComponent(OCRSelectPanelPanel oCRSelectPanelPanel,
            EntityPanel entityPanel,
            OCREngineFactory oCREngineFactory,
            OCREngineConf oCREngineConf,
            DocumentScannerConf documentScannerConf,
            final Set<JPanel> autoOCRValueDetectionPanels) {
        this.oCRSelectPanelPanel = oCRSelectPanelPanel;
        this.entityPanel = entityPanel;
        this.oCREngineFactory = oCREngineFactory;
        this.oCREngineConf = oCREngineConf;
        this.documentScannerConf = documentScannerConf;

        /*zoomInButton = new Button(" + ");
        zoomOutButton = new Button(" - ");
        autoOCRValueDetectionResultsButton = new Button("Set auto detection results on form");
        autoOCRValueDetectionButton = new Button("(Re-)Run auto detection");
        autoOCRValueDetectionCheckBox = new CheckBox("Show auto detection components on form");
        toolbar = new ToolBar(zoomInButton,
                zoomOutButton,
                autoOCRValueDetectionResultsButton,
                autoOCRValueDetectionButton,
                autoOCRValueDetectionCheckBox);
        toolbarPanel.setScene(new Scene(toolbar));
        zoomInButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        OCRSelectComponent.this.zoomLevel *= OCRSelectComponent.this.documentScannerConf.getZoomLevelMultiplier();
                        OCRSelectComponent.this.oCRSelectPanelPanel.setZoomLevels(OCRSelectComponent.this.zoomLevel);
                        OCRSelectComponent.this.repaint();
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
                        OCRSelectComponent.this.repaint();
                        //zooming out requires a scroll event to occur in order to
                        //paint other pages than the first only; revalidate doesn't
                        //help
                    }
                });
            }
        });
        autoOCRValueDetectionResultsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        OCRSelectComponent.this.entityPanel.autoOCRValueDetection(new OCRSelectPanelPanelFetcher(OCRSelectComponent.this.getoCRSelectPanelPanel(),
                                OCRSelectComponent.this.oCREngineFactory,
                                OCRSelectComponent.this.oCREngineConf),
                                false //forceRenewal
                        );
                    }
                });
            }
        });
        autoOCRValueDetectionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        OCRSelectComponent.this.entityPanel.autoOCRValueDetection(new OCRSelectPanelPanelFetcher(OCRSelectComponent.this.getoCRSelectPanelPanel(),
                                OCRSelectComponent.this.oCREngineFactory,
                                OCRSelectComponent.this.oCREngineConf),
                                true //forceRenewal
                        );
                    }
                });
            }
        });
        autoOCRValueDetectionCheckBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        for(JPanel autoOCRValueDetectionPanel : autoOCRValueDetectionPanels) {
                            boolean visible = autoOCRValueDetectionCheckBox.isSelected();
                            autoOCRValueDetectionPanel.setVisible(visible);
                        }
                    }
                });
            }
        });
        autoOCRValueDetectionCheckBox.setSelected(true); //should trigger
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
        toolbarPanel.add(autoOCRValueDetectionResultsButton);
        toolbarPanel.add(autoOCRValueDetectionButton);
        toolbarPanel.add(autoOCRValueDetectionCheckBox);
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
                DefaultOCRSelectComponent.this.zoomLevel *= DefaultOCRSelectComponent.this.documentScannerConf.getZoomLevelMultiplier();
                DefaultOCRSelectComponent.this.oCRSelectPanelPanel.setZoomLevels(DefaultOCRSelectComponent.this.zoomLevel);
                DefaultOCRSelectComponent.this.repaint();
            }
        });
        zoomOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultOCRSelectComponent.this.zoomLevel /= DefaultOCRSelectComponent.this.documentScannerConf.getZoomLevelMultiplier();
                DefaultOCRSelectComponent.this.oCRSelectPanelPanel.setZoomLevels(DefaultOCRSelectComponent.this.zoomLevel);
                DefaultOCRSelectComponent.this.repaint();
                    //zooming out requires a scroll event to occur in order to
                    //paint other pages than the first only; revalidate doesn't
                    //help
            }
        });
        autoOCRValueDetectionResultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultOCRSelectComponent.this.entityPanel.autoOCRValueDetection(new DefaultOCRSelectPanelPanelFetcher(DefaultOCRSelectComponent.this.getoCRSelectPanelPanel(),
                        DefaultOCRSelectComponent.this.oCREngineFactory,
                        DefaultOCRSelectComponent.this.oCREngineConf),
                        false //forceRenewal
                );
            }
        });
        autoOCRValueDetectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultOCRSelectComponent.this.entityPanel.autoOCRValueDetection(new DefaultOCRSelectPanelPanelFetcher(DefaultOCRSelectComponent.this.getoCRSelectPanelPanel(),
                        DefaultOCRSelectComponent.this.oCREngineFactory,
                        DefaultOCRSelectComponent.this.oCREngineConf),
                        true //forceRenewal
                );
            }
        });
        autoOCRValueDetectionCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(JPanel autoOCRValueDetectionPanel : autoOCRValueDetectionPanels) {
                    autoOCRValueDetectionPanel.setVisible(autoOCRValueDetectionCheckBox.isSelected());
                }
            }
        });
        autoOCRValueDetectionCheckBox.setSelected(true); //should trigger
            //action listener above
    }

    @Override
    public OCRSelectPanelPanel getoCRSelectPanelPanel() {
        return oCRSelectPanelPanel;
    }
}
