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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import richtercloud.document.scanner.model.WorkflowItem;

/**
 *
 * @author richter
 */
public class WorkflowItemValidator implements ConstraintValidator<ValidWorkflowItem, WorkflowItem> {

    public static void validate(WorkflowItem value) throws WorkflowItemValidationException {
        //validate that value isn't contained in the list of previous or
        //following items/doesn't reference itself
        if(value.getFollowingItems().contains(value)) {
            throw new WorkflowItemValidationException(String.format("workflow item '%s' is contained in its list of following items", value));
        }
        if(value.getPreviousItems().contains(value)) {
            throw new WorkflowItemValidationException(String.format("workflow item '%s' is contained in its list of previous items", value));
        }

        //validate that all previous items of each item list them in their lists
        //of following items
        Queue<WorkflowItem> itemQueue = new LinkedList<>(Arrays.asList(value));
        Set<WorkflowItem> seen = new HashSet<>(); //checks for loops
        while(!itemQueue.isEmpty()) {
            WorkflowItem head = itemQueue.poll();
            for(WorkflowItem headPrevious : head.getPreviousItems()) {
                if(!headPrevious.getFollowingItems().contains(head)) {
                    throw new WorkflowItemValidationException(String.format(
                            "previous workflow item %s of item %s is not "
                                    + "contained in list of following items of "
                                    + "the former", headPrevious, head));
                }
                if(seen.contains(headPrevious)) {
                    throw new WorkflowItemValidationException("list of "
                            + "previous items of item %s contains a refence "
                            + "loop in previous items");
                }
            }
            itemQueue.addAll(head.getPreviousItems());
            seen.add(head);
        }
        //validate that all following items of each item list them in their
        //lists of previous items
        itemQueue = new LinkedList<>(Arrays.asList(value));
        seen = new HashSet<>();
        while(!itemQueue.isEmpty()) {
            WorkflowItem head = itemQueue.poll();
            for(WorkflowItem headFollowing : head.getFollowingItems()) {
                if(!headFollowing.getPreviousItems().contains(head)) {
                    throw new WorkflowItemValidationException(String.format(
                            "following workflow item %s of item %s is not "
                                    + "contained in list of previous items of "
                                    + "the former", headFollowing, head));
                }
                if(seen.contains(headFollowing)) {
                    throw new WorkflowItemValidationException("list of "
                            + "previous items of item %s contains a refence "
                            + "loop in following items");
                }
            }
            itemQueue.addAll(head.getFollowingItems());
            seen.add(head);
        }
    }

    @Override
    public void initialize(ValidWorkflowItem constraintAnnotation) {
        //do nothing
    }

    @Override
    public boolean isValid(WorkflowItem value, ConstraintValidatorContext context) {
        try {
            validate(value);
            return true;
        }catch(WorkflowItemValidationException ex) {
            context.buildConstraintViolationWithTemplate(ex.getMessage());
            return false;
        }
    }
}
