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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.OneToOne;
import richtercloud.document.scanner.ifaces.ImageWrapper;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.FieldInfo;

/**
 * Wraps the relationship between {@link Employment} and {@link Document}.
 * @author richter
 */
@Entity
@Inheritance
@ClassInfo(name="Withdrawal")
public class Withdrawal extends Document {
    private static final long serialVersionUID = 1L;
    @OneToOne(fetch = FetchType.EAGER)
    @FieldInfo(name = "Employment", description = "The employment which has "
            + "been ended through this withdrawal")
    private Employment employment;

    protected Withdrawal() {
    }

    public Withdrawal(Employment employment,
            String comment,
            String identifier,
            List<ImageWrapper> scanData,
            String scanOCRText,
            List<Payment> payments,
            Date date,
            Date receptionDate,
            Location originalLocation,
            boolean originalLost,
            boolean digitalOnly,
            Company sender,
            Company recipient) {
        super(comment,
                identifier,
                scanData,
                scanOCRText,
                payments,
                date,
                receptionDate,
                originalLocation,
                originalLost,
                digitalOnly,
                sender,
                recipient);
        this.employment = employment;
    }

    /**
     * @return the employment
     */
    public Employment getEmployment() {
        return employment;
    }

    /**
     * @param employment the employment to set
     */
    public void setEmployment(Employment employment) {
        this.employment = employment;
    }
}
