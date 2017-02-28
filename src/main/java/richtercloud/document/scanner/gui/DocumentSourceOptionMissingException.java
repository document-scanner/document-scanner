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

import au.com.southsky.jfreesane.SaneDevice;

/**
 * Used to allow upload of information about scanner devices (in form of a
 * {@link SaneDevice}) which return {@code null} for
 * {@link SaneDevice#getOption(java.lang.String) } invoked with
 * {@link richtercloud.document.scanner.gui.scanner.ScannerEditDialog#DOCUMENT_SOURCE_OPTION_NAME}.
 *
 * @author richter
 */
public class DocumentSourceOptionMissingException extends Exception {
    private static final long serialVersionUID = 1L;

    public DocumentSourceOptionMissingException(SaneDevice scannerDevice) {
        super(String.format("Scanner device '%s' returned null for document "
                + "source option", scannerDevice));
    }
}
