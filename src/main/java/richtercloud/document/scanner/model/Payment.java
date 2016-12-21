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

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;
import richtercloud.reflection.form.builder.FieldInfo;
import richtercloud.reflection.form.builder.jpa.panels.IdGenerationValidation;

/**
 *
 * @author richter
 */
/*
internal implementation notes:
- There're been an account field of type FinanceAccount with description "The
account which has been used to send or receive this payment". It has been
removed because it's unclear and wasn't specified why this isn't covered by
sender and recipient properties.
*/
@Entity
@Inheritance
public class Payment extends Identifiable {
    private static final long serialVersionUID = 1L;
    @NotNull
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Amount", description = "The amount and currency of the payment")
    @Column(length = 8191) //avoid truncation error
    private Amount<Money> amount;
    /**
     * The exact date and time of (the transfer) of the payment.
     */
    /*
    internal implementation notes:
    - an exact timestamp avoids the need for a further property to distinct
    multiple payments from the same sender to the same recipient with the same
    amount
    */
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(groups = {Default.class, IdGenerationValidation.class})
    @FieldInfo(name = "Date", description = "The date of the payment")
    private Date theDate;
    /**
     * The sender to the payment.
     */
    /*
    internal implementation notes:
    - it doesn't make sense to describe payments between Companys because they
    can have multiple FinanceAccounts
    */
    @OneToOne(fetch = FetchType.EAGER)
    @NotNull(groups = {Default.class, IdGenerationValidation.class})
    @FieldInfo(name = "Sender", description = "The account from which the payment has been sent")
    private FinanceAccount sender;
    /**
     * The recipient of the payment.
     */
    /*
    internal implementation notes:
    - it doesn't make sense to describe payments between Companys because they
    can have multiple FinanceAccounts
    */
    @OneToOne(fetch = FetchType.EAGER)
    @NotNull(groups = {Default.class, IdGenerationValidation.class})
    @FieldInfo(name = "Recipient", description = "The account to which the payment has been sent")
    private FinanceAccount recipient;
    /**
     * where the payment is associated in
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @FieldInfo(name = "Documents", description = "A list of associated documents")
    private List<Document> documents = new LinkedList<>();

    protected Payment() {
    }

    public Payment(Amount<Money> amount,
            Date date,
            FinanceAccount sender,
            FinanceAccount recipient) {
        this.amount = amount;
        this.theDate = date;
        this.sender = sender;
        this.recipient = recipient;
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

    /**
     * @return the theDate
     */
    public Date getTheDate() {
        return this.theDate;
    }

    /**
     * @param theDate the theDate to set
     */
    public void setTheDate(Date theDate) {
        this.theDate = theDate;
    }

    /**
     * @return the sender
     */
    public FinanceAccount getSender() {
        return this.sender;
    }

    /**
     * @param sender the sender to set
     */
    public void setSender(FinanceAccount sender) {
        this.sender = sender;
    }

    /**
     * @return the recipient
     */
    public FinanceAccount getRecipient() {
        return this.recipient;
    }

    /**
     * @param recipient the recipient to set
     */
    public void setRecipient(FinanceAccount recipient) {
        this.recipient = recipient;
    }

    public List<Document> getDocuments() {
        return Collections.unmodifiableList(this.documents);
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s: %s", this.getSender(), this.getRecipient(), this.getAmount());
    }
}
