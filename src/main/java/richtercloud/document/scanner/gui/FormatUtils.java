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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides different {@link NumberFormat}s and {@link DateFormat}s in order to
 * avoid handling this provision in static methods and variables in classes.
 *
 * Note that since Java doesn't have a useful way to manage currencies and
 * JScience is used instead (which has a quite limited set of currencies),
 * available currencies ought to be retrieved from a
 * {@link AmountMoneyCurrencyStorage}.
 *
 * @author richter
 */
/*
internal implementation notes:
- originally methods have been implemented to initialize return values lazily
which slows down the application during operations instead of slowing it down at
start -> keep implementation, but invoke all methods in static block in order to
trigger cache filling
*/
public class FormatUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(FormatUtils.class);
    public final static double NUMBER_FORMAT_VALUE = -12345.987;
    public final static Date DATE_FORMAT_VALUE = new Date();
    public final static Set<Integer> DATE_FORMAT_INTS = new HashSet<>(Arrays.asList(DateFormat.FULL, DateFormat.LONG, DateFormat.MEDIUM, DateFormat.SHORT));
    private static Set<DateFormat> allDateFormats = null;
    private static Set<DateFormat> allTimeFormats = null;
    private static Set<DateFormat> allDateTimeFormats = null;
    private static Set<DateFormat> allDateRelatedFormats = null;
    private static Map<DateFormat, Set<Locale>> disjointDateTimeFormats = null;
    private static Map<DateFormat, Set<Locale>> disjointDateFormats = null;
    private static Map<DateFormat, Set<Locale>> disjointTimeFormats = null;
    private static Map<DateFormat, Set<Locale>> disjointDateRelatedFormats = null;
    private static Set<NumberFormat> allCurrencyFormats = null;
    private static Map<NumberFormat, Set<Locale>> disjointCurrencyFormats = null;
    private final static Lock DISJOINT_CURRENCY_FORMAT_LOCK = new ReentrantLock();
    static {
        getAllDateTimeFormats();
        getAllDateFormats();
        getAllTimeFormats();
        getAllDateTimeFormats();
        getAllDateRelatedFormats();
        getDisjointDateFormats();
        getDisjointTimeFormats();
        getDisjointDateTimeFormats();
        getDisjointDateRelatedFormats();
        getAllCurrencyFormats();
        getDisjointCurrencyFormats();
    }

    public static Set<DateFormat> getAllDateRelatedFormats() {
        if(allDateRelatedFormats == null) {
            allDateRelatedFormats = new HashSet<>();
            allDateRelatedFormats.addAll(getAllDateFormats());
            allDateRelatedFormats.addAll(getAllTimeFormats());
            allDateRelatedFormats.addAll(getAllDateTimeFormats());
        }
        return Collections.unmodifiableSet(allDateRelatedFormats);
    }

    /**
     * Lazily creates all available date-time formats (all combinations of
     * available {@link Locale}s and combination of two times the set of
     * {@link #DATE_FORMAT_INTS}. The set in only generated once and then
     * cached.
     * @return all available date-time formats
     */
    public static Set<DateFormat> getAllDateTimeFormats() {
        if(allDateTimeFormats == null) {
            allDateTimeFormats = new HashSet<>();
            for(Locale locale : Locale.getAvailableLocales()) {
                for(int formatInt : DATE_FORMAT_INTS) {
                    for(int formatInt1: DATE_FORMAT_INTS) {
                        DateFormat dateFormat = DateFormat.getDateTimeInstance(formatInt, formatInt1, locale);
                        allDateTimeFormats.add(dateFormat);
                    }
                }
            }
        }
        return Collections.unmodifiableSet(allDateTimeFormats);
    }

    public static Set<DateFormat> getAllDateFormats() {
        if(allDateFormats == null) {
            allDateFormats = new HashSet<>();
            for(Locale locale : Locale.getAvailableLocales()) {
                for(int formatInt : DATE_FORMAT_INTS) {
                    DateFormat dateFormat = DateFormat.getDateInstance(formatInt, locale);
                    allDateFormats.add(dateFormat);
                }
            }
        }
        return Collections.unmodifiableSet(allDateFormats);
    }

    public static Set<DateFormat> getAllTimeFormats() {
        if(allTimeFormats == null) {
            allTimeFormats = new HashSet<>();
            for(Locale locale : Locale.getAvailableLocales()) {
                for(int formatInt : DATE_FORMAT_INTS) {
                    DateFormat dateFormat = DateFormat.getTimeInstance(formatInt, locale);
                    allTimeFormats.add(dateFormat);
                }
            }
        }
        return Collections.unmodifiableSet(allTimeFormats);
    }

    /**
     * Get all disjoint date, time and date-time formats. Note that some date
     * (sub)strings aren't recognized by date-time formats, i.e. date and time
     * formats should be used as well for recognizing date and time in random
     * input.
     *
     * @see #getDisjointDateTimeFormats()
     * @return
     */
    public static Map<DateFormat, Set<Locale>> getDisjointDateRelatedFormats() {
        if(disjointDateRelatedFormats == null) {
            disjointDateRelatedFormats = new HashMap<>();
            disjointDateRelatedFormats.putAll(getDisjointTimeFormats());
            disjointDateRelatedFormats.putAll(getDisjointDateFormats());
            disjointDateRelatedFormats.putAll(getDisjointDateTimeFormats());
        }
        return Collections.unmodifiableMap(disjointDateRelatedFormats);
    }

    public static Map<DateFormat, Set<Locale>> getDisjointDateFormats() {
        if(disjointDateFormats == null) {
            Map<String, Pair<DateFormat, Set<Locale>>> dateFormats = new HashMap<>();
            Iterator<Locale> localeIterator = new ArrayList<>(Arrays.asList(Locale.getAvailableLocales())).iterator();
            Locale firstLocale = localeIterator.next();
            for(int formatInt : DATE_FORMAT_INTS) {
                String dateString = DateFormat.getDateInstance(formatInt, firstLocale).format(DATE_FORMAT_VALUE);
                dateFormats.put(dateString,
                        new ImmutablePair<DateFormat, Set<Locale>>(DateFormat.getDateInstance(formatInt, firstLocale),
                                new HashSet<>(Arrays.asList(firstLocale))));
            }
            while(localeIterator.hasNext()) {
                Locale locale = localeIterator.next();
                for(int formatInt : DATE_FORMAT_INTS) {
                    String dateString = DateFormat.getDateInstance(formatInt, locale).format(DATE_FORMAT_VALUE);
                    Pair<DateFormat, Set<Locale>> dateFormatsPair = dateFormats.get(dateString);
                    if(dateFormatsPair == null) {
                        dateFormatsPair = new ImmutablePair<DateFormat, Set<Locale>>(DateFormat.getDateInstance(formatInt, locale),
                                new HashSet<Locale>());
                        dateFormats.put(dateString, dateFormatsPair);
                    }
                    Set<Locale> dateFormatsLocales = dateFormatsPair.getValue();
                    dateFormatsLocales.add(locale);
                }
            }
            disjointDateFormats = new HashMap<>();
            for(Pair<DateFormat, Set<Locale>> dateFormatsPair : dateFormats.values()) {
                disjointDateFormats.put(dateFormatsPair.getKey(),
                        dateFormatsPair.getValue());
            }
        }
        return Collections.unmodifiableMap(disjointDateFormats);
    }

    public static Map<DateFormat, Set<Locale>> getDisjointTimeFormats() {
        if(disjointTimeFormats == null) {
            Map<String, Pair<DateFormat, Set<Locale>>> timeFormats = new HashMap<>();
            Iterator<Locale> localeIterator = new ArrayList<>(Arrays.asList(Locale.getAvailableLocales())).iterator();
            Locale firstLocale = localeIterator.next();
            for(int formatInt : DATE_FORMAT_INTS) {
                String timeString = DateFormat.getTimeInstance(formatInt, firstLocale).format(DATE_FORMAT_VALUE);
                timeFormats.put(timeString,
                        new ImmutablePair<DateFormat, Set<Locale>>(DateFormat.getTimeInstance(formatInt, firstLocale),
                                new HashSet<>(Arrays.asList(firstLocale))));
            }
            while(localeIterator.hasNext()) {
                Locale locale = localeIterator.next();
                for(int formatInt : DATE_FORMAT_INTS) {
                    String timeString = DateFormat.getTimeInstance(formatInt, locale).format(DATE_FORMAT_VALUE);
                    Pair<DateFormat, Set<Locale>> timeFormatsPair = timeFormats.get(timeString);
                    if(timeFormatsPair == null) {
                        timeFormatsPair = new ImmutablePair<DateFormat, Set<Locale>>(DateFormat.getTimeInstance(formatInt, locale),
                                new HashSet<Locale>());
                        timeFormats.put(timeString, timeFormatsPair);
                    }
                    Set<Locale> timeFormatsLocales = timeFormatsPair.getValue();
                    timeFormatsLocales.add(locale);
                }
            }
            disjointTimeFormats = new HashMap<>();
            for(Pair<DateFormat, Set<Locale>> timeFormatsPair : timeFormats.values()) {
                disjointTimeFormats.put(timeFormatsPair.getKey(),
                        timeFormatsPair.getValue());
            }
        }
        return Collections.unmodifiableMap(disjointTimeFormats);
    }

    public static Map<DateFormat, Set<Locale>> getDisjointDateTimeFormats() {
        if(disjointDateTimeFormats == null) {
            Map<String, Pair<DateFormat, Set<Locale>>> dateTimeFormats = new HashMap<>();
            Iterator<Locale> localeIterator = new ArrayList<>(Arrays.asList(Locale.getAvailableLocales())).iterator();
            Locale firstLocale = localeIterator.next();
            for(int formatInt : DATE_FORMAT_INTS) {
                for(int formatInt1 : DATE_FORMAT_INTS) {
                    String dateTimeString = DateFormat.getDateTimeInstance(formatInt, formatInt1, firstLocale).format(DATE_FORMAT_VALUE);
                    dateTimeFormats.put(dateTimeString,
                            new ImmutablePair<DateFormat, Set<Locale>>(DateFormat.getDateTimeInstance(formatInt, formatInt1, firstLocale),
                                    new HashSet<>(Arrays.asList(firstLocale))));
                }
            }
            while(localeIterator.hasNext()) {
                Locale locale = localeIterator.next();
                for(int formatInt : DATE_FORMAT_INTS) {
                    for(int formatInt1 : DATE_FORMAT_INTS) {
                        String dateTimeString = DateFormat.getDateTimeInstance(formatInt, formatInt1, locale).format(DATE_FORMAT_VALUE);
                        Pair<DateFormat, Set<Locale>> dateTimeFormatsPair = dateTimeFormats.get(dateTimeString);
                        if(dateTimeFormatsPair == null) {
                            dateTimeFormatsPair = new ImmutablePair<DateFormat, Set<Locale>>(DateFormat.getDateTimeInstance(formatInt, formatInt1, locale),
                                    new HashSet<Locale>());
                            dateTimeFormats.put(dateTimeString, dateTimeFormatsPair);
                        }
                        Set<Locale> dateTimeFormatsLocales = dateTimeFormatsPair.getValue();
                        dateTimeFormatsLocales.add(locale);
                    }
                }
            }
            disjointDateTimeFormats = new HashMap<>();
            for(Pair<DateFormat, Set<Locale>> dateTimeFormatsPair : dateTimeFormats.values()) {
                disjointDateTimeFormats.put(dateTimeFormatsPair.getKey(),
                        dateTimeFormatsPair.getValue());
            }
        }
        return Collections.unmodifiableMap(disjointDateTimeFormats);
    }

    public static Set<NumberFormat> getAllCurrencyFormats() {
        if(allCurrencyFormats == null) {
            allCurrencyFormats = new HashSet<>();
            for(Locale locale : Locale.getAvailableLocales()) {
                for(Currency currency : Currency.getAvailableCurrencies()) {
                    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
                    numberFormat.setCurrency(currency);
                    allCurrencyFormats.add(numberFormat);
                }
            }
        }
        return Collections.unmodifiableSet(allCurrencyFormats);
    }

    /**
     * Since this method uses a cached value it's necessary to the the enty set
     * of the returned map with {@link #getDisjointCurrencyFormatsEntySet() }
     * which uses a lock in order to prevent a
     * {@link ConcurrentModificationException}.
     * @return
     */
    public static Map<NumberFormat, Set<Locale>> getDisjointCurrencyFormats() {
        if(disjointCurrencyFormats == null) {
            Map<String, Pair<NumberFormat, Set<Locale>>> currencyFormats = new HashMap<>();
            Iterator<Locale> localeIterator = new ArrayList<>(Arrays.asList(Locale.getAvailableLocales())).iterator();
            Locale firstLocale = localeIterator.next();
            for(Currency currency : Currency.getAvailableCurrencies()) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(firstLocale);
                currencyFormat.setCurrency(currency);
                String currencyString = currencyFormat.format(NUMBER_FORMAT_VALUE);
                currencyFormats.put(currencyString,
                        new ImmutablePair<NumberFormat, Set<Locale>>(currencyFormat,
                                new HashSet<>(Arrays.asList(firstLocale))));
            }
            while(localeIterator.hasNext()) {
                Locale locale = localeIterator.next();
                for(Currency currency : Currency.getAvailableCurrencies()) {
                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
                    currencyFormat.setCurrency(currency);
                    String currencyString = currencyFormat.format(NUMBER_FORMAT_VALUE);
                    Pair<NumberFormat, Set<Locale>> currencyFormatsPair = currencyFormats.get(currencyString);
                    if(currencyFormatsPair == null) {
                        currencyFormatsPair = new ImmutablePair<NumberFormat, Set<Locale>>(currencyFormat,
                                new HashSet<Locale>());
                        currencyFormats.put(currencyString, currencyFormatsPair);
                    }
                    Set<Locale> currencyFormatsLocales = currencyFormatsPair.getValue();
                    currencyFormatsLocales.add(locale);
                }
            }
            LOGGER.debug(Arrays.toString(currencyFormats.entrySet().toArray()));
            disjointCurrencyFormats = new HashMap<>();
            for(Pair<NumberFormat, Set<Locale>> currencyFormatsPair : currencyFormats.values()) {
                disjointCurrencyFormats.put(currencyFormatsPair.getKey(),
                        currencyFormatsPair.getValue());
            }
        }
        return Collections.unmodifiableMap(disjointCurrencyFormats);
    }

    public static Set<Entry<NumberFormat, Set<Locale>>> getDisjointCurrencyFormatsEntySet() {
        Set<Entry<NumberFormat, Set<Locale>>> retValue;
        DISJOINT_CURRENCY_FORMAT_LOCK.lock();
        try {
            retValue = getDisjointCurrencyFormats().entrySet();
        }finally {
            DISJOINT_CURRENCY_FORMAT_LOCK.unlock();
        }
        return retValue;
    }

    private FormatUtils() {
    }
}
