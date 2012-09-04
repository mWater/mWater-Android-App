package co.mwater.clientapp.ui;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import co.mwater.clientapp.R;
import co.mwater.clientapp.databinding.DataBinder;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.SourceCodes;
import co.mwater.clientapp.db.SourceCodes.NoMoreCodesException;
import co.mwater.clientapp.db.SourcesTable;
import co.mwater.clientapp.db.testresults.Risk;

public class SourceCreateDialog extends DialogFragment {
	DataBinder dataBinder;
	Uri uri;

	public SourceCreateDialog() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.source_create_dialog, container);

		// Load type spinner
		Spinner sourceType = (Spinner) view.findViewById(R.id.source_type);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.source_types, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		sourceType.setAdapter(adapter);

		dataBinder = new DataBinder(getActivity().getContentResolver(), new Handler());
		dataBinder.addTextView((TextView) view.findViewById(R.id.name), SourcesTable.COLUMN_NAME);
		dataBinder.addTextView((TextView) view.findViewById(R.id.desc), SourcesTable.COLUMN_DESC);
		dataBinder.addSpinner((Spinner) view.findViewById(R.id.source_type), SourcesTable.COLUMN_SOURCE_TYPE);

		// Connect buttons events
		((Button) view.findViewById(R.id.create)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onCreateClick();
			}
		});
		((Button) view.findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		});

		getDialog().setTitle("Source Details");
		return view;
	}

	private void onCreateClick() {
		// Create row
		ContentValues cv = new ContentValues();
		try {
			cv.put("code", SourceCodes.obtainCode(this.getActivity()));
		} catch (NoMoreCodesException e) {
			// TODO Obtain more source codes if needed
			showNoSourcesErrorDialog(getActivity());
			this.dismiss();
			return;
		}
		cv.put(SourcesTable.COLUMN_CREATED_BY, MWaterServer.getUsername(getActivity()));
		cv.put(SourcesTable.COLUMN_RISK, Risk.UNSPECIFIED.getValue());
		dataBinder.saveAllTo(cv);
		uri = getActivity().getContentResolver().insert(MWaterContentProvider.SOURCES_URI, cv);

		Intent intent = new Intent(this.getActivity(), SourceDetailActivity.class);
		intent.putExtra("uri", uri);
		intent.putExtra("setLocation", ((CheckBox) this.getView().findViewById(R.id.currentLocation)).isChecked());

		startActivity(intent);

		this.dismiss();
	}

	public static void showNoSourcesErrorDialog(Context context) {
		AlertDialog errorDialog = new AlertDialog.Builder(context)
				.setMessage("No source codes remaining. Please Synchronize to obtain more and then try again.")
				.setCancelable(false)
				.setNeutralButton("Close", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).create();
		errorDialog.show();
	}
}
