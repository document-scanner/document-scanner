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
package richtercloud.document.scanner.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextArea;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.gui.conf.DocumentScannerConf;
import richtercloud.document.scanner.setter.ValueSetter;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.reflection.form.builder.FieldInfo;
import richtercloud.reflection.form.builder.ReflectionFormBuilder;
import richtercloud.reflection.form.builder.ReflectionFormPanel;
import richtercloud.reflection.form.builder.message.Message;
import richtercloud.reflection.form.builder.message.MessageHandler;

/**
 * The counterpart of every {@link OCRSelectPanel} which contains a text field
 * to display OCR selection results and a toolbar with buttons and popup menus
 * to select different number formats for setting OCR selections on fields.
 * @author richter
 */
/*
internal implementation notes:
- popup menu has a single menu "Paste into" rather than a disabled menu item
which is (mis)used as label for the following menu items because that's more
elegant even if less easy to use (means one click more)
- Since NumberFormats are hard to compare (NumberFormat.equals isn't
implemented and a lot of properties aren't suitable for comparison (comparing
groupingUsed, parseIntegerOnly, maximumFractionDigits, maximumIntegerDigits,
minimumFractionDigits and minimumIntegerDigits for equality results in 2
different number formats which doesn't make sense and doesn't match with a list
of all available formats); providing selection for all 160 available locales is
overkill and strangely results in > 20 identical format results of "-12345,987"
-> compare format result of "-12345,987"
*/
public class OCRPanel extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;
    private final static Logger LOGGER = LoggerFactory.getLogger(OCRPanel.class);
    private final MessageHandler messageHandler;
    private final DocumentScannerConf documentScannerConf;
    private final static double FORMAT_VALUE = -12345.987;

    /**
     * Creates new form OCRResultPanel
     * @param reflectionFormPanelMap A map with references to the
     * {@link ReflectionFormPanel} for each entity class which is manipulated by
     * the context menu items
     */
    public OCRPanel(Set<Class<?>> entityClasses,
            Map<Class<?>, ReflectionFormPanel<?>> reflectionFormPanelMap,
            Map<Class<? extends JComponent>, ValueSetter<?,?>> valueSetterMapping,
            EntityManager entityManager,
            MessageHandler messageHandler,
            ReflectionFormBuilder reflectionFormBuilder,
            DocumentScannerConf documentScannerConf) {
        this.initComponents();
        if(messageHandler == null) {
            throw new IllegalArgumentException("messageHandler mustn't be null");
        }
        this.messageHandler = messageHandler;
        if(documentScannerConf == null) {
            throw new IllegalArgumentException("documentScannerConf mustn't be "
                    + "null");
        }
        this.documentScannerConf = documentScannerConf;
        List<Class<?>> entityClassesSort = EntityPanel.sortEntityClasses(entityClasses);
        for(Class<?> entityClass : entityClassesSort) {
            ReflectionFormPanel<?> reflectionFormPanel = reflectionFormPanelMap.get(entityClass);
            if(reflectionFormPanel == null) {
                throw new IllegalArgumentException(String.format("entityClass %s has no %s mapped in reflectionFormPanelMap",
                        entityClass,
                        ReflectionFormPanel.class));
            }
            String className;
            ClassInfo classInfo = entityClass.getAnnotation(ClassInfo.class);
            if(classInfo != null) {
                className = classInfo.name();
            }else {
                className = entityClass.getSimpleName();
            }
            JMenu entityClassMenu = new JMenu(className);
            List<Field> relevantFields = reflectionFormBuilder.getFieldRetriever().retrieveRelevantFields(entityClass);
            for(Field relevantField : relevantFields) {
                String fieldName;
                FieldInfo fieldInfo = relevantField.getAnnotation(FieldInfo.class);
                if(fieldInfo != null) {
                    fieldName = fieldInfo.name();
                }else {
                    fieldName = relevantField.getName();
                }
                JMenuItem relevantFieldMenuItem = new JMenuItem(fieldName);
                relevantFieldMenuItem.addActionListener(new FieldActionListener(reflectionFormPanel,
                        relevantField,
                        valueSetterMapping));
                entityClassMenu.add(relevantFieldMenuItem);
            }
            this.oCRResultPopupPasteIntoMenu.add(entityClassMenu);
        }
        Map<String, Pair<NumberFormat, Set<Locale>>> numberFormats = new HashMap<>();
        Map<String, Pair<NumberFormat, Set<Locale>>> percentFormats = new HashMap<>();
        Map<String, Pair<NumberFormat, Set<Locale>>> currencyFormats = new HashMap<>();
        Iterator<Locale> localeIterator = new ArrayList<>(Arrays.asList(Locale.getAvailableLocales())).iterator();
        Locale firstLocale = localeIterator.next();
        String numberString = NumberFormat.getNumberInstance(firstLocale).format(FORMAT_VALUE);
        String percentString = NumberFormat.getPercentInstance(firstLocale).format(FORMAT_VALUE);
        String currencyString = NumberFormat.getCurrencyInstance(firstLocale).format(FORMAT_VALUE);
        numberFormats.put(numberString,
                new ImmutablePair<NumberFormat, Set<Locale>>(NumberFormat.getNumberInstance(firstLocale),
                        new HashSet<>(Arrays.asList(firstLocale))));
        percentFormats.put(percentString,
                new ImmutablePair<NumberFormat, Set<Locale>>(NumberFormat.getPercentInstance(firstLocale),
                        new HashSet<>(Arrays.asList(firstLocale))));
        currencyFormats.put(currencyString,
                new ImmutablePair<NumberFormat, Set<Locale>>(NumberFormat.getCurrencyInstance(firstLocale),
                        new HashSet<>(Arrays.asList(firstLocale))));
        while(localeIterator.hasNext()) {
            Locale locale = localeIterator.next();
            numberString = NumberFormat.getNumberInstance(locale).format(FORMAT_VALUE);
            percentString = NumberFormat.getPercentInstance(locale).format(FORMAT_VALUE);
            currencyString = NumberFormat.getCurrencyInstance(locale).format(FORMAT_VALUE);
            Pair<NumberFormat, Set<Locale>> numberFormatsPair = numberFormats.get(numberString);
            if(numberFormatsPair == null) {
                numberFormatsPair = new ImmutablePair<NumberFormat, Set<Locale>>(NumberFormat.getNumberInstance(locale),
                        new HashSet<Locale>());
                numberFormats.put(numberString, numberFormatsPair);
            }
            Set<Locale> numberFormatsLocales = numberFormatsPair.getValue();
            numberFormatsLocales.add(locale);
            Pair<NumberFormat, Set<Locale>> percentFormatsPair = percentFormats.get(percentString);
            if(percentFormatsPair == null) {
                percentFormatsPair = new ImmutablePair<NumberFormat, Set<Locale>>(NumberFormat.getPercentInstance(locale),
                        new HashSet<Locale>());
                percentFormats.put(percentString, percentFormatsPair);
            }
            Set<Locale> percentFormatsLocales = percentFormatsPair.getValue();
            percentFormatsLocales.add(locale);
            Pair<NumberFormat, Set<Locale>> currencyFormatsPair = currencyFormats.get(currencyString);
            if(currencyFormatsPair == null) {
                currencyFormatsPair = new ImmutablePair<NumberFormat, Set<Locale>>(NumberFormat.getCurrencyInstance(locale),
                        new HashSet<Locale>());
                currencyFormats.put(currencyString, currencyFormatsPair);
            }
            Set<Locale> currencyFormatsLocales = currencyFormatsPair.getValue();
            currencyFormatsLocales.add(locale);
        }
        for(Map.Entry<String, Pair<NumberFormat, Set<Locale>>> numberFormat : numberFormats.entrySet()) {
            JRadioButtonMenuItem menuItem = new NumberFormatMenuItem(numberFormat.getValue().getKey());
            numberFormatPopup.add(menuItem);
            numberFormatPopupButtonGroup.add(menuItem);
            if(numberFormat.getValue().getValue().contains(this.documentScannerConf.getLocale())) {
                menuItem.setSelected(true);
            }
        }
        for(Map.Entry<String, Pair<NumberFormat, Set<Locale>>> percentFormat : percentFormats.entrySet()) {
            JRadioButtonMenuItem menuItem = new NumberFormatMenuItem(percentFormat.getValue().getKey());
            percentFormatPopup.add(menuItem);
            percentFormatPopupButtonGroup.add(menuItem);
            if(percentFormat.getValue().getValue().contains(this.documentScannerConf.getLocale())) {
                menuItem.setSelected(true);
            }
        }
        for(Map.Entry<String, Pair<NumberFormat, Set<Locale>>> currencyFormat : currencyFormats.entrySet()) {
            JRadioButtonMenuItem menuItem = new NumberFormatMenuItem(currencyFormat.getValue().getKey());
            currencyFormatPopup.add(menuItem);
            currencyFormatPopupButtonGroup.add(menuItem);
            if(currencyFormat.getValue().getValue().contains(this.documentScannerConf.getLocale())) {
                menuItem.setSelected(true);
            }
        }
    }

    private class NumberFormatMenuItem extends JRadioButtonMenuItem {
        private static final long serialVersionUID = 1L;
        private final NumberFormat numberFormat;

        NumberFormatMenuItem(NumberFormat numberFormat) {
            super(numberFormat.format(FORMAT_VALUE));
            this.numberFormat = numberFormat;
        }
    }

    public JTextArea getoCRResultTextArea() {
        return this.oCRResultTextArea;
    }

    private class FieldActionListener implements ActionListener {
        private final Field field;
        private final ReflectionFormPanel reflectionFormPanel;
        private final Map<Class<? extends JComponent>, ValueSetter<?,?>> valueSetterMapping;

        FieldActionListener(ReflectionFormPanel reflectionFormPanel, Field field, Map<Class<? extends JComponent>, ValueSetter<?,?>> valueSetterMapping) {
            this.field = field;
            this.reflectionFormPanel = reflectionFormPanel;
            this.valueSetterMapping = valueSetterMapping;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JComponent comp = this.reflectionFormPanel.getComponentByField(this.field);
            String oCRSelection = OCRPanel.this.oCRResultTextArea.getSelectedText();
            ValueSetter valueSetter = this.valueSetterMapping.get(comp.getClass());
            if(valueSetter == null) {
                throw new IllegalArgumentException(String.format("No %s mapped "
                        + "to component %s",
                        ValueSetter.class,
                        comp));
            }
            //cast outside try-catch block in order to cause a
            //ClassCastException in case of concept error which shouldn't be
            //caught
            NumberFormat numberFormat = null;
            NumberFormat percentFormat = null;
            NumberFormat currencyFormat = null;
            for(AbstractButton button : Collections.list(numberFormatPopupButtonGroup.getElements())) {
                NumberFormatMenuItem menuItem = (NumberFormatMenuItem) button;
                if(menuItem.isSelected()) {
                    numberFormat = menuItem.numberFormat;
                    break;
                }
            }
            for(AbstractButton button : Collections.list(percentFormatPopupButtonGroup.getElements())) {
                NumberFormatMenuItem menuItem = (NumberFormatMenuItem) button;
                if(menuItem.isSelected()) {
                    percentFormat = menuItem.numberFormat;
                    break;
                }
            }
            for(AbstractButton button : Collections.list(currencyFormatPopupButtonGroup.getElements())) {
                NumberFormatMenuItem menuItem = (NumberFormatMenuItem) button;
                if(menuItem.isSelected()) {
                    currencyFormat = menuItem.numberFormat;
                    break;
                }
            }
            try {
                valueSetter.setValue(new FormatOCRResult(numberFormat,
                        percentFormat,
                        currencyFormat,
                        oCRSelection),
                        comp);
            }catch(Exception ex) {
                LOGGER.error("An exception during setting the OCR value on "
                        + "component occured", ex);
                messageHandler.handle(new Message(String.format("The "
                        + "following exception occured while setting the "
                        + "selected value on the field: %s",
                        ExceptionUtils.getRootCauseMessage(ex)),
                        JOptionPane.ERROR_MESSAGE));
            }
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

        oCRResultPopup = new javax.swing.JPopupMenu();
        oCRResultPopupPasteIntoMenu = new javax.swing.JMenu();
        numberFormatPopup = new javax.swing.JPopupMenu();
        numberFormatPopupButtonGroup = new javax.swing.ButtonGroup();
        percentFormatPopup = new javax.swing.JPopupMenu();
        currencyFormatPopup = new javax.swing.JPopupMenu();
        percentFormatPopupButtonGroup = new javax.swing.ButtonGroup();
        currencyFormatPopupButtonGroup = new javax.swing.ButtonGroup();
        oCRResultLabel = new javax.swing.JLabel();
        oCRResultTextAreaScrollPane = new javax.swing.JScrollPane();
        oCRResultTextArea = new javax.swing.JTextArea();
        toolbar = new javax.swing.JToolBar();
        numberFormatButton = new javax.swing.JButton();
        percentFormatButton = new javax.swing.JButton();
        currencyFormatButton = new javax.swing.JButton();

        oCRResultPopupPasteIntoMenu.setText("Paste into");
        oCRResultPopup.add(oCRResultPopupPasteIntoMenu);

        oCRResultLabel.setText("OCR result");

        oCRResultTextArea.setColumns(20);
        oCRResultTextArea.setRows(5);
        oCRResultTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                oCRResultTextAreaMouseClicked(evt);
            }
        });
        oCRResultTextAreaScrollPane.setViewportView(oCRResultTextArea);

        toolbar.setRollover(true);
        toolbar.setToolTipText("formats");

        numberFormatButton.setText("Number");
        numberFormatButton.setFocusable(false);
        numberFormatButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        numberFormatButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        numberFormatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numberFormatButtonActionPerformed(evt);
            }
        });
        toolbar.add(numberFormatButton);

        percentFormatButton.setText("Percentage");
        percentFormatButton.setFocusable(false);
        percentFormatButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        percentFormatButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        percentFormatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                percentFormatButtonActionPerformed(evt);
            }
        });
        toolbar.add(percentFormatButton);

        currencyFormatButton.setText("Currency");
        currencyFormatButton.setFocusable(false);
        currencyFormatButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        currencyFormatButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        currencyFormatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currencyFormatButtonActionPerformed(evt);
            }
        });
        toolbar.add(currencyFormatButton);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(oCRResultTextAreaScrollPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(oCRResultLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(oCRResultLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(oCRResultTextAreaScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void oCRResultTextAreaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_oCRResultTextAreaMouseClicked
        if(evt.getButton() == MouseEvent.BUTTON3) {
            //right click
            this.oCRResultPopup.show(this.oCRResultTextArea, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_oCRResultTextAreaMouseClicked

    private void numberFormatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numberFormatButtonActionPerformed
        numberFormatPopup.show(toolbar, 0, 0);
    }//GEN-LAST:event_numberFormatButtonActionPerformed

    private void percentFormatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_percentFormatButtonActionPerformed
        percentFormatPopup.show(toolbar, 0, 0);
    }//GEN-LAST:event_percentFormatButtonActionPerformed

    private void currencyFormatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_currencyFormatButtonActionPerformed
        currencyFormatPopup.show(toolbar, 0, 0);
    }//GEN-LAST:event_currencyFormatButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton currencyFormatButton;
    private javax.swing.JPopupMenu currencyFormatPopup;
    private javax.swing.ButtonGroup currencyFormatPopupButtonGroup;
    private javax.swing.JButton numberFormatButton;
    private javax.swing.JPopupMenu numberFormatPopup;
    private javax.swing.ButtonGroup numberFormatPopupButtonGroup;
    private javax.swing.JLabel oCRResultLabel;
    private javax.swing.JPopupMenu oCRResultPopup;
    private javax.swing.JMenu oCRResultPopupPasteIntoMenu;
    private javax.swing.JTextArea oCRResultTextArea;
    private javax.swing.JScrollPane oCRResultTextAreaScrollPane;
    private javax.swing.JButton percentFormatButton;
    private javax.swing.JPopupMenu percentFormatPopup;
    private javax.swing.ButtonGroup percentFormatPopupButtonGroup;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables
}
