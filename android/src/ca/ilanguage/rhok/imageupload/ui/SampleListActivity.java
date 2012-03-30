package ca.ilanguage.rhok.imageupload.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import ca.ilanguage.rhok.imageupload.App;
import ca.ilanguage.rhok.imageupload.PetriFilmProcessingIntentService;
import ca.ilanguage.rhok.imageupload.R;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SampleListActivity extends ListActivity {
	private static final String TAG = "ca.ilanguage.rhok";
	public static final int POPULATING_DIALOG = 54;

	static String[] samples = new String[] { "Water Source #1",
			"Street #3 Sample" };

	static int PETRI_IMAGE_REQUEST = 1;
	static int PROCESS_IMAGE_REQUEST = 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samplelist);
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, samples));
		new PopulateSamplesListTask().execute(null);

	}

	public void onNewSampleClick(View v) {
		Intent intent = new Intent(this, PetrifilmCameraActivity.class);
		String guid = UUID.randomUUID().toString();
		intent.putExtra("filename", "petri_" + guid + ".jpg");
		startActivityForResult(intent, PETRI_IMAGE_REQUEST);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this, SampleDetailsActivity.class);
		intent.putExtra("filename", samples[position].replace("Pending - ", "").replace("Processed - ", "")+".jpg");
		Log.d(TAG, "Showing processed image "+samples[position]);
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PETRI_IMAGE_REQUEST && resultCode == RESULT_OK) {
			String filename = data.getStringExtra("filename");

			// Send image to be processed and saved
			Intent intent = new Intent(this,
					PetriFilmProcessingIntentService.class);
			intent.putExtra("inpath", App.getOriginalImageFolder(this)
					+ File.separator + filename);
			intent.putExtra("outimage", filename);
			Log.d(TAG, "Calling process image");
			startService(intent);
			new PopulateSamplesListTask().execute(null);
		}

		if (requestCode == PROCESS_IMAGE_REQUEST && resultCode == RESULT_OK) {
			Log.d(TAG, "Called process image");
			String outpath = data.getStringExtra("outpath");

			// Launch image viewer
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + outpath), "image/*");
			startActivity(intent);
		}
	}

	public class PopulateSamplesListTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			File dir = new File(
					App.getOriginalImageFolder(getApplicationContext()));
			samples = dir.list();
			for(int s = 0; s<samples.length; s++){
				String status = "Pending - ";
				File results = new File(App.getResultsFolder(getApplicationContext()) + File.separator + samples[s].replace(".jpg", ".xml"));
				if(results.exists()){
					status = "Processed - ";
				}
				samples[s] = status + samples[s].replace(".jpg", "");
			}
			return true;
		}

		protected void onPreExecute() {
			showDialog(POPULATING_DIALOG);
		}

		protected void onPostExecute(Boolean result) {
			dismissDialog(POPULATING_DIALOG);
			setListAdapter(new ArrayAdapter<String>(getApplicationContext(),
					android.R.layout.simple_list_item_1, samples));
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		if (id == POPULATING_DIALOG) {
			dialog = new ProgressDialog.Builder(this).setCancelable(true)
					.setTitle("Please wait")
					.setMessage("Populating samples, this may take a moment.")
					.create();
			return dialog;
		} else {
			dialog = super.onCreateDialog(id);
		}
		return dialog;
	}
}
