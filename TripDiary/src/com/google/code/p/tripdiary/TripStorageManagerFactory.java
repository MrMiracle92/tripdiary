/**
 * 
 */
package com.google.code.p.tripdiary;

import java.io.File;
import java.io.FilenameFilter;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

import com.google.code.p.tripdiary.DbDefs.TripCols;
import com.google.code.p.tripdiary.DbDefs.TripDetailCols;
import com.google.code.p.tripdiary.TripEntry.MediaType;

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

		TripStorageManagerFake() {

			// get photos
			// File dcimDir = Environment
			// .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
			File dcimDir = Environment.getExternalStorageDirectory();
			File photosDir = new File(dcimDir.getAbsolutePath()
					+ "/DCIM/Camera");
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
		}

		public void updateTrip(long tripId, String name,
				String tripDescription, boolean traceRouteEnabled,
				String thumbnailLocation) throws IllegalArgumentException {
			// TODO Auto-generated method stub

		}

		public TripDetail getTripDetail(long tripId)
				throws IllegalArgumentException {
			TripDetail td = new TripDetail();
			td.setName("Fake Trip " + tripId);
			td.setTripDescription("Fake Description");
			Time t = new Time();
			t.setToNow();
			td.setCreateTime(t.toMillis(false));
			return td;
		}

		public Cursor getEntriesForTrip(long tripId)
				throws IllegalArgumentException {
			String[] columnNamesEntry = new String[] { TripDetailCols._ID,
					TripDetailCols.TRIP_ID, TripDetailCols.CREATE_TIME,
					TripDetailCols.LAT, TripDetailCols.LON,
					TripDetailCols.MEDIA_TYPE, TripDetailCols.MEDIA_LOCATION };

			MatrixCursor entryCursor = new MatrixCursor(columnNamesEntry);
			
			if(tripId == 9999) {
				// new trip, empty cursor
				return entryCursor;
			}
			
			long id2 = 879;

			String entryPhoto = null;
			for (int j = 0; j < 50; j++) {
				if (photos != null && j < photos.length) {
					entryPhoto = photos[j].getAbsolutePath();
				}

				Time t = new Time();
				t.setToNow();

				TripEntry te = new TripEntry(47.465, -122.23, entryPhoto,
						MediaType.PHOTO, t.toMillis(false));

				entryCursor.addRow(new Object[] { id2++, tripId,
						te.creationTime, te.lat, te.lon, te.mediaType.name(),
						te.mediaLocation });
			}

			return entryCursor;
		}

		public Cursor getAllTrips() {

			String[] columnNamesTrip = new String[] { TripCols._ID,
					TripCols.TRIP_NAME, TripCols.TRIP_DESCRIPTION,
					TripCols.CREATE_TIME, TripCols.TRACEROUTE_ENABLED,
					TripCols.THUMBNAIL_LOCATION };

			MatrixCursor allTripsCursor = new MatrixCursor(columnNamesTrip);

			long id = 3253;
			Time t = new Time();
			t.setToNow();
			// create a list of trips
			for (int i = 0; i < 20; i++) {
				String photo = null;
				if (photos != null && i < photos.length) {
					photo = photos[i].getAbsolutePath();
				}
				allTripsCursor.addRow(new Object[] { id++, "Fake Trip " + id,
						"Fake Description", t.toMillis(false),
						Boolean.toString(false), photo });
			}
			// we'll use this as a new trip
			id = 9999;
			allTripsCursor.addRow(new Object[] { id, "Fake Trip " + id,
					"Fake Description", t.toMillis(false),
					Boolean.toString(false), null });

			return allTripsCursor;
		}

		public long createNewTrip(String name, String tripDescription,
				boolean traceRouteEnabled) {
			// TODO Auto-generated method stub
			return 9999;
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