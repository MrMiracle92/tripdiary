package com.google.code.p.tripdiary;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

/**
 * A singleton image cache to cache bitmaps shown on screen.
 * 
 * @author Ankan Mukherjee
 */
public class ImageCache {

	public static ImageCache getInstance() {
		synchronized (ImageCache.class) {
			if (sInstance == null) {
				sInstance = new ImageCache();
			}
		}
		return sInstance;
	}

	// the instance
	private static ImageCache sInstance;

	// no access to constructor
	private ImageCache() {
		// create map for cache
		mBitmapCache = new HashMap<String, SoftReference<Bitmap>>();
	};

	// let's use a soft reference so gc can clear the bitmaps if needed
	private Map<String, SoftReference<Bitmap>> mBitmapCache;

	public Bitmap getBitmap(String pathName, TripEntry.MediaType mediaType) {
		Bitmap bm = null;

		if (mBitmapCache.containsKey(pathName)) {
			bm = mBitmapCache.get(pathName).get();
			// let's check to confirm it was not cleared
			if (bm != null) {
				return bm;
			}
			// it was cleared, let's remove the key and proceed to create it
			// again
			mBitmapCache.remove(pathName);
		}

		// create
		switch (mediaType) {
		case PHOTO:
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 16;
			options.inTempStorage = new byte[16 * 1024];
			bm = BitmapFactory.decodeFile(pathName, options);
			break;
		case VIDEO:
			bm = ThumbnailUtils.createVideoThumbnail(pathName,
					MediaStore.Video.Thumbnails.MINI_KIND);
		}

		// add to cache
		mBitmapCache.put(pathName, new SoftReference<Bitmap>(bm));

		return bm;
	}
}