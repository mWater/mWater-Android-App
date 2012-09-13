package co.mwater.clientapp.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SamplesTable;

public class SampleListSummaryFragment extends SeeMoreListFragment {
	@Override
	protected CursorAdapter createAdapter() {
		return new SampleListNoSourceAdapter(getActivity(), null);
	}

	@Override
	protected Cursor performQuery() {
		// TODO sort
		return getActivity().getContentResolver().query(MWaterContentProvider.SAMPLES_URI, null, SamplesTable.COLUMN_SOURCE + "=?",
				new String[] { getArguments().getString("sourceUid") }, null);
	}

	@Override
	protected void seeAllClicked() {
		Intent intent = new Intent(getActivity(), SampleListActivity.class);
		intent.putExtra("sourceUid", getArguments().getString("sourceUid"));
		startActivity(intent);
	}

	@Override
	protected void onItemClick(long id) {
		Intent intent = new Intent(getActivity(), SampleDetailActivity.class);
		intent.putExtra("uri", Uri.withAppendedPath(MWaterContentProvider.SAMPLES_URI, id + ""));
		startActivity(intent);
	}

	@Override
	protected Uri[] getExtraWatchUris() {
		// Listen for any changes to tests
		return new Uri[] { MWaterContentProvider.TESTS_URI };
	}
}
