package ca.ilanguage.rhok.imageupload.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import ca.ilanguage.rhok.imageupload.PetrifilmAnalysisResults;
import ca.ilanguage.rhok.imageupload.PetrifilmImageProcessor;
import ca.ilanguage.rhok.imageupload.R;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SampleListActivity extends ListActivity {
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
		Intent intent = new Intent(this, PetrifilmSnapActivity.class);
		String guid=UUID.randomUUID().toString();
		File file = new File(getExternalFilesDir(null), "petri_" + guid + ".jpg");
		intent.putExtra("filepath", file.getAbsolutePath());
		intent.putExtra("guid", guid);

		startActivityForResult(intent, PETRI_IMAGE_REQUEST);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Toast.makeText(getApplicationContext(), "Would open sample data for viewing", 0).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PETRI_IMAGE_REQUEST && resultCode == RESULT_OK) {
			String guid = data.getStringExtra("guid");
			String filepath = data.getStringExtra("filepath");

			Intent intent = new Intent(this, ProcessImageActivity.class);
			intent.putExtra("inpath", filepath);

			File file = new File(getExternalFilesDir(null), "processed_" + guid + ".jpg");
			intent.putExtra("outpath", file.getAbsolutePath());

			startActivityForResult(intent, PROCESS_IMAGE_REQUEST);
		}

		if (requestCode == PROCESS_IMAGE_REQUEST && resultCode == RESULT_OK) {
			String outpath = data.getStringExtra("outpath");

			// Launch image viewer
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + outpath), "image/*");
			startActivity(intent);
		}
	}

}
