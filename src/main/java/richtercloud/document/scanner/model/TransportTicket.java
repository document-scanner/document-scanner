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
import java.util.LinkedList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.FieldInfo;
import richtercloud.reflection.form.builder.jpa.panels.IdGenerationValidation;

/**
 * Is a {@link Bill} mainly in order to reuse information, but in order to be
 * able to manage transport tickets for other persons it's good to have the
 * sender and recipient field to use them for the transport company and the
 * other person.
 *
 * The date refers to the date of the ticket purchase because it's closer to the
 * communication like in {@link Bill}. There's an extra field
 * {@code journeyDate} to store the beginning of the journey. The end of the
 * journey isn't worth being stored in the database and can be retrieved from
 * looking at the scan.
 *
 * @author richter
 */
@Entity
@Inheritance
@ClassInfo(name = "Transport ticket")
public class TransportTicket extends Bill {
    private static final long serialVersionUID = 1L;
    @Size(min=1, groups = {Default.class, IdGenerationValidation.class})
            //otherwise creation of TransportTicket doesn't make sense; used for
            //id generation
//    @NoEmptyEntriesList(groups = {Default.class, IdGenerationValidation.class})
    @ElementCollection(fetch = FetchType.EAGER)
    @FieldInfo(name = "Waypoints", description = "A list of waypoints of the ticket (stations, cities, coordinates")
    private List<String> waypoints = new LinkedList<>();
    /**
     * The date of the journey.
     */
    @Temporal(TemporalType.DATE)
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Journey date", description="The date of journey (if different from the date of buying the ticket)")
    /*
    internal implementation notes:
    - is allowed to be null because there might be small tickets without date
    information
    */
    private Date journeyDate;

    protected TransportTicket() {
    }

    public TransportTicket(List<String> waypoints,
            Date journeyDate,
            Amount<Money> amount,
            String comment,
            String identifier,
            Date date,
            Date receptionDate,
            Location originalLocation,
            boolean originalLost,
            boolean digitalOnly,
            Company sender,
            Company recipient) {
        super(amount,
                comment,
                identifier,
                date,
                receptionDate,
                originalLocation,
                originalLost,
                digitalOnly,
                sender,
                recipient);
        this.waypoints = waypoints;
        this.journeyDate = journeyDate;
    }

    /**
     * @return the waypoints
     */
    /*
    internal implementation notes:
    - returning an unmodifiable collection causes merging entities to fail
    */
    public List<String> getWaypoints() {
        return this.waypoints;
    }

    /**
     * @param waypoints the waypoints to set
     */
    public void setWaypoints(List<String> waypoints) {
        this.waypoints = waypoints;
    }

    /**
     * @return the journeyDate
     */
    public Date getJourneyDate() {
        return this.journeyDate;
    }

    /**
     * @param journeyDate the journeyDate to set
     */
    public void setJourneyDate(Date journeyDate) {
        this.journeyDate = journeyDate;
    }

    @Override
    public String toString() {
        return String.format("%s: %s -> %s", this.getJourneyDate(), this.getWaypoints().get(0), this.getWaypoints().get(this.getWaypoints().size()-1));
    }
}
