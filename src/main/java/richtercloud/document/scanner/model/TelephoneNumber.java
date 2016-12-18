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
package richtercloud.document.scanner.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.FieldInfo;

/**
 *
 * @author richter
 */
/*
internal implementation notes:
- there's not much sense in distinguishing connection from place number
- don't keep a reference to the owner of the telephone number because it
creates a cyclic dependency between Company and TelephoneNumber at
initialization which is annoying and the information who owns the telephone
number isn't essential and can be queried easily
*/
@Entity
@Inheritance
@ClassInfo(name = "Telephone number")
public class TelephoneNumber extends Identifiable {
    private static final long serialVersionUID = 1L;
    public final static int TYPE_LANDLINE = 1;
    public final static int TYPE_VOIP = 2;
    public final static int TYPE_FAX = 4;
    public final static Set<Integer> TYPES = new HashSet<>(Arrays.asList(TYPE_LANDLINE, TYPE_VOIP, TYPE_FAX));
    /**
     * Country code of the phone number (ranges from 1 to 999 which isn't even
     * assigned yet
     * <ref>https://en.wikipedia.org/wiki/List_of_country_calling_codes</ref>)
     */
    @Min(1) @Max(999)
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Country code", description = "The country code of the "
            + "telephone number")
    private int countryCode;
    /**
     * The prefix of the phone number which has different meaning in different
     * countries (e.g. city or part of city in Germany or region in USA).
     * There're no negative prefixes in phone numbers.
     */
    @Min(0)
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Prefix", description = "The prefix of the number (can "
            + "have different meaning for different countries)")
    private int thePrefix;
    /**
     * The part of the number after the thePrefix which can include room numbers
 after the line connection, consist of a thePrefix and suffix (e.g. in USA)
 and many more which shouldn't matter for this application.
     */
    @Min(0)
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Number", description = "The remaining part of the "
            + "number after the prefix (including room numbers or prefix and "
            + "suffix in USA)")
    private int number;
    /**
     * A contact representing a telephone service provider. Might be
     * {@code null} if the provider is unknown.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @FieldInfo(name = "Provider", description = "The provider which services "
            + "this telephone number")
    private Company provider;
    /**
     * One of {@link #TYPE_FAX}, {@link #TYPE_LANDLINE} or {@link #TYPE_VOIP} to
     * indicate communication facilities.
     */
    @Basic(fetch = FetchType.EAGER)
    @FieldInfo(name = "Type", description =  "The type of the telephone number")
    private int type;

    protected TelephoneNumber() {
    }

    /**
     *
     * @param countryCode the country code ({@code +nn} or {@code 00nn} where
     * {@code n}s are digits
     * @param prefix the thePrefix
     * @param number the number
     * @param provider the provider of the number or {@code null} if unknown
     * @param owner
     * @param type the type (landline, VOIP, fax; see {@link #TYPES} for
     * details)
     * @param id
     */
    public TelephoneNumber(int countryCode,
            int prefix,
            int number,
            Company provider,
            int type) {
        if(!TYPES.contains(type)) {
            throw new IllegalArgumentException(String.format("type %d isn't an allowed type", type));
        }
        this.countryCode = countryCode;
        this.thePrefix = prefix;
        this.number = number;
        this.provider = provider;
        this.type = type;
    }


    /**
     * @return the countryCode
     */
    public int getCountryCode() {
        return countryCode;
    }

    /**
     * @param countryCode the countryCode to set
     */
    public void setCountryCode(int countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * @return the thePrefix
     */
    public int getThePrefix() {
        return thePrefix;
    }

    /**
     * @param thePrefix the thePrefix to set
     */
    public void setThePrefix(int thePrefix) {
        this.thePrefix = thePrefix;
    }

    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * @return the provider
     */
    public Company getProvider() {
        return provider;
    }

    /**
     * @param provider the provider to set
     */
    public void setProvider(Company provider) {
        this.provider = provider;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("%d %d / %d", getCountryCode(), getThePrefix(), getNumber());
    }

}
