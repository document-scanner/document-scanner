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

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.ocrresult.OCRResult;
import richtercloud.document.scanner.model.Document;
import richtercloud.message.handler.ExceptionMessage;
import richtercloud.message.handler.IssueHandler;
import richtercloud.reflection.form.builder.jpa.storage.PersistenceStorage;
import richtercloud.reflection.form.builder.storage.StorageException;

/**
 *
 * @author richter
 */
/*
internal implementation notes:
- produces OCRResult instead of String value detection result because the type
is used in StringAutoCompletePanelSetter, but it's unclear why it's used there
and what the purpose of OCRResult is
*/
public class IdentifierValueDetectionService extends AbstractFormatValueDetectionService<OCRResult> {
    private final static Logger LOGGER = LoggerFactory.getLogger(IdentifierValueDetectionService.class);
    private final PersistenceStorage<Long> storage;
    private int longestIdentifierLength = -1;
    private List<String> identifiers = new LinkedList<>();
    private final int levenshteinDistanceLimit;

    public IdentifierValueDetectionService(IssueHandler issueHandler,
            PersistenceStorage<Long> storage,
            int levenshteinDistanceLimit) {
        super(issueHandler);
        this.storage = storage;
        this.levenshteinDistanceLimit = levenshteinDistanceLimit;
    }

    /**
     * Moves forwarding with windows of three times the lenght of the longest
     * identifier in order to be able to catch the location of the
     * @param input
     * @param languageIdentifier
     * @return
     */
    @Override
    protected LinkedHashSet<ValueDetectionResult<OCRResult>> fetchResults0(String input,
            String languageIdentifier) throws ResultFetchingException {
        try {
            this.identifiers = storage.runQuery("SELECT d.identifier FROM Document d",
                    String.class, //clazz
                    0 //queryLimit
            );
            for(String identifier : identifiers) {
                if(identifier.length() > longestIdentifierLength) {
                    this.longestIdentifierLength = identifier.length();
                }
            }
            LinkedHashSet<ValueDetectionResult<OCRResult>> retValue = super.fetchResults0(input,
                    languageIdentifier);
            return retValue;
        } catch (StorageException ex) {
            throw new ResultFetchingException(ex);
        }
    }

    @Override
    protected List<ValueDetectionResult<OCRResult>> checkResult(String inputSub,
            List<String> inputSplits,
            int index) throws ResultFetchingException {
        List<ValueDetectionResult<OCRResult>> retValue = new LinkedList<>();
        //check whether an identifier is exactly contained into the inputSub
        for(String identifier : identifiers) {
            if(StringUtils.containsIgnoreCase(inputSub, identifier)) {
                retValue.add(new ValueDetectionResult<>(inputSub,
                        new OCRResult(identifier)));
            }
        }
        //check with levenshtein distance
        int levenshteinDistanceMin = Integer.MAX_VALUE;
        String levenshteinDistanceMinIdentifier = null;
        for(String identifier : identifiers) {
            int levenshteinDistance = LevenshteinDistance.getDefaultInstance().apply(inputSub,
                    identifier);
            if(levenshteinDistance < levenshteinDistanceLimit
                    && levenshteinDistance < levenshteinDistanceMin) {
                levenshteinDistanceMin = levenshteinDistance;
                levenshteinDistanceMinIdentifier = identifier;
            }
        }
        if(levenshteinDistanceMinIdentifier != null) {
            retValue.add(new ValueDetectionResult<>(inputSub,
                    new OCRResult(levenshteinDistanceMinIdentifier)));
        }
        return retValue;
    }

    @Override
    protected List<String> generateInputSplits(String input) {
        List<String> retValue = new LinkedList<>();
        String input0 = input;
        while(input0.length() > 3*longestIdentifierLength) {
            retValue.add(input0.substring(0, 3*longestIdentifierLength));
            input0 = input0.substring(3*longestIdentifierLength);
        }
        if(!input0.isEmpty()) {
            retValue.add(input0);
        }
        return retValue;
    }

    /**
     * One word is a substing of input with the length
     * {@code longestIdentifierLength} and the scan should happen for the area
     * of one word length before and after the current word.
     *
     * @return {@code 3}
     */
    @Override
    protected int getMaxWords() {
        return 3;
    }

    /**
     * Supports all languages.
     *
     * @param languageIdentifier an arbitrary language identifier can be passed
     * @return always {@code true}
     */
    @Override
    public boolean supportsLanguage(String languageIdentifier) {
        return true;
    }

    @Override
    public boolean supportsField(Field field) {
        try {
            boolean retValue = field.equals(Document.class.getDeclaredField("identifier"));
            return retValue;
        } catch (NoSuchFieldException | SecurityException ex) {
            LOGGER.error("unexpected exception during retrieval of identifier field of document class",
                    ex);
            getIssueHandler().handleUnexpectedException(new ExceptionMessage(ex));
            return false;
        }
    }
}
