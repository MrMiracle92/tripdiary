/*
 * Copyright (C) 2011 Arpita Saha, Arunabha Ghosh
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
package com.google.code.p.tripdiary;

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
