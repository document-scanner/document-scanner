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
package richtercloud.document.scanner.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import richtercloud.document.scanner.model.APackage;
import richtercloud.document.scanner.model.Bill;
import richtercloud.document.scanner.model.Company;
import richtercloud.document.scanner.model.Document;
import richtercloud.document.scanner.model.Email;
import richtercloud.document.scanner.model.EmailAddress;
import richtercloud.document.scanner.model.Employment;
import richtercloud.document.scanner.model.FinanceAccount;
import richtercloud.document.scanner.model.Leaflet;
import richtercloud.document.scanner.model.Location;
import richtercloud.document.scanner.model.Payment;
import richtercloud.document.scanner.model.Person;
import richtercloud.document.scanner.model.Shipping;
import richtercloud.document.scanner.model.TelephoneCall;
import richtercloud.document.scanner.model.TelephoneNumber;
import richtercloud.document.scanner.model.Transport;
import richtercloud.document.scanner.model.TransportTicket;
import richtercloud.document.scanner.model.Withdrawal;
import richtercloud.document.scanner.model.Workflow;

/**
 *
 * @author richter
 */
public class Constants {
    public static final String APP_NAME = "Document scanner";
    public static final String APP_VERSION = "1.0";
    public static final String BUG_URL = "https://github.com/krichter722/document-scanner";
    /**
     * The default value for resolution in DPI. The closest value to it might be
     * chosen if the exact resolution isn't available.
     */
    public final static int RESOLUTION_DEFAULT = 300;
    public final static Set<Class<?>> ENTITY_CLASSES = Collections.unmodifiableSet(new HashSet<Class<?>>(
            Arrays.asList(APackage.class,
                    Bill.class,
                    Company.class,
                    Document.class,
                    Email.class,
                    EmailAddress.class,
                    Employment.class,
                    FinanceAccount.class,
                    Leaflet.class,
                    Location.class,
                    Payment.class,
                    Person.class,
                    Shipping.class,
                    TelephoneCall.class,
                    TelephoneNumber.class,
                    Transport.class,
                    TransportTicket.class,
                    Withdrawal.class,
                    Workflow.class
//                    WorkflowItem.class // is an entity, but abstract
                    )));
    public final static Class<?> PRIMARY_CLASS_SELECTION = Document.class;
    public final static int INITIAL_QUERY_LIMIT_DEFAULT = 20;
    public final static String BIDIRECTIONAL_HELP_DIALOG_TITLE = "Bidirectional relations help";
    public final static String SANED_BUG_INFO = "<br/>You might suffer from a "
            + "saned bug, try <tt>/usr/sbin/saned -d -s -a saned</tt> with "
            + "appropriate privileges in order to restart saned and try again"
            + "</html>";
    public final static int SELECTED_ENTITIES_EDIT_WARNING = 5;

    private Constants() {
    }
}
