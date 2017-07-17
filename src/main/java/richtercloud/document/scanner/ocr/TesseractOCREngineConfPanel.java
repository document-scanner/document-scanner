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
package richtercloud.document.scanner.ocr;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import richtercloud.message.handler.Message;
import richtercloud.message.handler.MessageHandler;

/**
 *
 * @author richter
 */
public class TesseractOCREngineConfPanel extends OCREngineConfPanel<TesseractOCREngineConf> {
    private static final long serialVersionUID = 1L;
    private final DefaultListModel<String> languageListModel = new DefaultListModel<>();
    private TesseractOCREngineConf conf;
    private final MessageHandler messageHandler;

    /**
     * Creates a new {@code TesseractOCREngineConfPanel} using an empty
     * {@link TesseractOCREngineConf}.
     * @param messageHandler receives a message if the {@code tesseract} binary
     * invoked with {@code --list-langs} returns a code {@code != 0}
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public TesseractOCREngineConfPanel(MessageHandler messageHandler) throws IOException, InterruptedException, BinaryNotFoundException {
        this(new TesseractOCREngineConf(),
                messageHandler);
    }

    /**
     * Creates a new {@code TesseractOCREngineConfPanel}.
     * @param conf the {@link TesseractOCREngineConf} which is updated by this
     * panel (consider invoking {@link TesseractOCREngineConf#validate() } and
     * handle exceptions before passing it to the constructor since it's invoked
     * in this constructor)
     * @param messageHandler receives a message if the {@code tesseract} binary
     * invoked with {@code --list-langs} returns a code {@code != 0}
     * @throws IOException
     * @throws InterruptedException
     */
    public TesseractOCREngineConfPanel(TesseractOCREngineConf conf,
            MessageHandler messageHandler) throws IOException, InterruptedException {
        this.initComponents();
        this.messageHandler = messageHandler;
        this.conf = conf;
        //assume that when the focus is lost the editing of the binary textfield
        //is finished and the list of available languages needs to be updated
        this.binaryTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    onBinaryChanged();
                }catch (IOException | InterruptedException | TesseractOCREngineAvailableLanguageRetrievalException ex) {
                    messageHandler.handle(new Message(ex, JOptionPane.WARNING_MESSAGE));
                }
            }
        });
        this.binaryTextField.setText(conf.getBinary());
        //don't call onBinaryChanged here, but on validation only
    }

    /**
     * Callback after the focus of the binary text field has been lost. Does not
     * run validation on the binary because that should be done when the save
     * button is pressed.
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws TesseractOCREngineAvailableLanguageRetrievalException if the
     * execution of the {@code tesseract} binary for information about supported
     * languages fails (see
     * {@link TesseractOCREngineConf#getAvailableLanguages() } for details)
     */
    private void onBinaryChanged() throws IOException,
            InterruptedException,
            TesseractOCREngineAvailableLanguageRetrievalException {
        TesseractOCREngineConf confToValidate = new TesseractOCREngineConf(this.conf);
        confToValidate.setBinary(binaryTextField.getText());

        //update model if validation succeeded (need to be able to handle
        //invalid configuration at initialization and after change)
        languageListModel.clear();
        List<String> availableLanguages = confToValidate.getAvailableLanguages();
        for(String lang : availableLanguages) {
            this.languageListModel.addElement(lang);
        }
        List<Integer> selectedLanguageIndices = new ArrayList<>();
        for(String selectedLanguage : confToValidate.getSelectedLanguages()) {
            int index = this.languageListModel.indexOf(selectedLanguage);
            selectedLanguageIndices.add(index);
        }
        int[] selectedLanguageIndicesArray = new int[selectedLanguageIndices.size()];
        for(int i =0; i<selectedLanguageIndices.size(); i++) {
            selectedLanguageIndicesArray[i] = selectedLanguageIndices.get(i);
        }
        this.languageList.setSelectedIndices(selectedLanguageIndicesArray);
        this.conf = confToValidate;
    }

    @Override
    public TesseractOCREngineConf getOCREngineConf() {
        return this.conf;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        languageListLabel = new javax.swing.JLabel();
        languageListScrollPane = new javax.swing.JScrollPane();
        languageList = new javax.swing.JList<>();
        binaryTextFieldLabel = new javax.swing.JLabel();
        binaryTextField = new javax.swing.JTextField();

        languageListLabel.setText("Language");

        languageList.setModel(languageListModel);
        languageListScrollPane.setViewportView(languageList);

        binaryTextFieldLabel.setText("tesseract binary");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(languageListLabel)
                    .addComponent(binaryTextFieldLabel))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(binaryTextField)
                    .addComponent(languageListScrollPane))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(binaryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(binaryTextFieldLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(languageListLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(languageListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                        .addContainerGap())))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField binaryTextField;
    private javax.swing.JLabel binaryTextFieldLabel;
    private javax.swing.JList<String> languageList;
    private javax.swing.JLabel languageListLabel;
    private javax.swing.JScrollPane languageListScrollPane;
    // End of variables declaration//GEN-END:variables

    @Override
    public void save() {
        this.conf.setSelectedLanguages(this.languageList.getSelectedValuesList());
        this.conf.setBinary(this.binaryTextField.getText());
    }

    @Override
    public void cancel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
