package co.mwater.clientapp.databinding;

import android.content.ContentValues;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class CheckBoxDataBinderElement implements DataBinderElement, OnCheckedChangeListener {
	String column;
	CheckBox control;
	boolean modified;

	public CheckBoxDataBinderElement(String column, CheckBox control) {
		this.column = column;
		this.control = control;
		control.setOnCheckedChangeListener(this);
	}

	public void Load(ContentValues content) {
		Boolean val = content.getAsBoolean(column);
		if (val != null)
			control.setChecked(val);
		else
			control.setChecked(false);
		modified = false;
	}

	public void Save(ContentValues content) {
		boolean val = control.isChecked();
		content.put(column, val);
	}

	public boolean isModified() {
		return modified;
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		modified = true;
	}
}