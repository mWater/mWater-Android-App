package co.mwater.clientapp.ui;

import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import co.mwater.clientapp.R;

import com.actionbarsherlock.app.SherlockFragment;

public abstract class SeeMoreListFragment extends SherlockFragment {
	private CursorAdapter adapter;
	private Observer observer;
	Handler handler;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		handler = new Handler();
		observer = new Observer(handler);
		adapter = createAdapter();

		requery();
	}

	private void requery() {
		Cursor cursor = performQuery();
		cursor.registerContentObserver(observer);
		adapter.changeCursor(cursor);
	}

	@Override
	public void onDestroy() {
		Cursor cursor = adapter.getCursor();
		if (cursor != null) {
			// TODO needed?
			cursor.unregisterContentObserver(observer);
			adapter.changeCursor(null);
		}
		super.onDestroy();
	}

	protected abstract CursorAdapter createAdapter();

	protected abstract Cursor performQuery();

	protected abstract void seeAllClicked();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.see_more_list, container, false);

		((TextView) view.findViewById(R.id.seeAll)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				seeAllClicked();
			}
		});

		// Fill list
		fillList(view);
		return view;
	}

	private void fillList(View view) {
		if (view == null)
			return;

		LinearLayout listLayout = (LinearLayout) view.findViewById(R.id.list);
		if (listLayout == null)
			return;

		listLayout.removeAllViews();
		addDivider(listLayout);

		Cursor cursor = adapter.getCursor();
		if (cursor == null)
			return;

		for (int i = 0; i < adapter.getCount(); i++)
		{
			View itemContents = adapter.getView(i, null, listLayout);

			FrameLayout item = new FrameLayout(getActivity());
			item.addView(itemContents);
			item.setClickable(true);
			item.setBackgroundResource(R.drawable.borderless_button_background);
			cursor.moveToPosition(i);
			final long id = cursor.getLong(cursor.getColumnIndex("_id"));
			item.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					onItemClick(id);
				}
			});

			listLayout.addView(item);
			addDivider(listLayout);
		}
	}

	abstract protected void onItemClick(long id);

	// private void clearList() {
	// LinearLayout listLayout =
	// (LinearLayout)getView().findViewById(R.id.list);
	// listLayout.removeAllViews();
	// addDivider(listLayout);
	// }

	private void addDivider(LinearLayout listLayout) {
		ImageView divider = new ImageView(getActivity());
		divider.setScaleType(ImageView.ScaleType.FIT_XY);
		divider.setImageResource(R.drawable.divider);
		listLayout.addView(divider);
	}

	private class Observer extends ContentObserver
	{
		public Observer(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			requery();
			fillList(getView());
		}
	}
}
