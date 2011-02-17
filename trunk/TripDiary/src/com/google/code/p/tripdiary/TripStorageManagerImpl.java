package com.google.code.p.tripdiary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.code.p.tripdiary.DbDefs.TripCols;
import com.google.code.p.tripdiary.DbDefs.TripDetailCols;

/**
 * Implementation of {@code TripStorageManager}
 * 
 * @author Arunabha Ghosh
 */
public class TripStorageManagerImpl implements TripStorageManager {
	private static final String DATABASE_NAME = "tripdiary.db";
	private static final int DATABASE_VERSION = 2;
	private static final String TRIP_METADATA_TABLE = "tripmetadada";
	private static final String TRIP_DETAIL_TABLE = "tripdetails";
	public static final String TAG = "TripStorageManagerImpl";

	private static final String TRIP_METADATA_TABLE_CREATOR =
		String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT, %s TEXT, %s TEXT," +
				" %s TEXT, %s TEXT);",
				TRIP_METADATA_TABLE, TripCols._ID, TripCols.TRIP_NAME, TripCols.TRIP_DESCRIPTION,
				TripCols.CREATE_TIME, TripCols.TRACEROUTE_ENABLED, TripCols.THUMBNAIL_LOCATION);

	private static final String TRIP_DETAIL_TABLE_CREATOR =
		String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s INTEGER, %s TEXT, %s TEXT, " +
				"%s TEXT, %s TEXT, %s TEXT);", TRIP_DETAIL_TABLE, TripDetailCols._ID,
				TripDetailCols.TRIP_ID,	TripDetailCols.LAT,	TripDetailCols.LON,
				TripDetailCols.CREATE_TIME,	TripDetailCols.MEDIA_TYPE,
				TripDetailCols.MEDIA_LOCATION);

	/** Query to select a given trip. */
	private static final String GET_TRIP_DETAIL =
		String.format("SELECT * FROM %s WHERE _id=?", TRIP_METADATA_TABLE);

	/** Query to select all trip entries for a given trip. */
	private static final String GET_TRIP_ENTRIES =
		String.format("SELECT * FROM %s WHERE %s=?", TRIP_DETAIL_TABLE, TripDetailCols.TRIP_ID);

	/** Query to select all trips. */
	private static final String GET_ALL_TRIPS =
		String.format("SELECT * FROM %s", TRIP_METADATA_TABLE);

	/** Where clause to select a given trip. */
	private static final String TRIP_SELECTOR =
		String.format("%s=?", TripCols._ID);

	private static class DbHelper extends SQLiteOpenHelper {
		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TRIP_METADATA_TABLE_CREATOR);
			db.execSQL(TRIP_DETAIL_TABLE_CREATOR);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TRIP_METADATA_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + TRIP_DETAIL_TABLE);
			onCreate(db);
		}
	}

	private final DbHelper dbHelper;

	public TripStorageManagerImpl(Context context) {
		dbHelper = new DbHelper(context);
	}

	@Override
	public long createNewTrip(String name, String tripDescription,
			boolean traceRouteEnabled) {
		return createNewTrip(name, tripDescription, traceRouteEnabled, System.currentTimeMillis());
	}

	public long createNewTrip(String name, String tripDescription,
			boolean traceRouteEnabled, long currentTime) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues insertValues = new ContentValues();
		insertValues.put(TripCols.TRIP_NAME, name);
		insertValues.put(TripCols.TRIP_DESCRIPTION, tripDescription);
		insertValues.put(TripCols.TRACEROUTE_ENABLED, traceRouteEnabled);
		insertValues.put(TripCols.CREATE_TIME, currentTime);
		long retval = db.insert(TRIP_METADATA_TABLE, TripCols.THUMBNAIL_LOCATION, insertValues);
		return retval;
	}

	@Override
	public void updateTrip(long tripId, String name, String tripDescription,
			boolean traceRouteEnabled, String thumbnailLocation) throws IllegalArgumentException {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues updateValues = new ContentValues();
		updateValues.put(TripCols.TRIP_NAME, name);
		updateValues.put(TripCols.TRIP_DESCRIPTION, tripDescription);
		updateValues.put(TripCols.TRACEROUTE_ENABLED, traceRouteEnabled);
		updateValues.put(TripCols.THUMBNAIL_LOCATION, thumbnailLocation);
		db.update(TRIP_METADATA_TABLE, updateValues, TRIP_SELECTOR,
				new String[] {String.format("%d", tripId)});
	}

	@Override
	public boolean addTripEntry(long tripId, TripEntry tripEntry)
	throws IllegalArgumentException {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues insertValues = new ContentValues();
		insertValues.put(TripDetailCols.TRIP_ID, tripId);
		insertValues.put(TripDetailCols.CREATE_TIME, tripEntry.creationTime);
		insertValues.put(TripDetailCols.LAT, tripEntry.lat);
		insertValues.put(TripDetailCols.LON, tripEntry.lon);
		insertValues.put(TripDetailCols.MEDIA_LOCATION, tripEntry.mediaLocation);
		insertValues.put(TripDetailCols.MEDIA_TYPE, tripEntry.mediaType.name());
		long retval = db.insert(TRIP_DETAIL_TABLE, TripDetailCols.MEDIA_LOCATION, insertValues);
		return (retval != -1);
	}

	@Override
	public Cursor getEntriesForTrip(long tripId)
	throws IllegalArgumentException {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.rawQuery(GET_TRIP_ENTRIES, new String[] {String.format("%d", tripId)});
	}

	@Override
	public Cursor getAllTrips() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.rawQuery(GET_ALL_TRIPS, new String[] {});
	}

	@Override
	public TripDetail getTripDetail(long tripId) throws IllegalArgumentException {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor result = db.rawQuery(GET_TRIP_DETAIL, new String[] {String.format("%d", tripId)});
		if (result.getCount() <= 0) {
			return null;
		} else {
			result.moveToFirst();
			TripDetail tripDetail = new TripDetail();
			tripDetail.name = result.getString(result.getColumnIndex(TripCols.TRIP_NAME));
			tripDetail.tripId = result.getLong(result.getColumnIndex(TripCols._ID));
			tripDetail.createTime = result.getLong(result.getColumnIndex(TripCols.CREATE_TIME));
			tripDetail.tripDescription =
				result.getString(result.getColumnIndex(TripCols.TRIP_DESCRIPTION));
			String traceStr = result.getString(result.getColumnIndex(TripCols.TRACEROUTE_ENABLED));
			tripDetail.traceRouteEnabled = traceStr.equals("1") ? true : false;
			// The thumbnail can be missing.
			int thumbnailIndex = result.getColumnIndex(TripCols.THUMBNAIL_LOCATION);
			if (thumbnailIndex != -1) {
				tripDetail.defaultThumbnail =
					result.getString(result.getColumnIndex(TripCols.THUMBNAIL_LOCATION));
			}
			result.close();
			return tripDetail;
		}
	}
}
