package co.mwater.clientapp.dbsync;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class SyncServerImpl implements SyncServer {
	public static final String TAG = SyncServerImpl.class.getCanonicalName();
	RESTClient restClient;
	String clientUid;

	ChangeSetJsonSerializer jsonSerializer = new ChangeSetJsonSerializer();

	/**
	 * Creates the server connection
	 * 
	 * @param restClient
	 *            rest client to use
	 * @param clientUid
	 */
	public SyncServerImpl(RESTClient restClient, String clientUid) {
		this.restClient = restClient;
		this.clientUid = clientUid;
	}

	public ChangeSet downloadChangeSet(DataSlice dataSlice, long since) throws SyncServerException {
		try {
			String cs = restClient.get("download",
					"clientuid", clientUid,
					"since", since + "",
					"slice", dataSlice.getSliceId());
			
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
			restClient.post("upload", "clientuid", clientUid, "changeset", csjson.toString());
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
