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

import java.text.NumberFormat;

/**
 *
 * @author richter
 */
public class FormatOCRResult extends OCRResult {
    private final NumberFormat numberFormat;
    private final NumberFormat percentFormat;
    private final NumberFormat currencyFormat;

    public FormatOCRResult(NumberFormat numberFormat, NumberFormat percentFormat, NumberFormat currencyFormat, String oCRResult) {
        super(oCRResult);
        if(numberFormat == null) {
            throw new IllegalArgumentException("numberFormat mustn't be null");
        }
        this.numberFormat = numberFormat;
        if(percentFormat == null) {
            throw new IllegalArgumentException("percentFormat mustn't be null");
        }
        this.percentFormat = percentFormat;
        if(currencyFormat == null) {
            throw new IllegalArgumentException("currencyFormat mustn't be null");
        }
        this.currencyFormat = currencyFormat;
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
}
