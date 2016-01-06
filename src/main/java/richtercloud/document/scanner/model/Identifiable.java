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
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import richtercloud.reflection.form.builder.FieldInfo;

/**
 * A superclass for all entities which allows management of {@link Id} annotated
 * property.
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
*/
@MappedSuperclass
public abstract class Identifiable implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotNull
    @FieldInfo(name = "ID", description = "The unique ID used for storage in the database (automatic and save generation is supported by components)")
    private Long id;

    protected Identifiable() {
    }

    public Identifiable(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    protected void setId(Long id) {
        this.id = id;
    }
}
