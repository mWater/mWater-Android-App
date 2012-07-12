package co.mwater.clientapp.ui;

import co.mwater.clientapp.db.MWaterDatabase;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.dbsync.DataSlice;
import co.mwater.clientapp.dbsync.SyncClientImpl;
import co.mwater.clientapp.dbsync.SyncServerException;
import co.mwater.clientapp.dbsync.SyncServerImpl;
import co.mwater.clientapp.dbsync.Synchronizer;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

public class SyncTask extends AsyncTask<DataSlice, Void, SyncServerException> {
	ProgressDialog dialog;
	Context context;

	public SyncTask(Context context) {
		super();
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		dialog = ProgressDialog.show(context, "", "Synchronizing data...", true);
	}

	@Override
	protected void onPostExecute(SyncServerException result) {
		dialog.dismiss();
		dialog = null;

		if (result == null)
			Toast.makeText(context, "Success", Toast.LENGTH_LONG).show();
		else {
			// TODO
			Toast.makeText(context, "Failed: " + result.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected SyncServerException doInBackground(DataSlice... slices) {
		// Open database
		MWaterDatabase mWaterDatabase = MWaterDatabase.getDatabase(context);
		SQLiteDatabase db = mWaterDatabase.getWritableDatabase();

		SyncClientImpl client = new SyncClientImpl(db, mWaterDatabase.getSyncTables());
		SyncServerImpl server = new SyncServerImpl(MWaterServer.serverUrl, MWaterServer.getClientId(context));
		Synchronizer synchronizer = new Synchronizer(client, server);

		try {
			synchronizer.synchronize(slices[0]);
			return null;
		} catch (SyncServerException e) {
			return e;
		}
	}

}
