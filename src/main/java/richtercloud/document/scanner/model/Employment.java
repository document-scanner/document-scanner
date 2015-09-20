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
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 *
 * @author richter
 */
@Entity
public class Employment extends Identifiable {
    private static final long serialVersionUID = 1L;
    @ManyToOne
    @NotNull
    private Company company;
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date theStart;
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date theEnd;

    protected Employment() {
    }

    public Employment(Long id, Company company, Date start, Date end) {
        super(id);
        this.company = company;
        this.theStart = start;
        this.theEnd = end;
    }

    /**
     * @return the company
     */
    public Company getCompany() {
        return this.company;
    }

    /**
     * @param company the company to set
     */
    public void setCompany(Company company) {
        this.company = company;
    }

    /**
     * @return the theStart
     */
    public Date getTheStart() {
        return this.theStart;
    }

    /**
     * @param theStart the theStart to set
     */
    public void setTheStart(Date theStart) {
        this.theStart = theStart;
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

}
