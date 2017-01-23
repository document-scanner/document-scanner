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
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import richtercloud.document.scanner.components.annotations.Invisible;
import richtercloud.document.scanner.components.annotations.Tags;
import richtercloud.reflection.form.builder.FieldInfo;
import richtercloud.reflection.form.builder.jpa.annotations.UsedUpdate;

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
internal implementation notes:
- Since there're entities which don't have properties which allow to use them
as a business key, IDs will be manually assigned using IdGenerator. Assignment
is enforced in constructor.
obsolete:
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
@Inheritance
public abstract class Identifiable implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
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
    @ElementCollection(fetch = FetchType.EAGER)
    /*
    internal implementation notes:
    - List<String> fails in OpenJPA due to `org.apache.openjpa.util.InternalException: Unexpected attribute type "String" for persistence-capable class "richtercloud.document.scanner.model.FinanceAccount" is detected.`
    -> this is an inacceptable error in OpenJPA (in case OpenJPA is the only
    remaining JPA provider which can be used while keeping basic sanity try to
    introduce a Tag embeddable)
    */
    private Set<String> tags = new HashSet<>();
    /**
     * The timestamp when the entity was last modified or used as a reference.
     * Updated automatically by JPA provider via {@link PrePersist},
     * {@link PreUpdate} and {@code PostLoad} hook.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Invisible
    @FieldInfo(name = "Last used", description = "When the entity was last modified/written to storage or used in a relationship")
    /*
    internal implementation notes:
    - Is a java.util.Date because otherwise weaving with Eclipselink 2.6.4 fails
    due to `org.eclipse.persistence.exceptions.ValidationException
Exception Description: The type [class java.sql.Timestamp] for the attribute [lastUsed] on the entity class [class richtercloud.document.scanner.model.Identifiable] is not a valid type for a temporal mapping. The attribute must be defined as java.util.Date or java.util.Calendar.`
    Don't annotate with richtercloud.reflection.form.builder.annotations.Skip
    because the value should still appear in query component tables (like ID,
    but it shouldn't be editable -> omit in DocumentScannerFieldHandler via
    Invisible annotation)
    */
    private Date lastUsed;

    protected Identifiable() {
    }

    @PrePersist
    @PreUpdate
    @UsedUpdate
    private void updateLastModifiedTimestamp() {
        this.lastUsed = new Date(System.currentTimeMillis());
    }

    public Long getId() {
        return this.id;
    }

    /**
     * IDs are supposed to be set using an {@link IdApplier}.
     * @param id the ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }

    /**
     * {@link Object#hashCode() } based on the {@code id}.
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.id);
        return hash;
    }

    /**
     * {@link Object#equals(java.lang.Object) () } based on the {@code id}.
     * @return {@code true} if {@code obj} has the same {@code id} as this
     * instance
     */
    /*
    internal implementation notes:
    - equals needs to be implemented in order to make lazily loaded entities
    work
    */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Identifiable other = (Identifiable) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
}
