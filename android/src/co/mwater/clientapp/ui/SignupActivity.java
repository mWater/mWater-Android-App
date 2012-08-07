package co.mwater.clientapp.ui;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.db.SourceCodes;
import co.mwater.clientapp.dbsync.RESTClient;
import co.mwater.clientapp.dbsync.RESTClientException;

import com.actionbarsherlock.app.SherlockActivity;

public class SignupActivity extends SherlockActivity {
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
		// TODO

		// Login to server
		// TODO put in task
		String email = ((TextView) findViewById(R.id.email)).getText().toString();
		String username = ((TextView) findViewById(R.id.username)).getText().toString();
		String password = ((TextView) findViewById(R.id.password)).getText().toString();

		RESTClient restClient = MWaterServer.createClient(this);
		try {
			JSONObject json = new JSONObject(restClient.get("signup",
					"email", email,
					"username", username,
					"password", password));

			String clientUid = json.getString("clientuid");
			List<String> roles = new ArrayList<String>();
			for (int i = 0; i < json.getJSONArray("roles").length(); i++)
				roles.add(json.getJSONArray("roles").getString(i));

			MWaterServer.login(this, username, clientUid, roles);

			// Obtain more sources if needed
			SourceCodes.requestNewCodesIfNeeded(this);

			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			this.finish();
		} catch (RESTClientException e) {
			if (e.responseCode == HttpURLConnection.HTTP_FORBIDDEN)
				Toast.makeText(this, "Username already taken or invalid email", Toast.LENGTH_LONG).show();
			else
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		} catch (JSONException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

}
