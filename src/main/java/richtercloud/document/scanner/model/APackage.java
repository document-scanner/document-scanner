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
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author richter
 */
/*
internal implementation notes:
- name prefixed with A in order to avoid collision with java.lang.Package
*/
@Entity
public class APackage implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    /**
     * the date and time (timestamp) of the reception (time is optional, but
     * will be persisted when specified)
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date reception;
    /**
     * the date and time (timestamp) on the package (e.g. the sending time)
     * (time is optional, but will be persisted when specified)
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date theDate;
    /**
     * the date and time (timestamp) of the delivery (by the sender) (time is
     * optional, but will be persisted when specified)
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date delivery;
    @ManyToMany
    private List<Shipping> shippings;

    protected APackage() {
    }

    public APackage(Long id, Date reception, Date date, Date delivery) {
        this.id = id;
        this.reception = reception;
        this.theDate = date;
        this.delivery = delivery;
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
     * @return the theDate
     */
    public Date getTheDate() {
        return this.theDate;
    }

    /**
     * @param theDate the theDate to set
     */
    public void setTheDate(Date theDate) {
        this.theDate = theDate;
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
