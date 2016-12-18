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

import richtercloud.document.scanner.model.Document;
import richtercloud.reflection.form.builder.jpa.storage.FieldInitializer;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;

/**
 *
 * @author richter
 */
public class DocumentScannerFieldInitializer implements FieldInitializer {
    private PersistenceStorage storage;

    public DocumentScannerFieldInitializer(PersistenceStorage storage) {
        this.storage = storage;
    }

    @Override
    public void initialize(Object entity) throws IllegalArgumentException, IllegalAccessException {
        //org.eclipse.persistence.sessions.Session.readObject(Object) and
        //refreshObject(Object) don't initialize lazy fields -> there seems
        //to be no way in EclipseLink
        if(entity instanceof Document) {
            Document entityCast = (Document) entity;
            entityCast.getScanData();
        }
    }
}
