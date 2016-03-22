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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import richtercloud.document.scanner.gui.DocumentScanner;
import richtercloud.document.scanner.storage.DerbyPersistenceStorage;
import richtercloud.reflection.form.builder.message.MessageHandler;

/**
 *
 * @author richter
 */
public class DerbyPersistenceStorageConf implements Serializable, StorageConf<DerbyPersistenceStorage, DerbyPersistenceStorageConfInitializationException> {
    private static final long serialVersionUID = 1L;
    private static DerbyPersistenceStorage instance;
    private final static String CONNECTION_URL_DEFAULT = "localhost";
    private final static String USERNAME_DEFAULT = "";
    private final static String PASSWORD_DEFAULT = "";
    private final static String DATABASE_DIR_NAME_DEFAULT = DocumentScanner.DATABASE_DIR_NAME_DEFAULT;
    public final static String SCHEME_CHECKSUM_FILE_NAME = "last-scheme.xml";

    /**
     * Generates a checksum to track changes to {@code clazz} from the hash codes of declared fields and methods (tracking both might cause redundancies, but increases safety of getting all changes of database relevant properties).
     *
     * This could be used to generate {@code serialVersionUID}, but shouldn't be necessary.
     *
     * Doesn't care about constructors since they have no influence on database schemes.
     *
     * @param clazz
     * @return
     */
    public static long generateSchemeChecksum(Class<?> clazz) {
        long retValue = 0L;
        for(Field field : clazz.getDeclaredFields()) {
            retValue += field.hashCode();
        }
        for(Method method : clazz.getDeclaredMethods()) {
            retValue += method.hashCode();
        }
        return retValue;
    }

    private static Map<Class<?>, Long> generateSchemeChecksumMap(Set<Class<?>> classes) {
        Map<Class<?>, Long> retValue = new HashMap<>();
        for(Class<?> clazz: classes) {
            long checksum = generateSchemeChecksum(clazz);
            retValue.put(clazz, checksum);
        }
        return retValue;
    }

    private String connectionURL = CONNECTION_URL_DEFAULT;
    private String username = USERNAME_DEFAULT;
    private String password = PASSWORD_DEFAULT;
    private String storageParentDirPath;
    private String databaseDirName = DATABASE_DIR_NAME_DEFAULT;
    private EntityManager entityManager;
    private File schemeChecksumFile;
    private MessageHandler messageHandler;
    private Set<Class<?>> entityClasses;

    protected DerbyPersistenceStorageConf() {
    }

    protected DerbyPersistenceStorageConf(File schemeChecksumFile) {
        super();
        this.schemeChecksumFile = schemeChecksumFile;
    }

    /**
     *
     * @param entityManager
     * @param messageHandler
     * @param entityClasses
     * @param schemeChecksumFile
     * @throws FileNotFoundException
     * @throws IOException
     */
    /*
    internal implementation notes:
    - it appears more elegant to enforce specification of scheme checksum file
    in constructor rather than provide a constructor with a parent directory
    argument
    */
    public DerbyPersistenceStorageConf(EntityManager entityManager,
            MessageHandler messageHandler,
            Set<Class<?>> entityClasses,
            File schemeChecksumFile) throws FileNotFoundException, IOException {
        if(!schemeChecksumFile.exists()) {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(schemeChecksumFile))) {
                Map<Class<?>, Long> checksumMap = generateSchemeChecksumMap(entityClasses);
                objectOutputStream.writeObject(checksumMap);
                objectOutputStream.flush();
            }
        }
        this.schemeChecksumFile = schemeChecksumFile;
        this.entityManager = entityManager;
        this.messageHandler = messageHandler;
        this.entityClasses = entityClasses;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return this.username;
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
        return this.password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the storageParentDirPath
     */
    public String getStorageParentDirPath() {
        return this.storageParentDirPath;
    }

    /**
     * @param storageParentDirPath the storageParentDirPath to set
     */
    public void setStorageParentDirPath(String storageParentDirPath) {
        this.storageParentDirPath = storageParentDirPath;
    }

    /**
     * @return the databaseDirName
     */
    public String getDatabaseDirName() {
        return this.databaseDirName;
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
        return this.connectionURL;
    }

    /**
     * @param connectionURL the connectionURL to set
     */
    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    @Override
    public DerbyPersistenceStorage getStorage() {
        if(instance == null) {
            instance = new DerbyPersistenceStorage(this.entityManager);
        }
        return instance;
    }

    /**
     * Retrieves a persisted version of the database scheme (stored in
     * {@code lastSchemeStorageFile} and fails the validation if it doesn't
     * match with the current set of classes.
     */
    /*
    internal implementation notes:
    - Metamodel implementations don't reliably implement `equals` (e.g.
    `org.hibernate.jpa.internal.metamodel.Metamodel` doesn't
    - java.lang.reflect.Field can't be serialized with `ObjectOutputStream`
    (fails with `java.io.NotSerializableException: java.lang.reflect.Field`) ->
    use version field, e.g. `serialVersionUID`
    obsolete internal implementation notes:
    - Metamodel can't be serialized with XMLEncoder because implementations
    don't guarantee to be persistable with it (needs a default constructor and
    also hibernate's MetamodelImpl doesn't provide one) -> ObjectOutputStream
    and ObjectInputStream
    */
    @Override
    public void validate() throws DerbyPersistenceStorageConfInitializationException {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(schemeChecksumFile));
            Map<Class<?>, Long> checksumMapOld = (Map<Class<?>, Long>) objectInputStream.readObject();
            Map<Class<?>, Long> checksumMap = generateSchemeChecksumMap(entityClasses);
            if(!checksumMap.equals(checksumMapOld)) {
                throw new DerbyPersistenceStorageConfInitializationException(String.format("The sum of checksum of class fields and methods doesn't match with the persisted map in '%s'", this.schemeChecksumFile.getAbsolutePath()), schemeChecksumFile);
            }
        } catch (IOException | ClassNotFoundException ex) {
            throw new DerbyPersistenceStorageConfInitializationException(ex, schemeChecksumFile);
        }
    }
}
