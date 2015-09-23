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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 *
 * @author richter
 */
@Entity
public class TelephoneCall extends CommunicationItem {
    private static final long serialVersionUID = 1L;
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Basic(fetch = FetchType.EAGER)
    private Date theEnd;
    @Basic(fetch = FetchType.EAGER)
    private String transcription;

    protected TelephoneCall() {
    }

    public TelephoneCall(Date begin, Date end, String transcription, Long id, Company sender, Company recipient) {
        super(id, sender, recipient, begin);
        this.theEnd = end;
        this.transcription = transcription;
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
}
