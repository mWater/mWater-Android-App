package ca.ilanguage.rhok.imageupload.ui;

import java.io.File;
import java.util.UUID;

import ca.ilanguage.rhok.imageupload.App;
import ca.ilanguage.rhok.imageupload.R;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SampleListActivity extends ListActivity {
	private static final String TAG = "ca.ilanguage.rhok";
	static final String[] Samples = new String[] { "Water Source #1",
			"Street #3 Sample" };

	static int PETRI_IMAGE_REQUEST = 1;
	static int PROCESS_IMAGE_REQUEST = 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samplelist);
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, Samples));
	}

	public void onNewSampleClick(View v) {
		Intent intent = new Intent(this, PetrifilmCameraActivity.class);
		String guid=UUID.randomUUID().toString();
		intent.putExtra("filename", "petri_" + guid + ".jpg");
		startActivityForResult(intent, PETRI_IMAGE_REQUEST);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Toast.makeText(getApplicationContext(), "Would open sample data for viewing", 0).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PETRI_IMAGE_REQUEST && resultCode == RESULT_OK) {
			String filename = data.getStringExtra("filename");

			Intent intent = new Intent(this, ProcessImageActivity.class);
			intent.putExtra("inpath", App.getOriginalImageFolder(this) + File.separator + filename);
			intent.putExtra("outpath", App.getProcessedImageFolder(this) + File.separator + filename);

			Log.d(TAG, "Calling process image");
			startActivityForResult(intent, PROCESS_IMAGE_REQUEST);
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

}
