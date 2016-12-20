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
package richtercloud.document.scanner.model;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.Session;

/**
 *
 * @author richter
 */
public class DocumentScannerSessionCustomizer implements SessionCustomizer {
    @Override
    public void customize(Session session) {
        session.getLogin().setUsesStreamsForBinding(false);
            //true slows down access to large byte[] in Document with setter
            //false doesn't avoid high memory consumption when running queries
            //on multiple EntityManagers in parallel
    }
}