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
		// current trip
		long currentTrip = 0;

		TripStorageManagerFake() {

			// get photos thumbnails
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

			// init id with some non-zero random long
			long id = 3253;
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
				detail.setTripId(id);
				Time t = new Time();
				t.setToNow();
				detail.setCreateTime(t.toMillis(false));

				trips.put(id, detail);
				id++;
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
			return trips.get(tripId);
		}

		public List<TripEntry> getEntriesForTrip(long tripId)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		// just a dummy method to create trips, pointing to a few camera
		// picture thumbnails (if any)
		public List<TripDetail> getAllTrips() {
			List<TripDetail> tripList = new ArrayList<TripDetail>();
			for(long tripId : trips.keySet()) {
				tripList.add(trips.get(tripId));
			}
			return tripList;
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
			currentTrip =  isCurrent ? tripId : 0;
		}

		public long getLastUpdatedTime(long tripId) {
			Time t = new Time();
			t.setToNow();
			return t.toMillis(false);
		}
	}
}
