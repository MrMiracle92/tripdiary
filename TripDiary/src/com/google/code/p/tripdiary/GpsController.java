package com.google.code.p.tripdiary;

import android.content.Context;
import android.content.Intent;

/**
 * Class used to control and communicate with the background GPS task.
 * 
 * @author Arunabha Ghosh
 */
public class GpsController {

	/**
	 * Start logging GPS coordinates for the given tripId
	 * 
	 * @param tripId
	 */
	public static void startGpsLogging(Context context, long tripId) {
		tripDiaryLogger.logError("GpsController - StartGpsLogging");
		
		Intent intent = new Intent(context, BackgroundGpsService.class);
		intent.putExtra(BackgroundGpsService.INTENT_TRIP_ID_KEY, tripId);

		// Start the background service.
		context.startService(intent);
	}

	public static void stopGpsLogging(Context context) {
		tripDiaryLogger.logError("GpsController - StopGpsLogging");
		
		Intent intent = new Intent(context, BackgroundGpsService.class);
		context.stopService(intent);
	}
}
