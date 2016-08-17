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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
public abstract class CachedOCREngine implements OCREngine {
    private final Map<BufferedImage, String> cache = new HashMap<>();
    private final Map<BufferedImage, Lock> lockMap = new HashMap<>();

    @Override
    public String recognizeImage(BufferedImage image) {
        String retValue = cache.get(image);
        if(retValue == null) {
            Lock imageLock = lockMap.get(image);
            if(imageLock == null) {
                imageLock = new ReentrantLock();
                lockMap.put(image, imageLock);
            }
            synchronized(this) {
                if(imageLock.tryLock()) {
                    imageLock.lock();
                    try {
                        retValue = recognizeImage0(image);
                        cache.put(image, retValue);
                    }finally {
                        imageLock.unlock();
                    }
                }else {
                    try {
                        imageLock.lock();
                        return recognizeImage(image);
                    }finally {
                        imageLock.unlock();
                    }
                }
            }
        }
        return retValue;
    }

    protected abstract String recognizeImage0(BufferedImage image);
}
