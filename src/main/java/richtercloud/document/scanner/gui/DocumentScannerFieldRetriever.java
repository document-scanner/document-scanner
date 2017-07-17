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
import java.util.List;
import java.util.ListIterator;
import richtercloud.document.scanner.components.annotations.Invisible;
import richtercloud.reflection.form.builder.jpa.JPACachedFieldRetriever;
import richtercloud.validation.tools.FieldRetrievalException;

/**
 *
 * @author richter
 */
public class DocumentScannerFieldRetriever extends JPACachedFieldRetriever {

    @Override
    public List<Field> retrieveRelevantFields(Class<?> entityClass) throws FieldRetrievalException {
        List<Field> retValue = super.retrieveRelevantFields(entityClass);
        ListIterator<Field> retValueItr = retValue.listIterator();
        while(retValueItr.hasNext()) {
            Field retValueNxt = retValueItr.next();
            if(retValueNxt.getAnnotation(Invisible.class) != null) {
                retValueItr.remove();
            }
        }
        return retValue;
    }
}
