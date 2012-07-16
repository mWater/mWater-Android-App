package co.mwater.clientapp.databinding;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Handler;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

public class DataBinder {
	List<DataBinderElement> elements;

	Uri uri;
	ContentResolver contentResolver;
	Handler handler;

	DataBinderContentObserver contentObserver;

	public DataBinder(ContentResolver contentResolver, Handler handler) {
		elements = new ArrayList<DataBinderElement>();
		this.contentResolver = contentResolver;
		this.handler = handler;
	}

	public void addTextView(TextView control, String column) {
		elements.add(new TextViewDataBinderElement(column, control));
	}

	public void addSpinner(Spinner control, String column) {
		elements.add(new SpinnerDataBinderElement(column, control));
	}

	public void addCheckBox(CheckBox control, String column) {
		elements.add(new CheckBoxDataBinderElement(column, control));
	}

	public void bind(Uri uri) {
		unbind();
		this.uri = uri;

		contentObserver = new DataBinderContentObserver(handler);
		contentResolver.registerContentObserver(uri, true, contentObserver);

		load(uri, true);
	}

	public void unbind() {
		// Unregister old content observer
		if (contentObserver != null)
			contentResolver.unregisterContentObserver(contentObserver);

		uri = null;
	}

	public void save() {
		if (uri == null)
			throw new IllegalArgumentException("Not bound");
		saveTo(uri);
	}

	/**
	 * Saves without binding
	 * 
	 * @param toUri
	 */
	public void saveTo(Uri toUri) {
		ContentValues content = new ContentValues();

		// Get contents to save
		for (DataBinderElement elem : elements) {
			if (elem.isModified())
				elem.Save(content);
		}

		if (content.size() > 0) {
			contentResolver.update(toUri, content, null, null);
		}
	}

	/**
	 * Saves without binding
	 * 
	 * @param toUri
	 */
	public void saveAllTo(ContentValues values) {
		// Get contents to save
		for (DataBinderElement elem : elements) {
				elem.Save(values);
		}
	}

	public void loadFrom(Uri fromUri) {
		load(fromUri, true);
	}

	public void revert() {
		load(uri, true);
	}

	void load(Uri loadUri, boolean includeModified) {
		if (loadUri == null)
			return;

		// Requery
		Cursor cursor = contentResolver.query(loadUri, null, null, null, null);
		if (!cursor.moveToFirst())
		{
			// Non-existant row. Do not load
			cursor.close();
			return;
		}

		ContentValues content = new ContentValues();
		DatabaseUtils.cursorRowToContentValues(cursor, content);

		// Reload all elements
		for (DataBinderElement elem : elements) {
			if (!elem.isModified() || includeModified)
				elem.Load(content);
		}
		cursor.close();
	}

	void handleContentChange() {
		load(uri, false);
	}

	class DataBinderContentObserver extends ContentObserver {
		public DataBinderContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			handleContentChange();
		}
	}
}