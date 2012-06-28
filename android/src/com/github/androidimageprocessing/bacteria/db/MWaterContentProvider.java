package com.github.androidimageprocessing.bacteria.db;

import android.net.Uri;

import com.github.androidimageprocessing.bacteria.dbsync.CRUDUriHandler;
import com.github.androidimageprocessing.bacteria.dbsync.SyncContentProvider;

public class MWaterContentProvider extends SyncContentProvider {
	public static String AUTHORITY = "com.github.androidimageprocessing.bacteria";
	public static final Uri SOURCES_URI = Uri.parse("content://" + AUTHORITY + "/sources");
	
	@Override
	public boolean onCreate() {
		this.helper = new MWaterDatabase(getContext());
		
		addUriHandler("sources", new CRUDUriHandler(this, helper, new SourcesTable()));
		addUriHandler("sources/#", new CRUDUriHandler(this, helper, new SourcesTable()));
		
		return true;
	}

	@Override
	protected String getAuthority() {
		return AUTHORITY;
	}
}
