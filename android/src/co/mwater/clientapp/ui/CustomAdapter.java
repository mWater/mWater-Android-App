package co.mwater.clientapp.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.widget.TextView;

public abstract class CustomAdapter extends CursorAdapter {
	public CustomAdapter(Context context, Cursor c) {
		super(context, c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
	}

	/**
	 * Convenience method to set the text of a text view
	 * 
	 * @param id
	 * @param text
	 */
	protected void setControlText(View view, int id, String text) {
		TextView textView = (TextView) view.findViewById(id);
		textView.setText(text != null ? text : "");
	}

}