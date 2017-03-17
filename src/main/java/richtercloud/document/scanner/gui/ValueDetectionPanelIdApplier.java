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

import java.util.Set;
import richtercloud.document.scanner.components.ValueDetectionPanel;
import richtercloud.reflection.form.builder.jpa.IdGenerationException;
import richtercloud.reflection.form.builder.jpa.IdGenerator;
import richtercloud.reflection.form.builder.jpa.idapplier.IdApplicationException;
import richtercloud.reflection.form.builder.jpa.idapplier.IdApplier;
import richtercloud.reflection.form.builder.jpa.panels.LongIdPanel;

/**
 *
 * @author richter
 */
public class ValueDetectionPanelIdApplier implements IdApplier<ValueDetectionPanel> {
    private final IdGenerator<Long> idGenerator;

    public ValueDetectionPanelIdApplier(IdGenerator<Long> idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public void applyId(Object entity, Set<ValueDetectionPanel> idFieldComponents) throws IdApplicationException {
        if(idFieldComponents.size() != 1) {
            throw new IllegalArgumentException("more than one item in idFieldComponents not supported yet");
        }
        ValueDetectionPanel valueDetectionPanel = idFieldComponents.iterator().next();
        LongIdPanel longIdPanel = (LongIdPanel) valueDetectionPanel.getClassComponent();
        if(longIdPanel.getValue() != null) {
            //ID already set
            return;
        }
        Long nextId;
        try {
            nextId = idGenerator.getNextId(entity);
        } catch (IdGenerationException ex) {
            throw new IdApplicationException(ex);
        }
        longIdPanel.setValue(nextId);
    }
}
