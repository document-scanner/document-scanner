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

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
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

    public TrimDocumentFilter(DocumentFilter documentFilterDelegate,
            MainPanel mainPanel) {
        this.documentFilterDelegate = documentFilterDelegate;
        this.mainPanel = mainPanel;
    }

    /**
     * Checks whether the trim-whitespace-checkbox of the associated
     * {@code mainPanel} is checked and whether {@code text} is the current
     * entry in the clipboard which allows distinction between typed and pasted
     * changes (see https://stackoverflow.com/questions/45928597/how-to-manipulate-text-pasted-into-a-jtextfield-but-not-typed-input
     * for details).
     *
     * @param text the text to trim or leave
     * @return the trimmed or untouched text
     */
    private String trim(String text) {
        assert text != null;
        String currentClipboardString = null;
        try {
            Object currentClipboardObject = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            assert currentClipboardObject instanceof String;
            currentClipboardString = (String) currentClipboardObject;
        }catch(UnsupportedFlavorException
                | IOException ex) {
            //can occur if a file or another non-string object is in the
            //clipboard -> ignore (currentClipboardString remains null and
            //trimWhiteSpace will be false
        }
        boolean trimWhiteSpace = mainPanel.getDocumentSwitchingMap().get(mainPanel.getoCRSelectComponent()).getKey().getTrimWhitespaceCheckBox().isSelected()
                && text.equals(currentClipboardString);
            //only pasted changes ought to be trimmed
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
