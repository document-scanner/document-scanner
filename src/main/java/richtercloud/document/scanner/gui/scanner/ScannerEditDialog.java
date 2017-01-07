/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package richtercloud.document.scanner.gui.scanner;

import au.com.southsky.jfreesane.OptionValueConstraintType;
import au.com.southsky.jfreesane.OptionValueType;
import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneException;
import au.com.southsky.jfreesane.SaneOption;
import au.com.southsky.jfreesane.SaneWord;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.DocumentScanner;
import richtercloud.message.handler.Message;
import richtercloud.message.handler.MessageHandler;

/**
 * Provides configuration for scan mode and resolution of SANE device with GUI
 * components in a {@link JDialog}.
 *
 * Option value changes are performed on the referenced {@link SaneDevice}
 * directly in order to KISS.
 *
 * @author richter
 */
public class ScannerEditDialog extends javax.swing.JDialog {
    private static final long serialVersionUID = 1L;
    private MutableComboBoxModel<String> modeComboBoxModel = new DefaultComboBoxModel<>();
    private MutableComboBoxModel<Integer> resolutionComboBoxModel = new DefaultComboBoxModel<>();
    private MutableComboBoxModel<String> documentSourceComboBoxModel = new DefaultComboBoxModel<>();
    private final static Logger LOGGER = LoggerFactory.getLogger(ScannerEditDialog.class);
    private final SaneDevice device;
    private final MessageHandler messageHandler;
    public final static String MODE_OPTION_NAME = "mode";
    public final static String RESOLUTION_OPTION_NAME = "resolution";
    public final static String DOCUMENT_SOURCE_OPTION_NAME = "source";
    public final static String TOP_LEFT_X = "tl-x";
    public final static String TOP_LEFT_Y = "tl-y";
    public final static String BOTTOM_RIGHT_X = "br-x";
    public final static String BOTTOM_RIGHT_Y = "br-y";
    private final ScannerConf scannerConf;
    private final DefaultListModel<ScannerConfPaperFormat> paperFormatListModel = new DefaultListModel<>();

    public ScannerEditDialog(Dialog parent,
            final SaneDevice device,
            ScannerConf scannerConf,
            int resolutionWish,
            MessageHandler messageHandler) throws IOException, SaneException {
        super(parent,
                true //modal
        );
        if(device == null) {
            throw new IllegalArgumentException("device mustn't be null");
        }
        this.device = device;
        if(messageHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.scannerConf = scannerConf;
        this.messageHandler = messageHandler;
        init(device,
                scannerConf,
                resolutionWish);
    }

    /**
     * Creates new form ScannerEditDialog
     * @param parent
     * @param device
     * @param scannerConf
     * @param messageHandler
     * @throws java.io.IOException if {@link SaneDevice#open() } fails
     * @throws au.com.southsky.jfreesane.SaneException if
     * {@link SaneDevice#open() } fails
     */
    public ScannerEditDialog(java.awt.Frame parent,
            final SaneDevice device,
            ScannerConf scannerConf,
            int resolutionWish,
            MessageHandler messageHandler) throws IOException, SaneException {
        super(parent,
                DocumentScanner.generateApplicationWindowTitle(String.format("Editing scanner settings of %s", device.toString()),
                        DocumentScanner.APP_NAME,
                        DocumentScanner.APP_VERSION),
                true //modal
        );
        if(device == null) {
            throw new IllegalArgumentException("device mustn't be null");
        }
        this.device = device;
        if(messageHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.scannerConf = scannerConf;
        this.messageHandler = messageHandler;
        init(device,
                scannerConf,
                resolutionWish);
    }

    private void init(final SaneDevice device,
            final ScannerConf scannerConf,
            int resolutionWish) throws IOException, SaneException {
        initComponents();
        if(!device.isOpen()) {
            device.open();
        }
        configureDefaultOptionValues(device,
                scannerConf,
                resolutionWish);
        //values in scannerConf should be != null after
        //configureDefaultOptionValues
        //set values after adding listeners below
        for(String mode : device.getOption("mode").getStringConstraints()) {
            modeComboBoxModel.addElement(mode);
        }
        for(SaneWord resolution : device.getOption("resolution").getWordConstraints()) {
            resolutionComboBoxModel.addElement(resolution.integerValue());
        }
        List<String> documentSourceConstraints = device.getOption("source").getStringConstraints();
        for(String documentSource : documentSourceConstraints) {
            this.documentSourceComboBoxModel.addElement(documentSource);
        }
        for(ScannerConfPaperFormat paperFormat : scannerConf.getAvailablePaperFormats()) {
            this.paperFormatListModel.addElement(paperFormat);
        }
        assert !scannerConf.getAvailablePaperFormats().isEmpty();
        this.paperFormatList.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                assert value instanceof ScannerConfPaperFormat;
                ScannerConfPaperFormat valueCast = (ScannerConfPaperFormat) value;
                String paperFormatString = String.format("%s (%d x %d)",
                        valueCast.getName(),
                        (int)valueCast.getWidth(), //skip trailing zeros
                            //because 0.1 mm are not interesting for
                            //paper format selection
                        (int)valueCast.getHeight());
                return super.getListCellRendererComponent(list,
                        paperFormatString,
                        index,
                        isSelected,
                        cellHasFocus);
            }
        });
        //add ItemListener after setup
        this.modeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String mode;
                try {
                    mode = (String) ScannerEditDialog.this.modeComboBox.getSelectedItem();
                    setMode(device,
                            mode);
                    scannerConf.setMode(mode);
                } catch(IllegalArgumentException ex) {
                    messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
                } catch (IOException | SaneException ex) {
                    //not supposed to happen
                    throw new RuntimeException(ex);
                }
            }
        });
        this.resolutionComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int resolution;
                try {
                    resolution = (Integer) ScannerEditDialog.this.resolutionComboBox.getSelectedItem();
                    setResolution(device, resolution);
                    scannerConf.setResolution(resolution);
                } catch(IllegalArgumentException ex) {
                    messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
                } catch (IOException | SaneException ex) {
                    //not supposed to happen
                    throw new RuntimeException(ex);
                }
            }
        });
        this.documentSourceComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String documentSource;
                try {
                    documentSource = (String) ScannerEditDialog.this.documentSourceComboBox.getSelectedItem();
                    setDocumentSource(device,
                            documentSource);
                    scannerConf.setSource(documentSource);
                } catch(IllegalArgumentException ex) {
                    messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
                } catch (IOException | SaneException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        this.paperFormatList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                try {
                    assert paperFormatList.getSelectedValue() != null;
                    ScannerConfPaperFormat selectedFormat = paperFormatList.getSelectedValue();
                    setPaperFormat(device,
                            selectedFormat.getWidth(),
                            selectedFormat.getHeight());
                    scannerConf.setPaperFormat(selectedFormat);
                } catch(IllegalArgumentException ex) {
                    messageHandler.handle(new Message(ex, JOptionPane.ERROR_MESSAGE));
                } catch (IOException | SaneException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        this.modeComboBox.setSelectedItem(scannerConf.getMode());
        this.resolutionComboBox.setSelectedItem(scannerConf.getResolution());
        this.documentSourceComboBox.setSelectedItem(scannerConf.getSource());
        this.paperFormatList.setSelectedValue(scannerConf.getPaperFormat(),
                true //shouldScroll
        );
            //should trigger selection listeners
    }

    private static String selectBestDocumentSource(List<String> documentSourceConstraints) {
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
    public static void configureDefaultOptionValues(SaneDevice device,
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

    public static void setMode(SaneDevice device,
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

    public static void configureModeDefault(SaneDevice device,
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

    public static void setResolution(SaneDevice device,
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

    public static void configureResolutionDefault(SaneDevice device,
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

    public static void setDocumentSource(SaneDevice device,
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

    public static void configureDocumentSourceDefault(SaneDevice device,
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

    public static void setPaperFormat(SaneDevice device,
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

    public static DocumentSource getDocumentSourceEnum(SaneDevice device) throws IOException, SaneException {
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

    public static void setDocumentSourceEnum(SaneDevice device,
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        modeComboBoxLabel = new javax.swing.JLabel();
        modeComboBox = new javax.swing.JComboBox<>();
        resolutionComboBox = new javax.swing.JComboBox<>();
        resolutionComboBoxLabel = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();
        documentSourceComboBox = new javax.swing.JComboBox<>();
        documentSourceComboBoxLabel = new javax.swing.JLabel();
        paperFormatListLabel = new javax.swing.JLabel();
        paperFormatListScrollPane = new javax.swing.JScrollPane();
        paperFormatList = new javax.swing.JList<>();
        paperFormatAddButton = new javax.swing.JButton();
        paperFormatEditButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        modeComboBoxLabel.setText("Mode");

        modeComboBox.setModel(modeComboBoxModel);

        resolutionComboBox.setModel(resolutionComboBoxModel);

        resolutionComboBoxLabel.setText("Resolution");

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        documentSourceComboBox.setModel(documentSourceComboBoxModel);

        documentSourceComboBoxLabel.setText("Document source");

        paperFormatListLabel.setText("Paper format");

        paperFormatList.setModel(paperFormatListModel);
        paperFormatList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        paperFormatListScrollPane.setViewportView(paperFormatList);

        paperFormatAddButton.setText("Add");
        paperFormatAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paperFormatAddButtonActionPerformed(evt);
            }
        });

        paperFormatEditButton.setText("Edit");
        paperFormatEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paperFormatEditButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 375, Short.MAX_VALUE)
                                .addComponent(closeButton))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(documentSourceComboBoxLabel)
                                    .addComponent(paperFormatListLabel))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(modeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(resolutionComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(documentSourceComboBox, 0, 278, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(paperFormatListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(paperFormatAddButton, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
                                            .addComponent(paperFormatEditButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
                        .addGap(12, 12, 12))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(resolutionComboBoxLabel)
                            .addComponent(modeComboBoxLabel))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modeComboBoxLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resolutionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resolutionComboBoxLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(documentSourceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(documentSourceComboBoxLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(paperFormatListLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(paperFormatAddButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(paperFormatEditButton)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(closeButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(paperFormatListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 66, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed

    private void paperFormatAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paperFormatAddButtonActionPerformed
        ScannerConfPaperFormat paperFormat = new ScannerConfPaperFormat();
        ScannerConfPaperFormatDialog paperFormatDialog = new ScannerConfPaperFormatDialog(this,
                messageHandler,
                paperFormat);
        paperFormatDialog.setVisible(true);
    }//GEN-LAST:event_paperFormatAddButtonActionPerformed

    private void paperFormatEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paperFormatEditButtonActionPerformed
        ScannerConfPaperFormatDialog paperFormatDialog = new ScannerConfPaperFormatDialog(this,
                messageHandler,
                scannerConf.getPaperFormat());
        paperFormatDialog.setVisible(true);
    }//GEN-LAST:event_paperFormatEditButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JComboBox<String> documentSourceComboBox;
    private javax.swing.JLabel documentSourceComboBoxLabel;
    private javax.swing.JComboBox<String> modeComboBox;
    private javax.swing.JLabel modeComboBoxLabel;
    private javax.swing.JButton paperFormatAddButton;
    private javax.swing.JButton paperFormatEditButton;
    private javax.swing.JList<ScannerConfPaperFormat> paperFormatList;
    private javax.swing.JLabel paperFormatListLabel;
    private javax.swing.JScrollPane paperFormatListScrollPane;
    private javax.swing.JComboBox<Integer> resolutionComboBox;
    private javax.swing.JLabel resolutionComboBoxLabel;
    // End of variables declaration//GEN-END:variables
}
