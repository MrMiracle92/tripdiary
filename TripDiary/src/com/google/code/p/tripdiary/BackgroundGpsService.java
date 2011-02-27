package com.google.code.p.tripdiary;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Implements the background service which logs the gps location at regular
 * intervals.
 * 
 * @author Arunabha Ghosh
 * @author Arpita Saha
 */
public class BackgroundGpsService extends Service implements LocationListener {
	/**
	 * The key for the field containing the trip id in the intent passed to
	 * {@code onStartCommand}.
	 */
	public static final String INTENT_TRIP_ID_KEY = "TRIP_ID";

	private LocationManager locationManager;
	private TripStorageManager storageManager;
	private long currentTripId = -1;

	private final float minUpdateDistanceMetres = 100.0f;
	private final long minUpdateIntervalMillis = 120000l;

	private final IBinder gpsBinder = new GPSBinder();

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
		// TODO Required definition, not used.
		return gpsBinder;
	}

	@Override
	public void onCreate() {
		logInfo("Background GPS created");

		// Get the location manager.
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		storageManager = TripStorageManagerFactory
				.getTripStorageManager(getBaseContext());
	}

	/**
	 * Called when an activity calls startService. The Intent passed contains
	 * details of the current trip.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		logInfo("Background GPS onStartCommand");

		Bundle extras = intent.getExtras();
		if (extras.containsKey(INTENT_TRIP_ID_KEY)) {
			currentTripId = extras.getLong(INTENT_TRIP_ID_KEY);
		}
		// Request location updates.
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				minUpdateIntervalMillis, minUpdateDistanceMetres, this);
		// Ensure Service is restarted if killed.
		return Service.START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		logInfo(String.format("Updated location lat: " + location.getLatitude()
				+ " lon: " + location.getLongitude()));
		TripEntry tripEntry = new TripEntry(location.getLatitude(),
				location.getLongitude());
		storageManager.addTripEntry(currentTripId, tripEntry);
	}

	public Location getLastKnownLocation() {
		return locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
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

	private void logInfo(String msg) {
		Log.i("BackgroundGpsService", msg);
	}
}