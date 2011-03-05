package com.google.code.p.tripdiary;

/**
 * Definitions for Application Data SharedPreferences.
 * 
 * @author Ankan Mukherjee
 */
public final class AppDataDefs {
	
	// Prevent instantiation.
	private AppDataDefs() {}
	
	/** The application data preferences file name */
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
	
//	//TODO:Just for debugging/test purposes.. to be removed later [[
//	public final static String USE_FAKE_TRIP_STORAGE = "com.google.code.p.tripdiary.testing.use.TripStorageManagerFake";
//	//TODO:Just for debugging/test purposes.. to be removed later ]]
}
