package com.vezeau.alex.stl;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class ActivitySTL extends Activity {
	private static final String tag = "DATABASEACTIVITY";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stl);
		
		

		Log.d(tag, "In the onCreate() event");

	}

	@Override
	protected void onStart() {
		super.onStart();

		Log.d(tag, "In the onStart() event");


	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Log.d(tag, "In the onDestroy() event");
	}

	@Override
	protected void onPause() {
		super.onPause();

		Log.d(tag, "In the onPause() event");
	}

	@Override
	protected void onRestart() {
		super.onRestart();

		Log.d(tag, "In the onRestart() event");
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.d(tag, "In the onResume() event");

	}

	@Override
	protected void onStop() {
		super.onStop();

		Log.d(tag, "In the onStop() event");
	}

}