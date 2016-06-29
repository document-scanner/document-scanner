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

/**
 *
 * @author richter
 */
/*
internal implementation notes:
- needs to be an Entity in order to be referencable in Workflow
*/
@Entity
public abstract class WorkflowItem extends CommunicationItem {
    private static final long serialVersionUID = 1L;

    protected WorkflowItem() {
    }

    public WorkflowItem(Long id, Company sender, Company recipient, Date theDate) {
        super(id, sender, recipient, theDate);
    }
}
