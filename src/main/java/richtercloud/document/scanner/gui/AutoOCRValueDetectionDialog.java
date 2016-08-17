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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.reflection.form.builder.ReflectionFormBuilder;
import richtercloud.reflection.form.builder.ReflectionFormPanel;
import richtercloud.reflection.form.builder.message.MessageHandler;

/**
 * Displays {@link AutoOCRValueDetectionResult}s in a {@link JTable} (which is
 * much more flexible than a custom {@link GroupLayout} or other layout.
 *
 * Naively assumes that all object which are detected have a string
 * representation which makes sense.
 *
 * Don't provide a close button because it'd have to be placed next to the
 * "Set on field" button which is strange because they belong to the table and
 * to the window so that they shouldn't be next to each other.
 *
 * @author richter
 */
/*
internal implementation notes:
- There's no sense in providing a button to set value on field because that just
creates extranous components which are also harder to manage in the JTable.
*/
public class AutoOCRValueDetectionDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final DefaultTableModel tableModel = new DefaultTableModel();
    private final JTable table = new JTable(tableModel) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if(column == 0) {
                //type
                return String.class;
            }else if(column == 1) {
                //result
                return Object.class; //manage any type and assume that toString
                    //works on it
            }else {
                //column == 1
                //OCR source
                return String.class;
            }
        }
    };
    private final JScrollPane tableScrollPane = new JScrollPane(table);
    private final JButton setButton = new JButton("Set on field");
    private final JLabel tableLabel = new JLabel("Auto OCR detection values");
    /**
     * Popup menu which is opened when {@code setButton} is clicked. Doesn't
     * contain a single top menu item explaining what the menu items
     * (representing classes) do because it this should be clear from the
     * buttons label.
     */
    private final JPopupMenu setButtonPopupMenu = new JPopupMenu();
    private final AbstractFieldPopupMenuFactory fieldPopupMenuFactory;

    public AutoOCRValueDetectionDialog(Window parent,
            List<AutoOCRValueDetectionResult<?>> detectionResults,
            Set<Class<?>> entityClasses,
            Map<Class<?>, ReflectionFormPanel<?>> reflectionFormPanelMap,
            ReflectionFormBuilder reflectionFormBuilder,
            Map<Class<? extends JComponent>, ValueSetter<?,?>> valueSetterMapping,
            MessageHandler messageHandler) {
        super(parent,
                DocumentScanner.generateApplicationWindowTitle("Auto OCR value detection results", DocumentScanner.APP_NAME, DocumentScanner.APP_VERSION),
                ModalityType.APPLICATION_MODAL //modalityType
        );
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        if(detectionResults == null) {
            throw new IllegalArgumentException("detectionResults mustn't be null");
        }
        if(detectionResults.isEmpty()) {
            throw new IllegalArgumentException("detectionResults mustn't be empty");
        }
        this.fieldPopupMenuFactory = new AutoOCRValueDetectionFieldPopupMenuFactory(table,
                messageHandler,
                valueSetterMapping);
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(tableLabel, GroupLayout.Alignment.LEADING)
                .addComponent(tableScrollPane)
                .addComponent(setButton, GroupLayout.Alignment.TRAILING));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(tableLabel)
                .addComponent(tableScrollPane)
                .addComponent(setButton));
        pack();

        tableModel.addColumn("Type");
        tableModel.addColumn("Result");
        tableModel.addColumn("OCR source");
        for(AutoOCRValueDetectionResult<?> detectionResult : detectionResults) {
            tableModel.addRow(new Object[] {detectionResult.getResult().getClass().getSimpleName(),
                detectionResult.getResult(),
                detectionResult.getoCRSource()});
        }
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addRowSelectionInterval(0, 0);

        List<Class<?>> entityClassesSort = EntityPanel.sortEntityClasses(entityClasses);
        List<JMenuItem> setButtonPopupMenuItems = this.fieldPopupMenuFactory.createFieldPopupMenuItems(entityClassesSort,
                reflectionFormPanelMap,
                reflectionFormBuilder);
        for(JMenuItem setButtonPopupMenuItem : setButtonPopupMenuItems) {
            this.setButtonPopupMenu.add(setButtonPopupMenuItem);
        }
        setButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(table.getSelectedRow() == -1) {
                    JOptionPane.showMessageDialog(AutoOCRValueDetectionDialog.this,
                            "No result selected",
                            DocumentScanner.generateApplicationWindowTitle("No result selected", DocumentScanner.APP_NAME, DocumentScanner.APP_VERSION),
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                setButtonPopupMenu.show(setButton, 0, 0);
            }
        });
    }
}
