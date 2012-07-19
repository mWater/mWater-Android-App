package co.mwater.clientapp.db;

import co.mwater.clientapp.dbsync.RESTClient;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

// TODO cleanup this class
public class MWaterServer {
	//###static final public String serverUrl = "http://data.mwater.co/mwater/sync/";
	static final public String serverUrl = "http://192.168.0.2:8000/mwater/sync/";
	
	static public void login(Context context, String username, String clientId) {
		SharedPreferences prefs = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString("username", username);
		editor.putString("clientId", clientId);
		editor.commit();
	}
	
	static public String getClientId(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
		return prefs.getString("clientId", null);
	}

	static public String getUsername(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
		return prefs.getString("username", null);
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
