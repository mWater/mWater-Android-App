package com.github.androidimageprocessing.bacteria.ui;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.github.androidimageprocessing.bacteria.R;
import com.github.androidimageprocessing.bacteria.db.MWaterServer;
import com.github.androidimageprocessing.bacteria.dbsync.RESTClient;
import com.github.androidimageprocessing.bacteria.dbsync.RESTClientException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login_activity);
	}

	public void onLoginClick(View v) {
		// Login to server
		// TODO put in task
		String username = ((TextView) findViewById(R.id.username)).getText().toString();
		String password = ((TextView) findViewById(R.id.username)).getText().toString();

		RESTClient restClient = new RESTClient(MWaterServer.serverUrl + "login");
		restClient.addParam("username", username);
		restClient.addParam("password", password);
		try {
			MWaterServer.login(this, username, restClient.get());
			finish();
		} catch (RESTClientException e) {
			if (e.responseCode==HttpURLConnection.HTTP_FORBIDDEN)
				Toast.makeText(this, "Incorrect username/password", Toast.LENGTH_LONG).show();
			else
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
}
