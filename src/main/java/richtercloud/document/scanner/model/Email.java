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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.FieldInfo;

/**
 *
 * @author richter
 */
@Entity
@Inheritance
@ClassInfo(name="Email")
public class Email extends WorkflowItem {
    private static final long serialVersionUID = 1L;
    @ManyToOne(fetch = FetchType.EAGER)
    @FieldInfo(name = "Sender address", description = "The email address of the sender")
    private EmailAddress senderAddress;
    @OneToMany(fetch = FetchType.EAGER)
    @FieldInfo(name = "CC recipients", description = "The email addresses of CC recipients")
    private List<EmailAddress> ccRecipientAddresses = new LinkedList<>();
    @OneToMany(fetch = FetchType.EAGER)
    @FieldInfo(name = "BCC recipients", description = "The email addresses of BCC recipients")
    private List<EmailAddress> bccRecipientAddresses = new LinkedList<>();

    protected Email() {
    }

    public Email(EmailAddress senderAddress,
            List<EmailAddress> ccRecipientAddresses,
            List<EmailAddress> bccRecipientAddresses,
            Company sender,
            Company recipient,
            Date theDate) {
        super(sender,
                recipient,
                theDate);
        this.senderAddress = senderAddress;
        this.ccRecipientAddresses = ccRecipientAddresses;
        this.bccRecipientAddresses = bccRecipientAddresses;
    }

    /**
     * @return the senderAddress
     */
    public EmailAddress getSenderAddress() {
        return senderAddress;
    }

    /**
     * @param senderAddress the senderAddress to set
     */
    public void setSenderAddress(EmailAddress senderAddress) {
        this.senderAddress = senderAddress;
    }

    /**
     * @return the ccRecipientAddresses
     */
    public List<EmailAddress> getCcRecipientAddresses() {
        return ccRecipientAddresses;
    }

    /**
     * @param ccRecipientAddresses the ccRecipientAddresses to set
     */
    public void setCcRecipientAddresses(List<EmailAddress> ccRecipientAddresses) {
        this.ccRecipientAddresses = ccRecipientAddresses;
    }

    /**
     * @return the bccRecipientAddresses
     */
    public List<EmailAddress> getBccRecipientAddresses() {
        return bccRecipientAddresses;
    }

    /**
     * @param bccRecipientAddresses the bccRecipientAddresses to set
     */
    public void setBccRecipientAddresses(List<EmailAddress> bccRecipientAddresses) {
        this.bccRecipientAddresses = bccRecipientAddresses;
    }
}
