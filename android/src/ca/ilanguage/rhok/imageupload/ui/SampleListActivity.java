package ca.ilanguage.rhok.imageupload.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ca.ilanguage.rhok.imageupload.App;
import ca.ilanguage.rhok.imageupload.PetriFilmProcessingIntentService;
import ca.ilanguage.rhok.imageupload.R;
import ca.ilanguage.rhok.imageupload.Sample;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SampleListActivity extends ListActivity {
	private static final String TAG = "ca.ilanguage.rhok";
	public static final int POPULATING_DIALOG = 54;

	List<Sample> samples;

	static int PETRI_IMAGE_REQUEST = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sample_list_activity);
		refreshList();
	}

	public void onNewSampleClick(View v) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("New Sample");
		alert.setMessage("Enter sample name");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String name = input.getText().toString();
				Intent intent = new Intent(SampleListActivity.this,
						PetrifilmCameraActivity.class);
				intent.putExtra("filename", name + ".jpg");
				startActivityForResult(intent, PETRI_IMAGE_REQUEST);
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		
		alert.show();
	}

	void refreshList() 
	{
		final Handler handler=new Handler();
		final Runnable r = new Runnable()
		{
		    public void run() 
		    {
				new PopulateSamplesListTask().execute();
		        handler.postDelayed(this, 1000);
		    }
		};

		handler.post(r);	
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Sample sample = samples.get(position);
		if (sample.processed) {
			Intent intent = new Intent(this, SampleDetailsActivity.class);
			intent.putExtra("name", sample.name);
			Log.d(TAG, "Showing processed image " + samples.get(position).name);
			startActivity(intent);
		} else {
			Toast.makeText(this, "Still processing...", Toast.LENGTH_SHORT).show();
		}
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
			new PopulateSamplesListTask().execute();
		}
	}

	public class PopulateSamplesListTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			File dir = new File(
					App.getOriginalImageFolder(getApplicationContext()));
			String[] sampleFiles = dir.list();
			samples = new ArrayList<Sample>();
			for (int s = 0; s < sampleFiles.length; s++) {
				// Trim .jpg
				Sample sample = new Sample();
				sample.name = sampleFiles[s].substring(0,
						sampleFiles[s].length() - 4);
				File results = new File(
						App.getResultsFolder(getApplicationContext()),
						sample.name + ".xml");
				if (results.exists())
					sample.processed = true;

				samples.add(sample);
			}
			return true;
		}

		protected void onPreExecute() {
			// Temporarily removing for auto-refresh. put back in when done by intent
			//showDialog(POPULATING_DIALOG);
		}

		protected void onPostExecute(Boolean result) {
			// Temporarily removing for auto-refresh. put back in when done by intent
			//dismissDialog(POPULATING_DIALOG);
			setListAdapter(new SampleAdapter(SampleListActivity.this,
					R.layout.sample_list_row, samples));
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

	class SampleAdapter extends ArrayAdapter<Sample> {
		private List<Sample> items;

		public SampleAdapter(Context context, int textViewResourceId,
				List<Sample> samples) {
			super(context, textViewResourceId, samples);
			this.items = samples;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.sample_list_row, null);
			}
			Sample o = items.get(position);
			if (o != null) {
				TextView tt = (TextView) v.findViewById(R.id.name);
				TextView bt = (TextView) v.findViewById(R.id.state);
				if (tt != null) {
					tt.setText("Name: " + o.name);
				}
				if (bt != null) {
					bt.setText("Status: "
							+ (o.processed ? "Processed" : "Pending"));
				}
			}
			return v;
		}
	}
}
