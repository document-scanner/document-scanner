/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package richtercloud.document.scanner.gui.conf;

import richtercloud.document.scanner.storage.Storage;

/**
 * Both a data container and factory for instance of {@code S}.
 * @author richter
 * @param <S> the type of the storage managed by this configuration
 */
public interface StorageConf<S extends Storage> {
    S getStorage();
}
