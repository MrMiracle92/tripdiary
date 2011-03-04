/**
 * 
 */
package com.google.code.p.tripdiary;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Ankan Mukherjee
 * 
 */
public final class AppDataUtil {

	public static long getCurrentTripId(Context appContext) {
		long currTrip = appContext.getSharedPreferences(
				AppDataDefs.APPDATA_FILE, MODE_PRIVATE).getLong(
				AppDataDefs.CURRENT_TRIP_ID_KEY, AppDataDefs.NO_CURRENT_TRIP);

		return (null == TripStorageManagerFactory.getTripStorageManager(
				appContext).getTripDetail(currTrip) ? AppDataDefs.NO_CURRENT_TRIP
				: currTrip);
	}

	public static void setCurrentTripId(Context appContext, long tripId) {
		SharedPreferences.Editor editPref = appContext.getSharedPreferences(
				AppDataDefs.APPDATA_FILE, MODE_PRIVATE).edit();
		editPref.putLong(AppDataDefs.CURRENT_TRIP_ID_KEY, tripId);
		editPref.commit();
	}

}
