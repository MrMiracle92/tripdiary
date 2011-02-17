package com.google.code.p.tripdiary;

/**
 * Holds the Metadata for a trip (Name, time etc).
 * 
 * @author Arunabha Ghosh
 */
public class TripDetail {
	/** The name of the trip. */
	public String name;

	/** The trip's ID. */
	public long tripId;

	/** The time the trip was started, in seconds since epoch. */
	public long createTime;

	/** Text description for the trip. */
	public String tripDescription;

	/** If true, trace route is enabled for this trip. */
	public boolean traceRouteEnabled;

	/** The location of the default thumbnail of the trip */
	public String defaultThumbnail;

	public boolean isCurrent = false;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getTripDescription() {
		return tripDescription;
	}

	public void setTripDescription(String tripDescription) {
		this.tripDescription = tripDescription;
	}

	public boolean isTraceRouteEnabled() {
		return traceRouteEnabled;
	}

	public void setTraceRouteEnabled(boolean traceRouteEnabled) {
		this.traceRouteEnabled = traceRouteEnabled;
	}

	public String getDefaultThumbnail() {
		return defaultThumbnail;
	}

	public void setDefaultThumbnail(String defaultThumbnail) {
		this.defaultThumbnail = defaultThumbnail;
	}

	public boolean isCurrent() {
		return isCurrent;
	}

	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}
}
