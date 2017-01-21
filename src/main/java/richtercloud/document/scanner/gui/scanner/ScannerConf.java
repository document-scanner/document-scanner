/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package richtercloud.document.scanner.gui.scanner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Since jFreeSane doesn't have persistable {@link SaneDevice} and
 * {@link SaneOption}s and working with them causes a slight headake wrap
 * options in a separate class.
 *
 * There's no need to persist type and model because they can be retrieved from
 * {@link SaneSession} based on {@code scannerName}.
 *
 * Only relevant SANE options are exposed. Handling {@code top-x},
 * {@code top-y}, {@code bottom-x} and {@code bottom-y} occurs in
 * {@link ScannerConfPaperFormat} without offset.
 *
 * @author richter
 */
/*
internal implementation notes:
- Don't manage resolutionWish in ScannerConf because it's not a scanner
configuration parameter, but a user configuration parameter
*/
public class ScannerConf implements Serializable {
    private static final long serialVersionUID = 1L;
    public final static ScannerConfPaperFormat PAPER_FORMAT_DEFAULT = new ScannerConfPaperFormat(210, 297, "DIN A4");
    private String scannerName;
    private String scannerAddress;
    private Integer resolution;
    private String mode;
    private String source;
    private ScannerConfPaperFormat paperFormat = PAPER_FORMAT_DEFAULT;
    private Set<ScannerConfPaperFormat> availablePaperFormats = new HashSet<>(Arrays.asList(PAPER_FORMAT_DEFAULT));

    public ScannerConf() {
    }

    /**
     * Creates an empty {@code ScannerConf} (in case the application has been
     * started for the first time or the configuration file has been deleted).
     */
    public ScannerConf(String scannerName) {
        this.scannerName = scannerName;
    }

    public ScannerConf(String scannerName,
            String scannerAddress,
            Integer resolution,
            String mode, String source) {
        this(scannerName);
        this.scannerAddress = scannerAddress;
        this.resolution = resolution;
        this.mode = mode;
        this.source = source;
    }

    public void setScannerName(String scannerName) {
        this.scannerName = scannerName;
    }

    public void setScannerAddress(String scannerAddress) {
        this.scannerAddress = scannerAddress;
    }

    public void setResolution(Integer resolution) {
        this.resolution = resolution;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getScannerName() {
        return scannerName;
    }

    public String getScannerAddress() {
        return scannerAddress;
    }

    public Integer getResolution() {
        return resolution;
    }

    public String getMode() {
        return mode;
    }

    public String getSource() {
        return source;
    }

    public ScannerConfPaperFormat getPaperFormat() {
        return paperFormat;
    }

    public void setPaperFormat(ScannerConfPaperFormat paperFormat) {
        this.paperFormat = paperFormat;
    }

    public Set<ScannerConfPaperFormat> getAvailablePaperFormats() {
        return availablePaperFormats;
    }

    public void setAvailablePaperFormats(Set<ScannerConfPaperFormat> availablePaperFormats) {
        this.availablePaperFormats = availablePaperFormats;
    }

    @Override
    public String toString() {
        ToStringBuilder toStringBuilder = new ReflectionToStringBuilder(this,
                new RecursiveToStringStyle());
        String retValue = toStringBuilder.toString();
        return retValue;
    }
}
