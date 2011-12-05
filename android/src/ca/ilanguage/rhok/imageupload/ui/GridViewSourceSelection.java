package ca.ilanguage.rhok.imageupload.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import ca.ilanguage.rhok.imageupload.R;
import ca.ilanguage.rhok.imageupload.db.ImageUploadHistoryDatabase.ImageUploadHistory;
import ca.ilanguage.rhok.imageupload.pref.PreferenceConstants;

public class GridViewSourceSelection extends Activity {

	private String mImageFilename;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gridview);

		try {
			mImageFilename = getIntent().getExtras().getString(
					PreferenceConstants.EXTRA_IMAGEFILE_FULL_PATH);
		} catch (Exception e) {
			// TODO: handle exception

		}

		GridView gridview = (GridView) findViewById(R.id.gridview);
		gridview.setAdapter(new GridImageAdapter(this));

		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Pair<Integer, String> p = (Pair<Integer, String>) parent
						.getItemAtPosition(position);
				Toast.makeText(
						GridViewSourceSelection.this,
						"" + position + " " + p.first + " " + p.second + " "
								+ id, Toast.LENGTH_SHORT).show();
			}
		});
	}

	Pair<Integer, String>[] query() {
		Cursor cursor = managedQuery(ImageUploadHistory.CONTENT_URI, null,
				null, null, null);
		List<Pair<Integer, String>> result = new ArrayList<Pair<Integer, String>>();
		int filepathColumn = cursor.getColumnIndexOrThrow("filepath");

		while (!cursor.isAfterLast()) {
			String filepath = cursor.getString(filepathColumn);
			if (cursor.getString(1).endsWith("source")) {
				result
						.add(new Pair<Integer, String>(cursor.getInt(0),
								filepath));
			}
			cursor.moveToNext();
		}
		return (Pair<Integer, String>[]) result.toArray(new Pair[] {});
	}
}
