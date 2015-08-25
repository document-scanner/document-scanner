/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package richtercloud.document.scanner.gui.conf;

import richtercloud.document.scanner.ocr.OCREngine;

/**
 * Both a data container and factory for instance of {@code E}.
 * @author richter
 * @param <E> the type of the OCR engine managed by this configuration
 */
public interface OCREngineConf<E extends OCREngine> {
    E getOCREngine();
}
