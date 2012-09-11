package co.mwater.clientapp.ui;

import android.os.Bundle;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.SourcesTable;
import co.mwater.clientapp.ui.PreferenceWidget.OnChangeListener;

public class SourceEditActivity extends DetailActivity {
	private static final String TAG = SourceEditActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.source_edit_activity);

		((PreferenceWidget) findViewById(R.id.name)).setOnChangeListener(new OnChangeListener() {
			public void onChange(Object value) {
				SourceEditActivity.this.updateRow(SourcesTable.COLUMN_NAME, value.toString());
			}
		});

		((PreferenceWidget) findViewById(R.id.desc)).setOnChangeListener(new OnChangeListener() {
			public void onChange(Object value) {
				SourceEditActivity.this.updateRow(SourcesTable.COLUMN_DESC, value.toString());
			}
		});

		((PreferenceWidget) findViewById(R.id.source_type)).setOnChangeListener(new OnChangeListener() {
			public void onChange(Object value) {
				SourceEditActivity.this.updateRow(SourcesTable.COLUMN_SOURCE_TYPE, (Integer)value);
			}
		});
		((PreferenceWidget) findViewById(R.id.source_type)).setList(
				getResources().getStringArray(R.array.source_types));
	}

	@Override
	protected void displayData() {
		if (rowValues == null)
			return;

		setPreferenceWidget(R.id.name, "Name",
				rowValues.getAsString(SourcesTable.COLUMN_NAME), true);
		setPreferenceWidget(R.id.desc, "Description",
				rowValues.getAsString(SourcesTable.COLUMN_DESC), true);
		setPreferenceWidget(R.id.source_type, "Type", getSourceTypeString(), 
				rowValues.getAsInteger(SourcesTable.COLUMN_SOURCE_TYPE), true);
	}

	String getSourceTypeString() {
		// Look up type
		String[] sourceTypes = getResources().getStringArray(R.array.source_types);
		Integer sourceType = rowValues.getAsInteger(SourcesTable.COLUMN_SOURCE_TYPE);
		if (sourceType == null)
			return "Unspecified";
		else if (sourceType >= sourceTypes.length)
			return "?";
		else
			return sourceTypes[sourceType];
	}
}
