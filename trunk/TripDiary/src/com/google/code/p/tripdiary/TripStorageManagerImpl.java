package com.google.code.p.tripdiary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.code.p.tripdiary.DbDefs.TripCols;
import com.google.code.p.tripdiary.DbDefs.TripDetailCols;
import com.google.code.p.tripdiary.TripEntry.MediaType;

/**
 * Implementation of {@code TripStorageManager}
 * 
 * @author Arunabha Ghosh
 * @author Arpita Saha
 * @author Ankan Mukherjee
 */
public class TripStorageManagerImpl implements TripStorageManager {
	private static final String DATABASE_NAME = "tripdiary.db";
	private static final int DATABASE_VERSION = 2;

	private static final String TRIP_METADATA_TABLE = "tripmetadada";
	private static final String TRIP_DETAIL_TABLE = "tripdetails";

	private static final String TRIP_METADATA_TABLE_CREATOR = String.format(
			"CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT, %s TEXT, %s TEXT,"
					+ " %s TEXT, %s TEXT);", TRIP_METADATA_TABLE, TripCols._ID,
			TripCols.TRIP_NAME, TripCols.TRIP_DESCRIPTION,
			TripCols.CREATE_TIME, TripCols.TRACEROUTE_ENABLED,
			TripCols.THUMBNAIL_LOCATION);

	private static final String TRIP_DETAIL_TABLE_CREATOR = String.format(
			"CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s INTEGER, %s TEXT, %s TEXT, "
					+ "%s TEXT, %s TEXT, %s TEXT, %s TEXT);",
			TRIP_DETAIL_TABLE, TripDetailCols._ID, TripDetailCols.TRIP_ID,
			TripDetailCols.LAT, TripDetailCols.LON, TripDetailCols.CREATE_TIME,
			TripDetailCols.MEDIA_TYPE, TripDetailCols.MEDIA_LOCATION,
			TripDetailCols.NOTE);

	/** Query to select a given trip. */
	private static final String GET_TRIP_DETAIL = String.format(
			"SELECT * FROM %s WHERE _id=?", TRIP_METADATA_TABLE);

	/** Query to select all trip entries for a given trip. */
	private static final String GET_TRIP_ENTRIES = String.format(
			"SELECT * FROM %s WHERE %s=?", TRIP_DETAIL_TABLE,
			TripDetailCols.TRIP_ID);

	/** Query to select all trip entries for a given trip. */
	private static final String GET_TRIP_MEDIA_ENTRIES = String.format(
			"SELECT * FROM %s WHERE %s=? AND %s<>%s", TRIP_DETAIL_TABLE,
			TripDetailCols.TRIP_ID, TripDetailCols.MEDIA_TYPE,
			TripEntry.MediaType.NONE.name());

	/** Query to select all trips. */
	private static final String GET_ALL_TRIPS = String.format(
			"SELECT * FROM %s ORDER BY %s DESC", TRIP_METADATA_TABLE,
			TripCols.CREATE_TIME);

	/** Query to select a given trip entry. */
	private static final String GET_TRIP_ENTRY = String.format(
			"SELECT * FROM %s WHERE _id=?", TRIP_DETAIL_TABLE);

	/** Query to select the latest entry in the given trip. */
	private static final String GET_TRIP_ENTRY_TIME_DESC = String.format(
			"SELECT %s FROM %s WHERE %s=? ORDER BY %s DESC",
			TripDetailCols.CREATE_TIME, TRIP_DETAIL_TABLE,
			TripDetailCols.TRIP_ID, TripDetailCols.CREATE_TIME);

	/** Where clause to select a given trip. */
	private static final String TRIP_SELECTOR = String.format("%s=?",
			TripCols._ID);

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
			TripDiaryLogger.logWarning("Upgrading database from version "
					+ oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
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
		return createNewTrip(name, tripDescription, traceRouteEnabled,
				System.currentTimeMillis());
	}

	public long createNewTrip(String name, String tripDescription,
			boolean traceRouteEnabled, long currentTime) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues insertValues = new ContentValues();
		insertValues.put(TripCols.TRIP_NAME, name);
		insertValues.put(TripCols.TRIP_DESCRIPTION, tripDescription);
		insertValues.put(TripCols.TRACEROUTE_ENABLED, traceRouteEnabled);
		insertValues.put(TripCols.CREATE_TIME, currentTime);
		long retval = db.insert(TRIP_METADATA_TABLE,
				TripCols.THUMBNAIL_LOCATION, insertValues);
		return retval;
	}

	@Override
	public void updateTrip(long tripId, String name, String tripDescription,
			boolean traceRouteEnabled, String thumbnailLocation)
			throws IllegalArgumentException {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues updateValues = new ContentValues();
		updateValues.put(TripCols.TRIP_NAME, name);
		updateValues.put(TripCols.TRIP_DESCRIPTION, tripDescription);
		updateValues.put(TripCols.TRACEROUTE_ENABLED, traceRouteEnabled);
		updateValues.put(TripCols.THUMBNAIL_LOCATION, thumbnailLocation);
		db.update(TRIP_METADATA_TABLE, updateValues, TRIP_SELECTOR,
				new String[] { String.format("%d", tripId) });
	}

	@Override
	public long addTripEntry(long tripId, TripEntry tripEntry)
			throws IllegalArgumentException {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues insertValues = new ContentValues();
		insertValues.put(TripDetailCols.TRIP_ID, tripId);
		insertValues.put(TripDetailCols.CREATE_TIME, tripEntry.creationTime);
		insertValues.put(TripDetailCols.LAT, tripEntry.lat);
		insertValues.put(TripDetailCols.LON, tripEntry.lon);
		insertValues
				.put(TripDetailCols.MEDIA_LOCATION, tripEntry.mediaLocation);
		insertValues.put(TripDetailCols.MEDIA_TYPE, tripEntry.mediaType.name());
		insertValues.put(TripDetailCols.NOTE, tripEntry.noteText);

		long tripNum = db.insertOrThrow(TRIP_DETAIL_TABLE,
				TripDetailCols.MEDIA_LOCATION, insertValues);
		TripDiaryLogger.logDebug("Adding a new entry " + tripNum + " : "
				+ tripEntry.lat + " " + tripEntry.lon + " "
				+ tripEntry.mediaType);

		if (tripEntry.mediaType == MediaType.TEXT)
			TripDiaryLogger.logDebug("Added entry has text : "
					+ tripEntry.noteText);
		return tripNum;
	}

	@Override
	public Cursor getMediaEntriesForTrip(long tripId)
			throws IllegalArgumentException {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.rawQuery(GET_TRIP_MEDIA_ENTRIES,
				new String[] { String.format("%d", tripId) });
	}

	@Override
	public Cursor getEntriesForTrip(long tripId)
			throws IllegalArgumentException {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.rawQuery(GET_TRIP_ENTRIES,
				new String[] { String.format("%d", tripId) });
	}

	@Override
	public Cursor getAllTrips() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.rawQuery(GET_ALL_TRIPS, new String[] {});
	}

	@Override
	public TripDetail getTripDetail(long tripId)
			throws IllegalArgumentException {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor result = db.rawQuery(GET_TRIP_DETAIL,
				new String[] { String.format("%d", tripId) });
		if (result.getCount() <= 0) {
			return null;
		} else {
			result.moveToFirst();
			TripDetail tripDetail = new TripDetail();
			tripDetail.name = result.getString(result
					.getColumnIndex(TripCols.TRIP_NAME));
			tripDetail.tripId = result.getLong(result
					.getColumnIndex(TripCols._ID));
			tripDetail.createTime = result.getLong(result
					.getColumnIndex(TripCols.CREATE_TIME));
			tripDetail.tripDescription = result.getString(result
					.getColumnIndex(TripCols.TRIP_DESCRIPTION));
			String traceStr = result.getString(result
					.getColumnIndex(TripCols.TRACEROUTE_ENABLED));
			tripDetail.traceRouteEnabled = traceStr.equals("1") ? true : false;
			// The thumbnail can be missing.
			int thumbnailIndex = result
					.getColumnIndex(TripCols.THUMBNAIL_LOCATION);
			if (thumbnailIndex != -1) {
				tripDetail.defaultThumbnail = result.getString(result
						.getColumnIndex(TripCols.THUMBNAIL_LOCATION));
			}
			result.close();
			return tripDetail;
		}
	}

	@Override
	public TripEntry getTripEntry(long tripEntryId)
			throws IllegalArgumentException {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor result = db.rawQuery(GET_TRIP_ENTRY,
				new String[] { String.format("%d", tripEntryId) });
		if (result.getCount() <= 0) {
			return null;
		} else {
			result.moveToFirst();
			TripEntry tripEntry = new TripEntry();
			tripEntry.tripEntryId = result.getLong(result
					.getColumnIndex(TripDetailCols._ID));
			tripEntry.lat = result.getLong(result
					.getColumnIndex(TripDetailCols.LAT));
			tripEntry.lon = result.getLong(result
					.getColumnIndex(TripDetailCols.LON));
			tripEntry.mediaLocation = result.getString(result
					.getColumnIndex(TripDetailCols.MEDIA_LOCATION));
			tripEntry.mediaType = TripEntry.MediaType
					.valueOf(result.getString(result
							.getColumnIndex(TripDetailCols.MEDIA_TYPE)));
			tripEntry.creationTime = result.getLong(result
					.getColumnIndex(TripDetailCols.CREATE_TIME));
			tripEntry.noteText = result.getString(result
					.getColumnIndex(TripDetailCols.NOTE));
			return tripEntry;
		}
	}

	@Override
	public long getLastUpdatedTime(long tripId) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor result = db.rawQuery(GET_TRIP_ENTRY_TIME_DESC,
				new String[] { String.format("%d", tripId) });
		if (result.getCount() <= 0) {
			return -1;
		} else {
			result.moveToFirst();
			return result.getLong(result
					.getColumnIndex(TripDetailCols.CREATE_TIME));
		}
	}

	@Override
	public void deleteTrip(long tripId) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		// delete trip from trip details
		db.delete(TRIP_DETAIL_TABLE,
				String.format("%s=?", TripDetailCols.TRIP_ID),
				new String[] { String.format("%d", tripId) });
		// delete trip from trip db
		db.delete(TRIP_METADATA_TABLE, TRIP_SELECTOR,
				new String[] { String.format("%d", tripId) });

	}

	@Override
	public void deleteTripEntry(long tripEntryId) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(TRIP_DETAIL_TABLE, String.format("%s=?", TripDetailCols._ID),
				new String[] { String.format("%d", tripEntryId) });
	}
}
