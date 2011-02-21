package com.google.code.p.tripdiary;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

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

	private final String TAG = "AudioRecorder";
	private final String DEFAULT_FILE_EXTENSION = ".3gpp";
	private final String DEFAULT_FILE_NAME = "tripDiaryAudioRecord";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mySavedInstanceState = savedInstanceState;

		setContentView(R.layout.trip_audio_record);

		ImageButton recordButton = (ImageButton) findViewById(R.id.audioStart);
		recordButton.setVisibility(View.VISIBLE);

		ImageButton stopFadedButton = (ImageButton) findViewById(R.id.audioStopFaded);
		stopFadedButton.setVisibility(View.VISIBLE);
		
		ImageButton startFadedButton = (ImageButton) findViewById(R.id.audioStartFaded);
		startFadedButton.setVisibility(View.GONE);
		
		ImageButton stopbutton = (ImageButton) findViewById(R.id.audioStop);
		stopbutton.setVisibility(View.GONE);

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
			Log.d(TAG, "SD Card is not mounted.  It is " + state + ".");
			return;
		}

		filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/Sounds" + "/" + DEFAULT_FILE_NAME + "_"
				+ TripViewActivity.getMediaFileName() + DEFAULT_FILE_EXTENSION;
		try {
			File mediafile = new File(filePath);
			if (mediafile.exists()) {
				mediafile.delete();
			}
			mediafile = null;
			
			// record button goes away
			ImageButton startbutton = (ImageButton) findViewById(R.id.audioStart);
			startbutton.setVisibility(View.GONE);
			
			ImageButton stopFadedButton = (ImageButton) findViewById(R.id.audioStopFaded);
			stopFadedButton.setVisibility(View.GONE);
			
			ImageButton startFadedButton = (ImageButton) findViewById(R.id.audioStartFaded);
			startFadedButton.setVisibility(View.VISIBLE);

			// stop button shows up
			ImageButton stopbutton = (ImageButton) findViewById(R.id.audioStop);
			stopbutton.setVisibility(View.VISIBLE);

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
			Log.d(TAG, "Exception while recording audio " + e.getMessage());
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

			// stop button goes away
			ImageButton stopbutton = (ImageButton) findViewById(R.id.audioStop);
			stopbutton.setVisibility(View.GONE);

			Intent data = new Intent();
			data.putExtra("returnKey", filePath);
			this.setResult(RESULT_OK, data);

			finish();

		} catch (Exception e) {
			Log.d(TAG,
					"Exception while stopping audio recording "
							+ e.getMessage());
			e.printStackTrace();
		}
	}
}