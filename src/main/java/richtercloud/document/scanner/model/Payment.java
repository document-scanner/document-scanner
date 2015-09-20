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
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 *
 * @author richter
 */
@Entity
public class Payment extends Identifiable {
    private static final long serialVersionUID = 1L;
    @OneToOne
    @NotNull
    private FinanceAccount account;
    @NotNull
    private float amount;
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
    @NotNull
    private Date theDate;
    /**
     * The sender to the payment.
     */
    /*
    internal implementation notes:
    - it doesn't make sense to describe payments between Companys because they
    can have multiple FinanceAccounts
    */
    @OneToOne
    @NotNull
    private FinanceAccount sender;
    /**
     * The recipient of the payment.
     */
    /*
    internal implementation notes:
    - it doesn't make sense to describe payments between Companys because they
    can have multiple FinanceAccounts
    */
    @OneToOne
    @NotNull
    private FinanceAccount recipient;
    /**
     * where the payment is associated in
     */
    @ManyToMany
    private List<Document> documents;

    protected Payment() {
    }

    public Payment(Long id, FinanceAccount account, float amount, Date date, FinanceAccount sender, FinanceAccount recipient) {
        super(id);
        this.account = account;
        this.amount = amount;
        this.theDate = date;
        this.sender = sender;
        this.recipient = recipient;
    }

    /**
     * @return the account
     */
    public FinanceAccount getAccount() {
        return this.account;
    }

    /**
     * @param account the account to set
     */
    public void setAccount(FinanceAccount account) {
        this.account = account;
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
}
