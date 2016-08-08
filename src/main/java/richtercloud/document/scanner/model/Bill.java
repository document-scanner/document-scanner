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

import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;
import richtercloud.reflection.form.builder.FieldInfo;

/**
 * Encapsulates a bill. The date when the bill has been paid is indicated by the
 * date of associated payments (see {@link Document#getPayments() }).
 * @author richter
 */
@Entity
@Inheritance
public class Bill extends Document {
    private static final long serialVersionUID = 1L;
    @NotNull
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Amount", description = "The amount and currency of the bill")
    private Amount<Money> amount;

    protected Bill() {
    }

    public Bill(Amount<Money> amount,
            String comment,
            String identifier,
            byte[] scanData,
            String scanOCRText,
            List<Payment> payments,
            Date date,
            Date receptionDate,
            Location originalLocation,
            boolean originalLost,
            boolean digitalOnly,
            Long id,
            Company sender,
            Company recipient) {
        super(comment,
                identifier,
                scanData,
                scanOCRText,
                payments,
                date,
                receptionDate,
                originalLocation,
                originalLost,
                digitalOnly,
                id,
                sender,
                recipient);
        this.amount = amount;
    }

    /**
     * @return the amount
     */
    public Amount<Money> getAmount() {
        return this.amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(Amount<Money> amount) {
        this.amount = amount;
    }
}
