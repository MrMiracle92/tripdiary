/*
 * Copyright (C) 2011 Ankan Mukherjee, Arunabha Ghosh
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
