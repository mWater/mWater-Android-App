package co.mwater.clientapp.db;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import co.mwater.clientapp.dbsync.RESTClient;

// TODO cleanup this class
public class MWaterServer {
	private static final String PREF_NAME = "Login";
	
	static final public String serverUrl = "https://data.mwater.co/mwater/sync/";
	//static final public String serverUrl = "http://192.168.0.2:8000/mwater/sync/";
	
	static public void login(Context context, String username, String clientUid, List<String> roles) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString("username", username);
		editor.putString("clientUid", clientUid);
		editor.putString("roles", PreferenceUtils.listToString(roles));
		editor.commit();
	}
	
	static public String getClientUid(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return prefs.getString("clientUid", null);
	}

	static public String getUsername(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return prefs.getString("username", null);
	}

	static public boolean hasRole(Context context, String role) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return PreferenceUtils.stringToList(prefs.getString("roles", "")).contains(role);
	}

	static public RESTClient createClient(Context context) {
		try {
			String userAgent = "mWater/"+context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
			return new RESTClient(serverUrl, userAgent);
		} catch (NameNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}
}
