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
	String sourceUid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		sourceUid = getArguments().getString("sourceUid");
	}

	@Override
	protected CursorAdapter createAdapter() {
		return new SampleListNoSourceAdapter(getActivity(), null, sourceUid);
	}

	@Override
	protected Loader<Cursor> performQuery() {
		// TODO sort
		return new CursorLoader(getActivity(), MWaterContentProvider.SAMPLES_URI, null, SamplesTable.COLUMN_SOURCE+"=?", new String[] { sourceUid }, null);
	}

	@Override
	protected void seeAllClicked() {
		// TODO Auto-generated method stub
		Toast.makeText(getActivity(), "Test", Toast.LENGTH_SHORT).show();
	}

}
