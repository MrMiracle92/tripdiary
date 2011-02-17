package com.google.code.p.tripdiary.test;

import java.util.ArrayList;
import java.util.List;

import com.google.code.p.tripdiary.DbTestActivity;
import com.google.code.p.tripdiary.TripDetail;
import com.google.code.p.tripdiary.TripEntry;
import com.google.code.p.tripdiary.TripEntry.MediaType;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

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
    }
    
    public void testTripEntryOps() {
       	long tripId = testActivity.testCreateTrip(TRIP_NAME, TRIP_DESC, true, CREATE_TIME);
       	assertTrue(tripId > 0);
    	int preexistingEntries  = testActivity.getEntriesForTrip(tripId).getCount();
       	TripEntry tripEntry = new TripEntry(1.0, 1.0, "test", MediaType.AUDIO, CREATE_TIME);
       	assertTrue(testActivity.addTripEntry(tripId, tripEntry));
       	int afterInsertEntries  = testActivity.getEntriesForTrip(tripId).getCount();
       	assertEquals(preexistingEntries, (afterInsertEntries - 1));
    }
    
    private void log(String msg) {
    	System.out.println(msg);
    }
}
