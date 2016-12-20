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
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.derby.jdbc.EmbeddedDriver;
import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.conf.model.TestClass;
import richtercloud.reflection.form.builder.jpa.storage.DerbyEmbeddedPersistenceStorageConf;
import richtercloud.reflection.form.builder.storage.StorageConfValidationException;

/**
 *
 * @author richter
 */
public class DerbyEmbeddedPersistenceStorageConfTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(DerbyEmbeddedPersistenceStorageConfTest.class);

    /**
     * Test of validate method, of class DerbyEmbeddedPersistenceStorageConf. Mocking
     * EntityManager or Metamodel is overly hard in comparison to an integration
     * test.
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    @Test
    public void testValidate() throws IOException, SQLException, InstantiationException, IllegalAccessException {
        Map<Object,Object> entityManagerFactoryProperties = new HashMap<>();
        File connTempFile = File.createTempFile("document-scanner-test", null);
        connTempFile.delete(); //don't create the directory because derby fails
            //otherwise
        LOGGER.info(String.format("using temporary directory '%s' for database storage", connTempFile.getAbsolutePath()));
        entityManagerFactoryProperties.put("javax.persistence.jdbc.url",
                String.format("jdbc:derby:%s;create=true", connTempFile.getAbsolutePath()) //create parameter is always true because using a temporary
                //directory
        );
        Class<?> driver = EmbeddedDriver.class; //this declaration facilitates
        //dependency management with an IDE with maven support and doesn't cause
        //any harm
        driver.newInstance();
        Set<Class<?>> entityClasses = new HashSet<>(Arrays.asList(TestClass.class));
        File lastSchemeStorageTempFile = File.createTempFile("document-scanner-test", null);
        lastSchemeStorageTempFile.delete(); //needs to be inexisting to trigger generation of default values in file
        LOGGER.info(String.format("using '%s' for temporary storage of last scheme", lastSchemeStorageTempFile.getAbsolutePath()));
        DerbyEmbeddedPersistenceStorageConf instance = new DerbyEmbeddedPersistenceStorageConf(entityClasses,
                connTempFile.getAbsolutePath(),
                lastSchemeStorageTempFile //prevent creating file with TestClass which isn't accessible outside tests
        );
        try {
            instance.validate(); //stores the metamodel into file
        } catch(StorageConfValidationException ex) {
            fail();
        }
        try {
            instance.validate(); //compares with stored metamodel
        } catch(StorageConfValidationException ex) {
            fail();
        }
        //@TODO: change class to trigger recognition of metamodel change (asked
        //http://stackoverflow.com/questions/34218150/how-to-simulate-a-class-change-in-an-jpa-integration-test for inputs)
        /*expResult = false;
        result = instance.validate();
        assertEquals(expResult, result);*/
    }

}
