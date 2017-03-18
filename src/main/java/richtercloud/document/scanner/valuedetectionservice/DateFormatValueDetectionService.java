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
package richtercloud.document.scanner.valuedetectionservice;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.FormatUtils;

/**
 * A brute-force "getting-started" attempt of implementation which has quite
 * horrible performance and is easily superceeded by
 * {@link SUTimeValueDetectionService}.
 *
 * @author richter
 */
public class DateFormatValueDetectionService extends AbstractFormatValueDetectionService<Date> {
    private final static Logger LOGGER = LoggerFactory.getLogger(DateFormatValueDetectionService.class);

    /**
     * The max. number of words a date can be made up from.
     */
    public final static int MAX_FORMAT_WORDS;
    private final static Date MAX_DATE_WORDS_TEST = new Date();
    static {
        int wordsLongest = 0;
        Set<DateFormat> dateFormats = FormatUtils.getAllDateRelatedFormats();
        for(DateFormat dateFormat : dateFormats) {
            String dateString = dateFormat.format(MAX_DATE_WORDS_TEST);
            int words = dateString.split("[\\s]+",
                    wordsLongest+1 //no need to split after longest
            ).length;
            if(words > wordsLongest) {
                wordsLongest = words;
            }
        }
        MAX_FORMAT_WORDS = wordsLongest;
        LOGGER.debug(String.format("Max. of words in every date format of every locale is %d", wordsLongest));
    }

    @Override
    protected int getMaxWords() {
        return MAX_FORMAT_WORDS;
    }

    @Override
    protected List<ValueDetectionResult<Date>> checkResult(String inputSub,
            List<String> inputSplits,
            int index) {
        List<ValueDetectionResult<Date>> retValue = new LinkedList<>();
        for(Entry<DateFormat, Set<Locale>> dateFormat : FormatUtils.getDisjointDateRelatedFormats().entrySet()) {
            try {
                Date date = dateFormat.getKey().parse(inputSub);
                ValueDetectionResult<Date> valueDetectionResult = new ValueDetectionResult<>(inputSub,
                        date
                );
                //not sufficient to check whether result
                //is already contained because the same date
                //might be retrieved from a longer and a
                //shorter substring of a substring
                retValue.add(valueDetectionResult);
                //don't break, but add all date formats as
                //result for the user to select
            }catch(ParseException ex) {
                //skip to next format
            }
        }
        getListeners().stream().forEach((listener) -> {
            listener.onUpdate(new ValueDetectionServiceUpdateEvent<>(new LinkedList<>(retValue),
                    inputSplits.size(),
                    index));
        });
        return retValue;
    }
}
