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

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import richtercloud.reflection.form.builder.FieldInfo;
import richtercloud.reflection.form.builder.jpa.panels.IdGenerationValidation;

/**
 *
 * @author richter
 */
@Entity
@Inheritance
public class Location extends Identifiable {
    private static final long serialVersionUID = 1L;
    @NotNull(groups = {Default.class, IdGenerationValidation.class}) //used for
            //id generation
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Description", description = "A description of the location identifying the location uniquely")
    private String description;

    protected Location() {
    }

    public Location(String description) {
        this.description = description;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.getDescription();
    }

}
