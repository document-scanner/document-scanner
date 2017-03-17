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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import richtercloud.document.scanner.model.Address;
import richtercloud.document.scanner.model.Company;
import richtercloud.document.scanner.model.EmailAddress;
import richtercloud.document.scanner.model.TelephoneNumber;

/**
 * Scans for letter headers in the form of <pre>Name
 * Address
 * Zipcode City</pre> and allows different {@link CallbackAction} including
 * editing recognized values, creating a contact and updating the address on
 * another contact.
 *
 * @author richter
 */
public class ContactValueDetectionService extends AbstractValueDetectionService<Company> {

    /**
     * Parses addresses which consist of 3 lines (name, street and number and
     * then zipcode and city
     * @param input
     * @return the list of detected results
     */
    @Override
    public LinkedHashSet<ValueDetectionResult<Company>> fetchResults0(String input) {
        LinkedHashSet<ValueDetectionResult<Company>> retValue = new LinkedHashSet<>();
        StringTokenizer tokenizer = new StringTokenizer(input,
                "\n", //delimiter
                false //return delimiters
        );
        List<String> queue = new LinkedList<>();
        while(tokenizer.hasMoreTokens()) {
            while(queue.size() < 3 && tokenizer.hasMoreTokens()) {
                String nextToken = tokenizer.nextToken();
                queue.add(nextToken);
            }
            if(queue.size() < 3) {
                //end of token stream and not enough lines to recognize an
                //address
                break;
            }
            Pattern cityPattern = Pattern.compile("(?<zipcode>[0-9]+)[\\s]+(?<city>.+).*|(?<city1>.+)[\\s]+(?<zipcode1>[0-9]+).*");
            Matcher cityPatternMatcher = cityPattern.matcher(queue.get(2));
            if(cityPatternMatcher.find()) {
                String zipcode;
                try {
                    zipcode = cityPatternMatcher.group("zipcode");
                }catch(IllegalStateException ex) {
                    zipcode = cityPatternMatcher.group("zipcode1");
                }
                String city;
                try {
                    city = cityPatternMatcher.group("city");
                }catch(IllegalStateException ex) {
                    city = cityPatternMatcher.group("city1");
                }
                Pattern streetPattern = Pattern.compile("(?<number>[0-9]+)[\\s]+(?<street>.+).*|(?<street1>(.+[\\s])*.+)[\\s]+(?<number1>[0-9]+).*");
                    //excluding numbers from street group in the second
                    //alternative of the regex might be wrong (since
                    //street names which contain numbers exist), but that
                    //assures that the first
                Matcher streetPatternMatcher = streetPattern.matcher(queue.get(1));
                if(streetPatternMatcher.find()) {
                    String number;
                    try {
                        number = streetPatternMatcher.group("number");
                    }catch(IllegalStateException ex) {
                        number = streetPatternMatcher.group("number1");
                    }
                    String street;
                    try {
                        street = streetPatternMatcher.group("street");
                    }catch(IllegalStateException ex) {
                        street = streetPatternMatcher.group("street1");
                    }
                    //not necessary to search name so complicated, but keep
                    //the scheme in order to be extendable
                    Pattern namePattern = Pattern.compile("[\\s]*(?<name>.+)[\\s]*");
                    Matcher namePatternMatcher = namePattern.matcher(queue.get(0));
                    if(namePatternMatcher.find()) {
                        String name = namePatternMatcher.group("name");
                        StringBuilder oCRSourceBuilder = new StringBuilder();
                        oCRSourceBuilder.append(queue.get(0)).append(queue.get(1)).append(queue.get(2));
                        String oCRSource = oCRSourceBuilder.toString();
                        Address address = new Address(street,
                                number,
                                null, //postOfficeBox
                                zipcode,
                                null, //region
                                city,
                                null //country
                        );
                        Company company = new Company(name,
                                new LinkedList<>(Arrays.asList(name.split("[\\s]+"))),
                                new LinkedList<>(Arrays.asList(address)),
                                new LinkedList<EmailAddress>(), //emails (can't set everything here because otherwise the service becomes too complicated)
                                new LinkedList<TelephoneNumber>() //telephoneNumbers
                        );
                        ValueDetectionResult<Company> valueDetectionResult = new ValueDetectionResult<>(oCRSource,
                                company
                        );
                        retValue.add(valueDetectionResult);
                    }
                    queue.remove(0); //remove no matter whether Matcher.find
                        //returned true or false
                }else {
                    queue.remove(0);
                }
            }else {
                queue.remove(0);
            }
        }
        return retValue;

//        // Only get things under html -> body -> div (class=header)
//        XPathParser xhtmlParser = new XPathParser("xhtml", XHTMLContentHandler.XHTML);
//        Matcher divContentMatcher = xhtmlParser.parse("/xhtml:html/xhtml:body/xhtml:div/descendant::node()");
//        ContentHandler handler = new MatchingContentHandler(
//                new ToXMLContentHandler(), divContentMatcher);
//
//        AutoDetectParser parser = new AutoDetectParser();
//        Metadata metadata = new Metadata();
//        try (InputStream stream = new ByteArrayInputStream(input.getBytes())) {
//            parser.parse(stream, handler, metadata);
////            return handler.toString();
//        } catch (IOException ex) {
//            Logger.getLogger(AddressValueDetectionService.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (SAXException ex) {
//            Logger.getLogger(AddressValueDetectionService.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (TikaException ex) {
//            Logger.getLogger(AddressValueDetectionService.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
    }
}
