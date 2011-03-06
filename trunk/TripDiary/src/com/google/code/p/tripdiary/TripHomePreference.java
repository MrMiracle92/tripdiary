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
	private Preference folderPreference;
	private Preference videoLengthPreference;
	private Preference videoQualityPreference;
	private Preference trackMinDistancePreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preference);

		folderPreference = (Preference) findPreference(AppDataDefs.PREF_KEY_FOLDER);
		folderPreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getBaseContext());
						SharedPreferences.Editor editor = prefs.edit();

						String changedName = prefs.getString(AppDataDefs.PREF_KEY_FOLDER,
								"tripDiary");
						TripDiaryLogger.logDebug("Changed folder name is "
								+ changedName);
						editor.putString(AppDataDefs.PREF_KEY_FOLDER, changedName);
						editor.commit();
						return true;
					}
				});

		videoLengthPreference = (Preference) findPreference(AppDataDefs.PREF_KEY_VIDEOLEN);
		videoLengthPreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getBaseContext());
						SharedPreferences.Editor editor = prefs.edit();

						String changedLength = prefs.getString(
								AppDataDefs.PREF_KEY_VIDEOLEN, "10");
						Integer changedLengthVal = 10;
						try {
							changedLengthVal = Integer.parseInt(changedLength);
						} catch (NumberFormatException e) {
							TripDiaryLogger
									.logError("Number format exception while configuring trip video max. length"
											+ e.getMessage());
							changedLengthVal = 10;
						}

						TripDiaryLogger.logDebug("Changed duration is "
								+ changedLengthVal);
						editor.putString(AppDataDefs.PREF_KEY_VIDEOLEN, changedLengthVal.toString());
						editor.commit();
						return true;
					}
				});
		
		
		videoQualityPreference = (Preference) findPreference(AppDataDefs.PREF_KEY_VIDEOQUALITY);
		videoQualityPreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getBaseContext());
						SharedPreferences.Editor editor = prefs.edit();

						String quality = prefs.getString(
								AppDataDefs.PREF_KEY_VIDEOQUALITY, "0");
						Integer qualityVal = 10;
						try {
							qualityVal = Integer.parseInt(quality);
						} catch (NumberFormatException e) {
							TripDiaryLogger
									.logError("Number format exception while configuring video quality"
											+ e.getMessage());
							qualityVal = 0;
						}

						TripDiaryLogger.logDebug("Changed duration is "
								+ qualityVal);
						editor.putString(AppDataDefs.PREF_KEY_VIDEOQUALITY, qualityVal.toString());
						editor.commit();
						return true;
					}
				});
		
		trackMinDistancePreference = (Preference) findPreference(AppDataDefs.PREF_KEY_TRACKMINDIST);
		trackMinDistancePreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(getBaseContext());
						SharedPreferences.Editor editor = prefs.edit();

						String minDist = prefs.getString(
								AppDataDefs.PREF_KEY_TRACKMINDIST, "1");
						Integer minDistVal = 1;
						try {
							minDistVal = Integer.parseInt(minDist);
						} catch (NumberFormatException e) {
							TripDiaryLogger
									.logError("Number format exception while configuring track minimum distance"
											+ e.getMessage());
							minDistVal = 1;
						}

						TripDiaryLogger.logDebug("Changed minimum distance is "
								+ minDistVal);
						editor.putString(AppDataDefs.PREF_KEY_TRACKMINDIST, minDistVal.toString());
						editor.commit();
						return true;
					}
				});
	}
}