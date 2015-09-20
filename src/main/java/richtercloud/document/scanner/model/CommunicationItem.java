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
import javax.validation.constraints.NotNull;

/**
 *
 * @author richter
 */
@MappedSuperclass
public abstract class CommunicationItem extends Identifiable {
    private static final long serialVersionUID = 1L;
    @ManyToOne
    @NotNull
    private Company sender;
    @ManyToOne
    @NotNull
    private Company recipient;
    /**
     * The data and time (timestamp) indicated on the document.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date theDate;

    protected CommunicationItem() {
    }

    public CommunicationItem(Long id, Company sender, Company recipient, Date theDate) {
        super(id);
        this.sender = sender;
        this.recipient = recipient;
        this.theDate = theDate;
    }

    /**
     * @return the sender
     */
    public Company getContact() {
        return this.getSender();
    }

    /**
     * @param contact the sender to set
     */
    public void setContact(Company contact) {
        this.setSender(contact);
    }

    /**
     * @return the sender
     */
    public Company getSender() {
        return this.sender;
    }

    /**
     * @param sender the sender to set
     */
    public void setSender(Company sender) {
        this.sender = sender;
    }

    /**
     * @return the recipient
     */
    public Company getRecipient() {
        return this.recipient;
    }

    /**
     * @param recipient the recipient to set
     */
    public void setRecipient(Company recipient) {
        this.recipient = recipient;
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
}
