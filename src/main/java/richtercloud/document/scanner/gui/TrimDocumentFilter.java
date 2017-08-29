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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.ifaces.MainPanel;
import richtercloud.message.handler.ExceptionMessage;
import richtercloud.message.handler.IssueHandler;

/**
 *
 * @author richter
 */
public class TrimDocumentFilter extends DocumentFilter {
    private final static Logger LOGGER = LoggerFactory.getLogger(TrimDocumentFilter.class);
    private final DocumentFilter documentFilterDelegate;
    private final MainPanel mainPanel;
    private final IssueHandler issueHandler;

    public TrimDocumentFilter(DocumentFilter documentFilterDelegate,
            MainPanel mainPanel,
            IssueHandler issueHandler) {
        this.documentFilterDelegate = documentFilterDelegate;
        this.mainPanel = mainPanel;
        this.issueHandler = issueHandler;
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
        try {
            Object currentClipboardObject = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            assert currentClipboardObject instanceof String;
            String currentClipboardString = (String) currentClipboardObject;
            boolean trimWhiteSpace = mainPanel.getDocumentSwitchingMap().get(mainPanel.getoCRSelectComponent()).getKey().getTrimWhitespaceCheckBox().isSelected()
                    && currentClipboardString.equals(text);
                //only pasted changes ought to be trimmed
            String text0;
            if (trimWhiteSpace) {
                text0 = text.trim();
            } else {
                text0 = text;
            }
            return text0;
        }catch(UnsupportedFlavorException
                | IOException ex) {
            LOGGER.error("unexpected exception during retrieval of clipboard data occured",
                    ex);
            issueHandler.handleUnexpectedException(new ExceptionMessage(ex));
            return null;
        }
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
