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

/**
 * An interface to share documentation of resource dependent classes which ought
 * to initialize and close resources outside a constructor in order to allow
 * closing such resources if an intialization fails.
 *
 * @author richter
 * @param <E> allowed exception to be thrown by {@link #init() }
 */
public interface Managed<E extends Exception> {

    /**
     * Initializes all resources of the class.
     * @throws E
     */
    void init() throws E;

    /**
     * Is expected to close all resources which have been opened in
     * {@link #init() }, also part of them, e.g. if {@code init} failed because
     * it threw an exception.
     */
    void close();
}
