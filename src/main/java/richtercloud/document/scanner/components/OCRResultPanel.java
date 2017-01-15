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

import javax.swing.JOptionPane;
import richtercloud.document.scanner.ifaces.OCREngineRecognitionException;
import richtercloud.message.handler.Message;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.panels.CancelablePanel;

/**
 *
 * @author richter
 */
public class OCRResultPanel extends CancelablePanel<OCRResultPanelPanel, String> {
    private static final long serialVersionUID = 1L;
    private OCRResultPanelFetcher oCRResultPanelFetcher;
    private final MessageHandler messageHandler;

    public OCRResultPanel(OCRResultPanelFetcher retriever,
            String initialValue,
            MessageHandler messageHandler) {
        this(retriever, initialValue, messageHandler, true, true);
    }

    public OCRResultPanel(OCRResultPanelFetcher retriever,
            String initialValue,
            MessageHandler messageHandler,
            boolean async,
            boolean cancelable) {
        super(new OCRResultPanelPanel(initialValue,
                async,
                cancelable));
        this.oCRResultPanelFetcher = retriever;
        this.messageHandler = messageHandler;
    }

    @Override
    protected String doTaskNonGUI() {
        String oCRResult;
        try {
            oCRResult = oCRResultPanelFetcher.fetch();
            assert oCRResult != null;
        } catch (OCREngineRecognitionException ex) {
            messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
            return null;
        }
        return oCRResult;
    }

    @Override
    protected void doTaskGUI(String nonGUIResult) {
        if(nonGUIResult != null) {
            //might be null if fetch has been canceled (this check is
            //unnecessary for non-async processing, but don't care
            getMainPanel().setValue(nonGUIResult);
            getMainPanel().getoCRResultTextArea().setCaretPosition(0);
                //scroll to top after setting value
                //from http://stackoverflow.com/questions/291115/java-swing-using-jscrollpane-and-having-it-scroll-back-to-top
                //JTextArea.scrollRectToVisible doesn't work
        }
    }

    @Override
    protected void cancelTask() {
        oCRResultPanelFetcher.cancelFetch();
    }

    public void reset() {
        getMainPanel().reset();
    }

    public void addUpdateListener(OCRResultPanelUpdateListener updateListener) {
        getMainPanel().addUpdateListener(updateListener);
    }

    public void removeUpdateListener(OCRResultPanelUpdateListener updateListener) {
        getMainPanel().removeUpdateListener(updateListener);
    }
}
