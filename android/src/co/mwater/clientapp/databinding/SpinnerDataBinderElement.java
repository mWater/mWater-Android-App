package co.mwater.clientapp.databinding;

import android.content.ContentValues;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

public class SpinnerDataBinderElement implements DataBinderElement, OnItemSelectedListener {
	String column;
	Spinner control;
	boolean modified;

	public SpinnerDataBinderElement(String column, Spinner control) {
		this.column = column;
		this.control = control;
		control.setOnItemSelectedListener(this);
	}

	public void Load(ContentValues content) {
		Integer pos = content.getAsInteger(column);
		if (pos == null)
			modified = true;
		else
		{
			control.setSelection(pos);
			modified = false;
		}
	}

	public void Save(ContentValues content) {
		int pos = control.getSelectedItemPosition();
		if (pos == Spinner.INVALID_POSITION)
			content.putNull(column);
		else 
			content.put(column, pos);
	}

	public boolean isModified() {
		return modified;
	}

	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		modified = true;
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		modified = true;
	}
}