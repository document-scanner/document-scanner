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
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.FieldInfo;

/**
 *
 * @author richter
 */
@Entity
@Inheritance
@ClassInfo(name="Telephone call")
public class TelephoneCall extends WorkflowItem {
    private static final long serialVersionUID = 1L;
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "End", description="The date and time of the end of the call")
    private Date theEnd;
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Transcription", description = "The transcription of the call")
    private String transcription;
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @FieldInfo(name = "Telephone number", description = "The telephone number used for the call")
    private TelephoneNumber telephoneNumber;

    protected TelephoneCall() {
    }

    public TelephoneCall(Date begin,
            Date end,
            String transcription,
            TelephoneNumber telephoneNumber,
            Company sender,
            Company recipient) {
        super(sender,
                recipient,
                begin);
        this.theEnd = end;
        this.transcription = transcription;
        this.telephoneNumber = telephoneNumber;
    }

    public TelephoneCall(Date begin,
            Date end,
            String transcription,
            TelephoneNumber telephoneNumber,
            Company sender,
            Company recipient,
            List<WorkflowItem> inReplyTo) {
        super(sender,
                recipient,
                begin,
                inReplyTo);
        this.theEnd = end;
        this.transcription = transcription;
        this.telephoneNumber = telephoneNumber;
    }

    /**
     * @return the theEnd
     */
    public Date getTheEnd() {
        return this.theEnd;
    }

    /**
     * @param theEnd the theEnd to set
     */
    public void setTheEnd(Date theEnd) {
        this.theEnd = theEnd;
    }

    /**
     * @return the transcription
     */
    public String getTranscription() {
        return this.transcription;
    }

    /**
     * @param transcription the transcription to set
     */
    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }

    public TelephoneNumber getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(TelephoneNumber telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", super.toString(), this.getTelephoneNumber());
    }
}
