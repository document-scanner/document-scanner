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
package richtercloud.document.scanner.valuedetectionservice;

import richtercloud.message.handler.IssueHandler;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;

/**
 *
 * @author richter
 */
public class IdentifierValueDetectionServiceFactory implements ValueDetectionServiceFactory<IdentifierValueDetectionService, IdentifierValueDetectionServiceConf> {
    private final IssueHandler issueHandler;
    private final PersistenceStorage<Long> storage;

    public IdentifierValueDetectionServiceFactory(IssueHandler issueHandler,
            PersistenceStorage<Long> storage) {
        this.issueHandler = issueHandler;
        this.storage = storage;
    }

    @Override
    public IdentifierValueDetectionService createService(IdentifierValueDetectionServiceConf serviceConf) throws ValueDetectionServiceCreationException {
        IdentifierValueDetectionService retValue = new IdentifierValueDetectionService(issueHandler,
                storage,
                serviceConf.getLevenshteinDistanceLimit());
        return retValue;
    }
}
