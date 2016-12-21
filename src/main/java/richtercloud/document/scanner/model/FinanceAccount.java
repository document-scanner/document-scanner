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
import java.util.LinkedList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import richtercloud.document.scanner.model.validator.ValidFinanceAccount;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.FieldInfo;
import richtercloud.reflection.form.builder.jpa.panels.IdGenerationValidation;

/**
 * Represents a financial account which either has the {@code iban} property
 * set or the {@code blz} and {@code number} property (representing the german
 * BLZ and account number). The entity id is generated based on either.
 * @author richter
 */
@Entity
@Inheritance
@ValidFinanceAccount(groups = {Default.class, IdGenerationValidation.class})
@ClassInfo(name="Finance account")
public class FinanceAccount extends Identifiable {
    private static final long serialVersionUID = 1L;
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull
    @FieldInfo(name = "Owner", description = "The owner of the account")
    private Company owner;
    /**
     * Can be {@code null} if the BIC isn't necessary for the transfer for the
     * user.
     */
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "BIC", description = "The Business Identifier Code (BIC) of the business where the account is managed")
    private String bic;
    @NotNull
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "IBAN", description = "The International Bank Account Number (IBAN) of the account")
    private String iban;
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "BLZ", description = "The german Bankleitzahl (in case BIC is omitted)")
    private String blz;
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Number", description = "The account number (in case IBAN is omitted)")
    private String number;
    @OneToMany(fetch = FetchType.EAGER)
    @FieldInfo(name = "Payments", description = "A list of payments sent and received with this account")
    private List<Payment> payments = new LinkedList<>();

    protected FinanceAccount() {
    }

    public FinanceAccount(String bic,
            String iban,
            String blz,
            String number,
            List<Payment> payments, Company owner) {
        this.bic = bic;
        this.iban = iban;
        this.blz = blz;
        this.number = number;
        this.payments = payments;
        this.owner = owner;
    }

    /**
     * @return the bic
     */
    public String getBic() {
        return this.bic;
    }

    /**
     * @param bic the bic to set
     */
    public void setBic(String bic) {
        this.bic = bic;
    }

    /**
     * @return the iban
     */
    public String getIban() {
        return this.iban;
    }

    /**
     * @param iban the iban to set
     */
    public void setIban(String iban) {
        this.iban = iban;
    }

    /**
     * @return the blz
     */
    public String getBlz() {
        return this.blz;
    }

    /**
     * @param blz the blz to set
     */
    public void setBlz(String blz) {
        this.blz = blz;
    }

    /**
     * @return the number
     */
    public String getNumber() {
        return this.number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(String number) {
        this.number = number;
    }

    /**
     * @return the payments
     */
    public List<Payment> getPayments() {
        return Collections.unmodifiableList(this.payments);
    }

    /**
     * @param payments the payments to set
     */
    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    /**
     * @return the owner
     */
    public Company getOwner() {
        return this.owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(Company owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        if(this.getIban() != null && this.getBic() != null) {
            return String.format("%s %s of %s", this.getIban(), this.getBic(), this.getOwner());
        }else {
            return String.format("%s %s of %s", this.getNumber(), this.getBlz(), this.getOwner());
        }
    }

}
