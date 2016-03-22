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
package richtercloud.document.scanner.model;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author richter
 */
public class TransportTest extends AbstractTest {

    /**
     * Test of toString method, of class Transport.
     */
    @Test
    public void testToString() {
        String startWaypoint = "1";
        String endWaypoint = "z";
        long startDateValue = System.currentTimeMillis()-1000;
        Date startDate = new Date(startDateValue);
        Date endDate = new Date(startDateValue+500);
        TransportTicket start = new TransportTicket(getRandom().nextLong(),
                new Company(getRandom().nextLong(),
                        "name",
                        new LinkedList<>(Arrays.asList("allNames")),
                        new LinkedList<>(Arrays.asList(new Address("street", "number", "zipcode", "region", "city", "country"))),
                        new LinkedList<>(Arrays.asList(new EmailAddress(getRandom().nextLong(), "kd@kdls.dkl", null))),
                        new LinkedList<>(Arrays.asList(new TelephoneNumber(getRandom().nextLong(),
                                1,
                                2,
                                3,
                                null,
                                TelephoneNumber.TYPE_FAX)))),
                new LinkedList<>(Arrays.asList(startWaypoint,"2")),
                startDate);
        TransportTicket end = new TransportTicket(getRandom().nextLong(),
                new Company(getRandom().nextLong(),
                        "name",
                        new LinkedList<>(Arrays.asList("allNames")),
                        new LinkedList<>(Arrays.asList(new Address("street", "number", "zipcode", "region", "city", "country"))),
                        new LinkedList<>(Arrays.asList(new EmailAddress(getRandom().nextLong(), "kd@kdls.dkl", null))),
                        new LinkedList<>(Arrays.asList(new TelephoneNumber(getRandom().nextLong(),
                                1,
                                2,
                                3,
                                null,
                                TelephoneNumber.TYPE_FAX)))),
                new LinkedList<>(Arrays.asList("y",endWaypoint)),
                endDate);
        Transport instance = new Transport(getRandom().nextLong(),
                new LinkedList<>(Arrays.asList(start, end)));
        String expResult = startDate+": "+startWaypoint+" -> "+endWaypoint;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}
