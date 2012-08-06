package co.mwater.clientapp.ui;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragment;

import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.db.SourceCodes;
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

public class SignupFragment extends SherlockFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.signup_fragment, container, false);

		((Button) view.findViewById(R.id.signup)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				signup();
			}
		});
		return view;
	}

	void signup() {
		// TODO

		// Login to server
		// TODO put in task
		String email = ((TextView) getView().findViewById(R.id.email)).getText().toString();
		String username = ((TextView) getView().findViewById(R.id.username)).getText().toString();
		String password = ((TextView) getView().findViewById(R.id.password)).getText().toString();

		RESTClient restClient = MWaterServer.createClient(getActivity());
		try {
			JSONObject json = new JSONObject(restClient.get("signup",
					"email", email,
					"username", username,
					"password", password));

			String clientUid = json.getString("clientuid");
			List<String> roles = new ArrayList<String>();
			for (int i = 0; i < json.getJSONArray("roles").length(); i++)
				roles.add(json.getJSONArray("roles").getString(i));

			MWaterServer.login(this.getActivity(), username, clientUid, roles);

			// Obtain more sources if needed
			SourceCodes.requestNewCodesIfNeeded(getActivity());

			Intent intent = new Intent(getActivity(), MainActivity.class);
			startActivity(intent);
			this.getActivity().finish();
		} catch (RESTClientException e) {
			if (e.responseCode == HttpURLConnection.HTTP_FORBIDDEN)
				Toast.makeText(getActivity(), "Username already taken or invalid email", Toast.LENGTH_LONG).show();
			else
				Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
		} catch (JSONException e) {
			Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

}
