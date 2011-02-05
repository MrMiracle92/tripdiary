package com.google.code.p.tripdiary;

/**
 * Represents an entry in a Trip.
 * 
 * @author Arunabha Ghosh
 */
public class TripEntry {
	/** Internally used id. */
	public long tripEntryId;

	/** The latitude where this entry was made. */
	public double lat;

	/** The longitude where this entry was made. */
	public double lon;

	/** location (on phone), of the photo associated with this entry (if any). */
	public String photoLocation;

	/** location (on phone), of the audio associated with this entry (if any). */
	public String audioLocation;

	/** location (on phone), of the video associated with this entry (if any). */
	public String videoLocation;

	// TODO (Arunabha) add an appropriate ctor and extra fields if necessary.
}
