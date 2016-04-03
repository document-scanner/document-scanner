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
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;
import richtercloud.document.scanner.components.annotations.OCRResult;
import richtercloud.document.scanner.components.annotations.ScanResult;
import richtercloud.reflection.form.builder.FieldInfo;
import richtercloud.reflection.form.builder.jpa.panels.IdGenerationValidation;

/**
 *
 * @author richter
 */
@Entity
@Inheritance
public class TransportTicket extends Identifiable {
    private static final long serialVersionUID = 1L;
    @NotNull(groups = {Default.class, IdGenerationValidation.class}) //used for
            //id generation
    @ManyToOne(fetch = FetchType.EAGER)
    @FieldInfo(name = "Transport company", description = "A reference to the transport company (in form of contact information)")
    private Company transportCompany;
    @Size(min=1, groups = {Default.class, IdGenerationValidation.class})
            //otherwise creation of TransportTicket doesn't make sense; used for
            //id generation
//    @NoEmptyEntriesList(groups = {Default.class, IdGenerationValidation.class})
    @ElementCollection(fetch = FetchType.EAGER)
    @FieldInfo(name = "Waypoints", description = "A list of waypoints of the ticket (stations, cities, coordinates")
    private List<String> waypoints;
    @NotNull(groups = {Default.class, IdGenerationValidation.class})
    @Temporal(TemporalType.DATE)
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Date", description="The date of ticket (begin of the journey")
    private Date theDate;
    @ScanResult
    @Basic(fetch = FetchType.LAZY) //fetch lazy as long as no issue occur
            //because this might quickly create performance impacts
    @Lob
    @FieldInfo(name = "Scan data", description = "The binary data of the scan")
    private byte[] scanData;
    @OCRResult
    @Basic(fetch = FetchType.LAZY)//fetch lazy as long as no issue occur
            //because this might quickly create performance impacts
    @Lob
    @Column(length = 1048576) //2^20; the column length needs to be set in order
    //to avoid a truncation error where any type (VARCHAR, CLOB, etc.) is cut to
    //length 255 which is the default
    @FieldInfo(name= "Scan OCR text", description = "The text which has been retrieved by OCR")
    private String scanOCRText;

    protected TransportTicket() {
    }

    public TransportTicket(Long id, Company transportCompany, List<String> waypoints, Date theDate) {
        super(id);
        this.transportCompany = transportCompany;
        this.waypoints = waypoints;
        this.theDate = theDate;
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

    public void setScanOCRText(String scanOCRText) {
        this.scanOCRText = scanOCRText;
    }

    public String getScanOCRText() {
        return scanOCRText;
    }

    public void setScanData(byte[] scanData) {
        this.scanData = scanData;
    }

    public byte[] getScanData() {
        return scanData;
    }

    @Override
    public String toString() {
        return String.format("%s: %s -> %s", this.getTheDate(), this.getWaypoints().get(0), this.getWaypoints().get(this.getWaypoints().size()-1));
    }
}
