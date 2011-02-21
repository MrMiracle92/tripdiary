package com.google.code.p.tripdiary;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * 
 * @author Arpita Saha
 * 
 */
public class AudioRecorder extends Activity {
	private MediaRecorder mediaRecorder;
	Bundle mySavedInstanceState;

	public void onCreate(Bundle savedInstanceState) {
		Toast.makeText(getBaseContext(), "onCreate on audiorecorder",
				Toast.LENGTH_SHORT).show();
		super.onCreate(savedInstanceState);

		mySavedInstanceState = savedInstanceState;

		setContentView(R.layout.trip_audio_record);

		Button recordButton = (Button) findViewById(R.id.audioStart);

		// recordButton.setOnClickListener(new OnClickListener() {
		// public void onClick(View v) {
		// startRecord("/sdcard/audiorecordexample.3gpp");
		// }
		// });

		// recordbutton.setOnClickListener(new OnClickListener() {
		// public void onClick(View v) {
		// startRecord("/sdcard/audiorecordexample.3gpp");
		// }
		// }
		// );

		Button stopbutton = (Button) findViewById(R.id.audioStop);

		// stopbutton.setOnClickListener(new OnClickListener() {
		// public void onClick(View v) {
		// stopRecord();
		// }
		// });
	}

	public void startRecord(View view) {
		String state = android.os.Environment.getExternalStorageState();
		if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
			Log.d("ARPITA", "SD Card is not mounted.  It is " + state + ".");
			return;
		}

		String filePath = "/sdcard/audiorecordexample.3gpp";
		try {
			File mediafile = new File(filePath);
			if (mediafile.exists()) {
				mediafile.delete();
			}
			mediafile = null;

			// record button goes away
			Button button = (Button) findViewById(R.id.audioStart);
			button.setVisibility(View.GONE);
			// stop button shows up
			Button stopbutton = (Button) findViewById(R.id.audioStop);
			stopbutton.setVisibility(View.VISIBLE);

			// set up media recorder
			if (mediaRecorder == null)
				mediaRecorder = new MediaRecorder();
			mediaRecorder.reset();
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			// mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			// mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			mediaRecorder.setOutputFile(filePath);

			// prepare media recorder
			mediaRecorder.prepare();
			// start media recorder
			mediaRecorder.start();
		} catch (Exception e) { // TODO Change to IllegalStateException ,
								// IOException
			Log.d("ARPITA", "Exception while recording audio " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void stopRecord(View view) {
		try {
			// stop media recorder
			if (mediaRecorder != null) {
				mediaRecorder.stop();
				// reset media recorder
				mediaRecorder.reset();
				mediaRecorder = null;
			} else {

			}

			// stop button goes away
			Button stopbutton = (Button) findViewById(R.id.audioStop);
			stopbutton.setVisibility(View.GONE);

			view.invalidate();

			setResult(RESULT_OK);
			finish();

		} catch (Exception e) {
			Log.d("ARPITA",
					"Exception while stopping audio recording "
							+ e.getMessage());
			e.printStackTrace();
		}
	}
}