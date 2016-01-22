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

/**
 *
 * @author richter
 */
public class OCRResultPanelFetcherProgressEvent {
    private final String newValue;
    private final double progress;

    public OCRResultPanelFetcherProgressEvent(String newValue, double progress) {
        this.newValue = newValue;
        this.progress = progress;
    }

    public double getProgress() {
        return progress;
    }

    public String getNewValue() {
        return newValue;
    }
}
