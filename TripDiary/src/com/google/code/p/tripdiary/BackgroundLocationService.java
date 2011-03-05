package com.google.code.p.tripdiary;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.code.p.tripdiary.TripEntry.MediaType;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Implements the background service which logs the location at regular
 * intervals.
 * 
 * @author Arunabha Ghosh
 * @author Arpita Saha
 * @author Ankan Mukherjee
 */
public class BackgroundLocationService extends Service implements
		LocationListener {

	private LocationManager mLocationManager;
	private TripStorageManager mStorageManager;
	private Location mLastUpdatedLocation;
	private Location mLastKnownLocation;
	private boolean mIsBound;
	private long mLastBindTime;

	private final float minUpdateDistanceMetres = 100.0f;
	private final long minUpdateIntervalMillis = 120000l;

	private Queue<QueueItem> mEntryQueue = null;

	// Note: After much tinkering around, it turns out that we need to create
	// this member for the preferences to get called. For details see
	// http://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently/3104265#3104265
	SharedPreferences.OnSharedPreferenceChangeListener mPrefListener;

	private final IBinder locationBinder = new LocationBinder();

	/**
	 * Class for clients to access location.
	 */
	public class LocationBinder extends Binder {
		BackgroundLocationService getService() {
			return BackgroundLocationService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		mIsBound = true;
		requestLocationUpdates();
		mLastBindTime = System.currentTimeMillis();
		return locationBinder;
	}

	public boolean onUnbind(Intent intent) {
		mIsBound = false;
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		TripDiaryLogger.logDebug("BackgroundLocationService - onCreate");

		// get the location manager, storage manager
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mStorageManager = TripStorageManagerFactory
				.getTripStorageManager(getBaseContext());
		// create the entry queue
		mEntryQueue = new ConcurrentLinkedQueue<QueueItem>();

		// do this to get at least the first fix
		requestLocationUpdates();

		// subscribe to changes to current trip
		// listen for changes to current trip
		mPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				if (key == AppDataDefs.CURRENT_TRIP_ID_KEY) {
					// reset last updated location
					mLastUpdatedLocation = null;
					checkAndStopSelf();
				}
			}
		};
		getApplicationContext().getSharedPreferences(AppDataDefs.APPDATA_FILE,
				MODE_PRIVATE).registerOnSharedPreferenceChangeListener(
				mPrefListener);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		TripDiaryLogger.logDebug("BackgroundLocationService - onDestroy");

		// if current log warning (this is a problem, perhaps the phone is low
		// on memory)
		if (AppDataUtil.getCurrentTripId(getApplicationContext()) != AppDataDefs.NO_CURRENT_TRIP
				&& mStorageManager.getTripDetail(
						AppDataUtil.getCurrentTripId(getApplicationContext()))
						.isTraceRouteEnabled()) {
			TripDiaryLogger
					.logWarning("Background Location Service getting destroyed "
							+ "even when current trip has save track on! :-(");
		}

		if (!mEntryQueue.isEmpty()) {
			TripDiaryLogger
					.logWarning("Background Location Service getting destroyed "
							+ "even when current trip has some data in queue to be updated! :-(");
		}

		// unsubscribe to shared preferences updates
		getApplicationContext().getSharedPreferences(AppDataDefs.APPDATA_FILE,
				MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(
				mPrefListener);

		// unsubscribe to location changes
		mLocationManager.removeUpdates(this);
	}

	/**
	 * Called when an activity calls startService
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		TripDiaryLogger.logDebug("BackgroundLocationService - onStartCommand");

		/**
		 * We want this service to continue running until it is explicitly
		 * stopped, so return sticky.
		 */
		return Service.START_STICKY;
	}

	/**
	 * If the last known location older than this much time, we'll consider it
	 * unknown
	 */
	private static int TOO_OLD_A_LOCATION_INTERVAL = 1000 * 60 * 60; // 1 hr

	private void requestLocationUpdates() {
		if (mLastKnownLocation == null
				|| System.currentTimeMillis() - mLastKnownLocation.getTime() >= TOO_OLD_A_LOCATION_INTERVAL) {
			// get location ASAP as we don't have a good one
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, this);
			mLocationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, this);
		} else {
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, minUpdateIntervalMillis,
					minUpdateDistanceMetres, this);
			mLocationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, minUpdateIntervalMillis,
					minUpdateDistanceMetres, this);
		}
	}

	/**
	 * We'll not stop the service if the last bind happened less than this much
	 * time ago
	 **/
	private static int TOO_EARLY_TO_STOP_INTERVAL = 1000 * 60 * 2; // 2 mins

	/** Checks and if possible, stops self **/
	private void checkAndStopSelf() {

		// let's not stop if other activies are bound or entry queue has items
		// or the last bind happened just a little while ago
		if (mIsBound
				|| !mEntryQueue.isEmpty()
				|| System.currentTimeMillis() - mLastBindTime <= TOO_EARLY_TO_STOP_INTERVAL) {
			return;
		}

		// if the current trip does not have trace route enabled, let's stop
		if (AppDataUtil.getCurrentTripId(getApplicationContext()) == AppDataDefs.NO_CURRENT_TRIP
				|| !mStorageManager.getTripDetail(
						AppDataUtil.getCurrentTripId(getApplicationContext()))
						.isTraceRouteEnabled()) {
			TripDiaryLogger
					.logDebug("BackgroundLocationService - Stopping Self.");
			stopSelf();
		}
	}

	/**
	 * We'll not update the entry location the request to update was earlier
	 * than this much time ago
	 */
	private static int TOO_LATE_TO_UPDATE_INTERVAL = 1000 * 60 * 5; // 5 mins

	@Override
	public void onLocationChanged(Location location) {
		TripDiaryLogger.logDebug(String.format("Location changed lat: "
				+ location.getLatitude() + " lon: " + location.getLongitude()));

		// use it if it's better than the last known
		if (isBetterLocation(location, mLastKnownLocation)) {
			mLastKnownLocation = location;
			LocationController.setLastKnownLocation(location);
		}

		if (!mEntryQueue.isEmpty()) { // if there are entries to update
			QueueItem item = null;
			while ((item = mEntryQueue.poll()) != null) {
				if (!item.hasLastKnownLocation
						|| !((location.getTime() - item.requestedAt) >= TOO_LATE_TO_UPDATE_INTERVAL)) {
					if (item.tripEntry.tripEntryId > 0) {
						TripDiaryLogger
								.logDebug("Deleting an entry because we can update with its new location : "
										+ item.tripEntry.tripEntryId);
						mStorageManager
								.deleteTripEntry(item.tripEntry.tripEntryId);
					}
					item.tripEntry.lat = location.getLatitude();
					item.tripEntry.lon = location.getLongitude();
					TripDiaryLogger.logDebug("Updating an already entered "
							+ item.tripEntry.mediaType
							+ " entry with latest location.");

					if (item.tripEntry.mediaType == MediaType.TEXT)
						TripDiaryLogger.logDebug("Entered text is : "
								+ item.tripEntry.noteText);

					item.tripEntry.tripEntryId = mStorageManager.addTripEntry(
							item.tripId, item.tripEntry);
					mLastUpdatedLocation = location;
				} else
					TripDiaryLogger
							.logDebug("Entry "
									+ item.tripEntry.mediaType
									+ "had last known location or it is too late to update :-( ");
			}
		} else { // add an entry for the route only if trace route is enabled
					// for current trip and a min distance has passed since the
					// last updated location
			long currentTripId = AppDataUtil
					.getCurrentTripId(getApplicationContext());
			if (currentTripId != AppDataDefs.NO_CURRENT_TRIP
					&& mStorageManager.getTripDetail(currentTripId) != null
					&& mStorageManager.getTripDetail(currentTripId)
							.isTraceRouteEnabled()
					&& ((mLastUpdatedLocation == null) ? true
							: mLastUpdatedLocation.distanceTo(location) >= minUpdateDistanceMetres)) {
				TripEntry tripEntry = new TripEntry(location.getLatitude(),
						location.getLongitude());
				mStorageManager.addTripEntry(currentTripId, tripEntry);
				mLastUpdatedLocation = location;
				TripDiaryLogger.logDebug("Location entry made for trip: "
						+ currentTripId + " [Lat: " + tripEntry.lat + ", Lon: "
						+ tripEntry.lon + "]");
			}
		}
		checkAndStopSelf();
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	// public Location getLastKnownLocation() {
	// TripDiaryLogger
	// .logDebug("BackgroundLocationService - getLastKnownLocation");
	// return mLastKnownLocation;
	// }

	private class QueueItem {
		long tripId;
		TripEntry tripEntry;
		long requestedAt;
		boolean hasLastKnownLocation = true;

		public QueueItem(long tripId, TripEntry tripEntry,
				boolean hasLastKnownLocation) {
			this.tripId = tripId;
			this.tripEntry = tripEntry;
			this.hasLastKnownLocation = hasLastKnownLocation;
			this.requestedAt = System.currentTimeMillis();
		}
	}

	public void updateEntryWithBestCurrentLocation(long tripId,
			TripEntry tripEntry) {
		boolean hasLastKnownLocation = false;
		if (mLastKnownLocation != null) {
			tripEntry.lat = mLastKnownLocation.getLatitude();
			tripEntry.lon = mLastKnownLocation.getLongitude();
			hasLastKnownLocation = true;
			TripDiaryLogger
					.logDebug("updateEntryWithBestCurrentLocation - lastKnownLocation is true");
		} else {
			hasLastKnownLocation = false;
			TripDiaryLogger
					.logDebug("updateEntryWithBestCurrentLocation - lastKnownLocation is false");
		}
		requestLocationUpdates();
		tripEntry.tripEntryId = mStorageManager.addTripEntry(tripId, tripEntry);

		TripDiaryLogger
				.logDebug("updateEntryWithBestCurrentLocation - adding an entry "
						+ tripEntry.mediaType
						+ " Lat : "
						+ tripEntry.lat
						+ " Lon : " + tripEntry.lon);
		if (tripEntry.mediaType == MediaType.TEXT)
			TripDiaryLogger
					.logDebug("updateEntryWithBestCurrentLocation - added entry's text is : "
							+ tripEntry.noteText);

		// add entry to queue and request location
		mEntryQueue.add(new QueueItem(tripId, tripEntry, hasLastKnownLocation));
	}

	/*
	 * The logic to determine better location below is from -
	 * http://developer.android.
	 * com/guide/topics/location/obtaining-user-location.html
	 */

	private static final int SIGNIFICANTLY_NEWER_INTERVAL = 1000 * 60 * 2;

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	protected boolean isBetterLocation(Location location,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > SIGNIFICANTLY_NEWER_INTERVAL;
		boolean isSignificantlyOlder = timeDelta < -SIGNIFICANTLY_NEWER_INTERVAL;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

}
