/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package richtercloud.document.scanner.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.MappedSuperclass;
import richtercloud.document.scanner.components.annotations.Tags;
import richtercloud.reflection.form.builder.FieldInfo;

/**
 * A superclass for all entities which allows management of {@link Id} annotated
 * property.
 *
 * There's no large sense in giving the user the ability to determine the ID
 * manually. It's simpler to provide GUI components which show the ID and
 * changes to it, but don't allow editing it. There's also no usecase for
 * retrieving instances with IDs based on properties and maintaining the
 * programmatic creation of those is a huge effort and a large source of errors.
 *
 * @author richter
 */
/*
- In order to provide a common super type for entities and embeddables which
don't need an id the generation of an id is moved to SelfIdentifiable -> this
requires passing down a type for the root entity or embeddable because it needs
to include Identifiables as well as SelfIdentifiables and a separate type for
the id generator -> don't handle id generation in entity as it's just wrong and
overkill in the code complexity and use IdGenerator as id factory (such a
factory can still be enforced as reference here). Using a factory requires to
duplicate the inheritance hierarchy of ids in the factory inheritance, though
(using a generic class parameter to enforce this is inacceptable because the
code gets very ugly and duplicating the hierarchy manually is an inacceptable
source of mistakes) -> keep the factory code in a central factory class (which
is IdGenerator and subclasses) and manage dependencies of id generation there
and only there.
- Switching to @GeneratedValue caused the storage of entities with identical
important properties to be possible (e.g. two companies with the same name).
This is fine if it's handled well (including warnings for the user and internal
checks). The switch should have been documented which apparently isn't the case.
 */
@MappedSuperclass
@Inheritance
public abstract class Identifiable implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    @FieldInfo(name = "ID", description = "The unique ID used for storage in the database (automatic and save generation is supported by components)")
    /*
    internal implementation notes:
    - there should be no need for a @NotNull annotation because JPA provider
    will handle everything
    - a @FieldInfo makes sense because the ID field will still be visible in a
    read-only component
    */
    private Long id;
    @Tags
    @FieldInfo(name="Tags", description = "A list of tags which can be freely associated with the entity")
    private Set<String> tags = new HashSet<>();

    protected Identifiable() {
    }

    public Long getId() {
        return this.id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    public Set<String> getTags() {
        return tags;
    }

    protected void setTags(Set<String> tags) {
        this.tags = tags;
    }
}
