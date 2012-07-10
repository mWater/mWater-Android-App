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

	public void bind(Uri uri) {
		unbind();
		this.uri = uri;

		contentObserver = new DataBinderContentObserver(handler);
		contentResolver.registerContentObserver(uri, true, contentObserver);

		load(true);
	}

	public void unbind() {
		// Unregister old content observer
		if (contentObserver != null)
			contentResolver.unregisterContentObserver(contentObserver);

		uri = null;
	}

	public void save() {
		ContentValues content = new ContentValues();

		// Get contents to save
		for (DataBinderElement elem : elements) {
			if (elem.isModified())
				elem.Save(content);
		}

		if (content.size() > 0) {
			contentResolver.update(uri, content, null, null);
		}
	}

	public void revert() {
		load(true);
	}
	
	void load(boolean includeModified) {
		if (uri == null)
			return;

		// Requery
		Cursor cursor = contentResolver.query(uri, null, null, null, null);
		if (!cursor.moveToFirst())
		{
			// Non-existant row. Do not bind
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
		load(false);
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