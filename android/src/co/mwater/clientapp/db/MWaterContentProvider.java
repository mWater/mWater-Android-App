package co.mwater.clientapp.db;

import android.net.Uri;

import co.mwater.clientapp.dbsync.CRUDUriHandler;
import co.mwater.clientapp.dbsync.SyncContentProvider;

public class MWaterContentProvider extends SyncContentProvider {
	public static String AUTHORITY = "co.mwater.clientapp";
	public static final Uri SOURCES_URI = Uri.parse("content://" + AUTHORITY + "/sources");
	
	@Override
	public boolean onCreate() {
		this.helper = new MWaterDatabase(getContext());
		
		addUriHandler("sources", new CRUDUriHandler(this, helper, new SourcesTable()));
		addUriHandler("sources/#", new CRUDUriHandler(this, helper, new SourcesTable()));

		addUriHandler("source_notes", new CRUDUriHandler(this, helper, new SourceNotesTable()));
		addUriHandler("source_notes/#", new CRUDUriHandler(this, helper, new SourceNotesTable()));

		addUriHandler("samples", new CRUDUriHandler(this, helper, new SamplesTable()));
		addUriHandler("samples/#", new CRUDUriHandler(this, helper, new SamplesTable()));

		addUriHandler("tests", new CRUDUriHandler(this, helper, new TestsTable()));
		addUriHandler("tests/#", new CRUDUriHandler(this, helper, new TestsTable()));

		return true;
	}

	@Override
	protected String getAuthority() {
		return AUTHORITY;
	}
}
