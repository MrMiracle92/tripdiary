package com.google.code.p.tripdiary;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Implements the background service which logs the gps location at regular
 * intervals.
 * 
 * @author Arunabha Ghosh
 * @author Arpita Saha
 * @author Ankan Mukherjee
 */
public class BackgroundGpsService extends Service implements LocationListener {
	/**
	 * The key for the field containing the trip id in the intent passed to
	 * {@code onStartCommand}.
	 */
	public static final String INTENT_TRIP_ID_KEY = "TRIP_ID";

	private LocationManager mLocationManager;
	private TripStorageManager mStorageManager;
	private Location mLastUpdatedLocation;
	private long currentTripId = -1;

	private final float minUpdateDistanceMetres = 100.0f;
	private final long minUpdateIntervalMillis = 120000l;

	private final IBinder gpsBinder = new GPSBinder();

	private Queue<QueueItem> mEntryQueue = null;

	/**
	 * Class for clients to access location.
	 */
	public class GPSBinder extends Binder {
		BackgroundGpsService getService() {
			return BackgroundGpsService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return gpsBinder;
	}

	@Override
	public void onCreate() {
		TripDiaryLogger.logDebug("BackgroundGpsService - onCreate");

		// Get the location manager.
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mStorageManager = TripStorageManagerFactory
				.getTripStorageManager(getBaseContext());
		mEntryQueue = new ConcurrentLinkedQueue<QueueItem>();
	}

	/**
	 * Called when an activity calls startService. The Intent passed contains
	 * details of the current trip.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		TripDiaryLogger.logDebug("BackgroundGpsService - onStartCommand");

		Bundle extras = intent.getExtras();
		if (extras.containsKey(INTENT_TRIP_ID_KEY)) {
			currentTripId = extras.getLong(INTENT_TRIP_ID_KEY);
		}
		// Request location updates.
		requestLocationUpdatesStd();

		/**
		 * We want this service to continue running until it is explicitly
		 * stopped, so return sticky.
		 */
		return Service.START_STICKY;
	}

	private void requestLocationUpdatesStd() {
		mLocationManager.removeUpdates(this);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				minUpdateIntervalMillis, minUpdateDistanceMetres, this);
	}

	private void requestLocationUpdateASAP() {
		mLocationManager.removeUpdates(this);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				0, 0, this);
	}

	@Override
	public void onDestroy() {
		TripDiaryLogger.logDebug("BackgroundGpsService - onDestroy");
		mLocationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		TripDiaryLogger.logDebug(String.format("Location changed lat: "
				+ location.getLatitude() + " lon: " + location.getLongitude()));

		if (!mEntryQueue.isEmpty()) { // if there are entries to update
			QueueItem item = null;
			while ((item = mEntryQueue.poll()) != null) {
				if (item.tripEntry.tripEntryId > 0) {
					mStorageManager.deleteTripEntry(item.tripEntry.tripEntryId);
				}
				item.tripEntry.lat = location.getLatitude();
				item.tripEntry.lon = location.getLongitude();
				item.tripEntry.tripEntryId = mStorageManager.addTripEntry(
						item.tripId, item.tripEntry);
				mLastUpdatedLocation = location;
			}
			requestLocationUpdatesStd();
		} else { // add an entry for the route only if trace route is enabled
					// for current trip and a min distance has passed since the
					// last updated location
			if (mStorageManager.getTripDetail(getCurrentTripId())
					.isTraceRouteEnabled()
					&& mLastUpdatedLocation.distanceTo(location) >= minUpdateDistanceMetres) {
				TripEntry tripEntry = new TripEntry(location.getLatitude(),
						location.getLongitude());
				mStorageManager.addTripEntry(currentTripId, tripEntry);
			}
		}
	}

	public Location getLastKnownLocation() {
		TripDiaryLogger.logDebug("BackgroundGpsService - getLastKnownLocation");

		return mLocationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}

	private class QueueItem {
		long tripId;
		TripEntry tripEntry;

		public QueueItem(long tripId, TripEntry tripEntry) {
			this.tripId = tripId;
			this.tripEntry = tripEntry;
		}
	}

	public void updateEntryWithBestCurrentLocation(long tripId,
			TripEntry tripEntry) {
		// add entry to queue and request location asap
		mEntryQueue.add(new QueueItem(tripId, tripEntry));
		requestLocationUpdateASAP();
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

	private long getCurrentTripId() {
		return getApplicationContext().getSharedPreferences(
				AppDataDefs.APPDATA_FILE, MODE_PRIVATE).getLong(
				AppDataDefs.CURRENT_TRIP_ID_KEY, AppDataDefs.NO_CURRENT_TRIP);
	}
}
