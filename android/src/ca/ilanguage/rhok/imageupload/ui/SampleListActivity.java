package ca.ilanguage.rhok.imageupload.ui;

import java.util.UUID;

import ca.ilanguage.rhok.imageupload.R;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SampleListActivity extends ListActivity {
	static final String[] Samples = new String[] { "Water Source #1", "Street #3 Sample" };

	static int PETRI_IMAGE_REQUEST = 1;

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
		intent.putExtra("guid", UUID.randomUUID().toString());

		startActivityForResult(intent, PETRI_IMAGE_REQUEST);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PETRI_IMAGE_REQUEST && resultCode == RESULT_OK) {
			Toast.makeText(getApplicationContext(), "Captured image", 0).show();
		}
	}

}
