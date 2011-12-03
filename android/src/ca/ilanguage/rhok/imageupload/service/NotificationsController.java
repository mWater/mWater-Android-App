package ca.ilanguage.rhok.imageupload.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Controller to start and stop a service.
 * 
 * Demonstrates how to pass information to the service via extras
 * 
 * Clicking on the notification brings user here, this is where user can do
 * extra actions, like schedule uplaods for later, import transcriptions into
 * aublog? TODO button to stop recording. (sent from dictationrecorderservice)
 * add buttons Turn on wifi Open aublog settings Retry xxx audio file (add files
 * to cue)
 */

public class NotificationsController extends Activity {
	private Uri mUri;
	private String mAuBlogInstallId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUri = getIntent().getData();
		setContentView(R.layout.notifying_controller);

		Button button = (Button) findViewById(R.id.notifyStart);
		button.setOnClickListener(mStartListener);
		button = (Button) findViewById(R.id.notifyStop);
		button.setVisibility(Button.INVISIBLE);
		button.setOnClickListener(mStopListener);

		SharedPreferences prefs = getSharedPreferences(
				PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
		mAuBlogInstallId = prefs.getString(
				PreferenceConstants.AUBLOG_INSTALL_ID, "0");

	}

	@Override
	protected void onDestroy() {
		String release = Build.VERSION.RELEASE;
		super.onDestroy();
		/*
		 * if(release.equals("2.2")){ //this does not show a force close, but
		 * does sucessfully allow the user to disconnect the bluetooth after
		 * they close aublog. //if they have android 2.2 and they disconnect the
		 * bluetooth without quitting aublog then the device will reboot.
		 * android.os.Process.killProcess(android.os.Process.myPid()); }else{
		 * //do nothing, bluetooth issue is fixed in 2.2.1 and above }
		 */
	}

	private OnClickListener mStartListener = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(NotificationsController.this,
					DictationRecorderService.class);
			stopService(intent);
		}
	};

	private OnClickListener mStopListener = new OnClickListener() {
		public void onClick(View v) {

			Intent i = new Intent(
					EditBlogEntryActivity.DICTATION_STILL_RECORDING_INTENT);
			i.setData(mUri);
			sendBroadcast(i);
		}
	};
}
