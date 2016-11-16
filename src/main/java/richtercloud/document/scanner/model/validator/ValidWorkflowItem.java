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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 *
 * @author richter
 */
@Target( { ElementType.TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = WorkflowItemValidator.class)
@Documented
public @interface ValidWorkflowItem {
    String message() default "Not a valid workflow item (contains cyclic "
            + "references in list of previous or following messages or "
            + "references don't create a double-linked list)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
