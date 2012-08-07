package co.mwater.clientapp.dbsync;

import java.io.File;
import java.io.IOException;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.ImageStorage;
import co.mwater.clientapp.db.MWaterDatabase;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.db.SourceCodes;
import co.mwater.clientapp.ui.MainActivity;

import com.jakewharton.notificationcompat2.NotificationCompat2;

public class SyncIntentService extends IntentService {
	private static final String TAG = SyncIntentService.class.getCanonicalName();

	NotificationManager notificationManager;

	public SyncIntentService() {
		super("Synchronizer");

	}

	@Override
	protected void onHandleIntent(Intent intent) {
		DataSlice slice = intent.getParcelableExtra("dataSlice");
		boolean includeImages = intent.getBooleanExtra("includeImages", false);

		if (notificationManager == null)
			notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

		try {
			notifyProgress(0);
			synchronizeDatabase(slice);
			
			// Obtain more sources if needed
			SourceCodes.requestNewCodesIfNeeded(getApplicationContext());
			
			if (includeImages)
				uploadImages();

			notifyFinished();
		} catch (SyncServerException e) {
			Log.w(TAG, e.getLocalizedMessage());
			notifyError(e.getLocalizedMessage());
		} catch (RESTClientException e) {
			Log.w(TAG, e.getLocalizedMessage());
			notifyError(e.getLocalizedMessage());
		} catch (IOException e) {
			Log.w(TAG, e.getLocalizedMessage());
			notifyError(e.getLocalizedMessage());
		}
	}

	void synchronizeDatabase(DataSlice slice) throws SyncServerException {
		// Open database
		MWaterDatabase mWaterDatabase = MWaterDatabase.getDatabase(getApplicationContext());
		SQLiteDatabase db = mWaterDatabase.getWritableDatabase();

		SyncClientImpl client = new SyncClientImpl(db, mWaterDatabase.getSyncTables());
		SyncServerImpl server = new SyncServerImpl(MWaterServer.createClient(getApplicationContext()), MWaterServer.getClientUid(getApplicationContext()));
		Synchronizer synchronizer = new Synchronizer(client, server);

		synchronizer.synchronize(slice);
	}

	void uploadImages() throws RESTClientException, IOException {
		final String[] uids = ImageStorage.getPendingUids(getApplicationContext());
		RESTClient restClient = MWaterServer.createClient(getApplicationContext());
		
		// For each uid
		for (int i = 0; i < uids.length; i++) {
			notifyProgress(i * 100 / uids.length);

			// Get image
			File imageFile = new File(ImageStorage.getPendingImagePath(getApplicationContext(), uids[i]));
			final int i2 = i;
			// Call rest client
			restClient.postFile("uploadimage", imageFile, new RESTClient.PostStatus() {
				public void progress(long completed, long total) {
					double prog = (((double) completed / total) + i2) / uids.length;
					notifyProgress((int) (prog * 100));
				}

				public boolean isCancelled() {
					return false;
				}
			}, "imageuid", uids[i], "clientuid", MWaterServer.getClientUid(getApplicationContext()));

			// Move file to cached
			ImageStorage.movePendingImageFileToCached(getApplicationContext(), uids[i]);
		}
	}

	void notifyProgress(int percent) {
		Notification notification = new NotificationCompat2.Builder(getApplicationContext())
				.setSmallIcon(R.drawable.ic_action_refresh)
				.setWhen(System.currentTimeMillis())
				.setContentTitle("Synchronizing mWater")
				.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
				.setProgress(100, percent, false)
				.setContentText(percent + "% complete").build();
		notificationManager.notify(TAG, 0, notification);
	}

	void notifyFinished() {
		notificationManager.cancel(TAG, 0);
	}

	void notifyError(String message) {
		Notification notification = new NotificationCompat2.Builder(getApplicationContext())
				.setSmallIcon(R.drawable.ic_action_refresh)
				.setWhen(System.currentTimeMillis())
				.setContentTitle("Synchronization failed")
				.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
				.setContentText(message)
				.setAutoCancel(true).build();
		notificationManager.notify(TAG, 0, notification);
	}
}
