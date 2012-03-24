//package ca.ilanguage.rhok.imageupload.service;
//
//import ca.ilanguage.rhok.imageupload.R;
//import ca.ilanguage.rhok.imageupload.pref.PreferenceConstants;
//import ca.ilanguage.rhok.imageupload.ui.MainPortal;
//import android.app.Activity;
//import android.content.ComponentName;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//
///**
// * Controller to start and stop a service.
// * 
// * Demonstrates how to pass information to the service via extras
// * 
// * Clicking on the notification brings user here, this is where user can do
// * extra actions
// */
//
//public class NotificationsController extends Activity {
//	private Uri mUri;
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//
//		mUri = getIntent().getData();
//		setContentView(R.layout.notifying_controller);
//
//		Button button = (Button) findViewById(R.id.notifyStart);
//		button.setOnClickListener(mStartListener);
//		button = (Button) findViewById(R.id.notifyStop);
//		button.setVisibility(Button.INVISIBLE);
//		button.setOnClickListener(mStopListener);
//
//	}
//
//	@Override
//	protected void onDestroy() {
//		String release = Build.VERSION.RELEASE;
//		super.onDestroy();
//	
//	}
//
//	private OnClickListener mStartListener = new OnClickListener() {
//		public void onClick(View v) {
//			Intent intent = new Intent(NotificationsController.this,
//					MainPortal.class);
//			stopService(intent);
//		}
//	};
//
//	private OnClickListener mStopListener = new OnClickListener() {
//		public void onClick(View v) {
//			//TODO
//		}
//	};
//}
