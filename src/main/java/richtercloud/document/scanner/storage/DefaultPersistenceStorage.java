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

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.slf4j.LoggerFactory;

/**
 *
 * @author richter
 */
public class DefaultPersistenceStorage extends PersistenceStorage {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PersistenceStorage.class);
    private Connection conn;
    private EntityManager entityManager;

    public DefaultPersistenceStorage(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    protected EntityManager retrieveEntityManager() {
        return this.entityManager;
    }

    public void recreateEntityManager(EntityManagerFactory entityManagerFactory, String connectionURL, String username, String password) {
        Map<String, String> properties = new HashMap<>(3);
        properties.put("javax.persistence.jdbc.url", connectionURL);
        properties.put("javax.persistence.jdbc.user", username);
        properties.put("javax.persistence.jdbc.password", password);
        this.entityManager = entityManagerFactory.createEntityManager(properties);
    }
}
