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
package richtercloud.document.scanner.model.warninghandler;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.swing.JOptionPane;
import richtercloud.document.scanner.model.Company;
import richtercloud.message.handler.ConfirmMessageHandler;
import richtercloud.message.handler.Message;
import richtercloud.reflection.form.builder.jpa.WarningHandler;

/**
 *
 * @author richter
 */
public class CompanyWarningHandler implements WarningHandler<Company> {
    private final EntityManager entityManager;
    private final ConfirmMessageHandler confirmMessageHandler;

    public CompanyWarningHandler(EntityManager entityManager,
            ConfirmMessageHandler confirmMessageHandler) {
        if(entityManager == null) {
            throw new IllegalArgumentException("entityManager mustn't be null");
        }
        this.entityManager = entityManager;
        if(confirmMessageHandler == null) {
            throw new IllegalArgumentException("confirmMessageHandler mustn't be null");
        }
        this.confirmMessageHandler = confirmMessageHandler;
    }

    @Override
    public boolean handleWarning(Company instance) {
        CriteriaQuery<Company> criteria = entityManager.getCriteriaBuilder().createQuery( Company.class );
        Root<Company> personRoot = criteria.from( Company.class );
        criteria.select( personRoot );
        criteria.where( entityManager.getCriteriaBuilder().equal( personRoot.get("name"), instance.getName() ) );
            //attributeName Company.name was used before, unclear why (causes
            //` java.lang.IllegalArgumentException: The attribute [Company.name] is not present in the managed type [EntityTypeImpl@553585467:Company [ javaType: class richtercloud.document.scanner.model.Company descriptor: RelationalDescriptor(richtercloud.document.scanner.model.Company --> [DatabaseTable(COMPANY)]), mappings: 8]].`)
        List<Company> results = entityManager.createQuery( criteria ).getResultList();
        if(!results.isEmpty()) {
            int answer = confirmMessageHandler.confirm(new Message(String.format("An instance with the name '%s' already exists in the database. Continue anyway?", instance.getName()),
                    JOptionPane.WARNING_MESSAGE,
                    "Name already used"));
            if(answer != JOptionPane.YES_OPTION) {
                return false;
            }
        }
        return true;
    }
}
