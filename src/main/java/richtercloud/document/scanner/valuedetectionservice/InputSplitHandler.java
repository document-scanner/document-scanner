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
package richtercloud.document.scanner.valuedetectionservice;

import java.util.List;

/**
 *
 * @author richter
 */
public abstract class InputSplitHandler {

    public void handle(List<String> inputSplits) {
        for (int i = 0; i < inputSplits.size(); //there's no sense in substracting
                i++) {
            for (int j = Math.min(inputSplits.size(), i + getMaxWords()); j > i; //there's no sense in using j>=i because that creates empty lists
                    j--) {
                //start with longer input, then decrease
                List<String> inputSplitsSubs = inputSplits.subList(i, j);
                handle0(inputSplitsSubs, inputSplits, i);
            }
        }
    }

    protected abstract void handle0(List<String> inputSplitsSubs, List<String> inputSplits, int index);

    protected abstract int getMaxWords();
}
