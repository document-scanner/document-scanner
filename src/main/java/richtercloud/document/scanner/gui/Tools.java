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

import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.ifaces.DocumentAddException;
import richtercloud.document.scanner.ifaces.ImageWrapper;
import richtercloud.document.scanner.model.imagewrapper.CachingImageWrapper;
import richtercloud.reflection.form.builder.ClassInfo;
import richtercloud.swing.worker.get.wait.dialog.SwingWorkerCompletionWaiter;
import richtercloud.swing.worker.get.wait.dialog.SwingWorkerGetWaitDialog;

/**
 *
 * @author richter
 */
public class Tools {
    private final static Logger LOGGER = LoggerFactory.getLogger(Tools.class);

    public static List<Class<?>> sortEntityClasses(Set<Class<?>> entityClasses) {
        List<Class<?>> entityClassesSort = new LinkedList<>(entityClasses);
        Collections.sort(entityClassesSort, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                String o1Value;
                ClassInfo o1ClassInfo = o1.getAnnotation(ClassInfo.class);
                if(o1ClassInfo != null) {
                    o1Value = o1ClassInfo.name();
                }else {
                    o1Value = o1.getSimpleName();
                }
                String o2Value;
                ClassInfo o2ClassInfo = o2.getAnnotation(ClassInfo.class);
                if(o2ClassInfo != null) {
                    o2Value = o2ClassInfo.name();
                }else {
                    o2Value = o2.getSimpleName();
                }
                return o1Value.compareTo(o2Value);
            }
        });
        return entityClassesSort;
    }

    /**
     * Uses a modal dialog in order to display the progress of the retrieval and
     * make the operation cancelable.
     * @param documentFile
     * @return the retrieved images or {@code null} if the retrieval has been
     * canceled (in dialog)
     * @throws DocumentAddException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    /*
    internal implementation notes:
    - can't use ProgressMonitor without blocking EVT instead of a model dialog
    when using SwingWorker.get
    */
    public static List<ImageWrapper> retrieveImages(final File documentFile,
            Window waitDialogParent,
            File imageWrapperStorageDir) throws DocumentAddException, InterruptedException, ExecutionException {
        if(documentFile == null) {
            throw new IllegalArgumentException("documentFile mustn't be null");
        }
        final SwingWorkerGetWaitDialog dialog = new SwingWorkerGetWaitDialog(SwingUtilities.getWindowAncestor(waitDialogParent), //owner
                DocumentScanner.generateApplicationWindowTitle("Wait",
                        Constants.APP_NAME,
                        Constants.APP_VERSION), //dialogTitle
                "Retrieving image data", //labelText
                null //progressBarText
        );
        final SwingWorker<List<ImageWrapper>, Void> worker = new SwingWorker<List<ImageWrapper>, Void>() {
            @Override
            protected List<ImageWrapper> doInBackground() throws Exception {
                List<ImageWrapper> retValue = new LinkedList<>();
                try {
                    InputStream pdfInputStream = new FileInputStream(documentFile);
                    try (PDDocument document = PDDocument.load(pdfInputStream)) {
                        PDFRenderer pdfRenderer = new PDFRenderer(document);
                        for(int page=0; page<document.getNumberOfPages(); page++) {
                            if(dialog.isCanceled()) {
                                document.close();
                                LOGGER.debug("tab generation aborted");
                                return null;
                            }
                            BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                            ImageWrapper imageWrapper = new CachingImageWrapper(imageWrapperStorageDir, image);
                            retValue.add(imageWrapper);
                        }
                    }
                }catch(IOException ex) {
                    throw new DocumentAddException(ex);
                }
                return retValue;
            }

            @Override
            protected void done() {
            }
        };
        worker.addPropertyChangeListener(
            new SwingWorkerCompletionWaiter(dialog));
        worker.execute();
        //the dialog will be visible until the SwingWorker is done
        dialog.setVisible(true);
        List<ImageWrapper> retValue = worker.get();
        return retValue;
    }

    private Tools() {
    }
}
