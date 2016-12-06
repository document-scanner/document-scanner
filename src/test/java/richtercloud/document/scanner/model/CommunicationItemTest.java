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
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author richter
 */
public class CommunicationItemTest extends AbstractTest {

    /**
     * Test of toString method, of class CommunicationItem.
     */
    @Test
    public void testToString() {
        Long id = getRandom().nextLong();
        String senderName = "company";
        List<String> senderAllNames = new LinkedList<>(Arrays.asList("Some", "new", "company"));
        List<Address> senderAddresses = new LinkedList<>(Arrays.asList(new Address("street",
                "number",
                "postofficebox",
                "zipcode",
                "region",
                "city",
                "country")));
        List<EmailAddress> senderEmailAddresses = new LinkedList<>(Arrays.asList(new EmailAddress("company@bla.net",
                new LinkedList<String>())));
        List<TelephoneNumber> senderTelephoneNumbers = new LinkedList<>(Arrays.asList(new TelephoneNumber(0,
                0,
                0,
                null, TelephoneNumber.TYPE_LANDLINE)));
        Company sender = new Company(senderName, senderAllNames, senderAddresses, senderEmailAddresses, senderTelephoneNumbers);
        List<Address> recipientAddresses = new LinkedList<>(Arrays.asList(new Address("street",
                "number",
                "postofficebox",
                "zipcode",
                "region",
                "city",
                "country")));
        List<EmailAddress> recipientEmailAddresses = new LinkedList<>(Arrays.asList(new EmailAddress("recipient@blu.net", new LinkedList<String>())));
        List<TelephoneNumber> recipientTelephoneNumbers = new LinkedList<>(Arrays.asList(new TelephoneNumber(0, 0, 0, null, TelephoneNumber.TYPE_LANDLINE)));
        Company recipient = new Company("recipientCompany", new LinkedList<>(Arrays.asList("Some", "other", "company")), recipientAddresses, recipientEmailAddresses, recipientTelephoneNumbers);
        Date theDate = new Date();
        CommunicationItem instance = new CommunicationItemImpl(sender, recipient, theDate);
        String expResult = theDate+": "+sender.toString()+" -> "+recipient.toString();
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    private class CommunicationItemImpl extends CommunicationItem {
        private static final long serialVersionUID = 1L;

        CommunicationItemImpl(Company sender, Company recipient, Date theDate) {
            super(sender, recipient, theDate);
        }

    }

}
