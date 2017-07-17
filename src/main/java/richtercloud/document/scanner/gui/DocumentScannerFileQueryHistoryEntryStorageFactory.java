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

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import richtercloud.document.scanner.model.Identifiable;
import richtercloud.message.handler.MessageHandler;
import richtercloud.reflection.form.builder.jpa.panels.QueryHistoryEntryStorageFactory;
import richtercloud.reflection.form.builder.jpa.panels.XMLFileQueryHistoryEntryStorageFactory;

/**
 *
 * @author richter
 */
public class DocumentScannerFileQueryHistoryEntryStorageFactory extends XMLFileQueryHistoryEntryStorageFactory {
    private final static String LAST_USED_FIELD_NAME;
    static {
        //assert that the field exists/hasn't been renamed
        try {
            LAST_USED_FIELD_NAME = Identifiable.class.getDeclaredField("lastUsed").getName();
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public DocumentScannerFileQueryHistoryEntryStorageFactory(File file,
            Set<Class<?>> entityClasses,
            boolean forbidSubtypes,
            MessageHandler messageHandler) {
        super(file,
                entityClasses,
                forbidSubtypes,
                messageHandler);
    }

    @Override
    public List<String> generateInitialQueryTexts(Class<?> entityClass,
            boolean forbidSubtypes) {
        List<String> superRetValues = super.generateInitialQueryTexts(entityClass,
                forbidSubtypes);
        List<String> retValues = new LinkedList<>();
        for(String superRetValue : superRetValues) {
            if(!superRetValue.contains("order by")) {
                String retValue = superRetValue.concat(String.format(" ORDER BY %s.%s DESC",
                        QueryHistoryEntryStorageFactory.generateEntityClassQueryIdentifier(entityClass),
                        LAST_USED_FIELD_NAME));
                    //have the last modified at top of the list (avoids scrolling)
                retValues.add(retValue);
            }else {
                retValues.add(superRetValue);
            }
        }
        return retValues;
    }
}
