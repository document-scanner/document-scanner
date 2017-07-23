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

import au.com.southsky.jfreesane.OptionValueConstraintType;
import au.com.southsky.jfreesane.OptionValueType;
import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneException;
import au.com.southsky.jfreesane.SaneOption;
import au.com.southsky.jfreesane.SaneSession;
import au.com.southsky.jfreesane.SaneWord;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.scanner.DocumentSource;
import richtercloud.document.scanner.gui.scanner.ScannerConf;
import static richtercloud.document.scanner.gui.scanner.ScannerEditDialog.BOTTOM_RIGHT_X;
import static richtercloud.document.scanner.gui.scanner.ScannerEditDialog.BOTTOM_RIGHT_Y;
import static richtercloud.document.scanner.gui.scanner.ScannerEditDialog.DOCUMENT_SOURCE_OPTION_NAME;
import static richtercloud.document.scanner.gui.scanner.ScannerEditDialog.MODE_OPTION_NAME;
import static richtercloud.document.scanner.gui.scanner.ScannerEditDialog.RESOLUTION_OPTION_NAME;
import static richtercloud.document.scanner.gui.scanner.ScannerEditDialog.TOP_LEFT_X;
import static richtercloud.document.scanner.gui.scanner.ScannerEditDialog.TOP_LEFT_Y;
import richtercloud.document.scanner.ifaces.DocumentAddException;
import richtercloud.document.scanner.ifaces.ImageWrapper;
import richtercloud.message.handler.MessageHandler;

/**
 * Manages shared resources related to scanning and is a controller in the sence
 * of a MVC architecture (which doesn't apply to the whole application, yet).
 *
 * {@code DocumentController} manages all scanner device used in the application
 * and provides instances of {@link SaneDevice} based on the unique device name
 * through
 * {@link #getScannerDevice(java.lang.String, java.util.Map, java.lang.String, int) }.
 * It'd be nice if {@code DocumentController} would hide instances of {@link SaneDevice}
 * from callers, but that would require to pass all arguments of
 * {@link #getScannerDevice(java.lang.String, java.util.Map, java.lang.String, int) }
 * to every location where a scanner device is involved.
 *
 * @author richter
 */
/*
internal implementation notes:
- There's no sense to keep a SaneDevice reference in DocumentController because
SaneDevice instances are retrieved from getScannerDevice based on the scanner
device name which come from the caller.
*/
public class DocumentController {
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);
    /**
     * The lock to avoid any issues coming from accessing {@code scannerDevice}
     * from different threads. Only one scan can take place at the same time
     * because of the nature of a scanner device anyway, so there's no need to
     * care about eventual parallelization in form of an executor service or
     * else; a list of results is fine.
     */
    private Lock scanJobLock = new ReentrantLock(true //fair (doesn't matter)
            );
    private final List<DocumentJob> documentJobs = new LinkedList<>();
    /**
     * The count of all scan jobs ever started. It doesn't make sense to
     * reassign numbers of already completed jobs to new jobs (they're visible
     * in GUI components) because that's confusing and prevents easy ordering of
     * jobs.
     */
    /*
    internal implementation notes:
    - is incremented before first use because of the logic of incrementAndGet
    */
    private final AtomicInteger documentJobCount = new AtomicInteger(0);
    /**
     * Mapping between scanner devices and the currently running opening
     * attempt. This allows to run time consuming opening of SANE devices in the
     * background, in parallel and restrict the opening attempts to one at a
     * time.
     */
    private Map<SaneDevice, Future<Void>> deviceOpeningFutureMap = new HashMap<>();
    /**
     * Since {@link SaneSession#getDevice(java.lang.String) } overwrites
     * configuration settings, keep a reference to once retrieved
     * {@link SaneDevice}.
     */
    private final Map<String, SaneDevice> nameDeviceMap = new HashMap<>();
    /**
     * Uses the string representation stored in {@link ScannerConf} in order to
     * avoid confusion with equality of {@link InetAddress}es.
     */
    private final Map<String, SaneSession> addressSessionMap = new HashMap<>();

    public SaneDevice getScannerDevice(String scannerName,
            Map<String, ScannerConf> scannerConfMap,
            String scannerAddressFallback,
            int resolutionWish) throws IOException, SaneException {
        if(scannerAddressFallback == null) {
            throw new IllegalArgumentException("scannerAddressFallback mustn't be null");
        }
        SaneDevice retValue = nameDeviceMap.get(scannerName);
        if(retValue == null) {
            ScannerConf scannerConf = scannerConfMap.get(scannerName);
            if(scannerConf == null) {
                scannerConf = new ScannerConf(scannerName);
                scannerConfMap.put(scannerName, scannerConf);
            }
            SaneSession saneSession = addressSessionMap.get(scannerConf.getScannerAddress());
            if(saneSession == null) {
                String scannerAddress = scannerConf.getScannerAddress();
                if(scannerAddress == null) {
                    scannerAddress = scannerAddressFallback;
                }
                InetAddress scannerInetAddress = InetAddress.getByName(scannerAddress);
                saneSession = SaneSession.withRemoteSane(scannerInetAddress);
                addressSessionMap.put(scannerConf.getScannerAddress(), saneSession);
                scannerConf.setScannerAddress(scannerAddress);
            }
            retValue = saneSession.getDevice(scannerName);
            nameDeviceMap.put(scannerName, retValue);
            configureDefaultOptionValues(retValue,
                    scannerConf,
                    resolutionWish
            );
        }
        return retValue;
    }

    public void openScannerDevice(SaneDevice scannerDevice,
            long scannerOpenWaitTime,
            TimeUnit scannerOpenWaitTimeUnit) throws DeviceOpeningAlreadyInProgressException, SaneException, IOException, DocumentAddException, InterruptedException, TimeoutException {
        if(!scannerDevice.isOpen()) {
            if(deviceOpeningFutureMap.get(scannerDevice) != null) {
                throw new DeviceOpeningAlreadyInProgressException(scannerDevice.getName());
            }
            LOGGER.debug(String.format("opening closed device '%s'",
                    scannerDevice));
            Future<Void> deviceOpeningFuture = Executors.newSingleThreadExecutor().submit(() -> {
                scannerDevice.open();
                return null;
                    //only Callables allow throwing of exceptions
            });
            deviceOpeningFutureMap.put(scannerDevice, deviceOpeningFuture);
            try {
                deviceOpeningFuture.get(scannerOpenWaitTime,
                       scannerOpenWaitTimeUnit);
            }catch(ExecutionException ex) {
                if(ex.getCause() instanceof SaneException) {
                    throw (SaneException)ex.getCause();
                }else if(ex.getCause() instanceof IOException) {
                    throw (IOException)ex.getCause();
                }else if(ex.getCause() instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException)ex.getCause();
                }else if(ex.getCause() instanceof IllegalStateException) {
                    throw (IllegalStateException)ex.getCause();
                }else if(ex.getCause() instanceof DocumentAddException) {
                    throw (DocumentAddException)ex.getCause();
                }
                throw new RuntimeException(ex);
            }
            this.deviceOpeningFutureMap.remove(scannerDevice);
        }
    }

    public boolean checkScannerDeviceOpen(String scannerDeviceName,
            Map<String, ScannerConf> scannerConfMap,
            String scannerAddressFallback,
            int resolutionWish) throws IOException,
            SaneException {
        SaneDevice scannerDevice = getScannerDevice(scannerDeviceName,
                scannerConfMap,
                scannerAddressFallback,
                resolutionWish);
        return scannerDevice.isOpen();
    }

    /**
     * Create and add a {@link DocumentJob} to be retrievable from the job list,
     * e.g. from a PDF file.
     *
     * @param documentJob the document job to add
     */
    public DocumentJob addDocumentJob(List<ImageWrapper> images) {
        DocumentJob retValue = new DocumentJob(images,
                this.documentJobCount.incrementAndGet() //jobNumber
        );
        this.documentJobs.add(retValue);
        return retValue;
    }

    public ScanJob addScanJob(DocumentController documentController,
            SaneDevice scannerDevice,
            DocumentSource selectedDocumentSource,
            File imageWrapperStorageDir,
            Integer pageCount,
            MessageHandler messageHandler) {
        ScanJob retValue = new ScanJob(documentController,
                scannerDevice,
                selectedDocumentSource,
                imageWrapperStorageDir,
                pageCount,
                messageHandler,
                this.documentJobCount.incrementAndGet() //jobNumber
        );
        this.documentJobs.add(retValue);
        return retValue;
    }

    public void shutdown() {
        for(SaneDevice scannerDevice : nameDeviceMap.values()) {
            if(scannerDevice != null) {
                if(scannerDevice.isOpen()) {
                    try {
                        scannerDevice.close();
                    } catch (IOException ex) {
                        LOGGER.warn(String.format("an unexpected exception occured during closing the scanner device '%s'",
                                scannerDevice.getName()));
                    }
                }
            }
        }
    }

    /**
     * Sets convenient default values on the scanner ("color" scan mode and the
     * resolution value closest to 300 DPI). Readability and writability of
     * options are checked. The type of the options are checked as well in order
     * to fail with a helpful error message in case of an errornous SANE
     * implementation.
     *
     * For a list of SANE options see
     * http://www.sane-project.org/html/doc014.html.
     *
     * @param device
     * @param scannerConf the {@link ScannerConf} to retrieve eventually
     * existing values from (e.g. persisted values from previous runs)
     * @throws IOException
     * @throws SaneException
     * @throws IllegalArgumentException if the option denoted by
     * {@link #MODE_OPTION_NAME} or {@link #RESOLUTION_OPTION_NAME} isn't
     * readable or writable
     */
    public void configureDefaultOptionValues(SaneDevice device,
            ScannerConf scannerConf,
            int resolutionWish) throws IOException, SaneException {
        assert device != null;
        assert scannerConf != null;
        assert scannerConf.getPaperFormat() != null;
        if(!device.isOpen()) {
            LOGGER.debug(String.format("opening closed device '%s'",
                    device));
            device.open();
        }
        configureModeDefault(device,
                scannerConf);
        configureResolutionDefault(device,
                scannerConf,
                resolutionWish);
        configureDocumentSourceDefault(device,
                scannerConf);
        setMode(device,
                scannerConf.getMode());
        setResolution(device,
                scannerConf.getResolution());
        setDocumentSource(device,
                scannerConf.getSource());
    }

    public void setMode(SaneDevice device,
            String mode) throws IOException, SaneException {
        if(!device.isOpen()) {
            LOGGER.debug(String.format("opening closed device '%s'",
                    device));
            device.open();
        }
        SaneOption modeOption = device.getOption(MODE_OPTION_NAME);
        if(!modeOption.isWriteable()) {
            throw new IllegalArgumentException(String.format("Option '%s' isn't writable.", MODE_OPTION_NAME));
        }
        LOGGER.debug(String.format("setting default mode '%s' on device '%s'", mode, device));
        modeOption.setStringValue(mode);
    }

    public void configureModeDefault(SaneDevice device,
            ScannerConf scannerConf) throws IOException {
        String mode = scannerConf.getMode();
        SaneOption modeOption = device.getOption(MODE_OPTION_NAME);
        if(!modeOption.isReadable()) {
            throw new IllegalArgumentException(String.format("option '%s' isn't readable", MODE_OPTION_NAME));
        }
        if(!modeOption.getType().equals(OptionValueType.STRING)) {
            throw new IllegalArgumentException(String.format("Option '%s' isn't of type STRING. This indicates an errornous SANE implementation. Can't proceed.", MODE_OPTION_NAME));
        }
        if(mode == null) {
            for(String modeConstraint : modeOption.getStringConstraints()) {
                if("color".equalsIgnoreCase(modeConstraint.trim())) {
                    mode = modeConstraint;
                    break;
                }
            }
            if(mode == null) {
                mode = modeOption.getStringConstraints().get(0);
            }
            scannerConf.setMode(mode);
        }
        scannerConf.setMode(mode);
    }

    public void setResolution(SaneDevice device,
            int resolution) throws IOException, SaneException {
        if(!device.isOpen()) {
            LOGGER.debug(String.format("opening closed device '%s'",
                    device));
            device.open();
        }
        SaneOption resolutionOption = device.getOption(RESOLUTION_OPTION_NAME);
        if(!resolutionOption.getType().equals(OptionValueType.INT)) {
            throw new IllegalArgumentException(String.format("Option '%s' isn't of type INT. This indicates an errornous SANE implementation. Can't proceed.", RESOLUTION_OPTION_NAME));
        }
        if(!resolutionOption.isWriteable()) {
            throw new IllegalArgumentException(String.format("option '%s' isn't writable", RESOLUTION_OPTION_NAME));
        }
        LOGGER.debug(String.format("setting default resolution '%d' on device '%s'", resolution, device));
        resolutionOption.setIntegerValue(resolution);
    }

    public void configureResolutionDefault(SaneDevice device,
            ScannerConf scannerConf,
            int resolutionWish) throws IOException {
        SaneOption resolutionOption = device.getOption(RESOLUTION_OPTION_NAME);
        if(!resolutionOption.isReadable()) {
            throw new IllegalArgumentException(String.format("Option '%s' isn't readable.", RESOLUTION_OPTION_NAME));
        }
        if(!resolutionOption.getType().equals(OptionValueType.INT)) {
            throw new IllegalArgumentException(String.format("Option '%s' isn't of type INT. This indicates an errornous SANE implementation. Can't proceed.", RESOLUTION_OPTION_NAME));
        }
        Integer resolution = scannerConf.getResolution();
        if(resolution == null) {
            int resolutionDifference = Integer.MAX_VALUE;
            for(SaneWord resolutionConstraint : resolutionOption.getWordConstraints()) {
                int resolutionConstraintValue = resolutionConstraint.integerValue();
                int resolutionConstraintDifference = Math.abs(resolutionWish-resolutionConstraintValue);
                if(resolutionConstraintDifference < resolutionDifference) {
                    resolution = resolutionConstraintValue;
                    resolutionDifference = resolutionConstraintDifference;
                    if(resolutionDifference == 0) {
                        //not possible to find more accurate values
                        break;
                    }
                }
            }
            assert resolution != null;
            scannerConf.setResolution(resolution);
        }
    }

    public void setDocumentSource(SaneDevice device,
            String documentSource) throws IOException, SaneException {
        if(!device.isOpen()) {
            LOGGER.debug(String.format("opening closed device '%s'",
                    device));
            device.open();
        }
        SaneOption documentSourceOption = device.getOption(DOCUMENT_SOURCE_OPTION_NAME);
        if(!documentSourceOption.isWriteable()) {
            throw new IllegalArgumentException(String.format("option '%s' isn't writable", DOCUMENT_SOURCE_OPTION_NAME));
        }
        LOGGER.debug(String.format("setting default document source '%s' on device '%s'", documentSource, device));
        documentSourceOption.setStringValue(documentSource);
    }

    private String selectBestDocumentSource(List<String> documentSourceConstraints) {
        String documentSource = null;
        for(String documentSourceConstraint : documentSourceConstraints) {
            if("Duplex".equalsIgnoreCase(documentSourceConstraint)) {
                documentSource = documentSourceConstraint;
                break;
            }
        }
        if(documentSource == null) {
            for(String documentSourceConstraint : documentSourceConstraints) {
                if("ADF".equalsIgnoreCase(documentSourceConstraint)
                        || "Automatic document feeder".equalsIgnoreCase(documentSourceConstraint)) {
                    documentSource = documentSourceConstraint;
                    break;
                }
            }
        }
        if(documentSource == null) {
            documentSource = documentSourceConstraints.get(0);
        }
        return documentSource;
    }

    public void configureDocumentSourceDefault(SaneDevice device,
            ScannerConf scannerConf) throws IOException {
        SaneOption documentSourceOption = device.getOption(DOCUMENT_SOURCE_OPTION_NAME);
        if(!documentSourceOption.isReadable()) {
            throw new IllegalArgumentException(String.format("Option '%s' isn't readable.", DOCUMENT_SOURCE_OPTION_NAME));
        }
        if(!documentSourceOption.getType().equals(OptionValueType.STRING)) {
            throw new IllegalArgumentException(String.format("Option '%s' "
                    + "isn't of type STRING. This indicates an errornous SANE "
                    + "implementation. Can't proceed.",
                    DOCUMENT_SOURCE_OPTION_NAME));
        }
        String documentSource = scannerConf.getSource();
        if(documentSource == null) {
            documentSource = selectBestDocumentSource(documentSourceOption.getStringConstraints());
            assert documentSource != null;
            scannerConf.setSource(documentSource);
        }
    }

    public void setPaperFormat(SaneDevice device,
            float width,
            float height) throws IOException, SaneException {
        if(!device.isOpen()) {
            LOGGER.debug(String.format("opening closed device '%s'",
                    device));
            device.open();
        }
        SaneOption topLeftXOption = device.getOption(TOP_LEFT_X);
        SaneOption topLeftYOption = device.getOption(TOP_LEFT_Y);
        SaneOption bottomRightXOption = device.getOption(BOTTOM_RIGHT_X);
        SaneOption bottomRightYOption = device.getOption(BOTTOM_RIGHT_Y);
        if(!topLeftXOption.isReadable()) {
            throw new IllegalArgumentException(String.format("Option '%s' isn't readable.", TOP_LEFT_X));
        }
        if(!topLeftYOption.isReadable()) {
            throw new IllegalArgumentException(String.format("Option '%s' isn't readable.", TOP_LEFT_Y));
        }
        if(!bottomRightXOption.isReadable()) {
            throw new IllegalArgumentException(String.format("Option '%s' isn't readable.", BOTTOM_RIGHT_X));
        }
        if(!bottomRightYOption.isReadable()) {
            throw new IllegalArgumentException(String.format("Option '%s' isn't readable.", BOTTOM_RIGHT_Y));
        }
        if(!topLeftXOption.getType().equals(OptionValueType.FIXED)) {
            throw new IllegalArgumentException(String.format("Option '%s' "
                    + "isn't of type STRING. This indicates an errornous SANE "
                    + "implementation. Can't proceed.",
                    TOP_LEFT_X));
        }
        if(!topLeftYOption.getType().equals(OptionValueType.FIXED)) {
            throw new IllegalArgumentException(String.format("Option '%s' "
                    + "isn't of type STRING. This indicates an errornous SANE "
                    + "implementation. Can't proceed.",
                    TOP_LEFT_Y));
        }
        if(!bottomRightXOption.getType().equals(OptionValueType.FIXED)) {
            throw new IllegalArgumentException(String.format("Option '%s' "
                    + "isn't of type STRING. This indicates an errornous SANE "
                    + "implementation. Can't proceed.",
                    BOTTOM_RIGHT_X));
        }
        if(!bottomRightYOption.getType().equals(OptionValueType.FIXED)) {
            throw new IllegalArgumentException(String.format("Option '%s' "
                    + "isn't of type STRING. This indicates an errornous SANE "
                    + "implementation. Can't proceed.",
                    BOTTOM_RIGHT_Y));
        }
        assert width > 0;
        assert height > 0;
        if(!topLeftXOption.isWriteable()) {
            throw new IllegalArgumentException(String.format("option '%s' isn't writable", TOP_LEFT_X));
        }
        if(!topLeftYOption.isWriteable()) {
            throw new IllegalArgumentException(String.format("option '%s' isn't writable", TOP_LEFT_Y));
        }
        if(!bottomRightXOption.isWriteable()) {
            throw new IllegalArgumentException(String.format("option '%s' isn't writable", BOTTOM_RIGHT_X));
        }
        if(!bottomRightYOption.isWriteable()) {
            throw new IllegalArgumentException(String.format("option '%s' isn't writable", BOTTOM_RIGHT_Y));
        }
        LOGGER.debug(String.format("setting paper format %fx%f on device '%s'",
                width,
                height,
                device));
        if(!(OptionValueConstraintType.RANGE_CONSTRAINT.equals(topLeftXOption.getConstraintType())
                || OptionValueConstraintType.NO_CONSTRAINT.equals(topLeftXOption.getConstraintType()))) {
            throw new IllegalArgumentException(String.format("option '%s' has "
                    + "constraint type different from '%s' or '%s'",
                    TOP_LEFT_X,
                    OptionValueConstraintType.RANGE_CONSTRAINT,
                    OptionValueConstraintType.NO_CONSTRAINT //suggested
                        //descriptions at
                        //https://github.com/sjamesr/jfreesane/pull/62
            ));
        }
        if(!(OptionValueConstraintType.RANGE_CONSTRAINT.equals(topLeftYOption.getConstraintType())
                || OptionValueConstraintType.NO_CONSTRAINT.equals(topLeftYOption.getConstraintType()))) {
            throw new IllegalArgumentException(String.format("option '%s' has "
                    + "constraint type different from '%s' or '%s'",
                    TOP_LEFT_Y,
                    OptionValueConstraintType.RANGE_CONSTRAINT,
                    OptionValueConstraintType.NO_CONSTRAINT //suggested
                        //descriptions at
                        //https://github.com/sjamesr/jfreesane/pull/62
            ));
        }
        if(!(OptionValueConstraintType.RANGE_CONSTRAINT.equals(bottomRightXOption.getConstraintType())
                || OptionValueConstraintType.NO_CONSTRAINT.equals(bottomRightXOption.getConstraintType()))) {
            throw new IllegalArgumentException(String.format("option '%s' has "
                    + "constraint type different from '%s' or '%s'",
                    BOTTOM_RIGHT_X,
                    OptionValueConstraintType.RANGE_CONSTRAINT,
                    OptionValueConstraintType.NO_CONSTRAINT //suggested
                        //descriptions at
                        //https://github.com/sjamesr/jfreesane/pull/62
            ));
        }
        if(!(OptionValueConstraintType.RANGE_CONSTRAINT.equals(bottomRightYOption.getConstraintType())
                || OptionValueConstraintType.NO_CONSTRAINT.equals(bottomRightYOption.getConstraintType()))) {
            throw new IllegalArgumentException(String.format("option '%s' has "
                    + "constraint type different from '%s' or '%s'",
                    BOTTOM_RIGHT_Y,
                    OptionValueConstraintType.RANGE_CONSTRAINT,
                    OptionValueConstraintType.NO_CONSTRAINT //suggested
                        //descriptions at
                        //https://github.com/sjamesr/jfreesane/pull/62
            ));
        }
        //don't check topleft constraint values because it's just overkill
        if(OptionValueConstraintType.RANGE_CONSTRAINT.equals(bottomRightXOption.getConstraintType())) {
            double widthMinimum = bottomRightXOption.getRangeConstraints().getMinimumFixed();
            if(width < widthMinimum) {
                throw new IllegalArgumentException(String.format("width %f is "
                        + "less than the minimum %f specified by the constraint of "
                        + "option '%s'",
                        width,
                        widthMinimum,
                        BOTTOM_RIGHT_X));
            }
            double widthMaximum = bottomRightXOption.getRangeConstraints().getMaximumFixed();
            if(width > widthMaximum) {
                throw new IllegalArgumentException(String.format("width %f is "
                        + "greater than the maximum %f specified by the constraint of "
                        + "option '%s'",
                        width,
                        widthMaximum,
                        BOTTOM_RIGHT_X));
            }
        }
        if(OptionValueConstraintType.RANGE_CONSTRAINT.equals(bottomRightYOption.getConstraintType())) {
            double heightMinimum = bottomRightYOption.getRangeConstraints().getMinimumFixed();
            if(height < heightMinimum) {
                throw new IllegalArgumentException(String.format("height %f is "
                        + "less than the minimum %f specified by the constraint of "
                        + "option '%s'",
                        height,
                        heightMinimum,
                        BOTTOM_RIGHT_Y));
            }
            double heightMaximum = bottomRightYOption.getRangeConstraints().getMaximumFixed();
            if(height > heightMaximum) {
                throw new IllegalArgumentException(String.format("height %f is "
                        + "greater than the maximum %f specified by the constraint of "
                        + "option '%s'",
                        height,
                        heightMaximum,
                        BOTTOM_RIGHT_Y));
            }
        }
        topLeftXOption.setFixedValue(0);
        topLeftYOption.setFixedValue(0);
        bottomRightXOption.setFixedValue(width);
        bottomRightYOption.setFixedValue(height);
    }

    public DocumentSource getDocumentSourceEnum(SaneDevice device) throws IOException, SaneException {
        if(!device.isOpen()) {
            LOGGER.debug(String.format("opening closed device '%s'",
                    device));
            device.open();
        }
        SaneOption documentSourceOption = device.getOption(DOCUMENT_SOURCE_OPTION_NAME);
        if(!documentSourceOption.isReadable()) {
            throw new IllegalArgumentException(String.format("option '%s' isn't readable", DOCUMENT_SOURCE_OPTION_NAME));
        }
        String documentSource = documentSourceOption.getStringValue();
        DocumentSource retValue = DocumentSource.UNKNOWN;
        if(documentSource.equalsIgnoreCase("Flatbed")) {
            retValue = DocumentSource.FLATBED;
        }else if(documentSource.equalsIgnoreCase("ADF") || documentSource.equalsIgnoreCase("Automated document feeder")) {
            retValue = DocumentSource.ADF;
        }else if(documentSource.equalsIgnoreCase("Duplex")) {
            retValue = DocumentSource.ADF_DUPLEX;
        }
        return retValue;
    }

    public void setDocumentSourceEnum(SaneDevice device,
            DocumentSource documentSource) throws IOException, SaneException {
        switch(documentSource) {
            case FLATBED:
                setDocumentSource(device, "Flatbed");
                break;
            case ADF:
                setDocumentSource(device, "ADF");
                break;
            case ADF_DUPLEX:
                setDocumentSource(device, "Duplex");
                break;
            default:
                throw new IllegalStateException(String.format("document source %s not supported", documentSource));
        }
    }

    public List<DocumentJob> getDocumentJobs() {
        return documentJobs;
    }

    public AtomicInteger getDocumentJobCount() {
        return documentJobCount;
    }

    public Lock getScanJobLock() {
        return scanJobLock;
    }
}
