package co.mwater.clientapp.db;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import co.mwater.clientapp.dbsync.RESTClient;
import co.mwater.clientapp.dbsync.RESTClientException;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SourceCodes {
	private static final String PREF_NAMES = "SourceCodes";
	private static final String AVAILABLE_CODES = "AvailableCodes";
	private static final int minCodes = 5;

	public static String obtainCode(Context context) throws NoMoreCodesException {
		// Get available codes string
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAMES, Context.MODE_PRIVATE);
		List<String> availableCodes = PreferenceUtils.stringToList(prefs.getString(AVAILABLE_CODES, ""));

		// Take first one
		if (availableCodes.size() == 0)
			throw new NoMoreCodesException();

		String code = availableCodes.remove(0);

		Editor editor = prefs.edit();
		editor.putString(AVAILABLE_CODES, PreferenceUtils.listToString(availableCodes));
		editor.commit();

		return code;
	}

	public static boolean anyCodesAvailable(Context context) {
		// Get available codes string
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAMES, Context.MODE_PRIVATE);
		List<String> availableCodes = PreferenceUtils.stringToList(prefs.getString(AVAILABLE_CODES, ""));
		return availableCodes.size() > 0;
	}
	
	public static boolean newCodesNeeded(Context context) {
		// Get available codes string
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAMES, Context.MODE_PRIVATE);
		List<String> availableCodes = PreferenceUtils.stringToList(prefs.getString(AVAILABLE_CODES, ""));
		return availableCodes.size() < minCodes;
	}

	public static boolean requestNewCodesIfNeeded(Context context) {
		while (newCodesNeeded(context))
			if (!requestNewCodes(context))
				return false;

		return true;
	}

	private static boolean requestNewCodes(Context context) {
		RESTClient restClient = MWaterServer.createClient(context);
		
		try {
			String codesjson = restClient.get("requestcodes", 
					"clientuid", MWaterServer.getClientUid(context));
			
			JSONArray arr = new JSONArray(codesjson);

			// Get available codes string
			SharedPreferences prefs = context.getSharedPreferences(PREF_NAMES, Context.MODE_PRIVATE);
			List<String> availableCodes = PreferenceUtils.stringToList(prefs.getString(AVAILABLE_CODES, ""));

			// Add new codes
			for (int i = 0; i < arr.length(); i++)
				availableCodes.add(arr.getString(i));

			// Save codes
			Editor editor = prefs.edit();
			editor.putString(AVAILABLE_CODES, PreferenceUtils.listToString(availableCodes));
			editor.commit();
			
			return true;
		} catch (RESTClientException e) {
			return false;
		} catch (JSONException e) {
			return false;
		}
	}

	public static class NoMoreCodesException extends Exception {
		private static final long serialVersionUID = -4836074561313007370L;

	}
}
