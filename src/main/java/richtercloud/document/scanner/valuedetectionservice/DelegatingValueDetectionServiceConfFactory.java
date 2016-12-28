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

import richtercloud.reflection.form.builder.components.money.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetriever;

/**
 *
 * @author richter
 */
public class DelegatingValueDetectionServiceConfFactory implements ValueDetectionServiceConfFactory<ValueDetectionService<?>, ValueDetectionServiceConf> {
    private final ContactValueDetectionServiceConfFactory contactValueDetectionServiceConfFactory;
    private final CurrencyFormatValueDetectionServiceConfFactory currencyFormatValueDetectionServiceConfFactory;
    private final CurrencyFormatValueDetectionService2ConfFactory currencyFormatValueDetectionService2ConfFactory;
    private final DateFormatValueDetectionServiceConfFactory dateFormatValueDetectionServiceConfFactory;

    public DelegatingValueDetectionServiceConfFactory(AmountMoneyCurrencyStorage amountMoneyCurrencyStorage,
            AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever) {
        this.contactValueDetectionServiceConfFactory = new ContactValueDetectionServiceConfFactory();
        this.currencyFormatValueDetectionServiceConfFactory = new CurrencyFormatValueDetectionServiceConfFactory(amountMoneyCurrencyStorage,
                amountMoneyExchangeRateRetriever);
        this.currencyFormatValueDetectionService2ConfFactory = new CurrencyFormatValueDetectionService2ConfFactory(amountMoneyCurrencyStorage,
                amountMoneyExchangeRateRetriever);
        this.dateFormatValueDetectionServiceConfFactory = new DateFormatValueDetectionServiceConfFactory();
    }

    @Override
    public ValueDetectionService<?> createService(ValueDetectionServiceConf serviceConf) {
        ValueDetectionService<?> retValue;
        if(serviceConf instanceof ContactValueDetectionServiceConf) {
            retValue = contactValueDetectionServiceConfFactory.createService((ContactValueDetectionServiceConf) serviceConf);
        }else if(serviceConf instanceof CurrencyFormatValueDetectionServiceConf) {
            retValue = currencyFormatValueDetectionServiceConfFactory.createService((CurrencyFormatValueDetectionServiceConf) serviceConf);
        }else if(serviceConf instanceof CurrencyFormatValueDetectionService2Conf) {
            retValue = currencyFormatValueDetectionService2ConfFactory.createService((CurrencyFormatValueDetectionService2Conf) serviceConf);
        }else if(serviceConf instanceof DateFormatValueDetectionServiceConf) {
            retValue = dateFormatValueDetectionServiceConfFactory.createService((DateFormatValueDetectionServiceConf) serviceConf);
        }else {
            throw new IllegalArgumentException(String.format("service "
                    + "configuration of type %s not supported",
                    serviceConf.getClass()));
        }
        return retValue;
    }
}
