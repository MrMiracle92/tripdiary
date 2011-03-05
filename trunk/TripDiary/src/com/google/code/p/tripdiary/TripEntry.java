package com.google.code.p.tripdiary;

/**
 * Represents an entry in a Trip.
 * 
 * @author Arunabha Ghosh
 * @author Arpita Saha
 */
public class TripEntry {
	public enum MediaType {
		NONE, PHOTO, AUDIO, VIDEO, TEXT
	}

	/** Internally used id. */
	public long tripEntryId;

	/** The latitude where this entry was made. */
	public double lat;

	/** The longitude where this entry was made. */
	public double lon;

	/** The location of the media associated with this entry. */
	public String mediaLocation;

	/** The type of media stored at the mediaLocation. */
	public MediaType mediaType;

	/** The time this entry was created. */
	public long creationTime;

	/** Notes (if any). */
	public String noteText;

	/**
	 * Method called to insert notes with time specified. MediaType is set by the
	 * method
	 */
	public TripEntry(double lat, double lon, long creationTime, String noteText) {
		this.lat = lat;
		this.lon = lon;
		this.mediaLocation = ""; // Notes saved in db itself
		this.creationTime = creationTime;
		this.mediaType = MediaType.TEXT;
		this.noteText = noteText;
	}

	/**
	 * Method called to insert notes with no time specified. MediaType is set by
	 * the method
	 */
	public TripEntry(double lat, double lon, String noteText) {
		this.lat = lat;
		this.lon = lon;
		this.mediaLocation = ""; // Notes saved in db itself
		this.creationTime = System.currentTimeMillis();
		this.mediaType = MediaType.TEXT;
		this.noteText = noteText;
	}

	/**
	 * Method called to insert all other types of media with time specified.
	 */
	public TripEntry(double lat, double lon, String mediaLocation,
			MediaType mediaType, long creationTime) {
		this.lat = lat;
		this.lon = lon;
		this.mediaLocation = mediaLocation;
		this.creationTime = creationTime;
		this.mediaType = mediaType;
		this.noteText = ""; // Empty note
	}

	/**
	 * Method called to insert all other types of media with no time specified.
	 */
	public TripEntry(double lat, double lon, String mediaLocation,
			MediaType mediaType) {
		this.lat = lat;
		this.lon = lon;
		this.mediaLocation = mediaLocation;
		this.creationTime = System.currentTimeMillis();
		this.mediaType = mediaType;
		this.noteText = ""; // Empty note
	}

	/**
	 * No media
	 */
	public TripEntry(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
		this.creationTime = System.currentTimeMillis();
		this.mediaType = MediaType.NONE;
		this.mediaLocation = "";
		this.noteText = "";
	}

	public TripEntry() {
	}

	@Override
	public String toString() {
		return String.format("%d,%d,%d,%s,%s,%d", tripEntryId, lat, lon,
				mediaLocation, mediaType, creationTime);
	}
	
	public String toStringMultiline() {
		switch (mediaType) {
		case PHOTO:
		case VIDEO:
		case AUDIO:
			return String.format("Type: %s \nLat: %.6f \nLon: %.6f \nFile: %s",
					mediaType.name(), lat, lon, mediaLocation);
		case TEXT:
			return String.format("Type: %s \nLat: %.6f \nLon: %.6f \nText: %s",
					mediaType.name(), lat, lon, noteText);
		case NONE:
			return String.format("Lat: %.6f \nLon: %.6f",
					lat, lon);
		}
		return "Unknown Type!";
	}
}
