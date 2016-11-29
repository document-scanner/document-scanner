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

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.time.*;
import edu.stanford.nlp.time.SUTime.Temporal;
import edu.stanford.nlp.util.CoreMap;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AutoOCRValueDetectionService} which uses the <a href=""></a>.
 * @author richter
 */
public class SUTimeAutoOCRValueDetectionService extends AbstractAutoOCRValueDetectionService<Date> {
    private final static Logger LOGGER = LoggerFactory.getLogger(SUTimeAutoOCRValueDetectionService.class);
    /**
     * The {@code SUTime} pipeline used for date and time value discovery. Is
     * reusage according to class javadoc.
     */
    private final static AnnotationPipeline PIPELINE = new AnnotationPipeline();
    static {
        Properties props = new Properties();
        PIPELINE.addAnnotator(new TokenizerAnnotator(false));
        PIPELINE.addAnnotator(new WordsToSentencesAnnotator(false));
        PIPELINE.addAnnotator(new POSTaggerAnnotator(false));
        PIPELINE.addAnnotator(new TimeAnnotator("sutime", props));
    }
    private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected LinkedHashSet<AutoOCRValueDetectionResult<Date>> fetchResults0(String input) {
        LinkedHashSet<AutoOCRValueDetectionResult<Date>> retValue = new LinkedHashSet<>();
        Annotation annotation = new Annotation(input);
        annotation.set(CoreAnnotations.DocDateAnnotation.class, "2013-07-14"); //@TODO: ??
        PIPELINE.annotate(annotation);
        List<CoreMap> timexAnnsAll = annotation.get(TimeAnnotations.TimexAnnotations.class);
        for (CoreMap coreMap : timexAnnsAll) {
            String oCRSource = coreMap.toString();
            Date value;
            Temporal temporal = coreMap.get(TimeExpression.Annotation.class).getTemporal();
            try {
                value = SIMPLE_DATE_FORMAT.parse(temporal.getRange().begin().toString());
            } catch (ParseException ex) {
                //Something like `Caused by: java.text.ParseException: Unparseable date: "716-XX-XX"`
                //can happen @TODO: figure out
                LOGGER.error("an unexpected exception occured during",
                        ex);
                continue;
            }
            AutoOCRValueDetectionResult<Date> result = new AutoOCRValueDetectionResult<>(oCRSource,
                    value
            );
            retValue.add(result);
        }
        return retValue;
    }
}