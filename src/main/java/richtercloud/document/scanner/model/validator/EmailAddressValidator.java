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
import org.apache.commons.validator.routines.EmailValidator;

/**
 *
 * @author richter
 */
public class EmailAddressValidator implements ConstraintValidator<ValidEmailAddress, String> {
    public static final String MESSAGE_DEFAULT = "Not a valid finance account (specify either IBAN or both number and BLZ)";

    @Override
    public void initialize(ValidEmailAddress constraintAnnotation) {
        //do nothing
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return EmailValidator.getInstance(false //allowLocal
                ).isValid(value);
    }

}
