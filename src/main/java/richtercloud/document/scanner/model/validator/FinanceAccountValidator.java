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
import richtercloud.document.scanner.model.FinanceAccount;

/**
 *
 * @author richter
 */
public class FinanceAccountValidator implements ConstraintValidator<ValidFinanceAccount, FinanceAccount> {
    public static final String MESSAGE_DEFAULT = "Not a valid finance account (specify either IBAN or both number and BLZ)";

    @Override
    public void initialize(ValidFinanceAccount constraintAnnotation) {
        //do nothing
    }

    @Override
    public boolean isValid(FinanceAccount value, ConstraintValidatorContext context) {
        return value.getIban() != null || (value.getNumber() != null && value.getBlz() != null);
    }

}
