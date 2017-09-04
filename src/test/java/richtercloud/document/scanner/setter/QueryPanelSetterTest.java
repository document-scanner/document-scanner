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
package richtercloud.document.scanner.setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ListSelectionModel;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import richtercloud.document.scanner.gui.ocrresult.OCRResult;
import richtercloud.document.scanner.model.Company;
import richtercloud.document.scanner.model.Identifiable;
import richtercloud.message.handler.IssueHandler;
import richtercloud.reflection.form.builder.ResetException;
import richtercloud.reflection.form.builder.jpa.panels.BidirectionalControlPanel;
import richtercloud.reflection.form.builder.jpa.panels.QueryHistoryEntry;
import richtercloud.reflection.form.builder.jpa.panels.QueryHistoryEntryStorage;
import richtercloud.reflection.form.builder.jpa.panels.QueryPanel;
import richtercloud.reflection.form.builder.jpa.storage.FieldInitializer;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;
import richtercloud.validation.tools.FieldRetriever;

/**
 *
 * @author richter
 */
public class QueryPanelSetterTest {

    /**
     * Test of setOCRResult method, of class QueryPanelSetter.
     */
    @Test
    public void testSetOCRResult() {
        OCRResult oCRResult = null;
        QueryPanel<Identifiable> comp = null;
        PersistenceStorage<Long> storage = null;
        QueryPanelSetter instance = new QueryPanelSetter(storage);
        try {
            instance.setOCRResult(oCRResult, comp);
            fail("UnsupportedOperationException expected");
        }catch(UnsupportedOperationException ex) {
            //expected
        }
    }

    /**
     * Test of setValue method, of class QueryPanelSetter.
     */
    @Test
    public void testSetValue() throws IllegalArgumentException,
            IllegalAccessException,
            ResetException,
            NoSuchFieldException {
        Company value1 = new Company("name",
                new LinkedList<>(Arrays.asList("name")),
                new LinkedList<>(), //addresses
                new LinkedList<>(), //emails
                new LinkedList<>() //telephoneNumbers
        );
        Long id1 = 134234L;
        value1.setId(id1);
        Company value2 = new Company("name1234567890a",
                new LinkedList<>(Arrays.asList("name1234567890a")),
                new LinkedList<>(), //addresses
                new LinkedList<>(), //emails
                new LinkedList<>() //telephoneNumbers
        );
        Long id2 = 393939L;
        value2.setId(id2);
        PersistenceStorage<Long> storage = mock(PersistenceStorage.class);
        when(storage.isClassSupported(Company.class)).thenReturn(true);
        when(storage.isStarted()).thenReturn(true);
        IssueHandler issueHandler = mock(IssueHandler.class);
        FieldRetriever fieldRetriever = mock(FieldRetriever.class);
        BidirectionalControlPanel bidirectionalControlPanel = new BidirectionalControlPanel(Company.class,
                "title", //bidirectionalHelpDialogTitle
                null, //mappedByField
                new HashSet<>() //mappedFieldCandidates (mustn't be null)
        );
            //can't be mocked because that causes NullPointerException when
            //adding to layout
        FieldInitializer fieldInitializer = mock(FieldInitializer.class);
        QueryHistoryEntryStorage entryStorage = mock(QueryHistoryEntryStorage.class);
        QueryPanel<Identifiable> comp = new QueryPanel(storage,
                Company.class, //entityClass
                issueHandler,
                fieldRetriever,
                value1,
                bidirectionalControlPanel,
                ListSelectionModel.SINGLE_SELECTION,
                fieldInitializer,
                entryStorage);
        QueryPanelSetter instance = new QueryPanelSetter(storage);
        //test no value found (two values with levenshtein distance > 10)
        Company value3 = new Company("name0987654321a",
                new LinkedList<>(Arrays.asList("name0987654321a")),
                new LinkedList<>(), //addresses
                new LinkedList<>(), //emails
                new LinkedList<>() //telephoneNumbers
        );
        List<Company> values = new LinkedList<>(Arrays.asList(value3,
                value2));
        when(storage.runQueryAll(Company.class)).thenReturn(values);
        instance.setValue(value1,
                comp);
        assertEquals(comp.getQueryComponent().getQueryComboBox().getSelectedItem(),
                null);
        //test one value found (beside one with levenshtein distance > 10)
        values = new LinkedList<>(Arrays.asList(value1,
                value2));
        when(storage.runQueryAll(Company.class)).thenReturn(values);
        instance.setValue(value1,
                comp);
        assertEquals(comp.getQueryComponent().getQueryComboBox().getSelectedItem(),
                new QueryHistoryEntry(String.format("SELECT i from Company i WHERE i.id = %d",
                        id1)));
        //test two possible values
        Company value4 = new Company("nume",
                new LinkedList<>(Arrays.asList("nume")),
                new LinkedList<>(), //addresses
                new LinkedList<>(), //emails
                new LinkedList<>() //telephoneNumbers
        );
        Long id4 = 78383294L;
        value4.setId(id4);
        Company value5 = new Company("nime",
                new LinkedList<>(Arrays.asList("nime")),
                new LinkedList<>(), //addresses
                new LinkedList<>(), //emails
                new LinkedList<>() //telephoneNumbers
        );
        values = new LinkedList<>(Arrays.asList(value1,
                value4));
        when(storage.runQueryAll(Company.class)).thenReturn(values);
        instance.setValue(value5,
                comp);
        QueryHistoryEntry queryString = (QueryHistoryEntry) comp.getQueryComponent().getQueryComboBox().getSelectedItem();
        assertTrue(queryString != null &&
                queryString.equals(new QueryHistoryEntry(String.format("SELECT i from Company i WHERE i.id = %d OR i.id = %d",
                        id1,
                        id4)))
                || queryString.equals(new QueryHistoryEntry(String.format("SELECT i from Company i WHERE i.id = %d OR i.id = %d",
                        id4,
                        id1))));
    }

    /**
     * Test of isSupportsOCRResultSetting method, of class QueryPanelSetter.
     */
    @Test
    public void testIsSupportsOCRResultSetting() {
        PersistenceStorage<Long> storage = mock(PersistenceStorage.class);
        QueryPanelSetter instance = new QueryPanelSetter(storage);
        boolean expResult = true;
        boolean result = instance.isSupportsOCRResultSetting();
        assertEquals(expResult,
                result);
    }
}
