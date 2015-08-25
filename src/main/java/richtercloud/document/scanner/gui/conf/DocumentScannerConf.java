/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package richtercloud.document.scanner.gui.conf;

import java.io.Serializable;
import richtercloud.document.scanner.storage.Storage;

/**
 *
 * @author richter
 */
public class DocumentScannerConf implements Serializable {
    private static final long serialVersionUID = 1L;
    private final static String SCANNER_SANE_ADDRESS_DEFAULT = "localhost";
    private final static StorageConf<?> STORAGE_CONF_DEFAULT = new DerbyPersistenceStorageConf();
    private final static OCREngineConf<?> OCR_ENGINE_CONF_DEFAULT = new TesseractOCREngineConf();
    private String scannerName;
    private String scannerSaneAddress = SCANNER_SANE_ADDRESS_DEFAULT;
    private StorageConf<?> storageConf = STORAGE_CONF_DEFAULT;
    private OCREngineConf<?> oCREngineConf = OCR_ENGINE_CONF_DEFAULT;

    public DocumentScannerConf() {
    }

    /**
     * @return the scannerName
     */
    public String getScannerName() {
        return scannerName;
    }

    /**
     * @param scannerName the scannerName to set
     */
    public void setScannerName(String scannerName) {
        this.scannerName = scannerName;
    }

    /**
     * @return the scannerSaneAddress
     */
    public String getScannerSaneAddress() {
        return scannerSaneAddress;
    }

    /**
     * @param scannerSaneAddress the scannerSaneAddress to set
     */
    public void setScannerSaneAddress(String scannerSaneAddress) {
        this.scannerSaneAddress = scannerSaneAddress;
    }

    /**
     * @return the oCREngineConf
     */
    public OCREngineConf<?> getoCREngineConf() {
        return oCREngineConf;
    }

    /**
     * @param oCREngineConf the oCREngineConf to set
     */
    public void setoCREngineConf(OCREngineConf<?> oCREngineConf) {
        this.oCREngineConf = oCREngineConf;
    }

    /**
     * @return the storageConf
     */
    public StorageConf<?> getStorageConf() {
        return storageConf;
    }

    /**
     * @param storageConf the storageConf to set
     */
    public void setStorageConf(StorageConf<?> storageConf) {
        this.storageConf = storageConf;
    }
}
