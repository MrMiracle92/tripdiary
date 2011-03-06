package com.google.code.p.tripdiary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.code.p.tripdiary.DbDefs.TripDetailCols;
import com.google.code.p.tripdiary.utils.Util;

/**
 * 
 * Activity creating the KML file for sharing.
 * 
 * Creating XML file using StringBuilder as
 * javax.xml.transform.TransformerFactory is not available in Android.
 * 
 * @author Arpita Saha
 * 
 */
public class TripExport extends Activity {
	private final String DEFAULT_FILE_EXTENSION = ".kml";

	private String fileName;
	private String returnKey;

	private long thisTripId = AppDataDefs.NO_CURRENT_TRIP;
	private TripStorageManager mStorageMgr;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.) //TODO

		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can not write
			Toast.makeText(getBaseContext(),
					"External storage not available for writing.",
					Toast.LENGTH_SHORT).show();
			this.setResult(RESULT_CANCELED);
			finish();
			return;
		}

		// Get the tripId
		if (thisTripId == AppDataDefs.NO_CURRENT_TRIP) {
			Bundle extras = getIntent().getExtras();
			thisTripId = extras != null ? extras
					.getLong(AppDataDefs.KEY_TRIP_ID) : 0;

			if (thisTripId == 0) {
				TripDiaryLogger.logError("Could not find trip.");
				setResult(RESULT_CANCELED);
				finish();
				return;
			}

			TripDiaryLogger.logDebug("Exporting trip " + thisTripId);
		}

		// Storage manager
		mStorageMgr = TripStorageManagerFactory
				.getTripStorageManager(getApplicationContext());

		if (mStorageMgr == null) {
			TripDiaryLogger.logError("Could not find trip.");
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		if (genAndWriteToDisk()) {
			Intent data = new Intent();
			data.putExtra("returnKey", returnKey);
			this.setResult(RESULT_OK, data);
		} else
			this.setResult(RESULT_CANCELED);
		finish();
	}

	private String toXMLString() {
		StringBuilder sb = new StringBuilder("");
		StringBuilder lineString = new StringBuilder("");

		String xmlStart = "<?xml version=\"1.0\"?>"
				+ "<kml xmlns=\"http://www.opengis.net/kml/2.2\">"
				+ "<Document>";
		sb.append(xmlStart);

		// Cursor to the trip entries
		Cursor tripEntryCursor = mStorageMgr.getEntriesForTrip(thisTripId);
		TripDiaryLogger.logDebug("Number of entries to be exported : "
				+ tripEntryCursor.getCount());

		tripEntryCursor.moveToFirst();
		boolean isFirst = true;

		while (tripEntryCursor.isAfterLast() == false) {
			TripDiaryLogger.logDebug("Exporting entry "
					+ tripEntryCursor.getString(tripEntryCursor
							.getColumnIndex(TripDetailCols._ID))
					+ " Content : "
					+ tripEntryCursor.getString(tripEntryCursor
							.getColumnIndex(TripDetailCols.MEDIA_TYPE)));

			// For mediatype none, add to lineString : to draw the path.
			if (tripEntryCursor.getString(
					tripEntryCursor.getColumnIndex(TripDetailCols.MEDIA_TYPE))
					.equalsIgnoreCase("NONE")) {

				if (isFirst == true)
					isFirst = false;
				else
					lineString.append(",");

				String latlon = tripEntryCursor.getString(tripEntryCursor
						.getColumnIndex(TripDetailCols.LON))
						+ ","
						+ tripEntryCursor.getString(tripEntryCursor
								.getColumnIndex(TripDetailCols.LAT)) + ",0";
				lineString.append(latlon);
			} else {
				// Create the <Placemark> element
				sb.append("<Placemark>");

				// If text, add it as a description
				if (tripEntryCursor.getString(
						tripEntryCursor
								.getColumnIndex(TripDetailCols.MEDIA_TYPE))
						.equalsIgnoreCase("TEXT")) {
					sb.append("<description>");
					String noteText = tripEntryCursor.getString(tripEntryCursor
							.getColumnIndex(TripDetailCols.NOTE));
					sb.append(noteText);
					sb.append("</description>");
				} else { // For other media types, add default description
					sb.append("<description>"
							+ " Created by tripdiary application"
							+ "</description>");
				}

				sb.append("<Point>");
				String latlon = tripEntryCursor.getString(tripEntryCursor
						.getColumnIndex(TripDetailCols.LON))
						+ ","
						+ tripEntryCursor.getString(tripEntryCursor
								.getColumnIndex(TripDetailCols.LAT));
				sb.append("<coordinates>" + latlon + "</coordinates>");
				sb.append("</Point>");
				sb.append("</Placemark>");
			}

			tripEntryCursor.moveToNext();
		}

		// Add the LineString element now
		sb.append("<Placemark><name>");
		sb.append("</name>");
		sb.append("<description>" + " Created by tripdiary application"
				+ "</description>");
		sb.append("<styleUrl>#yellowLineGreenPoly</styleUrl>");
		sb.append("<LineString><extrude>1</extrude><tessellate>1</tessellate><altitudeMode>absolute</altitudeMode>");
		sb.append("<coordinates>");
		sb.append(lineString.toString());
		sb.append("</coordinates></LineString></Placemark>");

		String xmlEnd = "</Document></kml>";
		sb.append(xmlEnd);

		tripEntryCursor.close();
		return sb.toString();
	}

	private boolean genAndWriteToDisk() {
		// Generate
		String kmlStr = toXMLString();

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String folderNameFromPref = sp.getString("folderPref", "tripdiary"); // Default
																				// is
																				// tripdiary
		TripDiaryLogger.logDebug("KML dir is : " + folderNameFromPref);

		// Create the default file storage path
		File kmlDir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/" + folderNameFromPref);
		if (!kmlDir.exists()) // first time
		{
			kmlDir.mkdir();
		}
		fileName = Util.tripDiaryFileName() + DEFAULT_FILE_EXTENSION;
		returnKey = folderNameFromPref + "/" + fileName;
		File kmlFile = new File(kmlDir, fileName);

		// Write to disk
		try {
			FileOutputStream fos = new FileOutputStream(kmlFile, false);
			byte[] data = kmlStr.getBytes();
			fos.write(data);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			TripDiaryLogger.logError("Error writing " + kmlFile
					+ e.getMessage());
			return false;
		}

		return true;
	}
}
