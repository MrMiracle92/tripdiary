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
		// list of trips
		Map<Long, TripDetail> trips = new HashMap<Long, TripDetail>();
		
		MatrixCursor mMatrixCursor = null;

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

			String[] columnNames = new String[]{TripCols._ID, TripCols.TRIP_NAME, TripCols.TRIP_DESCRIPTION,
					TripCols.CREATE_TIME, TripCols.TRACEROUTE_ENABLED, TripCols.THUMBNAIL_LOCATION};
			mMatrixCursor = new MatrixCursor(columnNames);
			// init id with some non-zero random long
			long id = 3253;
			// create a list of trips
			for (int i = 0; i < 20; i++) {
				
				Time t = new Time();
				t.setToNow();
				String photo = null;
				if(photos != null && i < photos.length) {
					photo = photos[i].getAbsolutePath();
				}
				TripDetail td = new TripDetail();
				td.setName("Fake Trip " + i);
				td.setTripDescription("Fake Description");
				td.setCreateTime(t.toMillis(false));
				trips.put(id, td);
				mMatrixCursor.addRow(new Object[] { id++, td.getName(),
						td.getTripDescription(), td.getCreateTime(),
						Boolean.toString(td.isTraceRouteEnabled()), photo });
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
			// TODO Auto-generated method stub
			return null;
		}

		public Cursor getAllTrips() {
			return mMatrixCursor;
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
