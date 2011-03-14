/*
 * Copyright (C) 2011 Arpita Saha
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

import android.util.Log;

/**
 * Log class for the application.
 * 
 * @author Arpita Saha
 * 
 */
public class TripDiaryLogger {
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
