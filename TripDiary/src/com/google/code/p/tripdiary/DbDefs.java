package com.google.code.p.tripdiary;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Definitions of columns of the two tables in the database.
 * 
 * @author Arunabha Ghosh
 * @author Arpita Saha
 */
public class DbDefs {
	public static final String AUTHORITY = "com.google.code.p.tripDiary";

	public static final class TripCols implements BaseColumns {
		// Prevent instantiation.
		private TripCols() {
		}

		/** The name column. */
		public static final String TRIP_NAME = "Tripname";

		/** The creation time column. */
		public static final String CREATE_TIME = "Creationtime";

		/** The traceroute enabled column. */
		public static final String TRACEROUTE_ENABLED = "Traceroute";

		/** The trip description column. */
		public static final String TRIP_DESCRIPTION = "Tripdescription";

		/** Location of the thumbnail for the trip. */
		public static final String THUMBNAIL_LOCATION = "Thumbnaillocation";
	}

	public static final class TripDetailCols implements BaseColumns {
		// Prevent instantiation.
		private TripDetailCols() {
		}

		/** The trip to which this entry belongs. */
		public static final String TRIP_ID = "Tripid";

		/** The creation time column. */
		public static final String CREATE_TIME = "Creationtime";

		/** The latitude column. */
		public static final String LAT = "Latitude";

		/** The longitude column. */
		public static final String LON = "Longitude";

		/** The media type column. */
		public static final String MEDIA_TYPE = "Mediatype";

		/** The media location column. */
		public static final String MEDIA_LOCATION = "Medialocation";

		/** Notes column (if any). */
		public static final String NOTE = "note";
	}
}
