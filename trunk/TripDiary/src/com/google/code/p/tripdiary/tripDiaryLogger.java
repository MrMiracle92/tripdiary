package com.google.code.p.tripdiary;

import android.util.Log;

/**
 * Log class for the application.
 * @author Arpita Saha
 *
 */
public class tripDiaryLogger {
	private static final String TAG = "tripDiary";

	public static int logInfo(String message) {
		return Log.i(TAG, message);
	}
	
	public static int logDebug(String message) {
		return Log.d(TAG, message);
	}
	
	public static int logWarning(String message) {
		return Log.w(TAG, message);
	}

	public static int logError(String message) {
		return Log.e(TAG, message);
	}
}
