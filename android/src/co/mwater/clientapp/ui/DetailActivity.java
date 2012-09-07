package co.mwater.clientapp.ui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import co.mwater.clientapp.db.ImageManager;
import co.mwater.clientapp.db.ImageStorage;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.dbsync.SyncTable;

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
	private static final String TAG = DetailActivity.class.getSimpleName();

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

		if (savedInstanceState != null) {
			takePhotoColumn = savedInstanceState.getString("takePhotoColumn");
			takePhotoUid = savedInstanceState.getString("takePhotoUid");
		}

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
		super.onStart();

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
		textView.setText(text != null ? text : "");
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
	 * Convenience method to set the editability of a text view
	 * 
	 * @param id
	 * @param text
	 */
	protected void setControlTextEditable(int id, boolean editable) {
		TextView textView = (TextView) findViewById(id);
		textView.setEnabled(editable);
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

	/**
	 * Convenience method to set the checkbox
	 * 
	 * @param id
	 * @param text
	 */
	protected void setControlBoolean(int id, Boolean value) {
		CheckBox checkBox = (CheckBox) findViewById(id);
		checkBox.setChecked(value != null && value);
	}

	/**
	 * Convenience method to get the text of a text view
	 * 
	 * @param id
	 * @param text
	 */
	protected boolean getControlBoolean(int id) {
		CheckBox checkBox = (CheckBox) findViewById(id);
		return checkBox.isChecked();
	}

	/**
	 * Determine if the record was created by me
	 * 
	 * @return
	 */
	protected boolean isCreatedByMe() {
		// Workaround for strange recurring bug
		String username = MWaterServer.getUsername(this);
		if (username == null)
		{
			Log.e(TAG, "Null username");
			Intent intent = new Intent(this, SignupActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
			startActivity(intent);
			finish();
			return false;
		}
		return username.equals(rowValues.getAsString(SyncTable.COLUMN_CREATED_BY));
	}

	protected void displayImage(String imageColumn) {
		String photoUid = rowValues.getAsString(imageColumn);
		if (photoUid == null)
			return;

		// TODO massive work needed for cached, etc.
		try {
			File imageFile = new File(ImageStorage.getPendingImagePath(this, photoUid));
			if (!imageFile.exists()) {
				Toast.makeText(this, "Full-size image download not implemented", Toast.LENGTH_SHORT).show();
				return;
			}

			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(imageFile), "image/jpeg");
			startActivity(intent);
		} catch (IOException e) {
			return;
		}
	}

	final int TAKE_PHOTO_REQUEST_CODE = 101;

	String takePhotoUid;
	String takePhotoColumn;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("takePhotoColumn", takePhotoColumn);
		outState.putString("takePhotoUid", takePhotoUid);
	}

	protected void takePhoto(String columnPhoto) {
		try {
			Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			takePhotoUid = ImageStorage.createUid();
			takePhotoColumn = columnPhoto;
			File photo = new File(ImageStorage.getTempImagePath(this, takePhotoUid));
			Uri uri = Uri.fromFile(photo);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

			startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
		} catch (IOException e) {
			Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if (requestCode == TAKE_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
			try {
				ImageStorage.moveTempImageFileToPending(this, takePhotoUid);
			} catch (IOException e) {
				Log.e(TAG, e.getLocalizedMessage());
				return;
			}

			// Set photo
			ContentValues update = new ContentValues();
			update.put(takePhotoColumn, takePhotoUid);
			getContentResolver().update(uri, update, null, null);
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	// TODO massive work needed to cache, thumbnail, etc.
	protected void displayImageButton(int imageButtonId, String imageColumn, int defaultImage) {
		ImageButton imageButton = (ImageButton) findViewById(imageButtonId);

		String imageUid = rowValues.getAsString(imageColumn);
		if (imageUid == null) {
			// Use default
			imageButton.setImageResource(defaultImage);
			return;
		}

		ImageManager.getDefault(getApplicationContext()).displayThumbnailImage(imageUid, imageButton, defaultImage);
	}

	void handleContentChange() {
		// Reload data
		loadData();

		// Redisplay if started
		if (started && rowValues != null)
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
