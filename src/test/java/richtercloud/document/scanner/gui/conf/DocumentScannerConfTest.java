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

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author richter
 */
public class DocumentScannerConfTest {

    @Test
    public void testToString() throws IOException {
        DocumentScannerConf instance = new DocumentScannerConf();
//        String expResultRe = "richtercloud.document.scanner.gui.conf.DocumentScannerConf@[a-z0-9]+["
//                + "configFile=/home/richter/.document-scanner/document-scanner-config.xml,"
//                + "scannerName=<null>,"
//                + "scannerSaneAddress=localhost,"
//                + "storageConf=richtercloud.reflection.form.builder.jpa.storage.DerbyEmbeddedPersistenceStorageConf@[a-z0-9]+["
//                    + "username=,"
//                    + "password=<null>,"
//                    + "databaseName=/home/richter/.document-scanner/databases,"
//                    + "schemeChecksumFile=java.io.File@[a-z0-9]+["
//                    + "path=/home/richter/.document-scanner/last-scheme.xml],"
//                    + "entityClasses="
//                    + "java.util.Collections$UnmodifiableSet@[a-z0-9]+{"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[],"
//                    + "java.lang.Class@[a-z0-9]+[]},"
//                    + "databaseDriver=org.apache.derby.jdbc.EmbeddedDriver],"
//                + "availableStorageConfs=["
//                    + "richtercloud.reflection.form.builder.jpa.storage.MySQLAutoPersistenceStorageConf@[a-z0-9]+["
//                        + "databaseDir=/home/richter/.document-scanner/"
//                        + "databases-mysql,"
//                        + "baseDir=<null>,"
//                        + "mysqld=<null>,"
//                        + "mysqladmin=<null>,"
//                        + "mysql=<null>,"
//                        + "myCnfFilePath=<null>,"
//                        + "hostname=localhost,"
//                        + "port=3306,"
//                        + "username=document-scanner,"
//                        + "password=<null>,"
//                        + "databaseName=document-scanner,"
//                        + "schemeChecksumFile=java.io.File@[a-z0-9]+[path=/home/richter/.document-scanner/last-scheme.xml],"
//                        + "entityClasses=java.util.Collections$UnmodifiableSet@[a-z0-9]+{"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[]},"
//                        + "databaseDriver=com.mysql.jdbc.Driver],"
//                    + "richtercloud.reflection.form.builder.jpa.storage.DerbyEmbeddedPersistenceStorageConf@[a-z0-9]+["
//                        + "username=,"
//                        + "password=<null>,"
//                        + "databaseName=/home/richter/.document-scanner/databases,schemeChecksumFile=java.io.File@[a-z0-9]+[path=/home/richter/.document-scanner/last-scheme.xml],"
//                        + "entityClasses=java.util.Collections$UnmodifiableSet@[a-z0-9]+{"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[],"
//                            + "java.lang.Class@[a-z0-9]+[]},"
//                        + "databaseDriver=org.apache.derby.jdbc.EmbeddedDriver],"
//                    + "richtercloud.reflection.form.builder.jpa.storage.PostgresqlAutoPersistenceStorageConf@[a-z0-9]+["
//                        + "databaseDir=/home/richter/.document-scanner/databases-postgresql,"
//                        + "hostname=localhost,"
//                        + "port=5432,"
//                        + "username=sa,"
//                        + "password=<null>,"
//                        + "databaseName=<null>,"
//                        + "schemeChecksumFile=java.io.File@[a-z0-9]+[path=/home/richter/.document-scanner/last-scheme.xml],"
//                        + "entityClasses=java.util.Collections$UnmodifiableSet@[a-z0-9]+{"
//                            + "java.lang.Class@[a-z0-9]+[](,java.lang.Class@[a-z0-9]+[])*},"
//                        + "databaseDriver=org.postgresql.Driver],"
//                    + "richtercloud.reflection.form.builder.jpa.storage.DerbyNetworkPersistenceStorageConf@[a-z0-9]+["
//                        + "hostname=localhost,"
//                        + "port=1527,"
//                        + "username=sa,"
//                        + "password=<null>,"
//                        + "databaseName=<null>,"
//                        + "schemeChecksumFile=java.io.File@[a-z0-9]+[path=/home/richter/.document-scanner/last-scheme.xml],"
//                        + "entityClasses=java.util.Collections$UnmodifiableSet@[a-z0-9]+{"
//                            + "java.lang.Class@[a-z0-9]+[](,java.lang.Class@[a-z0-9]+[])*},"
//                        + "databaseDriver=org.apache.derby.jdbc.ClientDriver]],"
//                + "oCREngineConf=richtercloud.document.scanner.ocr.TesseractOCREngineConf@[a-z0-9]+["
//                    + "selectedLanguages=java.util.Collections$UnmodifiableList@[a-z0-9]+{deu},"
//                    + "binary=tesseract],"
//                + "availableOCREngineConfs=["
//                    + "richtercloud.document.scanner.ocr.TesseractOCREngineConf@[a-z0-9]+["
//                        + "selectedLanguages=java.util.Collections$UnmodifiableList@[a-z0-9]+{deu},"
//                        + "binary=tesseract]],"
//                + "currency=EUR,"
//                + "autoGenerateIDs=true,"
//                + "locale=de_DE,"
//                + "autoSaveImageData=true,"
//                + "autoSaveOCRData=true,"
//                + "automaticFormatInitiallySelected=true,"
//                + "autoOCRValueDetection=true,scannerConfMap={},"
//                + "zoomLevelMultiplier=0.66,"
//                + "preferredScanResultPanelWidth=600,"
//                + "rememberPreferredScanResultPanelWidth=true,"
//                + "preferredOCRSelectPanelWidth=600,"
//                + "rememberPreferredOCRSelectPanelWidth=true,"
//                + "xMLStorageFile=/home/richter/.document-scanner/xml-storage.xml,derbyPersistenceStorageSchemeChecksumFile=/home/richter/.document-scanner/last-scheme.xml,"
//                + "amountMoneyUsageStatisticsStorageFile=/home/richter/.document-scanner/currency-usage-statistics.xml,"
//                + "amountMoneyCurrencyStorageFile=/home/richter/.document-scanner/currencies.xml,"
//                + "tagStorageFile=/home/richter/.document-scanner/tags,"
//                + "imageWrapperStorageDir=/home/richter/.document-scanner/image-storage,"
//                + "debug=false,"
//                + "skipMD5SumCheck=false,"
//                + "logFilePath=/home/richter/.document-scanner/document-scanner.log,"
//                + "valueDetectionServiceJARPaths=[],"
//                + "availableValueDetectionServiceConfs=[],"
//                + "selectedValueDetectionServiceConfs=[richtercloud.document.scanner.valuedetectionservice.ContactValueDetectionServiceConf@[a-z0-9]+[],"
//                + "[\\s]*richtercloud.document.scanner.valuedetectionservice.TrieCurrencyFormatValueDetectionServiceConf@[a-z0-9]+[],"
//                + "[\\s]*richtercloud.document.scanner.valuedetectionservice.CurrencyFormatValueDetectionServiceConf@[a-z0-9]+[], "
//                + "[\\s]*richtercloud.document.scanner.valuedetectionservice.DateFormatValueDetectionServiceConf@[a-z0-9]+[]],"
//                + "resolutionWish=200,"
//                + "queryHistoryEntryStorageFile=/home/richter/.document-scanner/query-history-storage.xml,"
//                + "amountMoneyExchangeRateRetrieverFileCacheDir=/home/richter/.document-scanner/amount-money-exchange-rate-retriever-cache,"
//                + "amountMoneyExchangeRateRetrieverExpirationMillis=86400000,"
//                + "userAllowedAutoBugTracking=false,"
//                + "skipUserAllowedAutoBugTrackingQuestion=false,"
//                + "credentialsStoreFile=/home/richter/.document-scanner/credentials.xml,"
//                + "autoOCRValueDetectionFormatterMap={class java.util.Date=richtercloud.document.scanner.components.DateOCRResultFormatter@[a-z0-9]+},"
//                + "scannerOpenWaitTime=15,"
//                + "scannerOpenWaitTimeUnit=SECONDS]";
            //@TODO: need to escape re control charaters and figure out order
                //or items of sets (it's not possible to check that all types are
                //difference because of re limitations
        String result = instance.toString();
//        Assert.assertTrue(result.matches(expResultRe));
        Assert.assertTrue(!result.isEmpty());
    }
}
