/*
 * Copyright (C) 2011 Ankan Mukherjee, Arpita Saha
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

/**
 * Definitions for Application Data SharedPreferences.
 * 
 * @author Ankan Mukherjee
 * @author Arpita Saha
 */
public final class AppDataDefs {
	
	// Prevent instantiation.
	private AppDataDefs() {}
	
	/** The application data preferences file name.
	 *  Warning: changing this will mean users will lose their existing app prefs 
	 *  (it will be like starting a new app.) */
	public final static String APPDATA_FILE = "TripDiaryAppData"; 
	
	/** Key to store the current trip id */
	public final static String CURRENT_TRIP_ID_KEY = "com.google.code.p.tripdiary.CurrentTripId";
	
	/** Trip Id value when no trip is current */
	public final static long NO_CURRENT_TRIP = -1;
	
	/* Unknown Lat and Unknown Lon */
	public final static double LAT_UNKNOWN = -9999.9999;
	public final static double LON_UNKNOWN = -9999.9999;
	
	/* Defaults */
	public final static boolean DEFAULT_TRACE_ROUTE_ENABLED = false;
	
	/* Keys for Intents and Activities */
	public final static String KEY_TRIP_ID = "com.google.code.p.tripdiary.TripId";
	public final static String KEY_IS_NEW_TRIP = "com.google.code.p.tripdiary.IsNewTrip";
	public final static String KEY_PATH_TO_TRIP_IMAGE = "com.google.code.p.tripdiary.PathToTripImage";
	public final static String KEY_SETTINGS_TRIPDETAIL = "com.google.code.p.tripdiary.TripSettingsActivity.TripDetail";
	public final static String KEY_HAVE_ASKED_FOR_GPS = "com.google.code.p.tripdiary.HaveAskedForGPS";
	
	/* Keys for preferences */
	public static final String PREF_KEY_FOLDER = "folderPref";
	public static final String PREF_KEY_VIDEOLEN = "videoLengthPref";
	public static final String PREF_KEY_VIDEOQUALITY = "videoQualityPref";
	public static final String PREF_KEY_TRACKMINDIST = "trackMinDistancePref";
	
//	//TODO:Just for debugging/test purposes.. to be removed later [[
//	public final static String USE_FAKE_TRIP_STORAGE = "com.google.code.p.tripdiary.testing.use.TripStorageManagerFake";
//	//TODO:Just for debugging/test purposes.. to be removed later ]]
}
