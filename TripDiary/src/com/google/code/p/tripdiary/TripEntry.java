package com.google.code.p.tripdiary;

/**
 * Represents an entry in a Trip.
 * 
 * @author Arunabha Ghosh
 */
public class TripEntry {
	public enum MediaType {
		NONE,
		PHOTO,
		AUDIO,
		VIDEO,
		TEXT,
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

	public TripEntry(double lat, double lon, String mediaLocation, MediaType mediaType,
			long creationTime) {
		this.lat = lat;
		this.lon = lon;
		this.mediaLocation = mediaLocation;
		this.creationTime = creationTime;
		this.mediaType = mediaType;
	}

	@Override
	public String toString() {
		return String.format("%d,%d,%d,%s,%s,%d", tripEntryId, lat, lon, mediaLocation, mediaType,
				creationTime);
	}
}
