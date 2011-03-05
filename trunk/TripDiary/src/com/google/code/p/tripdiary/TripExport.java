package com.google.code.p.tripdiary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.google.code.p.tripdiary.DbDefs.TripDetailCols;
import com.google.code.p.tripdiary.utils.*;

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
		if (thisTripId == 0) {
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
			data.putExtra("returnKey", fileName);
			this.setResult(RESULT_OK, data);
		} else
			this.setResult(RESULT_CANCELED);
		finish();
	}

	private String toXMLString() {
		StringBuilder sb = new StringBuilder("");

		String xmlStart = "<?xml version=\"1.0\"?>"
				+ "<kml xmlns=\"http://www.opengis.net/kml/2.2\">"
				+ "<Document>";
		sb.append(xmlStart);

		// Cursor to the trip entries
		Cursor tripEntryCursor = mStorageMgr.getEntriesForTrip(thisTripId);
		TripDiaryLogger.logDebug("Number of entries to be exported : "
				+ tripEntryCursor.getCount());
		tripEntryCursor.moveToFirst();

		while (tripEntryCursor.isAfterLast() == false) {
			// Create the <Placemark> element
			sb.append("<Placemark>");

			TripDiaryLogger.logDebug("Exporting entry "
					+ tripEntryCursor.getString(tripEntryCursor
							.getColumnIndex(TripDetailCols._ID))
					+ " Content : "
					+ tripEntryCursor.getString(tripEntryCursor
							.getColumnIndex(TripDetailCols.MEDIA_TYPE)));
			if (tripEntryCursor.getString(
					tripEntryCursor.getColumnIndex(TripDetailCols.MEDIA_TYPE))
					.equalsIgnoreCase("TEXT")) {
				sb.append("<description>");
				String noteText = tripEntryCursor.getString(tripEntryCursor
						.getColumnIndex(TripDetailCols.NOTE));
				sb.append(noteText);
				sb.append("</description>");
			} else {
				sb.append("<description>" + " Created by tripDiary application"
						+ "</description>");
			}
			sb.append("<Point>");

			String latlon = tripEntryCursor.getString(tripEntryCursor
					.getColumnIndex(TripDetailCols.LAT))
					+ ","
					+ tripEntryCursor.getString(tripEntryCursor
							.getColumnIndex(TripDetailCols.LON)) + ",0";
			sb.append("<coordinates>" + latlon + "</coordinates>");
			sb.append("</Point>");
			sb.append("</Placemark>");

			tripEntryCursor.moveToNext();
		}

		String xmlEnd = "</Document> </kml>";
		sb.append(xmlEnd);

		tripEntryCursor.close();
		return sb.toString();
	}

	private boolean genAndWriteToDisk() {
		// Generate
		String kmlStr = toXMLString();

		// Create the default file storage path
		File kmlDir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/tripDiary");
		if (!kmlDir.exists()) // first time
		{
			kmlDir.mkdir();
		}
		fileName = Util.tripDiaryFileName() + ".txt"; // TODO use
														// DEFAULT_FILE_EXTENSION
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
