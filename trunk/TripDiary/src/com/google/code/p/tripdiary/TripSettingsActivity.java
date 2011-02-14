package com.google.code.p.tripdiary;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

/**
 * This activity is used to create and edit trip settings.
 * 
 * @author Ankan Mukherjee
 */
public class TripSettingsActivity extends Activity {
//	private final String TAG = "TripSettingsActivity";

	public final static String KEY_TRIP_ID = "tripId";

	private final int DIALOG_INVALID_INPUT_NAME_REQUIRED = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trip_settings);

		if (savedInstanceState != null
				&& savedInstanceState.containsKey(KEY_TRIP_ID)) {
			// TODO: fill up the name, description created, updated, title etc..
		} else {
			// this is a new trip
			findViewById(R.id.tvCreated).setVisibility(View.GONE);
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
			// create new trip or update trip
			// set current trip (?)
			// finish with result ok
			finish();
		}
	}
}
