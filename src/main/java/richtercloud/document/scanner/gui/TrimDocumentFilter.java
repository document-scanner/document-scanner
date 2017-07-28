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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import richtercloud.document.scanner.ifaces.MainPanel;

/**
 *
 * @author richter
 */
public class TrimDocumentFilter extends DocumentFilter {
    private final DocumentFilter documentFilterDelegate;
    private final MainPanel mainPanel;

    public TrimDocumentFilter(DocumentFilter documentFilterDelegate, MainPanel mainPanel) {
        this.documentFilterDelegate = documentFilterDelegate;
        this.mainPanel = mainPanel;
    }

    private String trim(String text) {
        boolean trimWhiteSpace = mainPanel.getDocumentSwitchingMap().get(mainPanel.getoCRSelectComponent()).getKey().getTrimWhitespaceCheckBox().isSelected();
        String text0;
        if (trimWhiteSpace) {
            text0 = text.trim();
        } else {
            text0 = text;
        }
        return text0;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
        String text0 = trim(text);
        documentFilterDelegate.insertString(fb, offset, text0, attr);
    }

    @Override
    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        String text0 = trim(text);
        documentFilterDelegate.replace(fb, offset, length, text0, attrs);
    }

    @Override
    public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
        documentFilterDelegate.remove(fb, offset, length);
    }
}
