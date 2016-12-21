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
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import richtercloud.document.scanner.model.validator.ValidEmailAddress;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.FieldInfo;
import richtercloud.reflection.form.builder.jpa.panels.IdGenerationValidation;

/**
 *
 * @author richter
 */
@Entity
@Inheritance
@ClassInfo(name = "Email address")
public class EmailAddress extends Identifiable {
    private static final long serialVersionUID = 1L;
    @NotNull(groups = {Default.class, IdGenerationValidation.class})
    @ValidEmailAddress
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Address", description = "The email address")
    private String address;
    @ElementCollection(fetch = FetchType.EAGER)
    @FieldInfo(name = "PGP key IDs", description = "A list of PGP key IDs")
    private List<String> pgpKeyIds = new LinkedList<>();

    protected EmailAddress() {
    }

    public EmailAddress(String address,
            List<String> pgpKeyIds) {
        this.address = address;
        this.pgpKeyIds = pgpKeyIds;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the pgpKeyIds
     */
    public List<String> getPgpKeyIds() {
        return Collections.unmodifiableList(this.pgpKeyIds);
    }

    /**
     * @param pgpKeyIds the pgpKeyIds to set
     */
    public void setPgpKeyIds(List<String> pgpKeyIds) {
        this.pgpKeyIds = pgpKeyIds;
    }

    /*
    internal implementation notes:
    - don't include PGP key IDs because they're not interesting for toString
    return value
    */
    @Override
    public String toString() {
        return String.format("%s", this.address);
    }
}
