package co.mwater.clientapp.ui;

import java.net.HttpURLConnection;

import org.apache.http.entity.mime.MinimalField;

import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.dbsync.RESTClient;
import co.mwater.clientapp.dbsync.RESTClientException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.login_fragment, container, false);

		((Button) view.findViewById(R.id.login)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				login();
			}
		});
		return view;
	}

	void login() {
		// Login to server
		// TODO put in task
		String username = ((TextView) getView().findViewById(R.id.username)).getText().toString();
		String password = ((TextView) getView().findViewById(R.id.password)).getText().toString();

		RESTClient restClient = new RESTClient(MWaterServer.serverUrl + "login");
		restClient.addParam("username", username);
		restClient.addParam("password", password);
		try {
			MWaterServer.login(this.getActivity(), username, restClient.get());
			
			Intent intent = new Intent(getActivity(), MainActivity.class);
			startActivity(intent);
			this.getActivity().finish();
		} catch (RESTClientException e) {
			if (e.responseCode == HttpURLConnection.HTTP_FORBIDDEN)
				Toast.makeText(getActivity(), "Incorrect username/password", Toast.LENGTH_LONG).show();
			else
				Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

}
