package co.mwater.clientapp.dbsync;

import org.json.JSONException;
import org.json.JSONObject;

import com.jakewharton.notificationcompat2.NotificationCompat2;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.ui.MainActivity;

public class WelcomeIntentService extends IntentService {
	private static final String TAG = WelcomeIntentService.class.getCanonicalName();

	Handler uiHandler;
	NotificationManager notificationManager;

	public WelcomeIntentService() {
		super("Welcomer");
		uiHandler = new Handler(Looper.getMainLooper());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {

			if (notificationManager == null)
				notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

			RESTClient client = MWaterServer.createClient(getApplicationContext());

			String jsonStr = client.get("welcome", "clientuid", MWaterServer.getClientUid(getApplicationContext()));
			Log.d(TAG, jsonStr);
			if (jsonStr.length() > 0) {
				final JSONObject json = new JSONObject(jsonStr);
				final String message = json.getString("message");
				final String display = json.getString("display");
				final boolean fatal = json.getBoolean("fatal");

				if (display.equals("notify")) {
					Notification notification = new NotificationCompat2.Builder(getApplicationContext())
							.setSmallIcon(R.drawable.mwater)
							.setWhen(System.currentTimeMillis())
							.setContentTitle("mWater")
							.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
							.setContentText(message)
							.setAutoCancel(true).build();
					notificationManager.notify(TAG, 0, notification);
				}
				else if (display.equals("toast")) {
					uiHandler.post(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
						}
					});
				}
				if (fatal)
					System.exit(1);
			}
		} catch (RESTClientException e) {
			Log.w(TAG, e.getLocalizedMessage());
		} catch (JSONException e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
	}
}
