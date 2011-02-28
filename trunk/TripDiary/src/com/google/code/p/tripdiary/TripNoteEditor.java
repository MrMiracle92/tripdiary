package com.google.code.p.tripdiary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * Captures a note entry from the user and saves in the database.
 * 
 * @author Arpita Saha
 * 
 */
public class TripNoteEditor extends Activity {
	/**
	 * A custom EditText that draws lines between each line of text that is
	 * displayed.
	 */
	public static class LinedEditText extends EditText {

		private Rect mRect;
		private Paint mPaint;

		// we need this constructor for LayoutInflater
		public LinedEditText(Context context, AttributeSet attrs) {
			super(context, attrs);

			mRect = new Rect();
			mPaint = new Paint();
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setColor(0x800000FF);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			int count = getLineCount();
			Rect r = mRect;
			Paint paint = mPaint;

			for (int i = 0; i < count; i++) {
				int baseline = getLineBounds(i, r);

				canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1,
						paint);
			}

			super.onDraw(canvas);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			setContentView(R.layout.trip_note);

			Button buttonOk = (Button) findViewById(R.id.tripNotesOkBtn);
			buttonOk.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					notesOkButtonPressed();
				}
			});

		} catch (Exception e) {
			TripDiaryLogger.logDebug("Fatal exception " + e.getMessage());
		}
	}

	private void notesOkButtonPressed() {
		// The text view for our note, identified by its ID in the XML file.
		EditText text = (EditText) findViewById(R.id.note);

		if (text != null) {
			String capturedText = text.getText().toString();

			text.setVisibility(View.GONE);
			((Button) findViewById(R.id.tripNotesOkBtn))
					.setVisibility(View.GONE);

			Intent data = new Intent();
			data.putExtra("capturedText", capturedText);

			this.setResult(RESULT_OK, data);
		} else {
			TripDiaryLogger.logDebug("No notes captured");

			// User pressed OK button - lets save this and go back
			this.setResult(RESULT_CANCELED);
		}

		finish();
	}
}
