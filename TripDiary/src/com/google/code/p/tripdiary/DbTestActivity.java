package com.google.code.p.tripdiary;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;

public class DbTestActivity extends Activity {
	private TripStorageManagerImpl impl;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		impl = new TripStorageManagerImpl(this);
	}

	public long testCreateTrip(String name, String tripDescription, boolean traceRouteEnabled,
			long currentTime) {
		return impl.createNewTrip(name, tripDescription, traceRouteEnabled, currentTime);
	}

	public void testUpdateTrip(long tripId, String name, String tripDescription,
			boolean traceRouteEnabled, String thumbnailLocation) {
		impl.updateTrip(tripId, name, tripDescription, traceRouteEnabled, thumbnailLocation);
	}

	public TripDetail getTripDetails(long tripId) {
		return impl.getTripDetail(tripId);
	}

	public Cursor getAllTrips() {
		return impl.getAllTrips();
	}

	public boolean addTripEntry(long tripId, TripEntry tripEntry) {
		return impl.addTripEntry(tripId, tripEntry);
	}

	public Cursor getEntriesForTrip(long tripId) {
		return impl.getEntriesForTrip(tripId);
	}

	public long getLastUpdatedTime(long tripId) {
		return impl.getLastUpdatedTime(tripId);
	}
}
