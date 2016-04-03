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
import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;
import richtercloud.document.scanner.model.validator.NoEmptyEntriesList;
import richtercloud.reflection.form.builder.FieldInfo;
import richtercloud.reflection.form.builder.jpa.panels.IdGenerationValidation;

/**
 *
 * @author richter
 */
@Entity
@Inheritance
public class Company extends Identifiable {
    private static final long serialVersionUID = 1L;
    @FieldInfo(name = "Name", description = "A unique name for the contact "
            + "excluding all extra names which ought to be specified in the "
            + "complete name list")
    @NotNull(groups = {Default.class, IdGenerationValidation.class})
    @Size(min = 1)
    @Basic(fetch = FetchType.EAGER)
    private String name;
    @FieldInfo(name = "Complete name list", description = "A list of all names "
            + "associated with this contact")
    @ElementCollection(fetch = FetchType.EAGER)
    @NoEmptyEntriesList
    private List<String> allNames;
    @FieldInfo(name = "Addresses", description = "Multiple contacts can have "
            + "the same address (shared office) and a contact can have multiple "
            + "addresses.")
    @ElementCollection(fetch = FetchType.EAGER)
    @NoEmptyEntriesList
    private List<Address> addresses;
    @FieldInfo(name = "Email addresses", description = "One company can have "
            + "multiple email addresses.")
    @OneToMany(fetch = FetchType.EAGER)
    /*
    internal implementation notes:
    - It's very unlikely that two contacts share the same email address.
    */
    @NoEmptyEntriesList
    private List<EmailAddress> emails;
    @FieldInfo(name = "Finance accounts", description = "A list of finance "
            + "accounts owned by the contact or somehow associated with it.")
    @OneToMany(fetch = FetchType.EAGER)
    @NoEmptyEntriesList
    private List<FinanceAccount> accounts;
    @FieldInfo(name = "Telephone numbers", description = "A list of telephone "
            + "numbers owned by the contact or somehow associated with it.")
    @OneToMany(fetch = FetchType.EAGER)
    @NoEmptyEntriesList
    private List<TelephoneNumber> telephoneNumbers;

    protected Company() {
    }

    public Company(Long id, String name, List<String> allNames, List<Address> addresses, List<EmailAddress> emails, List<TelephoneNumber> telephoneNumbers) {
        super(id);
        this.name = name;
        this.allNames = allNames;
        this.addresses = addresses;
        this.emails = emails;
        this.telephoneNumbers = telephoneNumbers;
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

    public List<TelephoneNumber> getTelephoneNumbers() {
        return telephoneNumbers;
    }

    public void setTelephoneNumbers(List<TelephoneNumber> telephoneNumbers) {
        this.telephoneNumbers = telephoneNumbers;
    }

    @Override
    public String toString() {
        if(allNames != null && !allNames.isEmpty()) {
            return String.join(" ", allNames);
        }else  {
            return name;
        }
    }
}
