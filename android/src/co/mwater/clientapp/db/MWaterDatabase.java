package co.mwater.clientapp.db;

import android.content.Context;

import co.mwater.clientapp.dbsync.SyncDatabaseHelper;
import co.mwater.clientapp.dbsync.SyncTable;

public class MWaterDatabase extends SyncDatabaseHelper {
	private static final String DATABASE_NAME = "mwater.db";
	private static final int DATABASE_VERSION = 8;
	
	private static MWaterDatabase mWaterDatabase;
	
	public static MWaterDatabase getDatabase(Context context) {
		if (mWaterDatabase == null)
			mWaterDatabase = new MWaterDatabase(context);
		return mWaterDatabase;
	}
	
	public MWaterDatabase(Context context) {
		super(context, DATABASE_NAME, DATABASE_VERSION, 
				new SyncTable[] { new SourcesTable(), new SourceNotesTable(), new SamplesTable(), new TestsTable() });
	}
}
