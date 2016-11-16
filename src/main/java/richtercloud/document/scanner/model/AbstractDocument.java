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
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import richtercloud.reflection.form.builder.FieldInfo;

/**
 *
 * @author richter
 */
@MappedSuperclass
public abstract class AbstractDocument extends WorkflowItem {
    private static final long serialVersionUID = 1L;
    /**
     * The date and time (timestamp) of the actual reception. {@code null}
     * indicates that this date is unknown.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Received", description = "The date of the reception")
    private Date receptionDate;
    /**
     * Where the document can be found. {@code null} indicates that the location
     * of the original is unknown (default).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @FieldInfo(name = "Original location", description = "A pointer to the location of the original document")
    private Location originalLocation = null;
    /**
     * Whether the original is definitely lost. The original location might be
     * unknown, but the original not definitely lost. In this case the
     * {@code originalLocation} is {@code null} and {@code originalLost} is
     * {@code false}.
     */
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Original lost", description = "A flag indicating that the original is lost")
    private boolean originalLost = false;
    /**
     * Whether this is a digital document only.
     */
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Digital only", description = "A flag indicating that "
            + "the document initially exists in digital form only")
    private boolean digitalOnly = false;

    protected AbstractDocument() {
    }

    public AbstractDocument(Date receptionDate,
            Date theDate,
            Location originalLocation,
            boolean originalLost,
            boolean digitalOnly,
            Company sender,
            Company recipient) {
        super(sender,
                recipient,
                theDate);
        this.receptionDate = receptionDate;
        this.originalLocation = originalLocation;
        this.originalLost = originalLost;
        this.digitalOnly = digitalOnly;
    }

    /**
     * @return the receptionDate
     */
    public Date getReceptionDate() {
        return this.receptionDate;
    }

    /**
     * @param date the receptionDate to set
     */
    public void setReceptionDate(Date date) {
        this.receptionDate = date;
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

    /**
     * @return the digitalOnly
     */
    public boolean isDigitalOnly() {
        return digitalOnly;
    }

    /**
     * @param digitalOnly the digitalOnly to set
     */
    public void setDigitalOnly(boolean digitalOnly) {
        this.digitalOnly = digitalOnly;
    }
}
