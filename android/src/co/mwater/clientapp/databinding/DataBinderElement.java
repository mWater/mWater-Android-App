package co.mwater.clientapp.databinding;

import android.content.ContentValues;

public interface DataBinderElement {
	void Load(ContentValues content);

	void Save(ContentValues content);

	boolean isModified();
}