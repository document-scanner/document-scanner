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
import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

/**
 *
 * @author richter
 */
@Entity
public class Company extends Identifiable {
    private static final long serialVersionUID = 1L;
    @NotNull
    private String name;
    @ElementCollection
    private List<String> allNames;
    /**
     * Multiple contacts can have the same address (shared office) and a contact
     * can have multiple addresses.
     */
    @ElementCollection
    private List<Address> addresses;
    /**
     * One {@code Company} can have multiple email addresses, but it's very
     * unlikely that two contacts share the same email address.
     */
    @OneToMany
    private List<EmailAddress> emails;
    @OneToMany
    private List<FinanceAccount> accounts;

    protected Company() {
    }

    public Company(Long id, String name, List<String> allNames, List<Address> addresses, List<EmailAddress> emails) {
        super(id);
        this.name = name;
        this.allNames = allNames;
        this.addresses = addresses;
        this.emails = emails;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the addresses
     */
    public List<Address> getAddresses() {
        return Collections.unmodifiableList(this.addresses);
    }

    /**
     * @param addresses the addresses to set
     */
    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    /**
     * @return the allNames
     */
    public List<String> getAllNames() {
        return Collections.unmodifiableList(this.allNames);
    }

    /**
     * @param allNames the allNames to set
     */
    public void setAllNames(List<String> allNames) {
        this.allNames = allNames;
    }

    /**
     * @return the emails
     */
    public List<EmailAddress> getEmails() {
        return Collections.unmodifiableList(this.emails);
    }

    /**
     * @param emails the emails to set
     */
    public void setEmails(List<EmailAddress> emails) {
        this.emails = emails;
    }

    /**
     * @return the accounts
     */
    public List<FinanceAccount> getAccounts() {
        return Collections.unmodifiableList(this.accounts);
    }

    /**
     * @param accounts the accounts to set
     */
    public void setAccounts(List<FinanceAccount> accounts) {
        this.accounts = accounts;
    }
}
