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

import java.util.Currency;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.validation.constraints.NotNull;

/**
 *
 * @author richter
 */
@Entity
public class Bill extends Document {
    private static final long serialVersionUID = 1L;
    @NotNull
    @Basic(fetch = FetchType.EAGER)
    private float amount;
    @Basic(fetch = FetchType.EAGER)
    @NotNull
    private Currency currency;

    protected Bill() {
    }

    public Bill(float amount, Currency currency, String comment, String identifier, byte[] scanData, String scanOCRText, List<Payment> payments, Date date, Date receptionDate, Location originalLocation, boolean originalLost, Long id, Company sender, Company recipient) {
        super(comment, identifier, scanData, scanOCRText, payments, date, receptionDate, originalLocation, originalLost, id, sender, recipient);
        this.amount = amount;
        this.currency = currency;
    }

    /**
     * @return the amount
     */
    public float getAmount() {
        return this.amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(float amount) {
        this.amount = amount;
    }

    /**
     * @return the currency
     */
    public Currency getCurrency() {
        return this.currency;
    }

    /**
     * @param currency the currency to set
     */
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}
