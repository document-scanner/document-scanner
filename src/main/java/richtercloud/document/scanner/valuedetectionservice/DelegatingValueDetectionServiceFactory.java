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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import richtercloud.document.scanner.valuedetectionservice.annotations.Factory;
import richtercloud.reflection.form.builder.components.money.AmountMoneyCurrencyStorage;
import richtercloud.reflection.form.builder.components.money.AmountMoneyExchangeRateRetriever;

/**
 *
 * @author richter
 */
public class DelegatingValueDetectionServiceFactory implements ValueDetectionServiceFactory<ValueDetectionService<?>, ValueDetectionServiceConf> {
    private final ContactValueDetectionServiceFactory contactValueDetectionServiceConfFactory;
    private final CurrencyFormatValueDetectionServiceFactory currencyFormatValueDetectionServiceConfFactory;
    private final TrieCurrencyFormatValueDetectionServiceFactory trieCurrencyFormatValueDetectionServiceConfFactory;
    private final DateFormatValueDetectionServiceFactory dateFormatValueDetectionServiceConfFactory;

    public DelegatingValueDetectionServiceFactory(AmountMoneyCurrencyStorage amountMoneyCurrencyStorage,
            AmountMoneyExchangeRateRetriever amountMoneyExchangeRateRetriever) {
        this.contactValueDetectionServiceConfFactory = new ContactValueDetectionServiceFactory();
        this.currencyFormatValueDetectionServiceConfFactory = new CurrencyFormatValueDetectionServiceFactory(amountMoneyCurrencyStorage,
                amountMoneyExchangeRateRetriever);
        this.trieCurrencyFormatValueDetectionServiceConfFactory = new TrieCurrencyFormatValueDetectionServiceFactory(amountMoneyCurrencyStorage,
                amountMoneyExchangeRateRetriever);
        this.dateFormatValueDetectionServiceConfFactory = new DateFormatValueDetectionServiceFactory();
    }

    @Override
    public ValueDetectionService<?> createService(ValueDetectionServiceConf serviceConf) throws ValueDetectionServiceCreationException {
        ValueDetectionService<?> retValue;
        if(serviceConf instanceof ContactValueDetectionServiceConf) {
            retValue = contactValueDetectionServiceConfFactory.createService((ContactValueDetectionServiceConf) serviceConf);
        }else if(serviceConf instanceof CurrencyFormatValueDetectionServiceConf) {
            retValue = currencyFormatValueDetectionServiceConfFactory.createService((CurrencyFormatValueDetectionServiceConf) serviceConf);
        }else if(serviceConf instanceof TrieCurrencyFormatValueDetectionServiceConf) {
            retValue = trieCurrencyFormatValueDetectionServiceConfFactory.createService((TrieCurrencyFormatValueDetectionServiceConf) serviceConf);
        }else if(serviceConf instanceof DateFormatValueDetectionServiceConf) {
            retValue = dateFormatValueDetectionServiceConfFactory.createService((DateFormatValueDetectionServiceConf) serviceConf);
        }else {
            Factory confFactory = serviceConf.getClass().getAnnotation(Factory.class);
            if(confFactory != null) {
                Class<? extends ValueDetectionServiceFactory> serviceConfFactoryClass = confFactory.confFactoryClass();
                try {
                    Constructor<? extends ValueDetectionServiceFactory> serviceConfFactoryClassConstructor = serviceConfFactoryClass.getConstructor();
                    try {
                        ValueDetectionServiceFactory serviceConfFactory = serviceConfFactoryClassConstructor.newInstance();
                        retValue = serviceConfFactory.createService(serviceConf);
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        throw new RuntimeException(ex);
                    }
                } catch (NoSuchMethodException | SecurityException ex) {
                    throw new RuntimeException(ex);
                }
            }else {
                throw new IllegalArgumentException(String.format("service "
                        + "configuration of type %s not supported",
                        serviceConf.getClass()));
            }
        }
        return retValue;
    }
}
