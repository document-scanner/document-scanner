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

import java.util.List;
import richtercloud.document.scanner.ifaces.ImageWrapper;
import richtercloud.reflection.form.builder.panels.CancelablePanel;

/**
 *
 * @author richter
 */
public class ScanResultPanel extends CancelablePanel<ScanResultPanelPanel, List<ImageWrapper>> {
    private ScanResultPanelFetcher retriever;

    public ScanResultPanel(ScanResultPanelFetcher retriever,
            List<ImageWrapper> initialValue,
            boolean async,
            boolean cancelable) {
        super(new ScanResultPanelPanel(initialValue,
                async,
                cancelable));
        this.retriever = retriever;
    }

    @Override
    public List<ImageWrapper> doTaskNonGUI() {
        List<ImageWrapper> retValue = this.retriever.fetch();
        return retValue;
    }

    @Override
    public void doTaskGUI(List<ImageWrapper> nonGUIResult) {
        getMainPanel().setValue(nonGUIResult);
    }

    @Override
    protected void cancelTask() {
        this.retriever.cancelFetch();
    }

    public void reset() {
        getMainPanel().reset();
    }

    public void addUpdateListerner(ScanResultPanelUpdateListener updateListener) {
        getMainPanel().addUpdateListerner(updateListener);
    }

    public void removeUpdateListener(ScanResultPanelUpdateListener updateListener) {
        getMainPanel().removeUpdateListener(updateListener);
    }
}
