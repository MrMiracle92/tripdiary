package com.google.code.p.tripdiary;

/*
 * Copyright (C) 2011 Arunabha Ghosh
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

	public long testCreateTrip(String name, String tripDescription,
			boolean traceRouteEnabled, long currentTime) {
		return impl.createNewTrip(name, tripDescription, traceRouteEnabled,
				currentTime);
	}

	public void testUpdateTrip(long tripId, String name,
			String tripDescription, boolean traceRouteEnabled,
			String thumbnailLocation) {
		impl.updateTrip(tripId, name, tripDescription, traceRouteEnabled,
				thumbnailLocation);
	}

	public void testDeleteTrip(long tripId) {
		impl.deleteTrip(tripId);
	}

	public TripDetail getTripDetails(long tripId) {
		return impl.getTripDetail(tripId);
	}

	public Cursor getAllTrips() {
		return impl.getAllTrips();
	}

	public long addTripEntry(long tripId, TripEntry tripEntry) {
		return impl.addTripEntry(tripId, tripEntry);
	}

	public Cursor getEntriesForTrip(long tripId) {
		return impl.getEntriesForTrip(tripId);
	}

	public long getLastUpdatedTime(long tripId) {
		return impl.getLastUpdatedTime(tripId);
	}
}
