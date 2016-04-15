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

import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;
import richtercloud.reflection.form.builder.components.AmountMoneyPanel;

/**
 *
 * @author richter
 */
public class AmountMoneyPanelSetter implements ValueSetter<AmountMoneyPanel> {
    private final static AmountMoneyPanelSetter INSTANCE = new AmountMoneyPanelSetter();

    public static AmountMoneyPanelSetter getInstance() {
        return INSTANCE;
    }

    @Override
    public void setValue(String value, AmountMoneyPanel comp) {
        Amount<Money> amountMoney = AmountMoneyPanel.parseValue(value);
        comp.setValue(amountMoney);
    }
}
