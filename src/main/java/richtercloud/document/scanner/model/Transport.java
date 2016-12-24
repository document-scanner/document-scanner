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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.ManyToMany;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;
import richtercloud.reflection.form.builder.FieldInfo;
import richtercloud.reflection.form.builder.jpa.panels.IdGenerationValidation;

/**
 *
 * @author richter
 */
@Entity
@Inheritance
public class Transport extends Identifiable {
    private static final long serialVersionUID = 1L;
    @ManyToMany(fetch = FetchType.EAGER)
    @Size(min = 1, groups = {Default.class, IdGenerationValidation.class}) //otherwise creating a Transport doesn't make sense; used for id generation
    @FieldInfo(name = "Tickets", description = "A list of tickets which make up this transport")
    private List<TransportTicket> tickets = new LinkedList<>();

    protected Transport() {
    }

    public Transport(List<TransportTicket> tickets) {
        this.tickets = tickets;
    }

    /**
     * @return the tickets
     */
    public List<TransportTicket> getTickets() {
        return Collections.unmodifiableList(this.tickets);
    }

    /**
     * @param tickets the tickets to set
     */
    public void setTickets(List<TransportTicket> tickets) {
        this.tickets = tickets;
    }

    @Override
    public String toString() {
        if(this.getTickets() == null || this.getTickets().isEmpty()) {
            return "(empty)";
        }
        assert !this.getTickets().contains(null);
        TransportTicket firstTicket = this.getTickets().get(0);
        TransportTicket lastTicket = this.getTickets().get(this.getTickets().size()-1);
        return String.format("%s: %s -> %s",
                firstTicket.getJourneyDate(),
                firstTicket.getWaypoints().get(0),
                lastTicket.getWaypoints().get(lastTicket.getWaypoints().size()-1));
    }

}
