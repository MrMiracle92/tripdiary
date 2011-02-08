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

	// TODO add more details as needed.
}
