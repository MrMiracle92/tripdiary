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
	
	// TODO add more details as needed.

}
