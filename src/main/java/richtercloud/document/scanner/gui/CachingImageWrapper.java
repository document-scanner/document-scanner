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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javafx.scene.image.WritableImage;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;

/**
 * Improvement of {@link DefaultImageWrapper} which caches image previews
 * because of the high need for resources for transformation and always loads
 * original image from disk in order to save memory/freeing of image data after
 * they've been used in memeory.
 *
 * Currently this class manages its cache in static constants which is not too
 * elegant.
 *
 * @author richter
 */
public class CachingImageWrapper extends DefaultImageWrapper {
    private final static CacheManager MANAGER =
                 Caching.getCachingProvider().getCacheManager();
    private final static Cache<Long, Map<Integer, BufferedImage>> CACHE;
    private final static Cache<Long, Map<Integer, WritableImage>> JAVAFX_CACHE;
    static {
        MutableConfiguration<Long, Map<Integer, BufferedImage>> config = new MutableConfiguration<>();
        MutableConfiguration<Long, Map<Integer, WritableImage>> javaFXConfig = new MutableConfiguration<>();
        for(MutableConfiguration config0 : new MutableConfiguration[] {config, javaFXConfig}) {
            config0.setStoreByValue(false)
                    .setStatisticsEnabled(true)
                    .setExpiryPolicyFactory(FactoryBuilder.factoryOf(
                            new AccessedExpiryPolicy(new Duration(TimeUnit.HOURS, 1))));
        }
        CACHE = MANAGER.createCache("cache",
                config);
        JAVAFX_CACHE = MANAGER.createCache("javafx-cache",
                javaFXConfig);
    }
    private static Long cacheIdCounter = 0L;
    private final long cacheId;

    public CachingImageWrapper(File storageDir, BufferedImage image) throws IOException {
        super(storageDir, image);
        synchronized(this) {
            this.cacheId = cacheIdCounter;
            cacheIdCounter += 1;
        }
    }

    @Override
    public BufferedImage getImagePreview(int width) throws IOException {
        Map<Integer, BufferedImage> wrapperCacheEntry = CACHE.get(cacheId);
        if(wrapperCacheEntry == null) {
            wrapperCacheEntry = new HashMap<>();
            CACHE.put(cacheId, wrapperCacheEntry);
        }
        BufferedImage imagePreview = wrapperCacheEntry.get(width);
        if(imagePreview == null) {
            imagePreview = super.getImagePreview(width);
            wrapperCacheEntry.put(width, imagePreview);
        }
        return imagePreview;
    }

    @Override
    public WritableImage getImagePreviewFX(int width) throws IOException {
        Map<Integer, WritableImage> wrapperCacheEntry = JAVAFX_CACHE.get(cacheId);
        if(wrapperCacheEntry == null) {
            wrapperCacheEntry = new HashMap<>();
            JAVAFX_CACHE.put(cacheId, wrapperCacheEntry);
        }
        WritableImage imagePreview = wrapperCacheEntry.get(width);
        if(imagePreview == null) {
            imagePreview = super.getImagePreviewFX(width);
            wrapperCacheEntry.put(width, imagePreview);
        }
        return imagePreview;
    }
}
