package ca.ilanguage.rhok.imageupload.ui;

import java.io.File;

import ca.ilanguage.rhok.imageupload.R;
import ca.ilanguage.rhok.imageupload.db.ImageUploadHistoryDatabase.ImageUploadHistory;
import ca.ilanguage.rhok.imageupload.pref.PreferenceConstants;
import ca.ilanguage.rhok.imageupload.service.TakePicture;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainPortal extends Activity {
<<<<<<< HEAD

	private static final String EXTRA_WATER_SOURCE_CODE = null;
	private String imageSourceCodeFileName = "";
	private String imageFileName = "";

=======
	private static final String TAG = "AndroidBacterialCountingMain";
	private String mOutputDir="";
	private String mSampleCodeCount="0";
	private String mSampleId ="0";
	private String mExperimenterCode = "AA";
	public static final int WATER_SOURCE = 1;
>>>>>>> ca9b2344d113a1553d49fdaa78b0783a8ac4ba25
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
		mOutputDir = prefs.getString(PreferenceConstants.OUTPUT_IMAGE_DIRECTORY, "/sdcard/BacteriaCounting/watersamples/");
		mSampleId = prefs.getString(PreferenceConstants.PREFERENCE_WATER_SAMPLE_ID, "unkown");
		mExperimenterCode = prefs.getString(PreferenceConstants.PREFERENCE_EXPERIMENTER_ID, "AA");
		
		saveStateToPreferences();
	}

	/**
	 * Creates a new database entry for the image and launches the take picture activity with this info
	 * 
	 * @param v
	 */
	public void onWaterSourceClick(View v) {
		Intent intent = new Intent(this, TakePicture.class);
		intent.putExtra(EXTRA_WATER_SOURCE_CODE, imageSourceCodeFileName);

		Uri uri = getContentResolver().insert(ImageUploadHistory.CONTENT_URI,
				null);
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
			intent.putExtra(PreferenceConstants.EXTRA_IMAGEFILE_FULL_PATH, mOutputDir+System.currentTimeMillis()+mExperimenterCode+mSampleCodeCount+"_source.jpg");
			startActivityForResult(intent, WATER_SOURCE);
			
		}
		
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		SharedPreferences prefens = getSharedPreferences(
				PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
		switch (requestCode) {
		case WATER_SOURCE:
			
			if (mSampleCodeCount == null){
				mSampleCodeCount="0";
			}
			Toast.makeText(getApplicationContext(),
					"TODO Display water sample code to write on the petri dish.",
					Toast.LENGTH_LONG).show();
			break;
		default:
			break;

		}
	}

	public void onWaterResultsClick(View v) {
		Intent intent = new Intent(this, TakePicture.class);
		intent.putExtra(EXTRA_WATER_SOURCE_CODE, imageFileName );
		startActivity(intent);
	}

	public void onSyncServerClick(View v) {
		startActivity(new Intent(this, ServerSync.class));
	}

	private void saveStateToPreferences(){
		if(mExperimenterCode != null || mSampleCodeCount != null){
			mSampleId = mExperimenterCode + mSampleCodeCount;
		}else{
			mSampleId = "unknown";
		}
		SharedPreferences prefs = getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
    	editor.putString(PreferenceConstants.PREFERENCE_WATER_SAMPLE_ID,mSampleId);
    	editor.commit();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		saveStateToPreferences();
		super.onDestroy();
	}
	

}