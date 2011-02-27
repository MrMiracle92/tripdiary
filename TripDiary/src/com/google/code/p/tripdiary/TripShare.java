package com.google.code.p.tripdiary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.google.code.p.tripdiary.DbDefs.TripDetailCols;

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
public class TripShare extends Activity {
	private static final String TAG = "TripShare";

	private final String DEFAULT_FILE_EXTENSION = ".kml";

	private String fileName;

	private long thisTripId = AppDataDefs.NO_CURRENT_TRIP;
	private TripStorageManager mStorageMgr;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.) //TODO

		// Get the tripId
		if (thisTripId == 0) {
			Bundle extras = getIntent().getExtras();
			thisTripId = extras != null ? extras
					.getLong(AppDataDefs.KEY_TRIP_ID) : 0;

			// by now there should be a trip id
			if (thisTripId == 0) {
				Log.e(TAG, "Could not find trip.");
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
		}

		// Storage manager
		mStorageMgr = TripStorageManagerFactory
				.getTripStorageManager(getApplicationContext());

		if (mStorageMgr == null) {
			Log.e(TAG, "Could not find trip.");
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
		sb.append("<kml>");

		// Cursor to the trip entries
		Cursor tripEntryCursor = mStorageMgr.getEntriesForTrip(thisTripId);
		tripEntryCursor.moveToFirst();

		while (tripEntryCursor.isAfterLast() == false) {
			// Create the <Placemark> element
			sb.append("<Placemark");
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
		fileName = getKMLFileName() + ".txt"; // TODO use DEFAULT_FILE_EXTENSION
		File kmlFile = new File(kmlDir, fileName);

		// Write to disk
		try {
			OutputStream os = new FileOutputStream(kmlFile);
			byte[] data = kmlStr.getBytes();
			os.write(data);
			os.close();
		} catch (IOException e) {
			Log.e(TAG, "Error writing " + kmlFile + e.getMessage());
			return false;
		}

		return true;
	}

	public static String getKMLFileName() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
		Date date = new Date();
		String kmlFileName = dateFormat.format(date);

		return kmlFileName;
	}
}
