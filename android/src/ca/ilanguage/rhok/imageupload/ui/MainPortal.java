package ca.ilanguage.rhok.imageupload.ui;

import java.io.File;
import java.util.List;

import ca.ilanguage.rhok.imageupload.R;
import ca.ilanguage.rhok.imageupload.db.ImageUploadHistoryDatabase.ImageUploadHistory;
import ca.ilanguage.rhok.imageupload.pref.PreferenceConstants;
import ca.ilanguage.rhok.imageupload.pref.SetPreferencesActivity;
import ca.ilanguage.rhok.imageupload.service.ImageUploadService;
import ca.ilanguage.rhok.imageupload.service.TakePicture;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainPortal extends Activity {

	private static final String EXTRA_WATER_SOURCE_CODE = null;
	private String imageSourceCodeFileName = "";
	private String mImageFileName = "";

	private static final String TAG = "AndroidBacterialCountingMain";
	private String mOutputDir = "";
	private String mSampleCodeCount = "0";
	private String mSampleId = "0";
	private String mExperimenterCode = "AA";
	public static final int WATER_SOURCE = 1;
	private static final int SWITCH_LANGUAGE = 2;
	private Menu mMenu;
	private Uri mUri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		SharedPreferences prefs = getSharedPreferences(
				PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
		mOutputDir = prefs.getString(
				PreferenceConstants.OUTPUT_IMAGE_DIRECTORY,
				"/sdcard/BacteriaCounting/watersamples/");
		mSampleId = prefs.getString(
				PreferenceConstants.PREFERENCE_WATER_SAMPLE_ID, "unkown");
		mExperimenterCode = prefs.getString(
				PreferenceConstants.PREFERENCE_EXPERIMENTER_ID, "AA");

		saveStateToPreferences();
	}

	/**
	 * Creates a new database entry for the image and launches the take picture
	 * activity with this info
	 * 
	 * @param v
	 */
	public void onWaterSourceClick(View v) {
		Intent intent = new Intent(this, TakePicture.class);
		intent.putExtra(EXTRA_WATER_SOURCE_CODE, mImageFileName);

		Uri uri = getContentResolver().insert(ImageUploadHistory.CONTENT_URI,
				null);
		if (uri != null){
			mUri = uri;
		}
		mSampleCodeCount = uri.getLastPathSegment();

		// If we were unable to create a new db entry, then just finish
		// this activity. A RESULT_CANCELED will be sent back to the
		// original activity if they requested a result.
		if (uri == null || mOutputDir.length() < 3) {
			Log.e(TAG, "Failed to insert new image entry into "
					+ getIntent().getData());
		} else {
			intent.setData(uri);
			new File(mOutputDir).mkdirs();
			mImageFileName = mOutputDir+System.currentTimeMillis()+mExperimenterCode+mSampleCodeCount+"_source.jpg";
			intent.putExtra(PreferenceConstants.EXTRA_IMAGEFILE_FULL_PATH, mImageFileName);
			startActivityForResult(intent, WATER_SOURCE);

		}

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		SharedPreferences prefens = getSharedPreferences(
				PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
		switch (requestCode) {
		case WATER_SOURCE:

			if (mSampleCodeCount == null) {
				mSampleCodeCount = "0";
			}
			Toast.makeText(
					getApplicationContext(),
					"TODO Display water sample code to write on the petri dish.",
					Toast.LENGTH_LONG).show();
			break;
		case SWITCH_LANGUAGE:
			//TODO
			break;
		default:
			break;

		}
	}

	public void onWaterResultsClick(View v) {
                Intent intent = new Intent(this, GridViewSourceSelection.class);
                intent.putExtra(EXTRA_WATER_SOURCE_CODE, mImageFileName );
                startActivity(intent);
		//Use this button temporarily to test the OpenCV activity on your machine:
//		Intent intent = new Intent(this, Sample3Native.class);
//		startActivity(intent);
	}

	public void onSyncServerClick(View v) {

		Intent intent = new Intent(this, ImageUploadService.class);
		if (mUri != null){
			intent.setData(mUri);
		}
		intent.putExtra(PreferenceConstants.EXTRA_IMAGEFILE_FULL_PATH, mImageFileName);
		startService(intent); 
		//TODO put the logic in this class later, for now this is just so teh server side cand ebug the connection
		//startActivity(new Intent(this, ServerSync.class));
	}

	private void saveStateToPreferences() {
		if (mExperimenterCode != null || mSampleCodeCount != null) {
			mSampleId = mExperimenterCode + mSampleCodeCount;
		} else {
			mSampleId = "unknown";
		}
		SharedPreferences prefs = getSharedPreferences(
				PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PreferenceConstants.PREFERENCE_WATER_SAMPLE_ID,
				mSampleId);
		editor.commit();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		saveStateToPreferences();
		super.onDestroy();
	}


	public boolean onCreateOptionsMenu(Menu menu) {
		// Hold on to this
		mMenu = menu;

		// Inflate the currently selected menu XML resource.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_menu, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// For "Title only": Examples of matching an ID with one assigned in
		// the XML
		case R.id.open_settings:

			Intent i = new Intent(getBaseContext(),
					SetPreferencesActivity.class);
			startActivity(i);
			return true;
		case R.id.language_settings:
			Intent inte = new Intent(getBaseContext(),
					SetPreferencesActivity.class);
			startActivityForResult(inte, SWITCH_LANGUAGE);
			return true;
		case R.id.result_folder:
			final boolean fileManagerAvailable = isIntentAvailable(this,
					"org.openintents.action.PICK_FILE");
			if (!fileManagerAvailable) {
				Toast.makeText(
						getApplicationContext(),
						"To open and export recorded files or "
								+ "draft data you can install the OI File Manager, "
								+ "it allows you to browse your SDCARD directly on your mobile device.",
								Toast.LENGTH_LONG).show();
				Intent goToMarket = new Intent(Intent.ACTION_VIEW)
				.setData(Uri
						.parse("market://details?id=org.openintents.filemanager"));
			} else {
				Intent openResults = new Intent(
						"org.openintents.action.PICK_FILE");
				openResults.setData(Uri.parse("file://"
						+ mOutputDir));
				startActivity(openResults);
			}

			break;
		case R.id.issue_tracker:

			Intent browserIntent = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("https://github.com/AndroidImageProcessing/AndroidBacteriaImageProcessing/issues"));
			startActivity(browserIntent);
			return true;
		default:
			// Do nothing

			break;
		}

		return false;
	}

	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

}
