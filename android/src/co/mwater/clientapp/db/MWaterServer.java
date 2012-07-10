package co.mwater.clientapp.db;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

// TODO cleanup this class
public class MWaterServer {
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
}
