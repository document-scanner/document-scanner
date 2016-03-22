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
package richtercloud.document.scanner.gui.conf;

import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import richtercloud.document.scanner.storage.XMLStorage;

/**
 *
 * @author richter
 */
public class XMLStorageConf implements StorageConf<XMLStorage, StorageConfInitializationException> {
    private File file;
    private XMLStorage xMLStorage;

    protected XMLStorageConf() {
    }

    public XMLStorageConf(File file) throws FileNotFoundException {
        this.file = file;
        this.xMLStorage = new XMLStorage(file);
    }

    @Override
    public XMLStorage getStorage() {
        return xMLStorage;
    }

    @Override
    public void validate() throws StorageConfInitializationException {
        XStream xStream = new XStream();
        try {
            List<Object> existingObjects = (List<Object>) xStream.fromXML(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            throw new StorageConfInitializationException(ex);
        }
    }

}
