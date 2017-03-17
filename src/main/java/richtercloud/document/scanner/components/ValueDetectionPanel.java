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
package richtercloud.document.scanner.components;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author richter
 */
public class ValueDetectionPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private JComponent classComponent;

    public ValueDetectionPanel(JComponent classComponent,
            JPanel valueDetectionPanel) {
        this.classComponent = classComponent;
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(valueDetectionPanel)
                .addComponent(classComponent));
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(valueDetectionPanel,
                        0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(classComponent,
                        0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
    }

    public JComponent getClassComponent() {
        return classComponent;
    }
}
