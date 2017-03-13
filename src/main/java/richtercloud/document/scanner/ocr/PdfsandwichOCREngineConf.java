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
package richtercloud.document.scanner.ocr;

import java.util.Objects;
import richtercloud.document.scanner.ifaces.OCREngineConfValidationException;

/**
 *
 * @author richter
 */
public class PdfsandwichOCREngineConf extends ProcessOCREngineConf {
    private static final long serialVersionUID = 1L;
    private final static String PDFSANDWICH_DEFAULT = "pdfsandwich";
    private final static String INPUT_TEMP_FILE_PREFIX = "pdfsandwich-ocr-engine-input";
    private String inputTempFilePrefix = INPUT_TEMP_FILE_PREFIX;

    public PdfsandwichOCREngineConf() {
        super(PDFSANDWICH_DEFAULT);
    }

    public String getInputTempFilePrefix() {
        return inputTempFilePrefix;
    }

    public void setInputTempFilePrefix(String inputTempFilePrefix) {
        this.inputTempFilePrefix = inputTempFilePrefix;
    }

    @Override
    public void validate() throws OCREngineConfValidationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 17 * hash + Objects.hashCode(this.inputTempFilePrefix);
        return hash;
    }

    protected boolean equalsTransitive(PdfsandwichOCREngineConf other) {
        if(!super.equalsTransitive(other)) {
            return false;
        }
        if (!Objects.equals(this.inputTempFilePrefix, other.inputTempFilePrefix)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PdfsandwichOCREngineConf other = (PdfsandwichOCREngineConf) obj;
        return equalsTransitive(other);
    }
}
