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

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import richtercloud.document.scanner.ifaces.ImageWrapper;
import richtercloud.document.scanner.ifaces.OCREngineConf;
import richtercloud.document.scanner.ifaces.OCREngineRecognitionException;

/**
 * An {@link OCREngine} which checks a non-persistent cache to fetch OCR
 * results. Note that cache keys are {@link BufferedImage}s which might be
 * non-identical when they're equal (see
 * {@link BufferedImage#equals(java.lang.Object) } for details).
 *
 * If two calls from different threads to
 * {@link #recognizeImage(java.awt.image.BufferedImage) } with the same argument
 * are made, one request is delayed until the first is processed and for the
 * second the result of the first request will be reused immediately.
 *
 * @author richter
 */
/*
internal implementation notes:
- implements both parallelization and caching since they're hard to separate
*/
public abstract class CachedOCREngine<C extends OCREngineConf> extends ParallelOCREngine<C> {
    private final static Logger LOGGER = LoggerFactory.getLogger(CachedOCREngine.class);
    private final transient Map<BufferedImage, String> cache = new HashMap<>();
    private final transient Map<ImageWrapper, String> imageWrapperCache = new HashMap<>();
    private final transient Map<BufferedImage, Lock> lockMap = new HashMap<>();
    private final transient Map<ImageWrapper, Lock> imageWrapperLockMap = new HashMap<>();

    public CachedOCREngine(C oCREngineConf) {
        super(oCREngineConf);
    }

    @Override
    protected String recognizeImage(BufferedImage image) throws OCREngineRecognitionException {
        String retValue = cache.get(image);
        if(retValue == null) {
            Lock imageLock = lockMap.get(image);
            if(imageLock == null) {
                imageLock = new ReentrantLock();
                lockMap.put(image,
                        imageLock);
            }
            imageLock.lock();
            try {
                retValue = recognizeImage0(image);
                cache.put(image, retValue);
            }finally {
                imageLock.unlock();
            }
        }
        return retValue;
    }

    @Override
    protected String recognizeImageStream(ImageWrapper image, InputStream inputStream) throws OCREngineRecognitionException {
        //The lock for the image need to be acquired before the
        //imageWrapperCache is checked, otherwise recognizeImageStream0 is
        //invoked for every parallel execution
        Lock imageLock;
        synchronized(this) {
            imageLock = imageWrapperLockMap.get(image);
            if(imageLock == null) {
                imageLock = new ReentrantLock();
                imageWrapperLockMap.put(image,
                        imageLock);
                LOGGER.trace(String.format("created lock for image %s", image));
            }
        }
        LOGGER.trace(String.format("acquiring lock for image %s", image));
        imageLock.lock();
        String retValue;
        try {
            retValue = imageWrapperCache.get(image);
            if(retValue == null) {
                LOGGER.trace(String.format("starting OCR for image %s", image));
                retValue = recognizeImageStream0(inputStream);
                if(retValue != null) {
                    //null if process has been canceled
                    imageWrapperCache.put(image,
                                retValue);
                }
            }else {
                LOGGER.trace(String.format("using cached OCR result for image %s", image));
            }
        }finally{
            LOGGER.trace(String.format("unlocking image lock for image %s", image));
            imageLock.unlock();
        }
        return retValue;
    }

    protected abstract String recognizeImage0(BufferedImage image) throws OCREngineRecognitionException;

    protected abstract String recognizeImageStream0(InputStream inputStream) throws OCREngineRecognitionException;
}
