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
import javax.persistence.ManyToOne;

/**
 *
 * @author richter
 */
@Entity
public class TransportTicket implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    @ManyToOne
    private Company transportCompany;
    @ElementCollection
    private List<String> waypoints;

    protected TransportTicket() {
    }

    public TransportTicket(Company transportCompany, List<String> waypoints) {
        this.transportCompany = transportCompany;
        this.waypoints = waypoints;
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
     * @return the transportCompany
     */
    public Company getTransportCompany() {
        return this.transportCompany;
    }

    /**
     * @param transportCompany the transportCompany to set
     */
    public void setTransportCompany(Company transportCompany) {
        this.transportCompany = transportCompany;
    }

    /**
     * @return the waypoints
     */
    public List<String> getWaypoints() {
        return Collections.unmodifiableList(this.waypoints);
    }

    /**
     * @param waypoints the waypoints to set
     */
    public void setWaypoints(List<String> waypoints) {
        this.waypoints = waypoints;
    }
}
