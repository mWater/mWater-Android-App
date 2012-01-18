package ca.ilanguage.rhok.imageupload.ui;

import ca.ilanguage.rhok.imageupload.R;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SampleListActivity extends ListActivity {
	static final String[] Samples = new String[] { "New Sample",
			"Water Source 1", "Street 3" };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.samplelist);
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, Samples));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position == 0) {
			Intent intent = new Intent(this, PetrifilmSnapActivity.class);
			startActivity(intent);
		}
	}
}
