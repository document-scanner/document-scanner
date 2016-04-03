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
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.FieldInfo;

/**
 *
 * @author richter
 */
/*
internal implementation notes:
- name prefixed with A in order to avoid collision with java.lang.Package
*/
@Entity
@Inheritance
@ClassInfo(name="Package")
public class APackage extends CommunicationItem {
    private static final long serialVersionUID = 1L;
    /**
     * the date and time (timestamp) of the receptionDate (time is optional, but
 will be persisted when specified)
     */
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Reception", description = "The date of the reception (not necessarily the date of the reception)")
    private Date receptionDate;
    /**
     * the date and time (timestamp) of the deliveryDate (by the deliveryDate service)
 (time is optional, but will be persisted when specified)
     */
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Delivery", description = "The date of the delivery as specified by the delivery service")
    private Date deliveryDate;
    @ManyToMany(fetch = FetchType.EAGER)
    @FieldInfo(name = "Shippings", description = "A list of separate shippings")
    private List<Shipping> shippings;

    protected APackage() {
    }

    public APackage(Long id, Company sender, Company receiver, Date receptionDate, Date theDate, Date deliveryDate) {
        super(id, sender, receiver, theDate);
        this.receptionDate = receptionDate;
        this.deliveryDate = deliveryDate;
    }

    /**
     * @return the receptionDate
     */
    public Date getReceptionDate() {
        return this.receptionDate;
    }

    /**
     * @param receptionDate the receptionDate to set
     */
    public void setReceptionDate(Date receptionDate) {
        this.receptionDate = receptionDate;
    }

    /**
     * @return the deliveryDate
     */
    public Date getDeliveryDate() {
        return this.deliveryDate;
    }

    /**
     * @param deliveryDate the deliveryDate to set
     */
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public List<Shipping> getShippings() {
        return Collections.unmodifiableList(this.shippings);
    }

    public void setShippings(List<Shipping> shippings) {
        this.shippings = shippings;
    }
}
