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
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 *
 * @author richter
 */
@Entity
public class FinanceAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    private String bic;
    private String iban;
    private String blz;
    private String number;
    @OneToMany
    private List<Payment> payments;

    protected FinanceAccount() {
    }

    public FinanceAccount(Long id, String bic, String iban, String blz, String number, List<Payment> payments) {
        this.id = id;
        this.bic = bic;
        this.iban = iban;
        this.blz = blz;
        this.number = number;
        this.payments = payments;
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
    
}
