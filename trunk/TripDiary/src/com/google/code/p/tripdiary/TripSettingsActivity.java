package com.google.code.p.tripdiary;

import java.sql.Date;
import java.text.DateFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity is used to create and edit trip settings.
 * 
 * @author Ankan Mukherjee
 */
public class TripSettingsActivity extends Activity {
//	private final String TAG = "TripSettingsActivity";
	
	public final static String KEY_TRIP_ID = "tripId";
	public final static String KEY_IS_NEW_TRIP = "newTripFlag";

	private TripStorageManager storageMgr;
	private long mThisTripId = 0;
	private boolean mIsNewTrip = false;
	
	private final int DIALOG_INVALID_INPUT_NAME_REQUIRED = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trip_settings);
		
		storageMgr = TripStorageManagerFactory.getTripStorageManager();
		DateFormat df = DateFormat.getDateInstance();
		
		// if the activity is resumed
		if (savedInstanceState != null) {
			mThisTripId = savedInstanceState.getLong(KEY_TRIP_ID,
					AppDataDefs.NO_CURRENT_TRIP);
			mIsNewTrip = savedInstanceState.getBoolean(KEY_IS_NEW_TRIP, false);
		} else {
			// if there is a bundle get details from there
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				mThisTripId = extras.getLong(KEY_TRIP_ID,
						AppDataDefs.NO_CURRENT_TRIP);
				mIsNewTrip = extras.getBoolean(KEY_IS_NEW_TRIP, false);
			}
		}

		if (!mIsNewTrip) {
			// this is an existing trip to edit
			TripDetail td = storageMgr.getTripDetail(mThisTripId);
			if(td == null) {
				// no trip with the given trip id exists
				Toast toast = Toast.makeText(getApplicationContext(), "Trip with id " + mThisTripId + " not found.", Toast.LENGTH_SHORT);
				toast.show();
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
			((TextView)findViewById(R.id.tvCreated)).setText(df.format(new Date(td.getCreateTime())));
			((TextView)findViewById(R.id.tvLastUpdated)).setText(df.format(new Date(storageMgr.getLastUpdatedTime(mThisTripId))));
			((TextView)findViewById(R.id.edName)).setText(td.getName());
			((TextView)findViewById(R.id.edDescription)).setText(td.getTripDescription());
		} else {
			// this is a new trip
			findViewById(R.id.tvCreatedLbl).setVisibility(View.GONE);
			findViewById(R.id.tvCreated).setVisibility(View.GONE);
			findViewById(R.id.tvLastUpdatedLbl).setVisibility(View.GONE);
			findViewById(R.id.tvLastUpdated).setVisibility(View.GONE);
			findViewById(R.id.settingsTripImage).setVisibility(View.GONE);
		}

		findViewById(R.id.btnOk).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				validateAndFinish();
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_INVALID_INPUT_NAME_REQUIRED:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Name cannot be empty.")
					.setCancelable(false)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// do nothing here
								}
							});
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	private void validateAndFinish() {
		EditText nameEditText = (EditText) findViewById(R.id.edName);
		String name = nameEditText.getText().toString();
		if (name.equals("")) {
			showDialog(DIALOG_INVALID_INPUT_NAME_REQUIRED);
			nameEditText.requestFocus();
			return;
		} else {
			String tripDescription = ((EditText) findViewById(R.id.edDescription)).getText().toString();
			boolean traceRouteEnabled = false;
			if(mThisTripId == 0) {
				mThisTripId = storageMgr.createNewTrip(name, tripDescription, traceRouteEnabled);
				SharedPreferences.Editor editPref = getApplicationContext().getSharedPreferences(AppDataDefs.CURRENT_TRIP_ID_KEY, MODE_PRIVATE).edit();
				editPref.putLong(AppDataDefs.CURRENT_TRIP_ID_KEY, mThisTripId);
				editPref.commit();
			} else {
				storageMgr.updateTrip(mThisTripId, name, tripDescription, traceRouteEnabled, null);
			}
			// finish with result ok
			finish();
		}
	}
}
