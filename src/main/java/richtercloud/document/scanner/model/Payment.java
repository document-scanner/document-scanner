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

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author richter
 */
@Entity
public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    @OneToOne
    private FinanceAccount account;
    private float amount;
    @Temporal(TemporalType.TIMESTAMP)
    private Date theDate;
    @OneToOne
    private FinanceAccount sender;
    @OneToOne
    private FinanceAccount recipient;
    /**
     * where the payment is associated in
     */
    @ManyToMany
    private List<Document> documents;

    protected Payment() {
    }

    public Payment(Long id, FinanceAccount account, float amount, Date date, FinanceAccount sender, FinanceAccount recipient) {
        this.id = id;
        this.account = account;
        this.amount = amount;
        this.theDate = date;
        this.sender = sender;
        this.recipient = recipient;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
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
    public Date getDate() {
        return this.theDate;
    }

    /**
     * @param date the theDate to set
     */
    public void setDate(Date date) {
        this.theDate = date;
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
