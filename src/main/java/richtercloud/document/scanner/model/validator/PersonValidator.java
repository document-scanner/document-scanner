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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import richtercloud.document.scanner.model.Person;

/**
 *
 * @author richter
 */
public class PersonValidator implements ConstraintValidator<ValidPerson, Person> {
    public static final String MESSAGE_DEFAULT = "Not a valid person (specify at least one firstname or one lastname)";

    @Override
    public void initialize(ValidPerson constraintAnnotation) {
        //do nothing
    }

    @Override
    public boolean isValid(Person value, ConstraintValidatorContext context) {
        context.buildConstraintViolationWithTemplate(MESSAGE_DEFAULT);
        return value.getFirstnames().size() > 0 || value.getLastnames().size() > 0;
    }

}
