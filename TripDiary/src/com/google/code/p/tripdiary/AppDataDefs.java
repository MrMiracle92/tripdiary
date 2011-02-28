package com.google.code.p.tripdiary;

/**
 * Defintions for Application Data SharedPreferences.
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
	public final static long LAT_UNKNOWN = -1;
	public final static long LON_UNKNOWN = -1;
	
	/* Defaults */
	public final static boolean DEFAULT_TRACE_ROUTE_ENABLED = false;
	
	/* Keys for Intents and Activities */
	public final static String KEY_TRIP_ID = "com.google.code.p.tripdiary.TripId";
	public final static String KEY_IS_NEW_TRIP = "com.google.code.p.tripdiary.IsNewTrip";
	public final static String KEY_PATH_TO_TRIP_IMAGE = "com.google.code.p.tripdiary.PathToTripImage";
	public final static String KEY_SETTINGS_TRIPDETAIL = "com.google.code.p.tripdiary.TripSettingsActivity.TripDetail";
}
