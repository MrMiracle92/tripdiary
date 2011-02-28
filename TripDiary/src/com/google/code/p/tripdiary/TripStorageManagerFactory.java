/**
 * 
 */
package com.google.code.p.tripdiary;

import java.io.File;
import java.io.FilenameFilter;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Environment;
import android.text.format.Time;

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

	public static TripStorageManager getTripStorageManager(Context appContext) {
		synchronized (TripStorageManagerFactory.class) {
			if (storageManagerInstance == null) {
				// TODO:need to use actual TripStorageManager impl when ready
				storageManagerInstance = new TripStorageManagerImpl(appContext);
//				storageManagerInstance = new TripStorageManagerFake();
			}
		}
		return storageManagerInstance;
	}

	/*
	 * TODO:Fake classes.. to be removed later
	 */
	private static class TripStorageManagerFake implements TripStorageManager {
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
						TripDiaryLogger.logDebug(filename);
						if (filename.endsWith("jpg") || filename.endsWith("3gp")) {
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
			td.setDefaultThumbnail( (photos != null && photos.length > 0) ? photos[0].getAbsolutePath() : null );
			return td;
		}
		
		@Override
		public TripEntry getTripEntry(long tripEntryId)
				throws IllegalArgumentException {
			String media = null;
			TripEntry.MediaType mediaType = TripEntry.MediaType.NONE;
			if(photos != null && photos.length > 0) {
				media = photos[(int) (tripEntryId-TRIP_ENTRY_ID_START)].getAbsolutePath();
				if(media.endsWith("3gp")) {
					mediaType = TripEntry.MediaType.VIDEO;
				} else if (media.endsWith("jpg")) {
					mediaType = TripEntry.MediaType.PHOTO;
				}
			}
			Time t = new Time();
			t.setToNow();

			TripEntry te = new TripEntry(47.465, -122.23, media,
					mediaType, t.toMillis(false));

			return te;
		}

		public Cursor getEntriesForTrip(long tripId)
				throws IllegalArgumentException {
			String[] columnNamesEntry = new String[] { TripDetailCols._ID,
					TripDetailCols.TRIP_ID, TripDetailCols.CREATE_TIME,
					TripDetailCols.LAT, TripDetailCols.LON,
					TripDetailCols.MEDIA_TYPE, TripDetailCols.MEDIA_LOCATION,
					TripDetailCols.NOTE};

			MatrixCursor entryCursor = new MatrixCursor(columnNamesEntry);
			
			if(tripId == 9999) {
				// new trip, empty cursor
				return entryCursor;
			}
			
			long id2 = TRIP_ENTRY_ID_START;

			String media = null;
			for (int j = 0; j < 100; j++) {
				if (photos != null && j < photos.length) {
					media = photos[j].getAbsolutePath();
				}

				Time t = new Time();
				t.setToNow();
				
				MediaType mediaType = MediaType.PHOTO;
				if(media.endsWith("3gp")) {
					mediaType = TripEntry.MediaType.VIDEO;
				} else if (media.endsWith("jpg")) {
					mediaType = TripEntry.MediaType.PHOTO;
				}

				TripEntry te = new TripEntry(47.465, -122.23, media,
						mediaType, t.toMillis(false));

				entryCursor.addRow(new Object[] { id2++, tripId,
						te.creationTime, te.lat, te.lon, te.mediaType.name(),
						te.mediaLocation, te.noteText });
			}

			return entryCursor;
		}
		
		private final static long TRIP_ID_START = 3253;
		private final static long TRIP_ENTRY_ID_START = 7000;

		public Cursor getAllTrips() {

			String[] columnNamesTrip = new String[] { TripCols._ID,
					TripCols.TRIP_NAME, TripCols.TRIP_DESCRIPTION,
					TripCols.CREATE_TIME, TripCols.TRACEROUTE_ENABLED,
					TripCols.THUMBNAIL_LOCATION };

			MatrixCursor allTripsCursor = new MatrixCursor(columnNamesTrip);

			long id = TRIP_ID_START;
			Time t = new Time();
			t.setToNow();
			// create a list of trips
			for (int i = 0; i < 20; i++) {
				String photo = null;
				if (photos != null && i < photos.length) {
					photo = photos[i].getAbsolutePath();
				}
				allTripsCursor.addRow(new Object[] { id++, "Fake Trip " + id,
						"Fake Description - " + photo + ". Let's see how a long description shows up. More words here and even more and more and more.", t.toMillis(false),
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

		public long addTripEntry(long tripId, TripEntry tripEntry)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return -1;
		}

		public long getLastUpdatedTime(long tripId) {
			Time t = new Time();
			t.setToNow();
			return t.toMillis(false);
		}

		@Override
		public void deleteTrip(long tripId) {
			// TODO Auto-generated method stub
		}

		@Override
		public void deleteTripEntry(long tripEntry) {
			// TODO Auto-generated method stub
		}
	}
}
