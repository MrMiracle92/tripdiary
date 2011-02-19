/**
 * 
 */
package com.google.code.p.tripdiary;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.code.p.tripdiary.DbDefs.TripCols;
import com.google.code.p.tripdiary.DbDefs.TripDetailCols;
import com.google.code.p.tripdiary.TripEntry.MediaType;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

/**
 * This is the Factory class for TripStorageManager.
 * 
 * @author Ankan Mukherjee
 * 
 */
public class TripStorageManagerFactory {

	private static TripStorageManager storageManagerInstance;

	public static TripStorageManager getTripStorageManager() {
		synchronized (TripStorageManagerFactory.class) {
			if (storageManagerInstance == null) {
				// TODO:need to use actual TripStorageManager impl when ready
				storageManagerInstance = new TripStorageManagerFake();
			}
		}
		return storageManagerInstance;
	}

	/*
	 * TODO:Fake classes.. to be removed later
	 */
	private static class TripStorageManagerFake implements TripStorageManager {
		private final String TAG = "TripStorageManagerFake";

		// list of photos
		private File[] photos = null;
		// map of trip id to trips
		Map<Long, TripDetail> trips = new HashMap<Long, TripDetail>();
		
		MatrixCursor mTripCursor = null;
		Map<Long, MatrixCursor> mEntryCursors = new HashMap<Long, MatrixCursor>();

		TripStorageManagerFake() {

			// get photos
//			File dcimDir = Environment
//					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
			File dcimDir = Environment.getExternalStorageDirectory();
			File photosDir = new File(dcimDir.getAbsolutePath()
					+ "/DCIM/.thumbnails");
			if (photosDir.exists() && photosDir.isDirectory()) {
				photos = photosDir.listFiles(new FilenameFilter() {

					public boolean accept(File dir, String filename) {
						Log.d(TAG, filename);
						if (filename.endsWith("jpg")) {
							return true;
						}
						return false;
					}
				});
			}

			String[] columnNamesTrip = new String[]{TripCols._ID, TripCols.TRIP_NAME, TripCols.TRIP_DESCRIPTION,
					TripCols.CREATE_TIME, TripCols.TRACEROUTE_ENABLED, TripCols.THUMBNAIL_LOCATION};
			mTripCursor = new MatrixCursor(columnNamesTrip);
			
			String[] columnNamesEntry = new String[]{TripDetailCols._ID, TripDetailCols.TRIP_ID, TripDetailCols.CREATE_TIME,
					TripDetailCols.LAT, TripDetailCols.LON, TripDetailCols.MEDIA_TYPE, TripDetailCols.MEDIA_LOCATION};
			
			// init id with some non-zero random long
			long id = 3253;
			long id2 = 879;
			// create a list of trips
			for (int i = 0; i < 20; i++) {
				Time t = new Time();
				t.setToNow();
				String photo = null;
				if (photos != null && i < photos.length) {
					photo = photos[i].getAbsolutePath();
				}
				TripDetail td = new TripDetail();
				td.setName("Fake Trip " + i);
				td.setTripDescription("Fake Description");
				td.setCreateTime(t.toMillis(false));
				trips.put(id, td);
				mTripCursor.addRow(new Object[] { id, td.getName(),
						td.getTripDescription(), td.getCreateTime(),
						Boolean.toString(td.isTraceRouteEnabled()), photo });
				String entryPhoto = null;
				MatrixCursor mEntryCursor = new MatrixCursor(columnNamesEntry);
				for (int j = 0; j < 50; j++) {
					if (photos != null && j < photos.length) {
						entryPhoto = photos[j].getAbsolutePath();
					}

					TripEntry te = new TripEntry(47.465, -122.23, entryPhoto,
							MediaType.PHOTO, td.getCreateTime());

					mEntryCursor.addRow(new Object[] { id2++, td.getTripId(),
							te.creationTime, te.lat, te.lon,
							te.mediaType.name(), te.mediaLocation });
				}
				mEntryCursors.put(id, mEntryCursor);
				id++;
			}
		}

		public void updateTrip(long tripId, String name,
				String tripDescription, boolean traceRouteEnabled,
				String thumbnailLocation) throws IllegalArgumentException {
			// TODO Auto-generated method stub
			
		}
		public TripDetail getTripDetail(long tripId)
				throws IllegalArgumentException {
			return trips.get(tripId);
		}

		public Cursor getEntriesForTrip(long tripId)
				throws IllegalArgumentException {
			return mEntryCursors.get(tripId);
		}

		public Cursor getAllTrips() {
			return mTripCursor;
		}

		public long createNewTrip(String name, String tripDescription,
				boolean traceRouteEnabled) {
			// TODO Auto-generated method stub
			return 0;
		}

		public boolean addTripEntry(long tripId, TripEntry tripEntry)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return false;
		}

		public long getLastUpdatedTime(long tripId) {
			Time t = new Time();
			t.setToNow();
			return t.toMillis(false);
		}
	}
}
