package com.google.code.p.tripdiary;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

/**
 * Class used to control and communicate with the background Location task.
 * 
 * @author Arunabha Ghosh
 * @author Ankan Mukherjee
 */
public class LocationController {

	/**
	 * Start logging Location coordinates
	 * 
	 * @param tripId
	 */
	public static void startLocationLogging(Context context) {
		TripDiaryLogger.logDebug("LocationController - StartLocationLogging");
		Intent intent = new Intent(context, BackgroundLocationService.class);
		context.startService(intent);
	}

	public static void stopLocationLogging(Context context) {
		TripDiaryLogger.logDebug("LocationController - StopLocationLogging");
		Intent intent = new Intent(context, BackgroundLocationService.class);
		context.stopService(intent);
	}

	private static Location lastKnownLocation;

	public static synchronized Location getLastKnownLocation() {
		return lastKnownLocation;
	}

	public static synchronized void setLastKnownLocation(Location location) {
		lastKnownLocation = location;
	}
}
