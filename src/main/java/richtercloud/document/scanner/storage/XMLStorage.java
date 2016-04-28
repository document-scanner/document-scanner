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
package richtercloud.document.scanner.storage;

import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import richtercloud.document.scanner.model.Identifiable;

/**
 *
 * @author richter
 */
public class XMLStorage implements Storage<Identifiable> {
    private File file;

    public XMLStorage(File file) throws FileNotFoundException {
        this.file = file;
    }

    @Override
    public void store(Object object) throws StorageException {
        XStream xStream = new XStream();
        List<Object> existingObjects;
        try {
            existingObjects = (List<Object>) xStream.fromXML(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            throw new StorageException(ex);
        }
        existingObjects.add(object);
        xStream = new XStream();
        try {
            xStream.toXML(existingObjects, new FileOutputStream(file));
        } catch (FileNotFoundException ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public Identifiable retrieve(Long id, Class<? extends Identifiable> clazz) throws StorageException {
        //@TODO: this is most certainly more efficient when implemented with an XStream ObjectInputStream
        XStream xStream = new XStream();
        List<Identifiable> existingObjects;
        try {
            existingObjects = (List<Identifiable>) xStream.fromXML(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            throw new StorageException(ex);
        }
        for(Identifiable existingObject : existingObjects) {
            if(existingObject.getId().equals(id)) {
                return existingObject;
            }
        }
        return null;
    }

}
