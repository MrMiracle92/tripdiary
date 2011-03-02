package com.google.code.p.tripdiary;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.code.p.tripdiary.utils.Util;

/**
 * Activity which records audio Another way of capturing audio is // Using
 * intent Intent recordIntent = new
 * Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION); String fileName =
 * "/sdcard/" + getMediaFileName() + "test.amr"; File file = new File(fileName);
 * recordIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
 * startActivityForResult(recordIntent, REQUEST_AUDIO);
 * 
 * However, this does not work for us as no intent is returned to the caller
 * activity. We need that to extract the path where the audio is stored.
 * 
 * 
 * Default path for saved audio clips :
 * /sdcard/Sounds/tripDiaryAudioRecord_<time>.3gpp
 * 
 * @author Arpita Saha
 * 
 */
public class AudioRecorder extends Activity {
	private MediaRecorder mediaRecorder;
	Bundle mySavedInstanceState;
	String filePath;

	private final String DEFAULT_FILE_EXTENSION = ".3gpp";
	private final String DEFAULT_FILE_NAME = "tripDiaryAudioRecord";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can not write
			Toast.makeText(getBaseContext(),
					"External storage not available for writing.",
					Toast.LENGTH_SHORT).show();
			this.setResult(RESULT_CANCELED);
			finish();
			return;
		}

		mySavedInstanceState = savedInstanceState;

		setContentView(R.layout.trip_audio_record);

		// Start button shows up
		ImageButton startButton = (ImageButton) findViewById(R.id.audioStart);
		startButton.setVisibility(View.VISIBLE);

		// Faded stop button shows up
		ImageButton stopFadedButton = (ImageButton) findViewById(R.id.audioStopFaded);
		stopFadedButton.setVisibility(View.VISIBLE);

		// faded start button goes away
		ImageButton startFadedButton = (ImageButton) findViewById(R.id.audioStartFaded);
		startFadedButton.setVisibility(View.GONE);

		// stop button goes away
		ImageButton stopButton = (ImageButton) findViewById(R.id.audioStop);
		stopButton.setVisibility(View.GONE);

		// Create the default file storage path
		File dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/Sounds");
		if (!dir.exists()) // first time
		{
			dir.mkdir();
		}
	}

	public void startRecord(View view) {
		String state = android.os.Environment.getExternalStorageState();
		if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
			Toast.makeText(getBaseContext(),
					"SD Card is not mounted.  It is " + state + ".",
					Toast.LENGTH_SHORT).show();
			TripDiaryLogger.logDebug( "SD Card is not mounted.  It is " + state + ".");
			return;
		}

		filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/Sounds" + "/" + DEFAULT_FILE_NAME + "_"
				+ Util.tripDiaryFileName() + DEFAULT_FILE_EXTENSION;
		try {
			File mediafile = new File(filePath);
			if (mediafile.exists()) {
				mediafile.delete();
			}
			mediafile = null;

			// Start button goes away
			ImageButton startbutton = (ImageButton) findViewById(R.id.audioStart);
			startbutton.setVisibility(View.GONE);

			// Faded Stop button goes away
			ImageButton stopFadedButton = (ImageButton) findViewById(R.id.audioStopFaded);
			stopFadedButton.setVisibility(View.GONE);

			// Faded Start button shows up
			ImageButton startFadedButton = (ImageButton) findViewById(R.id.audioStartFaded);
			startFadedButton.setVisibility(View.VISIBLE);

			// Stop button shows up
			ImageButton stopButton = (ImageButton) findViewById(R.id.audioStop);
			stopButton.setVisibility(View.VISIBLE);

			// set up media recorder
			if (mediaRecorder == null)
				mediaRecorder = new MediaRecorder();

			mediaRecorder.reset();

			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mediaRecorder.setOutputFile(filePath);

			// prepare media recorder
			mediaRecorder.prepare();

			// start media recorder
			mediaRecorder.start();
		} catch (Exception e) { // TODO Change to IllegalStateException ,
								// IOException
			TripDiaryLogger.logDebug( "Exception while recording audio " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void stopRecord(View view) {
		try {
			if (mediaRecorder != null) {
				// stop media recorder
				mediaRecorder.stop();

				// reset media recorder
				mediaRecorder.reset();

				// release the recorder
				mediaRecorder.release();

				mediaRecorder = null;
			} else {

			}

			// Faded Start button goes away
			ImageButton startFadedButton = (ImageButton) findViewById(R.id.audioStartFaded);
			startFadedButton.setVisibility(View.GONE);

			// stop button goes away
			ImageButton stopButton = (ImageButton) findViewById(R.id.audioStop);
			stopButton.setVisibility(View.GONE);

			Intent data = new Intent();
			data.putExtra("returnKey", filePath);
			this.setResult(RESULT_OK, data);

			finish();

		} catch (Exception e) {
			TripDiaryLogger.logDebug(
					"Exception while stopping audio recording "
							+ e.getMessage());
			e.printStackTrace();
		}
	}
}