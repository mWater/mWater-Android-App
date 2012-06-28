package com.github.androidimageprocessing.bacteria.databinding;

import android.content.ContentValues;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public class TextViewDataBinderElement implements DataBinderElement, TextWatcher {
	String column;
	TextView control;
	boolean modified;

	public TextViewDataBinderElement(String column, TextView control) {
		this.column = column;
		this.control = control;
		control.addTextChangedListener(this);
	}

	public void Load(ContentValues content) {
		control.setText(content.getAsString(column));
		modified = false;
	}

	public void Save(ContentValues content) {
		String val = control.getText().toString();
		content.put(column, val);
	}

	public boolean isModified() {
		return modified;
	}

	public void afterTextChanged(Editable s) {
		modified = true;
	}

	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
}