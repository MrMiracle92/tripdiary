package com.google.code.p.tripdiary;

import android.database.Cursor;

/**
 * Interface for the storage manager for the Trip Diary. This interface
 * abstracts the underlying storage mechanism and provides a high level
 * interface for managing individual trips.
 * 
 * @author Arunabha Ghosh
 */
public interface TripStorageManager {
	/**
	 * Creates a new trip with the given name and returns the trip's id.
	 * 
	 * @param name
	 *            the name of the trip
	 * @return the trip's id which can be used to further reference the trip, or
	 *         -1 if the trip cannot be created
	 */
	long createNewTrip(String name, String tripDescription, boolean traceRouteEnabled);

	/**
	 * Updates the metadata for a given trip
	 * 
	 * @param tripId the trip to update
	 * @param name
	 * @param tripDescription
	 * @param traceRouteEnabled
	 * @throws IllegalArgumentException if the trip does not exist.
	 */
	void updateTrip(long tripId, String name, String tripDescription, boolean traceRouteEnabled,
			String thumbnailLocation)
	throws IllegalArgumentException;

	/**
	 * Adds an entry to the given trip.
	 * 
	 * @param tripId the trip to which to add this entry
	 * @param tripEntry the entry to add
	 * @return true on success, false otherwise
	 * @throws IllegalArgumentException if the triId is invalid
	 */
	boolean addTripEntry(long tripId, TripEntry tripEntry) throws IllegalArgumentException;

	/**
	 * Returns the entries for a given trip.
	 * 
	 * @param tripId the trip to fetch
	 * @return the list of TripEntry objects
	 * @throws IllegalArgumentException if the trip does not exist.
	 */
	Cursor getEntriesForTrip(long tripId) throws IllegalArgumentException;

	/**
	 * Returns the list of all known trips
	 * 
	 * @return
	 */
	Cursor getAllTrips();

	/**
	 * Gets the details for a particular trip.
	 * 
	 * @param tripId the trip whose details to get.
	 * @return the details of trip corresponding to tripId
	 * @throws IllegalArgumentException if the tripId is invalid
	 */
	TripDetail getTripDetail(long tripId) throws IllegalArgumentException;
	
	/**
	 * Gets the last updated time (from the latest trip entry) for the given trip id. 
	 * If none, then gets the created time for the trip.
	 * 
	 * @param tripId the trip whose last updated time is needed
	 * @return
	 */
	long getLastUpdatedTime(long tripId);
}
