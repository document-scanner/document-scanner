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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import richtercloud.reflection.form.builder.FieldInfo;

/**
 *
 * @author richter
 */
@Entity
@Inheritance
public class Shipping extends WorkflowItem {
    private static final long serialVersionUID = 1L;
    @Size(min = 1) //otherwise creating a shipping doesn't make sense
    @FieldInfo(name = "Packages", description = "A list of packages which make this shipping")
    @OneToMany(mappedBy = "shipping", fetch = FetchType.EAGER)
    private List<APackage> packages = new LinkedList<>();
    /**
     * The date and time (timestamp) of the deliveryDate (by the delivery
     * service) (time is optional, but will be persisted when specified).
     */
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Delivery", description = "The date of the delivery as specified by the delivery service")
    private Date deliveryDate;

    protected Shipping() {
    }

    public Shipping(List<APackage> packages,
            Company sender,
            Company recipient,
            Date theDate,
            Date received) {
        super(sender,
                recipient,
                theDate);
        this.packages = packages;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
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
