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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.model.CommunicationItem;
import richtercloud.document.scanner.model.Company;
import richtercloud.document.scanner.model.Document;
import richtercloud.document.scanner.model.EmailAddress;
import richtercloud.document.scanner.model.Employment;
import richtercloud.document.scanner.model.FinanceAccount;
import richtercloud.document.scanner.model.Location;
import richtercloud.document.scanner.model.Payment;
import richtercloud.document.scanner.model.Person;
import richtercloud.document.scanner.model.Transport;
import richtercloud.document.scanner.model.TransportTicket;
import richtercloud.reflection.form.builder.jpa.SequentialIdGenerator;

/**
 * pro
 *
 * @author richter
 */
public class EntityIdGenerator extends SequentialIdGenerator {
    private final static EntityIdGenerator INSTANCE = new EntityIdGenerator();
    private final static Logger LOGGER = LoggerFactory.getLogger(EntityIdGenerator.class);

    public static EntityIdGenerator getInstance() {
        return INSTANCE;
    }

    protected EntityIdGenerator() {
    }

    /**
     * Generates ids for {@link CommunicationItem}, {@link EmailAddress},
     * {@link Employment}, {@link FinanceAccount}, {@link Company},
     * {@link Person}, {@link Location}, {@link Payment}, {@link Transport} and
     * {@link TransportTicket}.
     *
     * @param instance
     * @throws IllegalArgumentException if {@code instance} is not of the types
     * listed above
     * @return
     */
    @Override
    public Long getNextId(Object instance) {
        long retValue;
        if (instance instanceof CommunicationItem) {
            CommunicationItem communicationItem = (CommunicationItem) instance;
            retValue = communicationItem.getSender().hashCode()
                    * communicationItem.getRecipient().hashCode()
                    * communicationItem.getTheDate().hashCode();
            if (instance instanceof Document) {
                Document document = (Document) instance;
                retValue *= document.getIdentifier().hashCode();
            }
        } else if (instance instanceof EmailAddress) {
            EmailAddress emailAddress = (EmailAddress) instance;
            retValue = emailAddress.getAddress().hashCode();
        } else if (instance instanceof Employment) {
            Employment employment = (Employment) instance;
            retValue = employment.getCompany().hashCode()
                    * employment.getTheBegin().hashCode()
                    * employment.getTheEnd().hashCode();
        } else if (instance instanceof FinanceAccount) {
            FinanceAccount financeAccount = (FinanceAccount) instance;
            retValue = financeAccount.getIban() != null
                            ? financeAccount.getIban().hashCode()
                            : financeAccount.getBlz().hashCode()
                    * financeAccount.getNumber().hashCode();
        } else if (instance instanceof Company) {
            Company company = (Company) instance;
            retValue = company.getName().hashCode();
            if (instance instanceof Person) {
                Person person = (Person) instance;
                retValue = retValue
                        * person.getFirstnames().hashCode()
                        * person.getLastnames().hashCode();
            }
        } else if (instance instanceof Location) {
            Location location = (Location) instance;
            retValue = location.getDescription().hashCode();
        } else if (instance instanceof Payment) {
            Payment payment = (Payment) instance;
            retValue = payment.getSender().hashCode()
                    * payment.getRecipient().hashCode()
                    * payment.getTheDate().hashCode();
        } else if (instance instanceof Transport) {
            Transport transport = (Transport) instance;
            retValue = transport.getTickets().hashCode();
        } else if (instance instanceof TransportTicket) {
            TransportTicket transportTicket = (TransportTicket) instance;
            retValue = transportTicket.getTheDate().hashCode()
                    * transportTicket.getWaypoints().hashCode()
                    * transportTicket.getTransportCompany().hashCode(); //theDate is only sufficient to distungish if it is precise. In order to allow to specify a vague date in conjunction with waypoints and/or transportCompany, use all properties for hash code.
        }else {
            throw new IllegalArgumentException(String.format("type '%s' not supported", instance.getClass()));
        }
        return retValue;
    }

}
