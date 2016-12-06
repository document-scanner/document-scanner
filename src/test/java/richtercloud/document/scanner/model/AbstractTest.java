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
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author richter
 */
public class AbstractTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(CommunicationItemTest.class);
    /**
     * Creates a random contact.
     * @param random
     * @return the created contact
     */
    public static Company createContact(Random random) {
        List<String> allNames = new LinkedList<>();
        int nameCount = random.nextInt(5);
        for(int i=0; i<nameCount; i++) {
            allNames.add("name"+random.nextInt());
        }
        List<Address> addresses = new LinkedList<>();
        int addressCount = random.nextInt(3);
        for(int i=0; i<addressCount; i++) {
            addresses.add(new Address("street"+random.nextInt(),
                    "number"+random.nextInt(),
                    "postofficebox"+random.nextInt(),
                    "zipcode"+random.nextInt(),
                    "region"+random.nextInt(),
                    "city"+random.nextInt(),
                    "country"+random.nextInt()));
        }
        List<EmailAddress> emails = new LinkedList<>();
        int emailCount = random.nextInt(3);
        for(int i=0; i<emailCount; i++) {
            emails.add(new EmailAddress(random.nextInt()+"@bla.com",
                    new LinkedList<>(Arrays.asList(String.valueOf(random.nextInt())))));
        }
        List<TelephoneNumber> telephoneNumbers = new LinkedList<>();
        int telephoneNumberCount = random.nextInt(3);
        for(int i=0; i<telephoneNumberCount; i++) {
            telephoneNumbers.add(new TelephoneNumber(random.nextInt(100),
                    random.nextInt(999),
                    random.nextInt(10000),
                    null,
                    TelephoneNumber.TYPE_LANDLINE));
        }
        Company retValue = new Company("name"+random.nextInt(),
                allNames,
                addresses, emails, telephoneNumbers);
        return retValue;
    }
    private final Random random;

    public AbstractTest() {
        long seed = System.currentTimeMillis();
        LOGGER.info(String.format("initializing random with seed %d", seed));
        this.random = new Random(seed);
    }

    public Random getRandom() {
        return random;
    }

}
