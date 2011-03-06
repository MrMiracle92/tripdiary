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
	Preference folderPreference;
	Preference videoLengthPreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preference);

		folderPreference = (Preference) findPreference("folderPref");
		folderPreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getBaseContext());
						SharedPreferences.Editor editor = prefs.edit();

						String changedName = prefs.getString("folderPref",
								"tripDiary");
						TripDiaryLogger.logDebug("Changed folder name is "
								+ changedName);
						editor.putString("folderPref", changedName);
						editor.commit();
						return true;
					}
				});

		videoLengthPreference = (Preference) findPreference("videoLengthPref");
		videoLengthPreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getBaseContext());
						SharedPreferences.Editor editor = prefs.edit();

						String changedLength = prefs.getString(
								"videoLengthPref", "10");
						Integer changedLengthVal = 10;
						try {
							changedLengthVal = Integer.parseInt(changedLength);
						} catch (NumberFormatException e) {
							TripDiaryLogger
									.logError("Number format exception while configuring trip max. length"
											+ e.getMessage());
							changedLengthVal = 10;
						}

						TripDiaryLogger.logDebug("Changed duration is "
								+ changedLengthVal);
						editor.putString("videoLengthPref", changedLengthVal.toString());
						editor.commit();
						return true;
					}
				});
	}
}