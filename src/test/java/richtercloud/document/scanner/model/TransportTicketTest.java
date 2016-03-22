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
public class TransportTicketTest extends AbstractTest {

    /**
     * Test of toString method, of class TransportTicket.
     */
    @Test
    public void testToString() {
        long dateValue = System.currentTimeMillis();
        Date date = new Date(dateValue);
        String waypointStart = "a";
        String waypointEnd = "x";
        Company transportCompany = new Company(getRandom().nextLong(),
                "name",
                new LinkedList<>(Arrays.asList("allName")),
                new LinkedList<>(Arrays.asList(new Address("street", "number", "zipcode", "region", "city", "country"))),
                new LinkedList<>(Arrays.asList(new EmailAddress(getRandom().nextLong(), "a@b.n", null))),
                new LinkedList<>(Arrays.asList(new TelephoneNumber(getRandom().nextLong(),
                        1,
                        2,
                        3,
                        null,
                        TelephoneNumber.TYPE_LANDLINE))));
        TransportTicket instance = new TransportTicket(getRandom().nextLong(),
                transportCompany,
                new LinkedList<>(Arrays.asList(waypointStart, "k", waypointEnd)),
                date);
        String expResult = date+": "+waypointStart+" -> "+waypointEnd;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}
