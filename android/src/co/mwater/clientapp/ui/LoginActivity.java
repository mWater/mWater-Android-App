package co.mwater.clientapp.ui;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.db.SourceCodes;
import co.mwater.clientapp.dbsync.RESTClient;
import co.mwater.clientapp.dbsync.RESTClientException;
import co.mwater.clientapp.ui.SignupActivity.SignupAsyncTask;

import com.actionbarsherlock.app.SherlockActivity;

public class LoginActivity extends SherlockActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
	}

	public void onLoginClick(View v) {
		// Login to server
		// TODO put in task
		String username = ((TextView) findViewById(R.id.username)).getText().toString();
		String password = ((TextView) findViewById(R.id.password)).getText().toString();

		LoginAsyncTask task = new LoginAsyncTask(username, password);
		task.execute();
	}
	
	class LoginAsyncTask extends AsyncTask<Void, Void, Void> {
		String username;
		String password;

		Exception ex;

		public LoginAsyncTask(String username, String password) {
			this.username = username;
			this.password = password;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			RESTClient restClient = MWaterServer.createClient(LoginActivity.this);
			try {
				JSONObject json = new JSONObject(restClient.get("login",
						"username", username,
						"password", password));

				String clientUid = json.getString("clientuid");
				List<String> roles = new ArrayList<String>();
				for (int i = 0; i < json.getJSONArray("roles").length(); i++)
					roles.add(json.getJSONArray("roles").getString(i));

				MWaterServer.login(LoginActivity.this, username, clientUid, roles);

				// Obtain more sources if needed
				SourceCodes.requestNewCodesIfNeeded(LoginActivity.this);
			} catch (RESTClientException e) {
				ex = e;
			} catch (JSONException e) {
				ex = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (ex != null) {
				if (ex instanceof RESTClientException) {
					if (((RESTClientException) ex).responseCode == HttpURLConnection.HTTP_FORBIDDEN)
						Toast.makeText(LoginActivity.this, "Incorrect username/password", Toast.LENGTH_LONG).show();
					else
						Toast.makeText(LoginActivity.this, ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
				}
				else {

					Toast.makeText(LoginActivity.this, ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
				}
				return;
			}
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			startActivity(intent);
			LoginActivity.this.finish();
		}

	}
}
