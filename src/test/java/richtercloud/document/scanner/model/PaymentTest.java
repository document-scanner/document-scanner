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
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author richter
 */
public class PaymentTest extends AbstractTest {

    /**
     * Test of toString method, of class Payment.
     */
    @Test
    public void testToString() {
        FinanceAccount account = new FinanceAccount(getRandom().nextLong(),
                "bic",
                "iban",
                "blz",
                "number",
                new LinkedList<Payment>(),
                new Company(getRandom().nextLong(),
                        "name",
                        new LinkedList<>(Arrays.asList("allNames")),
                        new LinkedList<>(Arrays.asList(new Address("street", "number", "zipcode", "region", "city", "country"))),
                        new LinkedList<>(Arrays.asList(new EmailAddress(getRandom().nextLong(), "address", null))),
                        new LinkedList<>(Arrays.asList(new TelephoneNumber(getRandom().nextLong(),
                                40,
                                123,
                                456789,
                                null,
                                TelephoneNumber.TYPE_LANDLINE)))));

        FinanceAccount sender = new FinanceAccount(getRandom().nextLong(),
                "bic",
                "iban",
                "blz",
                "number",
                new LinkedList<Payment>(),
                new Company(getRandom().nextLong(),
                        "name",
                        new LinkedList<>(Arrays.asList("allNames")),
                        new LinkedList<>(Arrays.asList(new Address("street", "number", "zipcode", "region", "city", "country"))),
                        new LinkedList<>(Arrays.asList(new EmailAddress(getRandom().nextLong(), "address", null))),
                        new LinkedList<>(Arrays.asList(new TelephoneNumber(getRandom().nextLong(),
                                40,
                                123,
                                456789,
                                null,
                                TelephoneNumber.TYPE_LANDLINE)))));
        FinanceAccount recipient = new FinanceAccount(getRandom().nextLong(),
                "bic",
                "iban",
                "blz",
                "number",
                new LinkedList<Payment>(),
                new Company(getRandom().nextLong(),
                        "name",
                        new LinkedList<>(Arrays.asList("allNames")),
                        new LinkedList<>(Arrays.asList(new Address("street", "number", "zipcode", "region", "city", "country"))),
                        new LinkedList<>(Arrays.asList(new EmailAddress(getRandom().nextLong(), "address", null))),
                        new LinkedList<>(Arrays.asList(new TelephoneNumber(getRandom().nextLong(), 40, 123, 456789, null, TelephoneNumber.TYPE_LANDLINE)))));
        Amount<Money> amount = Amount.valueOf(10, Money.BASE_UNIT);
        Payment instance = new Payment(getRandom().nextLong(), account, amount,
                new Date(), sender, recipient);
        String expResult = account+": "+sender+" -> "+recipient+": "+amount;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}
