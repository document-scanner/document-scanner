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
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import richtercloud.document.scanner.components.annotations.CommunicationTree;
import richtercloud.document.scanner.model.validator.ValidWorkflowItem;
import richtercloud.reflection.form.builder.FieldInfo;

/**
 * In order to facilitate a lot of calculation and data managing the
 * {@code previousItems} relationship is a double linked list together with
 * {@code inResponseTo}. Both properties are validated with quite an effort to
 * make sure that it's a real double linked list. It doesn't hurt that there's a
 * visualization on both fields.
 *
 * @author richter
 */
/*
internal implementation notes:
- needs to be an Entity in order to be referencable in Workflow
- working with double linked list implementations might cause trouble in JPA,
not investigated further
*/
@Entity
@ValidWorkflowItem
public abstract class WorkflowItem extends CommunicationItem {
    private static final long serialVersionUID = 1L;
    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "followingItems", cascade = CascadeType.PERSIST)
    @CommunicationTree
    @FieldInfo(name = "Previous items", description="Communication items to "
            + "which this item is a reply")
    private List<WorkflowItem> previousItems = new LinkedList<>();
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    //@CommunicationTree //there's no need to a second communication tree
        //because referenced ought to be updated correctly due to mappedBy
        //parameter of ManyToMany on previousItems
    @FieldInfo(name = "Following Items", description="Communication items "
            + "which have been sent before this list of replies")
    private List<WorkflowItem> followingItems = new LinkedList<>();

    protected WorkflowItem() {
    }

    public WorkflowItem(Company sender,
            Company recipient,
            Date theDate) {
        super(sender,
                recipient,
                theDate);
    }
    public WorkflowItem(Company sender,
            Company recipient,
            Date theDate,
            List<WorkflowItem> inReplyTo) {
        this(sender,
                recipient,
                theDate);
        this.previousItems = inReplyTo;
    }


    public List<WorkflowItem> getPreviousItems() {
        return previousItems;
    }

    public void setPreviousItems(List<WorkflowItem> previousItems) {
        this.previousItems = previousItems;
    }

    public List<WorkflowItem> getFollowingItems() {
        return followingItems;
    }

    public void setFollowingItems(List<WorkflowItem> followingItems) {
        this.followingItems = followingItems;
    }
}
