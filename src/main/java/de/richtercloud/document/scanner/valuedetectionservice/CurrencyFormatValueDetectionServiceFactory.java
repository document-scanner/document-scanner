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
package de.richtercloud.document.scanner.valuedetectionservice;

import richtercloud.message.handler.IssueHandler;
import richtercloud.reflection.form.builder.components.money.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetriever;

/**
 *
 * @author richter
 */
public class CurrencyFormatValueDetectionServiceFactory implements ValueDetectionServiceFactory<CurrencyFormatValueDetectionService, CurrencyFormatValueDetectionServiceConf> {
    private final AmountMoneyCurrencyStorage amountMoneyCurrencyStorage;
    private final AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever;
    private final IssueHandler issueHandler;

    public CurrencyFormatValueDetectionServiceFactory(AmountMoneyCurrencyStorage amountMoneyCurrencyStorage,
            AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever,
            IssueHandler issueHandler) {
        this.amountMoneyCurrencyStorage = amountMoneyCurrencyStorage;
        this.amountMoneyExchangeRateRetriever = amountMoneyExchangeRateRetriever;
        this.issueHandler = issueHandler;
    }

    @Override
    public CurrencyFormatValueDetectionService createService(CurrencyFormatValueDetectionServiceConf serviceConf) {
        CurrencyFormatValueDetectionService retValue = new CurrencyFormatValueDetectionService(amountMoneyCurrencyStorage,
                amountMoneyExchangeRateRetriever,
                issueHandler);
        return retValue;
    }
}
