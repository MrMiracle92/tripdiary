package com.google.code.p.tripdiary;

/**
 * Holds the Metadata for a trip (Name, time etc).
 * 
 * @author Arunabha Ghosh
 */
public class TripDetail {
	/** The name of the trip. */
	private String name;

	/** The trip's ID. */
	private long tripId;

	/** The time the trip was started, in seconds since epoch. */
	private long createTime;

	/** Text description for the trip. */
	private String tripDescription;

	/** If true, trace route is enabled for this trip. */
	private boolean traceRouteEnabled;
	
	private String imageLocation;

	private boolean isCurrent;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTripDescription() {
		return tripDescription;
	}
	
	public void setTripDescription(String description) {
		this.tripDescription = description;
	}

	public String getImageLocation() {
		return imageLocation;
	}

	public void setImageLocation(String location) {
		this.imageLocation = location;
	}

	public String toString() {
		return name + "-" + tripDescription;
	}

	public long getTripId() {
		return tripId;
	}

	public void setTripId(long tripId) {
		this.tripId = tripId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public boolean isCurrent() {
		return isCurrent;
	}

	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}
	
	/** The location of the default thumbnail of the trip */
	public String defaultThumbnail;

	public TripDetail(String name, long tripId, long createTime, String tripDescription,
			boolean traceRouteEnabled, String defaultThumbnail) {
		this.name = name;
		this.tripId = tripId;
		this.createTime = createTime;
		this.tripDescription = tripDescription;
		this.traceRouteEnabled = traceRouteEnabled;
		this.defaultThumbnail = defaultThumbnail;
	}

	public TripDetail() {}

	@Override
	public String toString() {
		return String.format("%s,%d,%d,%s,%s,%s", name, tripId, createTime, tripDescription,
				traceRouteEnabled, defaultThumbnail);
	}
}
