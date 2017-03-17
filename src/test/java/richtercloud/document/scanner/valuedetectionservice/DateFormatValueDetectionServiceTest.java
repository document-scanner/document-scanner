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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.apache.commons.lang.RandomStringUtils;
import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author richter
 */
public class DateFormatValueDetectionServiceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(DateFormatValueDetectionServiceTest.class);
    private final static Random RANDOM;
    static {
        long randomSeed = System.currentTimeMillis();
        LOGGER.info(String.format("randomSeed: %d", randomSeed));
        RANDOM = new Random(randomSeed);
    }

    /**
     * Tests whether trailing string disturbs date parsing with
     * {@link SimpleDateFormat}.
     * @throws ParseException
     */
    @Test
    public void testGeneralDateParsing() throws ParseException {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        date = calendar.getTime();
        String input = String.format("%sabc", new SimpleDateFormat().format(date));
        Date result = new SimpleDateFormat().parse(input);
        calendar.setTime(result);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        result = calendar.getTime();
        assertEquals(date, result); //since the input and
            //parse result are not equals although the seconds have been set to
            //0 compare the string representation because that absolutely make
            //no sense at all
    }

    /**
     * Test of getResults method, of class DateFormatValueDetectionService.
     */
    @Test
    public void testGetResults() {
        System.out.println("getResults");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        date = calendar.getTime();
        //Test with one date
        StringBuilder inputBuilder = new StringBuilder();
        for(int i=0; i<RANDOM.nextInt(25); i++) {
            inputBuilder.append(RandomStringUtils.random(RANDOM.nextInt(50)));
            inputBuilder.append(" ");
        }
        inputBuilder.append(new SimpleDateFormat().format(date));
        for(int i=0; i<RANDOM.nextInt(25); i++) {
            inputBuilder.append(RandomStringUtils.random(RANDOM.nextInt(50)));
            inputBuilder.append(" ");
        }
        String input = inputBuilder.toString();
        LOGGER.debug(String.format("Testing single date with input '%s'", input));
        DateFormatValueDetectionService instance = new DateFormatValueDetectionService();
        List<ValueDetectionResult<Date>> results = instance.fetchResults(input);
        assertTrue(!results.isEmpty());
        boolean dateFound = false;
        for(ValueDetectionResult<?> result: results) {
            if(result.getValue().equals(date)) {
                dateFound = true;
                break;
            }
        }
        assertTrue(dateFound);

        //Test with multiple dates
        inputBuilder = new StringBuilder();
        int dateCount = 1+RANDOM.nextInt(9);
        Set<Date> dates = new HashSet<>();
        long currentTimeMillis = System.currentTimeMillis();
        int dateOffset = 0; //make sure that date have different minutes (since
            //seconds are set to 0)
        for(int j=0; j<dateCount; j++) {
            date = new Date(currentTimeMillis+dateOffset);
            calendar.setTime(date);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            date = calendar.getTime();
            dates.add(date);
            for(int i=0; i<RANDOM.nextInt(25); i++) {
                inputBuilder.append(RandomStringUtils.random(RANDOM.nextInt(50)));
                inputBuilder.append(" ");
            }
            inputBuilder.append(new SimpleDateFormat().format(date));
            for(int i=0; i<RANDOM.nextInt(25); i++) {
                inputBuilder.append(RandomStringUtils.random(RANDOM.nextInt(50)));
                inputBuilder.append(" ");
            }
            dateOffset += 3600;
        }
        input = inputBuilder.toString();
        LOGGER.debug(String.format("Test multiple dates (%d) with input '%s'", dateCount, input));
        instance = new DateFormatValueDetectionService();
        results = instance.fetchResults(input);
        assertTrue(!results.isEmpty());
        dateFound = false;
        for(ValueDetectionResult<?> result: results) {
            if(result.getValue().equals(date)) {
                dateFound = true;
                break;
            }
        }
        assertTrue(dateFound);

        //Special test
        instance = new DateFormatValueDetectionService();
        results = instance.fetchResults("27.10.2015 ");
        assertTrue(!results.isEmpty());
    }
}
