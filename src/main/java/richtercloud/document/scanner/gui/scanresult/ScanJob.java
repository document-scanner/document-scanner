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
package richtercloud.document.scanner.gui.scanresult;

import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneException;
import au.com.southsky.jfreesane.SaneStatus;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.scanner.DocumentSource;
import richtercloud.document.scanner.ifaces.ImageWrapper;
import richtercloud.document.scanner.model.imagewrapper.CachingImageWrapper;
import richtercloud.message.handler.Message;
import richtercloud.message.handler.MessageHandler;

/**
 *
 * @author richter
 */
/*
internal implementation notes:
- can't be a Java FX or Swing specific worker class, i.e. Task or SwingWorker
because that limits flexibility and executing UI-relevant callbacks on the EDT
with Platform.runLater and SwingUtilities.invokeLater should be sufficient
*/
public class ScanJob extends DocumentJob implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(ScanJob.class);
    private final SaneDevice scannerDevice;
    private final DocumentSource selectedDocumentSource;
    private final File imageWrapperStorageDir;
    private final Integer pageCount;
    private final MessageHandler messageHandler;
    private final DocumentController documentController;
    private ScanJobFinishCallback finishCallback;

    /**
     * Creates a new scan job. This is supposed to be called with a valid
     * job number assigned from {@link DocumentController}.
     *
     * @param scannerDevice
     * @param selectedDocumentSource
     * @param imageWrapperStorageDir
     * @param pageCount the number of pages to scan at most ({@code null} means
     * scan all pages from ADF)
     * @param scanJobLock
     * @param messageHandler
     */
    public ScanJob(DocumentController documentController,
            SaneDevice scannerDevice,
            DocumentSource selectedDocumentSource,
            File imageWrapperStorageDir,
            Integer pageCount,
            MessageHandler messageHandler,
            int jobNumber) {
        super(false,
                jobNumber);
        this.documentController = documentController;
        this.scannerDevice = scannerDevice;
        this.selectedDocumentSource = selectedDocumentSource;
        this.imageWrapperStorageDir = imageWrapperStorageDir;
        this.pageCount = pageCount;
        this.messageHandler = messageHandler;
    }

    public void setFinishCallback(ScanJobFinishCallback finishCallback) {
        this.finishCallback = finishCallback;
    }

    @Override
    public void run() {
        try {
            LOGGER.debug("waiting for scan job lock");
            documentController.getScanJobLock().lock();
            LOGGER.debug("scan job lock acquired");
            documentController.setDocumentSourceEnum(scannerDevice,
                    selectedDocumentSource);
            if(selectedDocumentSource == DocumentSource.FLATBED || selectedDocumentSource == DocumentSource.UNKNOWN) {
                BufferedImage scannedImage = scannerDevice.acquireImage();
                //catching ScanException and invoking scannerDevice.close
                //causes all settings to be reset (resolution, color, etc.)
                //and doesn't avoid SaneException at every following call to
                //scannerDevice.acquireImage
                ImageWrapper imageWrapper = new CachingImageWrapper(imageWrapperStorageDir,
                        scannedImage);
                getImages().add(imageWrapper);
            }else {
                //ADF or duplex ADF
                if(selectedDocumentSource == DocumentSource.ADF) {
                    documentController.setDocumentSource(scannerDevice, "ADF");
                }else {
                    documentController.setDocumentSource(scannerDevice, "Duplex");
                }
                if(pageCount == null) {
                    while (true) {
                        try {
                            BufferedImage scannedImage = scannerDevice.acquireImage();
                            ImageWrapper imageWrapper = new CachingImageWrapper(imageWrapperStorageDir,
                                    scannedImage);
                            getImages().add(imageWrapper);
                        } catch (SaneException e) {
                            if (e.getStatus() == SaneStatus.STATUS_NO_DOCS) {
                                // this is the out of paper condition that we expect
                                LOGGER.info("no pages left to scan");
                                break;
                            } else {
                                // some other exception that was not expected
                                throw e;
                            }
                        }
                    }
                }else {
                    int scannedPagesCount = 0;
                    while(scannedPagesCount < pageCount) {
                        LOGGER.info(String.format("requested scan of %d pages", pageCount));
                        try {
                            BufferedImage scannedImage = scannerDevice.acquireImage();
                            ImageWrapper imageWrapper = new CachingImageWrapper(imageWrapperStorageDir,
                                    scannedImage);
                            getImages().add(imageWrapper);
                        } catch (SaneException e) {
                            if (e.getStatus() == SaneStatus.STATUS_NO_DOCS) {
                                // this is the out of paper condition that we expect
                                LOGGER.info("no pages left to scan");
                                break;
                            } else {
                                // some other exception that was not expected
                                throw e;
                            }
                        }
                        scannedPagesCount += 1;
                    }
                    scannerDevice.cancel(); //scanner remains in scan mode otherwise
                }
            }
            this.setFinished(true);
        }catch(IOException | SaneException ex) {
            messageHandler.handle(new Message(ex.getCause(), JOptionPane.ERROR_MESSAGE));
        }finally {
            documentController.getScanJobLock().unlock();
            LOGGER.debug("scan job lock released");
        }
        this.finishCallback.callback(getImagesUnmodifiable());
            //should be called after releasing scanJobLock because if a
            //ScanResultDialog is displayed no other scans can be added inside
            //the dialog
    }
}
