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
package com.google.code.p.tripdiary.test;

import android.test.ActivityInstrumentationTestCase2;

import com.google.code.p.tripdiary.DbTestActivity;
import com.google.code.p.tripdiary.TripDetail;
import com.google.code.p.tripdiary.TripEntry;
import com.google.code.p.tripdiary.TripEntry.MediaType;

public class DbTest extends ActivityInstrumentationTestCase2<DbTestActivity> {
	private static final String TRIP_NAME = "TEST_TRIP";
	private static final String TRIP_DESC = "TEST_TRIP_DESC";
	private static final long CREATE_TIME = 1234L;
	private static final boolean TRACE_ENABLED = true;
	private static final String THUMBNAIL_LOC = null;
	private DbTestActivity testActivity;
	
	public DbTest() {
		super(DbTestActivity.class);
	}
	
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testActivity = this.getActivity();
    }
    
    public void testTripMetadataOps() {
    	int preexistingTrips  = testActivity.getAllTrips().getCount();
    	long tripId = testActivity.testCreateTrip(TRIP_NAME, TRIP_DESC, true, CREATE_TIME);
    	assertTrue(tripId > 0);
    	int afterInsertTrips  = testActivity.getAllTrips().getCount();
    	assertEquals(preexistingTrips, (afterInsertTrips - 1));
    	log(String.format("Got trip id %d", tripId));
    	TripDetail expected = new TripDetail(TRIP_NAME, tripId, CREATE_TIME, TRIP_DESC, 
    			TRACE_ENABLED, THUMBNAIL_LOC);
    	log("Expected " + expected.toString());
    	String tripDetails = testActivity.getTripDetails(tripId).toString();
    	log("Actual " + tripDetails);
    	assertEquals(expected.toString(), tripDetails);
    	
    	// Update the trip metadata and check again.
    	testActivity.testUpdateTrip(tripId, TRIP_NAME, TRIP_DESC, !TRACE_ENABLED, "test_thumb");
    	expected.traceRouteEnabled = !TRACE_ENABLED;
    	expected.defaultThumbnail = "test_thumb";
    	tripDetails = testActivity.getTripDetails(tripId).toString();
    	assertEquals(expected.toString(), tripDetails);
    	
    	// delete the trip and check again
    	testActivity.testDeleteTrip(tripId);
    	assertNull(testActivity.getTripDetails(tripId));
    	assertEquals(0, testActivity.getEntriesForTrip(tripId).getCount());
    	
    }
    
    public void testTripEntryOps() {
       	long tripId = testActivity.testCreateTrip(TRIP_NAME, TRIP_DESC, true, CREATE_TIME);
       	assertTrue(tripId > 0);
    	int preexistingEntries  = testActivity.getEntriesForTrip(tripId).getCount();
       	TripEntry tripEntry = new TripEntry(1.0, 1.0, "test", MediaType.AUDIO, CREATE_TIME);
       	assertTrue(testActivity.addTripEntry(tripId, tripEntry) >= 0);
       	int afterInsertEntries  = testActivity.getEntriesForTrip(tripId).getCount();
       	assertEquals(preexistingEntries, (afterInsertEntries - 1));
       	long currentTime = System.currentTimeMillis();
       	TripEntry tripEntryLatest = new TripEntry(1.0, 1.0, "test", MediaType.AUDIO, currentTime);
       	assertTrue(testActivity.addTripEntry(tripId, tripEntryLatest) >= 0);
       	assertEquals(currentTime, testActivity.getLastUpdatedTime(tripId));
    }
    
    private void log(String msg) {
    	System.out.println(msg);
    }
}
