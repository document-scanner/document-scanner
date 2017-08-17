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

import java.util.LinkedList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.OneToMany;
import richtercloud.document.scanner.gui.Constants;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.FieldInfo;
import richtercloud.reflection.form.builder.retriever.FieldGroup;
import richtercloud.reflection.form.builder.retriever.FieldGroups;
import richtercloud.reflection.form.builder.retriever.FieldPosition;

/**
 *
 * @author richter
 */
@Entity
@Inheritance
@ClassInfo(name="Workflow")
@FieldGroups(fieldGroups = @FieldGroup(name = Constants.WORKFLOW_FIELD_GROUP_NAME,
        beforeGroups = {Constants.TAGS_FIELD_GROUP_NAME, Constants.ID_FIELD_GROUP_NAME}))
public class Workflow extends Identifiable {
    private static final long serialVersionUID = 1L;
    @OneToMany(fetch = FetchType.EAGER)
    @FieldInfo(name = "Items", description = "The items which make up this "
            + "workflow")
    @FieldPosition(fieldGroup = Constants.WORKFLOW_FIELD_GROUP_NAME)
    private List<WorkflowItem> items = new LinkedList<>();

    protected Workflow() {
    }

    /**
     * @return the items
     */
    public List<WorkflowItem> getItems() {
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(List<WorkflowItem> items) {
        this.items = items;
    }
}
