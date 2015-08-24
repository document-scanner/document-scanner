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
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author richter
 */
@Entity
public class EmailAddress implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    private String address;
    @ElementCollection
    private List<String> pgpKeyIds;

    protected EmailAddress() {
    }

    public EmailAddress(Long id, String address, List<String> pgpKeyIds) {
        this.id = id;
        this.address = address;
        this.pgpKeyIds = pgpKeyIds;
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
}
