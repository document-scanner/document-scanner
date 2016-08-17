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
package richtercloud.document.scanner.setter;

import javax.swing.JComponent;

/**
 * An interface to handle different setter methods of different
 * {@link JComponent} and pass the OCR result to them (always a {@code String}).
 *
 * Implementations are expected to handle parsing of OCR results as well in
 * order to limit the association of field type and a handler component to one
 * which is {@code ValueSetter}.
 *
 * @author richter
 * @param <C> the type of the component the value is set on
 */
public interface ValueSetter<V, C extends JComponent> {

    void setValue(V value, C comp);
}
