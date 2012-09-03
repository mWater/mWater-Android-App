package co.mwater.clientapp.ui;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.db.SourceCodes;
import co.mwater.clientapp.dbsync.RESTClient;
import co.mwater.clientapp.dbsync.RESTClientException;
import co.mwater.clientapp.util.ActivityTask;
import co.mwater.clientapp.util.ProgressTask;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class SignupActivity extends SherlockFragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup_activity);
	}

	public void onLoginClick(View v) {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}

	public void onSignupClick(View v) {
		// Login to server
		String email = ((TextView) findViewById(R.id.email)).getText().toString();
		String username = ((TextView) findViewById(R.id.username)).getText().toString();
		String password = ((TextView) findViewById(R.id.password)).getText().toString();

		new SignupTask(getApplicationContext(), email, username, password).execute(this, "mWater", "Signing up...");
	}

	static class SignupTask extends ProgressTask {
		Context context;
		String email;
		String username;
		String password;

		public SignupTask(Context context, String email, String username, String password) {
			this.context = context;
			this.email = email;
			this.username = username;
			this.password = password;
		}

		@Override
		protected void runInBackground() {
			RESTClient restClient = MWaterServer.createClient(context);

			String errorMessage = null;
			try {
				JSONObject json = new JSONObject(restClient.get("signup",
						"email", email,
						"username", username,
						"password", password));

				String clientUid = json.getString("clientuid");
				List<String> roles = new ArrayList<String>();
				for (int i = 0; i < json.getJSONArray("roles").length(); i++)
					roles.add(json.getJSONArray("roles").getString(i));

				MWaterServer.login(context, username, clientUid, roles);

				// Obtain more sources if needed
				SourceCodes.requestNewCodesIfNeeded(context);
			} catch (RESTClientException e) {
				if (e.responseCode == HttpURLConnection.HTTP_FORBIDDEN)
					errorMessage = "Username already taken or invalid email";
				else
					errorMessage = e.getLocalizedMessage();
			} catch (JSONException e) {
				errorMessage = e.getLocalizedMessage();
			}

			final String msg = errorMessage;

			runOnActivity(new ActivityTask() {
				public void run(FragmentActivity activity) {
					if (msg != null)
						Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
					else {
						Intent intent = new Intent(activity, MainActivity.class);
						activity.startActivity(intent);
						activity.finish();
					}
				}
			});
		}
	}
}
