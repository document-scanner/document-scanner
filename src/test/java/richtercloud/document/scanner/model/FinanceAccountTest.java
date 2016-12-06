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
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author richter
 */
public class FinanceAccountTest extends AbstractTest {

    /**
     * Test of toString method, of class FinanceAccount.
     */
    @Test
    public void testToString() {
        String bic = "BIC";
        String iban = "iban";
        String number = "number";
        String blz = "blz";
        Company owner = new Company("name",
                        new LinkedList<>(Arrays.asList("name")),
                        new LinkedList<>(Arrays.asList(new Address("street",
                                "number",
                                "postofficebox",
                                "zipcode",
                                "region",
                                "city",
                                "country"))),
                        new LinkedList<>(Arrays.asList(new EmailAddress("a@b.com",
                                null))),
                        new LinkedList<>(Arrays.asList(new TelephoneNumber(49,
                                123,
                                456789,
                                null,
                                TelephoneNumber.TYPE_LANDLINE))));
        FinanceAccount instance = new FinanceAccount(bic,
                iban,
                blz,
                number,
                new LinkedList<Payment>(),
                owner);
        String expResult = iban+" "+bic+" of "+owner;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}
