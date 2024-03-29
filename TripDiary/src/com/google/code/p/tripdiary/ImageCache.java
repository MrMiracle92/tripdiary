/*
 * Copyright (C) 2011 Ankan Mukherjee
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.code.p.tripdiary;

import java.lang.ref.SoftReference;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.ImageView;

/**
 * A singleton image cache to cache bitmaps and update image views. Also
 * provides a method to generate the bitmap in a separate thread and update the
 * image view when done.
 * 
 * @author Ankan Mukherjee
 */
public class ImageCache {

	/**
	 * @return the ImageCache instance
	 */
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
		mBitmapCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>();
		mLoadQueue = new ConcurrentLinkedQueue<ImageView>();
		mImagePathMap = new ConcurrentHashMap<ImageView, PathAndType>();
		mRunner = new QueueRunner();
	};

	/**
	 * This is the bitmap cache. A soft reference allows the GC to clear out
	 * bitmaps if needed
	 */
	private ConcurrentMap<String, SoftReference<Bitmap>> mBitmapCache;

	/**
	 * This is a map of the ImageView and the path to the image. Having it as a
	 * separate map ensures that only the latest (last) image path is updated
	 * thus avoiding repeated image updates (there is no need to update the
	 * previous image if we know that there is already a new one)
	 */
	private ConcurrentMap<ImageView, PathAndType> mImagePathMap;

	/**
	 * The load queue serves as a queue of ImageViews that need updates.
	 */
	private Queue<ImageView> mLoadQueue;

	/**
	 * The runner is the runnable that works on the load queue and the image
	 * path map.
	 */
	private QueueRunner mRunner;

	/** The thread that uses the runner */
	private Thread mThread;

	/**
	 * Returns a bitmap for the path (creating and adding to the cache, it it
	 * was not already there.) This is the non-threaded method.
	 * 
	 * @param pathName
	 *            path to the file for which the bitmap is to be created
	 * @param mediaType
	 *            type of media
	 * @return bitmap for the file pointed to by pathName
	 */
	public Bitmap getBitmap(String pathName, TripEntry.MediaType mediaType,
			Context context) {
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

		// create bitmap
		switch (mediaType) {
		case PHOTO:
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 16;
			options.inTempStorage = new byte[16 * 1024];
			bm = BitmapFactory.decodeFile(pathName, options);
			if (bm == null) {
				bm = BitmapFactory.decodeResource(context.getResources(),
						R.drawable.picture);
			}
			break;
		case VIDEO:
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
				bm = ThumbnailUtils.createVideoThumbnail(pathName,
						MediaStore.Video.Thumbnails.MINI_KIND);
			}
			if (bm == null) {
				bm = BitmapFactory.decodeResource(context.getResources(),
						R.drawable.gallery_video);
			}
			break;
		default:
			bm = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.icon);
		}

		// add to cache
		mBitmapCache.put(pathName, new SoftReference<Bitmap>(bm));

		return bm;
	}

	/**
	 * This method sets the image bitmap if already cached. If not, it creates
	 * and queues a new task (queue item) to do so, and if needed starts the
	 * thread. This method should be called from the UI thread.
	 * 
	 * @param pathName
	 *            path to the file for which the bitmap is to be created
	 * @param mediaType
	 *            type of media
	 * @param ivImage
	 *            ImageView that needs to be updated
	 */
	public void setBitmapThreaded(final String pathName,
			final TripEntry.MediaType mediaType, final ImageView ivImage) {
		if (mBitmapCache.containsKey(pathName)) {
			Bitmap bm = mBitmapCache.get(pathName).get();
			// let's check to confirm it was not cleared
			if (bm != null) {
				ivImage.setImageBitmap(bm);
				return;
			}
			// it was cleared, let's remove the key and proceed
			mBitmapCache.remove(pathName);
			ivImage.setImageResource(R.drawable.loading);
		}

		// add to load queue
		PathAndType pathItem = new PathAndType();
		pathItem.path = pathName;
		pathItem.mediaType = mediaType;

		mImagePathMap.put(ivImage, pathItem);
		mLoadQueue.add(ivImage);

		// start thread if needed
		if (mThread == null || !mThread.isAlive()) {
			mThread = new Thread(mRunner);
			mThread.start();
		}
	}

	/** Queue item that contains the image path and the media type */
	private class PathAndType {
		public String path;
		public TripEntry.MediaType mediaType;
	}

	/**
	 * This is the a Runnable implementation that works on the load queue and
	 * image path map.
	 * <OL>
	 * <LI>Iterates through the queue</LI>
	 * <LI>If the image view needs to be updated (if there is an entry in the
	 * image path map)
	 * <UL>
	 * <LI>Posts a load to the to the image view</LI>
	 * <LI>Removes the image view entry from the image path map</LI>
	 * </UL>
	 * </OL>
	 */
	private class QueueRunner implements Runnable {
		@Override
		public void run() {
			while (!mLoadQueue.isEmpty()) {
				final ImageView ivImage = mLoadQueue.poll();
				// proceed only if the image view really needs an update (i.e.,
				// there is an entry in imagePathMap)
				PathAndType pathItem = mImagePathMap.remove(ivImage);
				if (pathItem != null) {
					final Bitmap bm = getBitmap(pathItem.path,
							pathItem.mediaType, ivImage.getContext());
					ivImage.post(new Runnable() {
						@Override
						public void run() {
							ivImage.setImageBitmap(bm);
						}
					});
				}
			}
		}
	}
}