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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import richtercloud.reflection.form.builder.FieldRetriever;
import richtercloud.reflection.form.builder.jpa.storage.ReflectionFieldInitializer;

/**
 *
 * @author richter
 */
public class DocumentScannerFieldInitializer extends ReflectionFieldInitializer {
    private final Set<Class<?>> initializeSkipTypes = new HashSet<>(Arrays.asList(byte[].class));

    public DocumentScannerFieldInitializer(FieldRetriever fieldRetriever) {
        super(fieldRetriever);
    }

    @Override
    protected boolean initializeField(Field field) {
        if(field.getType().equals(byte.class)) {
            return false;
        }
        return super.initializeField(field);
    }
}
