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
public class EmailAddressTest extends AbstractTest {

    /**
     * Test of toString method, of class EmailAddress.
     */
    @Test
    public void testToString() {
        String address = "x@y.com";
        List<String> pgpKeyIds = new LinkedList<>(Arrays.asList("1e2", "22"));
        EmailAddress instance = new EmailAddress(address,
                pgpKeyIds);
        String expResult = address;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}
