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

import javax.swing.JFrame;

/**
 * This interface has been extracted from {@link MainPanel}s functionality in
 * order to allow a quick exchange of Java docking frameworks of which some work
 * in a not too obvious way or with non-obvious scalability.
 *
 * @author richter
 */
public interface MainPanelDockingManager {

    void init(JFrame dockingControlFrame,
            MainPanel mainPanel);

    /**
     *
     * @param old the currently active {@link OCRSelectComponent}
     * @param aNew the created {@link OCRSelectComponent} for the new document
     */
    void addDocumentDockable(OCRSelectComponent old,
            OCRSelectComponent aNew);

    void switchDocument(OCRSelectComponent old,
            final OCRSelectComponent aNew);
}
