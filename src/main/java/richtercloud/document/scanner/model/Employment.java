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

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author richter
 */
@Entity
public class Employment implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    @ManyToOne
    private Company company;
    @Temporal(TemporalType.TIMESTAMP)
    private Date theStart;
    @Temporal(TemporalType.TIMESTAMP)
    private Date theEnd;

    protected Employment() {
    }

    public Employment(Long id, Company company, Date start, Date end) {
        this.id = id;
        this.company = company;
        this.theStart = start;
        this.theEnd = end;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
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
    public Date getStart() {
        return this.theStart;
    }

    /**
     * @param start the theStart to set
     */
    public void setStart(Date start) {
        this.theStart = start;
    }

    /**
     * @return the theEnd
     */
    public Date getEnd() {
        return this.theEnd;
    }

    /**
     * @param end the theEnd to set
     */
    public void setEnd(Date end) {
        this.theEnd = end;
    }
    
}
