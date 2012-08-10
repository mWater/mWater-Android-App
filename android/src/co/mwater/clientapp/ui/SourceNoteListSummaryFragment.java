package co.mwater.clientapp.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SourceNotesTable;

public class SourceNoteListSummaryFragment extends SeeMoreListFragment {
	@Override
	protected CursorAdapter createAdapter() {
		return new SourceNoteListAdapter(getActivity(), null);
	}

	@Override
	protected Cursor performQuery() {
		// TODO sort
		return getActivity().getContentResolver().query(MWaterContentProvider.SOURCE_NOTES_URI, null, SourceNotesTable.COLUMN_SOURCE + "=?",
				new String[] { getArguments().getString("sourceUid") }, null);
	}

	@Override
	protected void seeAllClicked() {
		Intent intent = new Intent(getActivity(), SourceNoteListActivity.class);
		intent.putExtra("sourceUid", getArguments().getString("sourceUid"));
		startActivity(intent);
	}

	@Override
	protected void onItemClick(long id) {
		Intent intent = new Intent(getActivity(), SourceNoteDetailActivity.class);
		intent.putExtra("uri", Uri.withAppendedPath(MWaterContentProvider.SOURCE_NOTES_URI, id + ""));
		startActivity(intent);
	}
}
