package co.mwater.clientapp.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import co.mwater.clientapp.R;

import com.actionbarsherlock.app.SherlockFragment;

public abstract class SeeMoreListFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final int LOADER_ID = 0x01;
	private CursorAdapter adapter;
	private Observer observer;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	protected abstract CursorAdapter createAdapter();

	protected abstract Loader<Cursor> performQuery();

	protected abstract void seeAllClicked();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.see_more_list, container, false);

		observer = new Observer((LinearLayout)view.findViewById(R.id.list));

		// Create adapter
		if (adapter != null) {
			adapter.swapCursor(null);
			adapter = null;
		}
		adapter = createAdapter();
		adapter.registerDataSetObserver(observer);

		this.getLoaderManager().initLoader(LOADER_ID, null, this);

		return view;
	}
	
	public void onSeeAllClick(View v) {
		seeAllClicked();
	}

	private class Observer extends DataSetObserver
	{
		LinearLayout listLayout;

		public Observer(LinearLayout listLayout)
		{
			this.listLayout = listLayout;
		}

		@Override
		public void onChanged()
		{
			List<View> oldViews = new ArrayList<View>(listLayout.getChildCount());

			for (int i = 0; i < listLayout.getChildCount(); i++)
				oldViews.add(listLayout.getChildAt(i));

			Iterator<View> iter = oldViews.iterator();

			listLayout.removeAllViews();

			for (int i = 0; i < SeeMoreListFragment.this.adapter.getCount(); i++)
			{
				View convertView = iter.hasNext() ? iter.next() : null;
				listLayout.addView(SeeMoreListFragment.this.adapter.getView(i, convertView, listLayout));
			}
			super.onChanged();
		}

		@Override
		public void onInvalidated()
		{
			listLayout.removeAllViews();
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
