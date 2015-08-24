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
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

/**
 *
 * @author richter
 */
@Entity
public class Shipping extends CommunicationItem {
    private static final long serialVersionUID = 1L;
    @ManyToMany(mappedBy = "shippings")
    private List<APackage> packages;

    protected Shipping() {
    }

    public Shipping(List<APackage> packages, Long id, Company contact) {
        super(id, contact);
        this.packages = packages;
    }

    /**
     * @return the packages
     */
    public List<APackage> getPackages() {
        return Collections.unmodifiableList(this.packages);
    }

    /**
     * @param packages the packages to set
     */
    public void setPackages(List<APackage> packages) {
        this.packages = packages;
    }
}
