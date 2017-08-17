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
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import richtercloud.document.scanner.gui.Constants;
import richtercloud.reflection.form.builder.FieldInfo;
import richtercloud.reflection.form.builder.jpa.panels.IdGenerationValidation;
import richtercloud.reflection.form.builder.retriever.FieldGroup;
import richtercloud.reflection.form.builder.retriever.FieldGroups;
import richtercloud.reflection.form.builder.retriever.FieldPosition;

/**
 *
 * @author richter
 */
@Entity
@Inheritance
@FieldGroups(fieldGroups = @FieldGroup(name = Constants.EMPLOYMENT_FIELD_GROUP_NAME,
        beforeGroups = {Constants.TAGS_FIELD_GROUP_NAME, Constants.ID_FIELD_GROUP_NAME}))
public class Employment extends Identifiable {
    private static final long serialVersionUID = 1L;
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(groups = {Default.class, IdGenerationValidation.class})
    @FieldInfo(name = "Company", description = "The company where the employment took place")
    @FieldPosition(fieldGroup = Constants.EMPLOYMENT_FIELD_GROUP_NAME)
    private Company company;
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(groups = {Default.class, IdGenerationValidation.class})
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Begin", description = "The date of the beginning of the employment")
    @FieldPosition(fieldGroup = Constants.EMPLOYMENT_FIELD_GROUP_NAME)
    private Date theBegin;
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(groups = {Default.class, IdGenerationValidation.class})
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "End", description = "The date of the end of the employment")
    @FieldPosition(fieldGroup = Constants.EMPLOYMENT_FIELD_GROUP_NAME)
    private Date theEnd;

    protected Employment() {
    }

    public Employment(Company company,
            Date begin,
            Date end) {
        this.company = company;
        this.theBegin = begin;
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
     * @return the theBegin
     */
    public Date getTheBegin() {
        return this.theBegin;
    }

    /**
     * @param theBegin the theBegin to set
     */
    public void setTheBegin(Date theBegin) {
        this.theBegin = theBegin;
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

    @Override
    public String toString() {
        return String.format("%s -> %s at %s", this.getTheBegin(), this.getTheEnd(), this.getCompany());
    }

}
