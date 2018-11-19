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
package de.richtercloud.document.scanner.gui;

import java.lang.reflect.Field;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import de.richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import de.richtercloud.document.scanner.components.annotations.Invisible;
import richtercloud.reflection.form.builder.jpa.retriever.JPAOrderedCachedFieldRetriever;
import richtercloud.reflection.form.builder.retriever.FieldOrderValidationException;

/**
 * A {@link FieldRetriever} which removes fields annotated with
 * {@link Invisible} from the list of fields returned by
 * {@link #retrieveRelevantFields(java.lang.Class) }.
 *
 * @author richter
 */
public class DocumentScannerFieldRetriever extends JPAOrderedCachedFieldRetriever {

    public DocumentScannerFieldRetriever(DocumentScannerConf documentScannerConf,
                                         Set<Class<?>> entityClasses) throws FieldOrderValidationException {
        super(documentScannerConf.getFieldOrderMap(),
                entityClasses,
                true);
    }

    @Override
    public List<Field> retrieveRelevantFields(Class<?> entityClass) {
        List<Field> retValue = super.retrieveRelevantFields(entityClass);
        ListIterator<Field> retValueItr = retValue.listIterator();
        while(retValueItr.hasNext()) {
            Field retValueNxt = retValueItr.next();
            if(retValueNxt.getAnnotation(Invisible.class) != null) {
                retValueItr.remove();
            }
        }
        //it shouldn't matter that we're manipulating
        //DocumentScannerConf.fieldOrderMap directly since noone would ever want
        //to have @Invisible fields in the ordering
        return retValue;
    }
}
