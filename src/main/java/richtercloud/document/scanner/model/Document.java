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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import richtercloud.document.scanner.components.annotations.OCRResult;
import richtercloud.document.scanner.components.annotations.ScanResult;

/**
 *
 * @author richter
 */
@Entity
public class Document extends AbstractDocument {
    private static final long serialVersionUID = 1L;
    private String comment;
    /**
     * a name for the document or a few words describing the context
     */
    @NotNull
    @Basic(fetch = FetchType.EAGER)
    private String identifier;
    @ScanResult
    @Basic(fetch = FetchType.LAZY) //fetch lazy as long as no issue occur
            //because this might quickly create performance impacts
    @Lob
    private byte[] scanData;
    @OCRResult
    @Basic(fetch = FetchType.LAZY)//fetch lazy as long as no issue occur
            //because this might quickly create performance impacts
    @Lob
    @Column(length = 1048576) //2^20; the column length needs to be set in order
    //to avoid a truncation error where any type (VARCHAR, CLOB, etc.) is cut to
    //length 255 which is the default
    private String scanOCRText;
    @ManyToMany(mappedBy = "documents", fetch = FetchType.EAGER)
    private List<Payment> payments;

    protected Document() {
    }

    public Document(String comment, String identifier, byte[] scanData, String scanOCRText, List<Payment> payments, Date date, Date receptionDate, Location originalLocation, boolean originalLost, Long id, Company sender, Company recipient) {
        super(date, receptionDate, originalLocation, originalLost, id, sender, recipient);
        this.comment = comment;
        this.identifier = identifier;
        this.scanData = scanData;
        this.scanOCRText = scanOCRText;
        this.payments = payments;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the scanData
     */
    public byte[] getScanData() {
        return this.scanData;
    }

    /**
     * @param scanData the scanData to set
     */
    public void setScanData(byte[] scanData) {
        this.scanData = scanData;
    }

    /**
     * @return the scanOCRText
     */
    public String getScanOCRText() {
        return this.scanOCRText;
    }

    /**
     * @param scanOCRText the scanOCRText to set
     */
    public void setScanOCRText(String scanOCRText) {
        this.scanOCRText = scanOCRText;
    }

    /**
     * @return the payments
     */
    public List<Payment> getPayments() {
        return Collections.unmodifiableList(this.payments);
    }

    /**
     * @param payments the payments to set
     */
    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

}
