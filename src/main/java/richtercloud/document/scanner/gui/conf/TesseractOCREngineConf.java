/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package richtercloud.document.scanner.gui.conf;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import richtercloud.document.scanner.ocr.TesseractOCREngine;

/**
 *
 * @author richter
 */
public class TesseractOCREngineConf implements Serializable, OCREngineConf<TesseractOCREngine> {
    private static final long serialVersionUID = 1L;
    private static TesseractOCREngine INSTANCE;
    private final static List<String> SELECTED_LANGUAGES_DEFAULT = Collections.unmodifiableList(new LinkedList<>(Arrays.asList("deu")));
    private List<String> selectedLanguages = SELECTED_LANGUAGES_DEFAULT;
    /**
     * the {@code tesseract} binary
     */
    private String tesseract = TesseractOCREngine.TESSERACT_DEFAULT;

    public TesseractOCREngineConf() {
    }

    /**
     * @return the selectedLanguages
     */
    public List<String> getSelectedLanguages() {
        return selectedLanguages;
    }

    /**
     * @param selectedLanguages the selectedLanguages to set
     */
    public void setSelectedLanguages(List<String> selectedLanguages) {
        this.selectedLanguages = selectedLanguages;
    }

    @Override
    public TesseractOCREngine getOCREngine() {
        if(INSTANCE == null) {
            INSTANCE = new TesseractOCREngine(selectedLanguages);
        }
        return INSTANCE;
    }

    public String getTesseract() {
        return tesseract;
    }

    public void setTesseract(String tesseract) {
        this.tesseract = tesseract;
    }
}
