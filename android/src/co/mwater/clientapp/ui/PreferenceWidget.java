package co.mwater.clientapp.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import co.mwater.clientapp.R;

public class PreferenceWidget extends LinearLayout {
	TextView title, summary;
	OnChangeListener onChangeListener;
	String[] listItems;
	int listValue = Spinner.INVALID_POSITION;
	boolean editable = true; 

	public PreferenceWidget(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.preference_widget, this);
		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		title = (TextView) findViewById(R.id.title);
		summary = (TextView) findViewById(R.id.summary);

		this.findViewById(R.id.pref).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				PreferenceWidget.this.onClick();
			}
		});
	}

	void onClick() {
		if (!editable)
			return;
		
		EditText inputText = null;
		Spinner inputSpinner = null;

		if (listItems == null) {
			inputText = new EditText(getContext());
			inputText.setText(summary.getText());
		}
		else {
			inputSpinner = new Spinner(getContext());
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
					R.array.source_types, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			inputSpinner.setAdapter(adapter);
			inputSpinner.setSelection(listValue);
		}

		final View input = inputText != null ? inputText : inputSpinner;

		new AlertDialog.Builder(getContext())
				.setTitle(title.getText())
				.setView(input)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Object value;
						if (input instanceof EditText)
							value = ((EditText) input).getText().toString();
						else {
							int pos = ((Spinner) input).getSelectedItemPosition();
							if (pos == Spinner.INVALID_POSITION)
								value = null;
							else
								value = pos;
						}
						if (PreferenceWidget.this.onChangeListener != null)
							PreferenceWidget.this.onChangeListener.onChange(value);
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing.
					}
				}).show();
	}

	public void setOnChangeListener(OnChangeListener l) {
		this.onChangeListener = l;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public void setTitle(CharSequence text) {
		title.setText(text);
	}

	public void setSummary(CharSequence text) {
		summary.setText(text);
	}
	
	public void setListValue(int listValue) {
		this.listValue = listValue;
	}

	public void setList(String[] listItems) {
		this.listItems = listItems;
	}

	public interface OnChangeListener {
		public void onChange(Object value);
	}

	// EditDialogFragment frag = new EditDialogFragment();
	// Bundle args = new Bundle();
	// args.putString("title", title.getText().toString());
	// args.putString("value", summary.getText().toString());
	// frag.setArguments(args);
	// frag.show(getContext(). getFragmentManager(), "dialog");
	//
	//
	// public static class EditDialogFragment extends DialogFragment {
	// @Override
	// public Dialog onCreateDialog(Bundle savedInstanceState) {
	// final EditText input = new EditText(getActivity());
	// input.setText(getArguments().getString("value"));
	// return new AlertDialog.Builder(getActivity())
	// .setTitle(getArguments().getString("title"))
	// .setMessage("Some message")
	// .setView(input)
	// .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int whichButton) {
	// Editable value = input.getText();
	// Toast.makeText(getActivity(), value, Toast.LENGTH_SHORT).show();
	// }
	// }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int whichButton) {
	// }
	// }).create();
	//
	// }
	// }
}
