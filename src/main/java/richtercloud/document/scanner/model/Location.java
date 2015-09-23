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

/**
 *
 * @author richter
 */
@Entity
public class Location extends Identifiable {
    private static final long serialVersionUID = 1L;
    @Basic(fetch = FetchType.EAGER)
    private String description;

    protected Location() {
    }

    public Location(Long id, String description) {
        super(id);
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

}
