package co.mwater.clientapp.ui;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Activity which displays details for a particular
 * 
 * intent must contain parameter "uri"
 * 
 * @author Clayton
 * 
 */
public abstract class DetailActivity extends SherlockFragmentActivity {
	protected long id;
	protected String idString;
	protected Uri uri;

	protected Cursor rowCursor;
	protected ContentValues rowValues;

	protected Handler handler;
	private DetailActivityContentObserver contentObserver;

	boolean started = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Create handler
		handler = new Handler();

		// Get uri
		uri = getIntent().getParcelableExtra("uri");
		idString = uri.getLastPathSegment();
		id = Long.parseLong(idString);

		// Load data so onCreate overrides have it
		loadData();

		// Listen for content changes
		contentObserver = new DetailActivityContentObserver(handler);
		getContentResolver().registerContentObserver(uri, true, contentObserver);
	}

	@Override
	protected void onStart() {
		super.onStop();

		started = true;
		displayData();
	}

	@Override
	protected void onStop() {
		started = false;

		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// Unregister content observer
		if (contentObserver != null)
			getContentResolver().unregisterContentObserver(contentObserver);

		// Destroy cursor
		if (rowCursor != null)
			rowCursor.close();

		super.onDestroy();
	}

	private void loadData() {
		if (rowCursor != null)
			rowCursor.close();

		// Query to get cursor
		rowCursor = getContentResolver().query(uri, null, null, null, null);
		if (!rowCursor.moveToFirst())
		{
			// Row not found. Probaly deleted.
			rowValues = null;
			return;
		}

		// Load fields
		rowValues = new ContentValues();
		DatabaseUtils.cursorRowToContentValues(rowCursor, rowValues);
	}

	/**
	 * Implement this to display the record. Will be called on Start and
	 * whenever data is changed.
	 * 
	 * @param rowCursor
	 * @param rowValues
	 */
	abstract protected void displayData();

	/**
	 * Convenience method to set the text of a text view
	 * 
	 * @param id
	 * @param text
	 */
	protected void setControlText(int id, String text) {
		TextView textView = (TextView) findViewById(id);
		textView.setText(text);
	}

	/**
	 * Convenience method to get the text of a text view
	 * 
	 * @param id
	 * @param text
	 */
	protected String getControlText(int id) {
		TextView textView = (TextView) findViewById(id);
		return textView.getText().toString();
	}

	/**
	 * Convenience method to set the text of a text view
	 * 
	 * @param id
	 * @param text
	 */
	protected void setControlInteger(int id, Integer integer) {
		TextView textView = (TextView) findViewById(id);
		textView.setText(integer == null ? "" : integer.toString());
	}

	/**
	 * Convenience method to get the text of a text view
	 * 
	 * @param id
	 * @param text
	 */
	protected Integer getControlInteger(int id) {
		TextView textView = (TextView) findViewById(id);
		String text = textView.getText().toString();
		if (text.equals(""))
			return null;
		return Integer.parseInt(text);
	}

	void handleContentChange() {
		// Reload data
		loadData();

		// Redisplay if started
		if (started)
			displayData();
	}

	class DetailActivityContentObserver extends ContentObserver {
		public DetailActivityContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			handleContentChange();
		}
	}
}
