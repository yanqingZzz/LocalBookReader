package com.cxample.bookread.thunbnail;

import android.graphics.Bitmap;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yanqing on 2018/4/12.
 */

public class ThumbnailCache {
    private static ConcurrentHashMap<String, SoftReference<Bitmap>> sThumbnailBitmapCache;

    static {
        sThumbnailBitmapCache = new ConcurrentHashMap<>();
    }

    public static void saveBitmapCache(String path, Bitmap bitmap) {
        if(bitmap != null) {
            sThumbnailBitmapCache.put(path, new SoftReference<>(bitmap));
        }
    }

    public static Bitmap getBitmapCache(String path) {
        SoftReference<Bitmap> softReference = sThumbnailBitmapCache.get(path);
        if(softReference != null) {
            return softReference.get();
        }
        return null;
    }
}
