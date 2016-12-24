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

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author richter
 */
public class AddressTest {

    /**
     * Test of toString method, of class Address.
     */
    @Test
    public void testToString() {
        String street = "street";
        String number = "1";
        String zipcode = "12345";
        String region = "region";
        String city = "city";
        String country = "country";
        Address instance = new Address(street,
                number,
                null, //postOfficeBox
                zipcode,
                region,
                city,
                country);
        String expResult = street+" "+number+", "+zipcode+" "+city+" ("+region+"), "
                +country;
        String result = instance.toString();
        assertEquals(expResult, result);
        String postOfficeBox = "POB 123";
        instance = new Address(street,
                number,
                postOfficeBox,
                zipcode,
                region,
                city,
                country);
        expResult = street+" "+number+"/"+postOfficeBox+", "+zipcode+" "+city+" ("+region+"), "
                +country;
        result = instance.toString();
        assertEquals(expResult, result);
    }
}
