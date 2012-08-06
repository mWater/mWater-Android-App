package co.mwater.clientapp.ui;

import java.net.HttpURLConnection;

import co.mwater.clientapp.db.MWaterDatabase;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.db.SourceCodes;
import co.mwater.clientapp.dbsync.DataSlice;
import co.mwater.clientapp.dbsync.RESTClientException;
import co.mwater.clientapp.dbsync.SyncClientImpl;
import co.mwater.clientapp.dbsync.SyncServerException;
import co.mwater.clientapp.dbsync.SyncServerImpl;
import co.mwater.clientapp.dbsync.Synchronizer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
		try {
			dialog.dismiss();
			dialog = null;
		} catch (Exception e) {
			// nothing
		}
		
		if (result == null)
			Toast.makeText(context, "Success", Toast.LENGTH_LONG).show();
		else {
			if (result.getCause() instanceof RESTClientException) {
				RESTClientException rex = (RESTClientException) result.getCause();
				if (rex.responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
					Toast.makeText(context, "Login required", Toast.LENGTH_LONG).show();
					Intent intent = new Intent(context, LoginActivity.class);
					context.startActivity(intent);
					return;
				}
			}
			showErrorDialog("Failed: " + result.getMessage());
		}
	}

	private void showErrorDialog(String message) {
		AlertDialog errorDialog = new AlertDialog.Builder(context).setMessage(message)
				.setCancelable(false)
				.setNeutralButton("Close", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int id) {
						d.dismiss();
					}
				}).create();
		errorDialog.show();
	}

	@Override
	protected SyncServerException doInBackground(DataSlice... slices) {
		// Open database
		MWaterDatabase mWaterDatabase = MWaterDatabase.getDatabase(context);
		SQLiteDatabase db = mWaterDatabase.getWritableDatabase();

		SyncClientImpl client = new SyncClientImpl(db, mWaterDatabase.getSyncTables());
		SyncServerImpl server = new SyncServerImpl(MWaterServer.createClient(context), MWaterServer.getClientUid(context));
		Synchronizer synchronizer = new Synchronizer(client, server);

		try {
			synchronizer.synchronize(slices[0]);

			// Obtain more sources if needed
			SourceCodes.requestNewCodesIfNeeded(context);

			return null;
		} catch (SyncServerException e) {
			return e;
		}
	}

}
