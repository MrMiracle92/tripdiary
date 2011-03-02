package com.google.code.p.tripdiary.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utilities class
 * 
 * @author Arpita Saha
 *
 */
public class Util {
	public static String tripDiaryFileName() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
		Date date = new Date();
		String mediaFileName = "tripDiary-" + dateFormat.format(date);

		return mediaFileName;
	}
}
