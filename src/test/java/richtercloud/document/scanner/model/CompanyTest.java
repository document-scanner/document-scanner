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
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author richter
 */
public class CompanyTest extends AbstractTest {

    /**
     * Test of toString method, of class Company.
     */
    @Test
    public void testToString() {
        String name = "company";
        List<String> allNames = new LinkedList<>(Arrays.asList("Some", "old", "company"));
        List<Address> addresses = new LinkedList<>(Arrays.asList(new Address("street",
                "number",
                "postofficebox",
                "zipcode",
                "region",
                "city",
                "country")));
        List<EmailAddress> emailAddresses = new LinkedList<>(Arrays.asList(new EmailAddress("comp@bli.net",
                new LinkedList<String>())));
        List<TelephoneNumber> telephoneNumbers = new LinkedList<>(Arrays.asList(new TelephoneNumber(0,
                0,
                0,
                null,
                TelephoneNumber.TYPE_LANDLINE)));
        Company instance = new Company("",
                allNames,
                addresses,
                emailAddresses,
                telephoneNumbers);
        String expResult = "Some old company";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}
