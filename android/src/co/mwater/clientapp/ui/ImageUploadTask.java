package co.mwater.clientapp.ui;

import java.io.File;
import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;
import co.mwater.clientapp.db.ImageStorage;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.dbsync.RESTClient;
import co.mwater.clientapp.dbsync.RESTClientException;

public class ImageUploadTask extends AsyncTask<Void, Integer, Exception> {
	ProgressDialog progressDialog;
	RESTClient restClient;
	Context context;
	String[] uids;

	public ImageUploadTask(RESTClient restClient, Context context, String[] uids) {
		this.restClient = restClient;
		this.context = context;
		this.uids = uids;
	}

	@Override
	protected Exception doInBackground(Void... params) {
		try {
			// For each uid
			for (int i = 0; i < uids.length; i++) {
				publishProgress(i * 1000 / uids.length);

				// Get image
				File imageFile = new File(ImageStorage.getPendingImagePath(context, uids[i]));
				final int i2 = i;
				// Call rest client
				restClient.postFile("uploadimage", imageFile, new RESTClient.PostStatus() {
					public void progress(long completed, long total) {
						double prog = (((double) completed / total) + i2) / uids.length;
						publishProgress((int) (prog * 1000));
					}

					public boolean isCancelled() {
						return ImageUploadTask.this.isCancelled();
					}
				}, "imageuid", uids[i], "clientuid", MWaterServer.getClientUid(context));

				// Move file to cached
				ImageStorage.movePendingImageFileToCached(context, uids[i]);
			}
		} catch (RESTClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e;
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... changed) {
		progressDialog.setProgress(changed[0]);
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(context);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage("Uploading Images");
		progressDialog.setMax(1000);
		progressDialog.setCancelable(false);
		progressDialog.setButton(ProgressDialog.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				ImageUploadTask.this.cancel(true);
			}
		});
		progressDialog.show();
	}

	@Override
	protected void onPostExecute(Exception result) {
		try {
			progressDialog.dismiss();
			progressDialog = null;
		} catch (Exception e) {
			// nothing
		}

		if (result == null) {
			Toast.makeText(context, "Upload succeeded", Toast.LENGTH_LONG).show();
		}
		else {
			if (isCancelled()) {
				Toast.makeText(context, "Cancelled", Toast.LENGTH_LONG).show();
			}
			else {
				Toast.makeText(context, "Error uploading: " + result.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}
}
