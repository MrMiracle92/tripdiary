package com.google.code.p.tripdiary;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * This activity is for setting the home preferences menu.
 * 
 * @author Arpita Saha
 * 
 */
public class TripHomePreference extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preference);

		Preference folderPreference = (Preference) findPreference("folderPref");
		folderPreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getBaseContext());
						SharedPreferences.Editor editor = prefs.edit();
						
						String temp = prefs.getString("folderPref", "tripDiary");
						TripDiaryLogger.logDebug("temp is " + temp);
						editor.putString("folderPref", "arpita");
						editor.commit();
						
						
						TripDiaryLogger.logDebug("In Preference");
						return true;
					}
				});
		
//		folderPreference.
	}
}