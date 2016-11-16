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

import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.FieldInfo;

/**
 * Describes part of a {@link Shipping} and is not itself a
 * {@link CommunicationItem} because {@link Shipping} already is.
 * @author richter
 */
/*
internal implementation notes:
- name prefixed with A in order to avoid collision with java.lang.Package
*/
@Entity
@Inheritance
@ClassInfo(name="Package")
public class APackage extends Identifiable {
    private static final long serialVersionUID = 1L;
    /**
     * The date and time (timestamp) of the delivery (by the delivery
     * service) (time is optional, but will be persisted when specified). This
     * property is used to specify different delivery times of a shipping with
     * more than one package and delivery at differnt times. The delivery time
     * of shipping should indicate the delivery of the last package in this
     * (rare) case. {@code null} if unknown.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Delivery", description = "The date of the delivery as specified by the delivery service")
    private Date deliveryDate;
    /**
     * The shipping reference this package belong to.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @FieldInfo(name = "Shipping", description = "The shipping of the package")
    private Shipping shipping;

    protected APackage() {
    }

    public APackage(Date deliveryDate, Shipping shipping) {
        this.deliveryDate = deliveryDate;
        this.shipping = shipping;
    }

    public Shipping getShipping() {
        return shipping;
    }

    public void setShipping(Shipping shipping) {
        this.shipping = shipping;
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
}
