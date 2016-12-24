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
import java.util.List;
import org.jscience.economics.money.Currency;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;
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
        Company transportCompany = new Company("name",
                new LinkedList<>(Arrays.asList("allName")),
                new LinkedList<>(Arrays.asList(new Address("street",
                        "number",
                        "postofficebox",
                        "zipcode",
                        "region",
                        "city",
                        "country"))),
                new LinkedList<>(Arrays.asList(new EmailAddress("a@b.n",
                        null))),
                new LinkedList<>(Arrays.asList(new TelephoneNumber(1,
                        2,
                        3,
                        null,
                        TelephoneNumber.TYPE_LANDLINE))));
        Company recipient = new Company("name1",
                new LinkedList<>(Arrays.asList("allName")),
                new LinkedList<>(Arrays.asList(new Address("street",
                        "number",
                        "postofficebox",
                        "zipcode",
                        "region",
                        "city",
                        "country"))),
                new LinkedList<>(Arrays.asList(new EmailAddress("a@b.n",
                        null))),
                new LinkedList<>(Arrays.asList(new TelephoneNumber(1,
                        2,
                        3,
                        null,
                        TelephoneNumber.TYPE_LANDLINE))));
        List<String> waypoints = new LinkedList<>(Arrays.asList(startWaypoint,
                endWaypoint));
        Amount<Money> amount = Amount.valueOf(1, Currency.EUR);
        Location location = new Location("description");
        TransportTicket start = new TransportTicket(waypoints,
                startDate,
                amount,
                "comment",
                "identifier",
                endDate,
                endDate,
                location,
                true,
                true,
                transportCompany,
                recipient);
        TransportTicket end = new TransportTicket(waypoints,
                endDate,
                amount,
                "comment",
                "identifier",
                endDate,
                endDate,
                location,
                true,
                true,
                transportCompany,
                recipient);
        Transport instance = new Transport( new LinkedList<>(Arrays.asList(start, end)));
        String expResult = startDate+": "+startWaypoint+" -> "+endWaypoint;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}
