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
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 *
 * @author richter
 */
/*
internal implementation notes:
- name prefixed with A in order to avoid collision with java.lang.Package
*/
@Entity
public class APackage extends CommunicationItem {
    private static final long serialVersionUID = 1L;
    /**
     * the date and time (timestamp) of the reception (time is optional, but
     * will be persisted when specified)
     */
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Basic(fetch = FetchType.EAGER)
    private Date reception;
    /**
     * the date and time (timestamp) of the delivery (by the sender) (time is
     * optional, but will be persisted when specified)
     */
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Basic(fetch = FetchType.EAGER)
    private Date delivery;
    @ManyToMany(fetch = FetchType.EAGER)
    private List<Shipping> shippings;

    protected APackage() {
    }

    public APackage(Long id, Company sender, Company receiver, Date reception, Date theDate, Date delivery) {
        super(id, sender, receiver, theDate);
        this.reception = reception;
        this.delivery = delivery;
    }

    /**
     * @return the reception
     */
    public Date getReception() {
        return this.reception;
    }

    /**
     * @param reception the reception to set
     */
    public void setReception(Date reception) {
        this.reception = reception;
    }

    /**
     * @return the delivery
     */
    public Date getDelivery() {
        return this.delivery;
    }

    /**
     * @param delivery the delivery to set
     */
    public void setDelivery(Date delivery) {
        this.delivery = delivery;
    }

    public List<Shipping> getShippings() {
        return Collections.unmodifiableList(this.shippings);
    }

    public void setShippings(List<Shipping> shippings) {
        this.shippings = shippings;
    }
}
