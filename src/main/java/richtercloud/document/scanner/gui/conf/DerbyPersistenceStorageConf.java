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
package richtercloud.document.scanner.gui.conf;

import java.io.Serializable;
import richtercloud.document.scanner.gui.DocumentScanner;
import richtercloud.document.scanner.storage.DefaultPersistenceStorage;

/**
 *
 * @author richter
 */
public class DerbyPersistenceStorageConf implements Serializable, StorageConf<DefaultPersistenceStorage> {
    private static final long serialVersionUID = 1L;
    private static DefaultPersistenceStorage instance;
    private final static String CONNECTION_URL_DEFAULT = "localhost";
    private final static String USERNAME_DEFAULT = "";
    private final static String PASSWORD_DEFAULT = "";
    private final static String STORAGE_PARENT_DIR_DEFAULT = "";
    private final static String DATABASE_DIR_NAME_DEFAULT = DocumentScanner.DATABASE_DIR_NAME_DEFAULT;
    private String connectionURL = CONNECTION_URL_DEFAULT;
    private String username = USERNAME_DEFAULT;
    private String password = PASSWORD_DEFAULT;
    private String storageParentDir = STORAGE_PARENT_DIR_DEFAULT;
    private String databaseDirName = DATABASE_DIR_NAME_DEFAULT;

    public DerbyPersistenceStorageConf() {
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the storageParentDir
     */
    public String getStorageParentDir() {
        return storageParentDir;
    }

    /**
     * @param storageParentDir the storageParentDir to set
     */
    public void setStorageParentDir(String storageParentDir) {
        this.storageParentDir = storageParentDir;
    }

    /**
     * @return the databaseDirName
     */
    public String getDatabaseDirName() {
        return databaseDirName;
    }

    /**
     * @param databaseDirName the databaseDirName to set
     */
    public void setDatabaseDirName(String databaseDirName) {
        this.databaseDirName = databaseDirName;
    }

    /**
     * @return the connectionURL
     */
    public String getConnectionURL() {
        return connectionURL;
    }

    /**
     * @param connectionURL the connectionURL to set
     */
    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    @Override
    public DefaultPersistenceStorage getStorage() {
        if(instance == null) {
            instance = new DefaultPersistenceStorage(DocumentScanner.ENTITY_MANAGER_FACTORY.createEntityManager());
        }
        return instance;
    }
}
