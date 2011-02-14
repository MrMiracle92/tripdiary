/**
 * 
 */
package com.google.code.p.tripdiary;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

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
		List<TripDetail> trips = new ArrayList<TripDetail>();
		// current trip
		long currentTrip = 0;

		TripStorageManagerFake() {

			// get photos thumbnails
			File dcimDir = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
			File photosDir = new File(dcimDir.getAbsolutePath()
					+ "/.thumbnails");
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

			// init id with some non-zero random long
			long id = ((long) Math.random()) + 1;
			// create a list of trips
			for (int i = 0; i < 20; i++) {
				TripDetail detail = new TripDetail();
				detail.setName("Fake Trip " + i);
				if (photos != null && photos.length > i) {
					detail.setImageLocation(photos[i].getAbsolutePath());
					Log.d(TAG, detail.getImageLocation());
				}
				detail.setTripDescription("Fake Description ["
						+ detail.getImageLocation() + "]");
				detail.setTripId(id++);
				Time t = new Time();
				t.setToNow();
				detail.setCreateTime(t.toMillis(false));

				trips.add(detail);
			}

			// make the last trip current
			currentTrip = id - 1;
		}

		public void updateTrip(long tripId, String name,
				String tripDescription, boolean traceRouteEnabled)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub

		}

		public TripDetail getTripDetail(long tripId)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		public List<TripEntry> getEntriesForTrip(long tripId)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		// just a dummy method to create trips, pointing to a few camera
		// picture thumbnails (if any)
		public List<TripDetail> getAllTrips() {
			return trips;
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

		public long getCurrentTripId() {
			return currentTrip;
		}

		public void setTripIsCurrent(long tripId, boolean isCurrent) {
			currentTrip = tripId;
		}
	}
}
