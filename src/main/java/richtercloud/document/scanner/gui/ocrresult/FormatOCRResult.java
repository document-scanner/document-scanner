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
package richtercloud.document.scanner.gui.ocrresult;

import java.text.DateFormat;
import java.text.NumberFormat;

/**
 *
 * @author richter
 */
public class FormatOCRResult extends OCRResult {
    private final NumberFormat numberFormat;
    private final NumberFormat percentFormat;
    private final NumberFormat currencyFormat;
    private final DateFormat dateFormat;
    private final DateFormat timeFormat;
    private final DateFormat dateTimeFormat;

    /**
     *
     * @param numberFormat the selected number format to be used in
     * {@link ValueSetter} ({@code null} indicates automatic parsing)
     * @param percentFormat the selected percent format to be used in
     * {@link ValueSetter} ({@code null} indicates automatic parsing)
     * @param currencyFormat the selected currency format to be used in
     * {@link ValueSetter} ({@code null} indicates automatic parsing)
     * @param dateFormat the selected date format to be used in
     * {@link ValueSetter} ({@code null} indicates automatic parsing)
     * @param timeFormat the selected time format to be used in
     * {@link ValueSetter} ({@code null} indicates automatic parsing)
     * @param dateTimeFormat the date-time number format to be used in
     * {@link ValueSetter} ({@code null} indicates automatic parsing)
     * @param oCRResult the OCR result to be used in {@link ValueSetter}
     */
    public FormatOCRResult(NumberFormat numberFormat,
            NumberFormat percentFormat,
            NumberFormat currencyFormat,
            DateFormat dateFormat,
            DateFormat timeFormat,
            DateFormat dateTimeFormat,
            String oCRResult) {
        super(oCRResult);
        this.numberFormat = numberFormat;
        this.percentFormat = percentFormat;
        this.currencyFormat = currencyFormat;
        this.dateFormat = dateFormat;
        this.timeFormat = timeFormat;
        this.dateTimeFormat = dateTimeFormat;
    }

    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    public NumberFormat getPercentFormat() {
        return percentFormat;
    }

    public NumberFormat getCurrencyFormat() {
        return currencyFormat;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public DateFormat getTimeFormat() {
        return timeFormat;
    }

    public DateFormat getDateTimeFormat() {
        return dateTimeFormat;
    }
}
