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
public class TelephoneCallTest extends AbstractTest {

    /**
     * Test of toString method, of class TelephoneCall.
     */
    @Test
    public void testToString() {
        long beginValue = System.currentTimeMillis()-1000;
        Date begin = new Date(beginValue);
        Date end = new Date(beginValue-10000);
        TelephoneNumber senderTelephoneNumber = new TelephoneNumber(getRandom().nextLong(),
                33,
                123,
                456789,
                null,
                TelephoneNumber.TYPE_LANDLINE);
        Company sender = new Company(getRandom().nextLong(),
                "name",
                new LinkedList<>(Arrays.asList("allNames")),
                new LinkedList<>(Arrays.asList(new Address("street", "number", "zipcode", "region", "city", "country"))),
                new LinkedList<>(Arrays.asList(new EmailAddress(getRandom().nextLong(), "a@b.com", null))),
                new LinkedList<>(Arrays.asList(senderTelephoneNumber)));
        Company recipient = new Company(getRandom().nextLong(),
                "name",
                new LinkedList<>(Arrays.asList("allNames")),
                new LinkedList<>(Arrays.asList(new Address("street", "number", "zipcode", "region", "city", "country"))),
                new LinkedList<>(Arrays.asList(new EmailAddress(getRandom().nextLong(), "a@b.com", null))),
                new LinkedList<>(Arrays.asList(new TelephoneNumber(getRandom().nextLong(),
                        33,
                        123,
                        456789,
                        null,
                        TelephoneNumber.TYPE_LANDLINE))));
        TelephoneNumber telephoneNumber = new TelephoneNumber(getRandom().nextLong(),
                        55,
                        123,
                        456789,
                        null,
                        TelephoneNumber.TYPE_LANDLINE);
        TelephoneCall instance = new TelephoneCall(begin,
                end,
                "transcription",
                getRandom().nextLong(),
                sender,
                recipient,
                telephoneNumber);
        String expResult = begin+": "+sender+" -> "+recipient+" ("+telephoneNumber+")";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}