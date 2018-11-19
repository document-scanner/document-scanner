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

/**
 *
 * @author richter
 */
public class IdentifierValueDetectionServiceConf implements ValueDetectionServiceConf {
    private static final long serialVersionUID = 1L;
    public final static int LEVENSHTEIN_DISTANCE_LIMIT_DEFAULT = 20;
    private int levenshteinDistanceLimit = LEVENSHTEIN_DISTANCE_LIMIT_DEFAULT;

    public void setLevenshteinDistanceLimit(int levenshteinDistanceLimit) {
        this.levenshteinDistanceLimit = levenshteinDistanceLimit;
    }

    public int getLevenshteinDistanceLimit() {
        return levenshteinDistanceLimit;
    }

    @Override
    public void validate() throws ValueDetectionServiceConfValidationException {
        if(this.levenshteinDistanceLimit < 0) {
            throw new ValueDetectionServiceConfValidationException("levenshteinDistanceLimit has to be >= 0");
        }
    }

    @Override
    public String getDescription() {
        return "Tries to compare the search input with identifiers of "
                + "previously stored document identifiers and returns "
                + "suggestions which are identical or close to existing ones.";
    }
}
