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
package richtercloud.document.scanner.gui;

import java.io.Serializable;

/**
 * Since jFreeSane doesn't have persistable {@link SaneDevice} and
 * {@link SaneOption}s and working with them causes a slight headake wrap
 * options in a separate class.
 *
 * There's no need to persist type and model because they can be retrieved from
 * {@link SaneSession}.
 *
 * @author richter
 */
public class ScannerConf implements Serializable {

    private static final long serialVersionUID = 1L;
    private String scannerName;
    private String scannerAddress;
    private Integer resolution;
    private String mode;
    private String source;

    /**
     * Creates an empty {@code ScannerConf} (in case the application has been
     * started for the first time or the configuration file has been deleted).
     */
    public ScannerConf() {
    }

    public ScannerConf(String scannerName, String scannerAddress, Integer resolution, String mode, String source) {
        this.scannerName = scannerName;
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
}
