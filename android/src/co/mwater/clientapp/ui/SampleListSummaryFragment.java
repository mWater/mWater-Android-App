package co.mwater.clientapp.ui;

import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SamplesTable;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.widget.Toast;

public class SampleListSummaryFragment extends SeeMoreListFragment {
	@Override
	protected CursorAdapter createAdapter() {
		return new SampleListNoSourceAdapter(getActivity(), null, getArguments().getString("sourceUid"));
	}

	@Override
	protected Cursor performQuery() {
		// TODO sort
		return getActivity().getContentResolver().query(MWaterContentProvider.SAMPLES_URI, null, SamplesTable.COLUMN_SOURCE+"=?", new String[] { getArguments().getString("sourceUid") }, null);
	}

	@Override
	protected void seeAllClicked() {
		// TODO Auto-generated method stub
		Toast.makeText(getActivity(), "Test", Toast.LENGTH_SHORT).show();
	}

}
