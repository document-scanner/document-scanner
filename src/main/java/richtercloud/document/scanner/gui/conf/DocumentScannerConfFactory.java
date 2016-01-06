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

import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.persistence.EntityManager;
import richtercloud.reflection.form.builder.message.MessageHandler;

/**
 *
 * @author richter
 */
public class DocumentScannerConfFactory {

    public DocumentScannerConf create(EntityManager entityManager,
            MessageHandler messageHandler,
            Set<Class<?>> entityClasses,
            File xMLStorageFile) throws IOException {
        return new DocumentScannerConf(entityManager, messageHandler, entityClasses, xMLStorageFile);
    }
}
