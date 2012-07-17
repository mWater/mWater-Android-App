package co.mwater.clientapp.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import co.mwater.clientapp.R;

import com.actionbarsherlock.app.SherlockFragment;

public abstract class SeeMoreListFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final int LOADER_ID = 0x02;
	private CursorAdapter adapter;
	private Observer observer;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		adapter = createAdapter();
		this.getLoaderManager().initLoader(LOADER_ID, null, this);
	}

	protected abstract CursorAdapter createAdapter();

	protected abstract Loader<Cursor> performQuery();

	protected abstract void seeAllClicked();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.see_more_list, container, false);

		((TextView)view.findViewById(R.id.seeAll)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				seeAllClicked();
			}
		});

		if (observer != null) {
			adapter.unregisterDataSetObserver(observer);
			observer = null;
		}
		
		observer = new Observer();
		adapter.registerDataSetObserver(observer);
		
		// Fill list
		fillList(view);

		return view;
	}

	private void fillList(View view) {
		LinearLayout listLayout = (LinearLayout)view.findViewById(R.id.list);

		List<View> oldViews = new ArrayList<View>(listLayout.getChildCount());

		for (int i = 0; i < listLayout.getChildCount(); i++)
			oldViews.add(listLayout.getChildAt(i));

		Iterator<View> iter = oldViews.iterator();

		listLayout.removeAllViews();

		Cursor cursor = adapter.getCursor();
		if (cursor == null)
			return;
		
		for (int i = 0; i < adapter.getCount(); i++)
		{
			View convertView = iter.hasNext() ? iter.next() : null;
			listLayout.addView(adapter.getView(i, convertView, listLayout));
		}
	}
	
	private void clearList() {
		LinearLayout listLayout = (LinearLayout)getView().findViewById(R.id.list);
		listLayout.removeAllViews();
	}

	private class Observer extends DataSetObserver
	{
		@Override
		public void onChanged()
		{
			fillList(getView());
			super.onChanged();
		}

		@Override
		public void onInvalidated()
		{
			clearList();
			super.onInvalidated();
		}
	}

	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return performQuery();
	}

	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		adapter.swapCursor(cursor);
	}

	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		adapter.swapCursor(null);
	}

}
