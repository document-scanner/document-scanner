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
package richtercloud.document.scanner.model.validator;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import javax.validation.ConstraintValidatorContext;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import richtercloud.document.scanner.model.Person;
import richtercloud.document.scanner.model.TelephoneCall;
import richtercloud.document.scanner.model.TelephoneNumber;
import richtercloud.document.scanner.model.WorkflowItem;

/**
 *
 * @author richter
 */
public class WorkflowItemValidatorTest {

    /**
     * Test of validate method, of class WorkflowItemValidator.
     */
    @Test
    @Ignore //WorkflowItem don't work yet
    public void testValidate() throws Exception {
        TelephoneNumber telephoneNumber = new TelephoneNumber(49,
                123,
                456,
                null, //provider
                TelephoneNumber.TYPE_LANDLINE);
        Person sender = new Person(new LinkedList<>(Arrays.asList("Alice")),
                new LinkedList<>(Arrays.asList("A")),
                "Alice A",
                new LinkedList<>(Arrays.asList("Alice", "A")),
                new LinkedList<>(),
                new LinkedList<>(),
                new LinkedList<>());
        Person recipient = new Person(new LinkedList<>(Arrays.asList("Bob")),
                new LinkedList<>(Arrays.asList("B")),
                "Bob B",
                new LinkedList<>(Arrays.asList("Bob", "B")),
                new LinkedList<>(),
                new LinkedList<>(),
                new LinkedList<>());

        //simplest test case
        WorkflowItem item1 = new TelephoneCall(new Date(1000),
                new Date(1001),
                "transcription",
                telephoneNumber,
                sender,
                recipient);
        WorkflowItemValidator.validate(item1); //will throw
            //WorkflowItemValidationException if validation fails

        //test item1 having a reply
        WorkflowItem item2 = new TelephoneCall(new Date(2000),
                new Date(2001),
                "transcription",
                telephoneNumber,
                sender,
                recipient);
        item1.getFollowingItems().add(item2);
        try {
            WorkflowItemValidator.validate(item1);
            Assert.fail("exception expected because item1 is not in list of "
                    + "previous items of item2");
        }catch(WorkflowItemValidationException ex) {
            //expected
        }
        WorkflowItemValidator.validate(item2);
            //validate item2 without item1 in list of previous items...
        item2.getPreviousItems().add(item1);
        WorkflowItemValidator.validate(item2);
            //...and with

        //test exception on self-reference
        item1.getFollowingItems().add(item1);
        try {
            WorkflowItemValidator.validate(item1);
            Assert.fail("exception due to self-reference expected");
        }catch(WorkflowItemValidationException ex) {
            //expected
        }
        item1.getFollowingItems().remove(item1);
        item1.getPreviousItems().add(item1);
        try {
            WorkflowItemValidator.validate(item1);
            Assert.fail("exception due to self-reference expected");
        }catch(WorkflowItemValidationException ex) {
            //expected
        }
        item1.getPreviousItems().remove(item1);

        //test item1 being in a communication chain
        WorkflowItem item0 = new TelephoneCall(new Date(0),
                new Date(1),
                "transcription",
                telephoneNumber,
                sender,
                recipient);
        item0.getFollowingItems().add(item1);
        try {
            WorkflowItemValidator.validate(item0);
            Assert.fail("exception expected because item0 is not in list of "
                    + "previous items of item1");
        }catch(WorkflowItemValidationException ex) {
            //expected
        }
        WorkflowItemValidator.validate(item1);
        WorkflowItemValidator.validate(item2);
        item1.getPreviousItems().add(item0);
        WorkflowItemValidator.validate(item0);
        WorkflowItemValidator.validate(item1);
        WorkflowItemValidator.validate(item2);

        //test cycle detection
        item2.getFollowingItems().add(item0);
        try {
            WorkflowItemValidator.validate(item0);
            Assert.fail("expected exception due to cyclic reference");
        }catch(WorkflowItemValidationException ex) {
            //expected
        }
        try {
            WorkflowItemValidator.validate(item1);
            Assert.fail("expected exception due to cyclic reference");
        }catch(WorkflowItemValidationException ex) {
            //expected
        }
        try {
            WorkflowItemValidator.validate(item2);
            Assert.fail("expected exception due to cyclic reference");
        }catch(WorkflowItemValidationException ex) {
            //expected
        }
    }

    /**
     * Test of isValid method, of class WorkflowItemValidator. Doesn't test
     * context.
     */
    @Test
    @Ignore //WorkflowItem don't work yet
    public void testIsValid() {
        WorkflowItemValidator instance = new WorkflowItemValidator();
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        TelephoneNumber telephoneNumber = new TelephoneNumber(49,
                123,
                456,
                null, //provider
                TelephoneNumber.TYPE_LANDLINE);
        Person sender = new Person(new LinkedList<>(Arrays.asList("Alice")),
                new LinkedList<>(Arrays.asList("A")),
                "Alice A",
                new LinkedList<>(Arrays.asList("Alice", "A")),
                new LinkedList<>(),
                new LinkedList<>(),
                new LinkedList<>());
        Person recipient = new Person(new LinkedList<>(Arrays.asList("Bob")),
                new LinkedList<>(Arrays.asList("B")),
                "Bob B",
                new LinkedList<>(Arrays.asList("Bob", "B")),
                new LinkedList<>(),
                new LinkedList<>(),
                new LinkedList<>());

        //simplest test case
        WorkflowItem item1 = new TelephoneCall(new Date(),
                new Date(),
                "transcription",
                telephoneNumber,
                sender,
                recipient);
        boolean result = instance.isValid(item1, context); //will throw
            //WorkflowItemValidationException if validation fails
        assertTrue(result);

        //test item1 having a reply
        WorkflowItem item2 = new TelephoneCall(new Date(),
                new Date(),
                "transcription",
                telephoneNumber,
                sender,
                recipient);
        item1.getFollowingItems().add(item2);
        result = instance.isValid(item1, context);
        assertFalse("expected false because item1 is not in the list of "
                + "previous items of item2", result);
        result = instance.isValid(item2, context);
            //validate item2 without item1 in list of previous items...
        assertTrue(result);
        item2.getPreviousItems().add(item1);
        result = instance.isValid(item2, context);
            //...and with
        assertTrue(result);

        //test exception on self-reference
        item1.getFollowingItems().add(item1);
        result = instance.isValid(item1, context);
        assertFalse(result);
        item1.getFollowingItems().remove(item1);
        item1.getPreviousItems().add(item1);
        result = instance.isValid(item1, context);
        assertFalse(result);
        item1.getPreviousItems().remove(item1);

        //test item1 being in a communication chain
        WorkflowItem item0 = new TelephoneCall(new Date(),
                new Date(),
                "transcription",
                telephoneNumber,
                sender,
                recipient);
        item0.getFollowingItems().add(item1);
        result = instance.isValid(item0, context);
        assertFalse("expected false because item0 is not in list of previous "
                + "items of item1", result);
        result = instance.isValid(item1, context);
        assertTrue(result);
        result = instance.isValid(item2, context);
        assertTrue(result);
        item1.getPreviousItems().add(item0);
        result = instance.isValid(item0, context);
        assertTrue(result);
        result = instance.isValid(item1, context);
        assertTrue(result);
        result = instance.isValid(item2, context);
        assertTrue(result);

        //test failures
        item2.getFollowingItems().add(item0);
        result = instance.isValid(item0, context);
        assertFalse(result);
        result = instance.isValid(item1, context);
        assertFalse(result);
        result = instance.isValid(item2, context);
        assertFalse(result);
    }
}
