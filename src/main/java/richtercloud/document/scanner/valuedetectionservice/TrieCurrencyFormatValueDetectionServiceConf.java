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

/**
 *
 * @author richter
 */
public class TrieCurrencyFormatValueDetectionServiceConf extends AbstractValueDetectionServiceConf {
    private static final long serialVersionUID = 1L;

    @Override
    public void validate() throws ValueDetectionServiceConfValidationException {
        //nothing to validate
    }

    @Override
    public String getDescription() {
        return "Prefix-tree/trie-based currency detection service (more efficient than plain currency detection service)";
    }
}
