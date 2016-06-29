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
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 *
 * @author richter
 */
public class OCRSelectComponent extends JPanel {
    private static final long serialVersionUID = 1L;
    private final OCRSelectPanelPanel oCRSelectPanelPanel;
    private final JToolBar toolbar = new JToolBar();
    private final JButton zoomInButton = new JButton(" + ");
    private final JButton zoomOutButton = new JButton(" - ");
    private float zoomLevel = 1;

    /**
     *
     * @param oCRSelectPanelPanel will be wrapped in a
     * {@link OCRSelectPanelPanelScrollPane}
     */
    public OCRSelectComponent(OCRSelectPanelPanel oCRSelectPanelPanel) {
        this.oCRSelectPanelPanel = oCRSelectPanelPanel;

        toolbar.add(zoomInButton);
        toolbar.add(zoomOutButton);

        GroupLayout groupLayout = new GroupLayout(this);
        OCRSelectPanelPanelScrollPane oCRSelectPanelPanelScrollPane =
                new OCRSelectPanelPanelScrollPane(oCRSelectPanelPanel);
        this.setLayout(groupLayout);
        groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
                .addComponent(oCRSelectPanelPanelScrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(toolbar,
                        GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE));
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup()
                .addComponent(oCRSelectPanelPanelScrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(toolbar,
                        GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE));

        zoomInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OCRSelectComponent.this.zoomLevel *= 2;
                OCRSelectComponent.this.oCRSelectPanelPanel.setZoomLevels(OCRSelectComponent.this.zoomLevel);
                OCRSelectComponent.this.repaint();
            }
        });
        zoomOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OCRSelectComponent.this.zoomLevel /= 2;
                OCRSelectComponent.this.oCRSelectPanelPanel.setZoomLevels(OCRSelectComponent.this.zoomLevel);
                OCRSelectComponent.this.repaint();
                    //zooming out requires a scroll event to occur in order to
                    //paint other pages than the first only; revalidate doesn't
                    //help
            }
        });
    }

    public OCRSelectPanelPanel getoCRSelectPanelPanel() {
        return oCRSelectPanelPanel;
    }
}
