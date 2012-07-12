package co.mwater.clientapp.dbsync;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import co.mwater.clientapp.ui.SourceDetailActivity;

public class SyncServerImpl implements SyncServer {
	public static final String TAG = SyncServerImpl.class.getCanonicalName();
	String serverUrl;
	String clientUid;

	ChangeSetJsonSerializer jsonSerializer = new ChangeSetJsonSerializer();

	/**
	 * Creates the server connection
	 * 
	 * @param serverUrl
	 *            url of api, ending in /
	 * @param clientUid
	 */
	public SyncServerImpl(String serverUrl, String clientUid) {
		this.serverUrl = serverUrl;
		this.clientUid = clientUid;
	}

	public ChangeSet downloadChangeSet(DataSlice dataSlice, long since) throws SyncServerException {
		RESTClient restClient = new RESTClient(serverUrl + "download");
		restClient.addParam("clientuid", clientUid);
		restClient.addParam("since", since + "");
		restClient.addParam("slice", dataSlice.getSliceId());
		try {
			String cs = restClient.get();
			Log.d(TAG, "Got: " + cs);
			JSONObject csjson = new JSONObject(cs);
			return jsonSerializer.deserialize(csjson);
		} catch (RESTClientException e) {
			e.printStackTrace();
			throw new SyncServerException(e.getMessage(), e);
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public void uploadChangeSet(ChangeSet changeSet) throws SyncServerException {
		// Create json
		try {
			JSONObject csjson = jsonSerializer.serialize(changeSet);

			RESTClient restClient = new RESTClient(serverUrl + "upload");
			restClient.addParam("clientuid", clientUid);
			restClient.addParam("changeset", csjson.toString());

			restClient.post();
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		} catch (RESTClientException e) {
			e.printStackTrace();
			throw new SyncServerException(e.getMessage(), e);
		}
	}

	public void cancel() {
		// TODO
	}

}
