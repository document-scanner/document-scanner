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
package richtercloud.document.scanner.setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import richtercloud.document.scanner.gui.ocrresult.OCRResult;
import richtercloud.document.scanner.model.Company;
import richtercloud.document.scanner.model.Identifiable;
import richtercloud.reflection.form.builder.jpa.panels.QueryPanel;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;

/**
 *
 * @author richter
 */
public class QueryPanelSetter implements ValueSetter<Identifiable, QueryPanel<Identifiable>> {
    private final PersistenceStorage<Long> storage;

    public QueryPanelSetter(PersistenceStorage<Long> storage) {
        this.storage = storage;
    }

    @Override
    public void setOCRResult(OCRResult oCRResult,
            QueryPanel<Identifiable> comp) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Identifiable value, QueryPanel<Identifiable> comp) {
        if(!storage.isStarted()) {
            throw new IllegalStateException("storage hasn't been started yet");
        }
        if(value instanceof Company) {
            Company valueCast = (Company) value;
            List<? extends Company> persistedValues = storage.runQueryAll(valueCast.getClass());
            //- mapping the levenshtein distance to the string doesn't work nicely
            //with Java streams (see
            //https://stackoverflow.com/questions/46040265/how-to-filter-and-map-on-a-value-in-a-java-8-stream-without-redundant-calculatio/46040704?noredirect=1#comment79042406_46040704
            //for details)
            //- mapping is unnecessary because only the minimal value is used
            int levenshteinDistanceMin = 10;
            Set<Company> levenshteinDistanceMinCompanys = new HashSet<>();
                //unlikely that there's a duplicate company added because
                //persistedValues should be free of duplicates
            for(Company persistedValue : persistedValues) {
                int levenshteinDistance = LevenshteinDistance.getDefaultInstance().apply(valueCast.getName(), persistedValue.getName());
                if(levenshteinDistance > levenshteinDistanceMin) {
                    continue;
                }
                if(levenshteinDistance < levenshteinDistanceMin) {
                    levenshteinDistanceMinCompanys.clear();
                }
                levenshteinDistanceMin = levenshteinDistance;
                levenshteinDistanceMinCompanys.add(persistedValue);
            }
            if(levenshteinDistanceMinCompanys.isEmpty()) {
                return;
            }
            List<Long> ids = levenshteinDistanceMinCompanys.stream().map(levenstheinDistanceMinCompany -> levenstheinDistanceMinCompany.getId()).collect(Collectors.toList());
            assert !ids.contains(null);
            String queryConditionString = "i.id = "+StringUtils.join(ids,
                    " OR i.id = " //separator
            );
            comp.getQueryComponent().runQuery(String.format("SELECT i from %s i WHERE %s",
                    value.getClass().getSimpleName(),
                    queryConditionString), //queryText
                    false, //async
                    false //skipHistoryEntryUsageCountIncrement
            );
                //run the query in order to express setting the value in the
                //component
        }
    }

    @Override
    public boolean isSupportsOCRResultSetting() {
        return true;
    }
}
