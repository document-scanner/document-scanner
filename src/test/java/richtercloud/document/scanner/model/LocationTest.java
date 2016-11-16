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
public class LocationTest extends AbstractTest {

    /**
     * Test of toString method, of class Location.
     */
    @Test
    public void testToString() {
        String description = "description";
        Location instance = new Location(description);
        String expResult = description;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}
