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
package richtercloud.document.scanner.storage;

import javax.persistence.EntityManager;

/**
 *
 * @author richter
 */
public abstract class PersistenceStorage implements Storage<Object> {

    /**
     * a wrapper around {@link EntityManager#persist(java.lang.Object) }
     * @param object
     */
    @Override
    public void store(Object object) {
        this.retrieveEntityManager().persist(object);
    }

    /**
     * a wrapper around {@link EntityManager#find(java.lang.Class, java.lang.Object) }
     * @param id
     * @param clazz
     * @return
     */
    @Override
    public Object retrieve(Long id, Class<? extends Object> clazz) {
        Object retValue = this.retrieveEntityManager().find(clazz, id);
        return retValue;
    }

    /**
     * get the {@link EntityManager} used for persistent storage
     * @return
     */
    protected abstract EntityManager retrieveEntityManager();

}
