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
	public final static String CURRENT_TRIP_ID_KEY = "CurrentTripId";
	
	/** Trip Id value when no trip is current */
	public final static long NO_CURRENT_TRIP = 0;

}
