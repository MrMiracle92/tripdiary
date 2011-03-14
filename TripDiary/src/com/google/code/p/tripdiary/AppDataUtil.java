/*
 * Copyright (C) 2011 Ankan Mukherjee
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
