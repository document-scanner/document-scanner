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
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author richter
 */
@MappedSuperclass
public abstract class AbstractDocument extends CommunicationItem {
    private static final long serialVersionUID = 1L;
    /**
     * the date and time (timestamp) of the actual reception
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date received;
    /**
     * the data and time (timestamp) of the reception indicated on the document
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date receptionDate;
    @ManyToOne
    private Location originalLocation;
    private boolean originalLost;

    protected AbstractDocument() {
    }

    public AbstractDocument(Date received, Date receptionDate, Location originalLocation, boolean originalLost, Long id, Company contact) {
        super(id, contact);
        this.received = received;
        this.receptionDate = receptionDate;
        this.originalLocation = originalLocation;
        this.originalLost = originalLost;
    }

    /**
     * @return the received
     */
    public Date getReceived() {
        return this.received;
    }

    /**
     * @param date the received to set
     */
    public void setReceived(Date date) {
        this.received = date;
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
     * @return the originalLocation
     */
    public Location getOriginalLocation() {
        return this.originalLocation;
    }

    /**
     * @param originalLocation the originalLocation to set
     */
    public void setOriginalLocation(Location originalLocation) {
        this.originalLocation = originalLocation;
    }

    /**
     * @return the originalLost
     */
    public boolean isOriginalLost() {
        return this.originalLost;
    }

    /**
     * @param originalLost the originalLost to set
     */
    public void setOriginalLost(boolean originalLost) {
        this.originalLost = originalLost;
    }
}
