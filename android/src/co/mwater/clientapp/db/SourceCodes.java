package co.mwater.clientapp.db;

import java.util.ArrayList;
import java.util.Arrays;
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
	private static final int minCodes = 50;

	public static String obtainCode(Context context) throws NoMoreCodesException {
		// Get available codes string
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAMES, Context.MODE_PRIVATE);
		List<String> availableCodes = stringToList(prefs.getString(AVAILABLE_CODES, ""));

		// Take first one
		if (availableCodes.size() == 0)
			throw new NoMoreCodesException();

		String code = availableCodes.remove(0);

		Editor editor = prefs.edit();
		editor.putString(AVAILABLE_CODES, listToString(availableCodes));
		editor.commit();

		return code;
	}

	public static boolean newCodesNeeded(Context context) {
		// Get available codes string
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAMES, Context.MODE_PRIVATE);
		List<String> availableCodes = stringToList(prefs.getString(AVAILABLE_CODES, ""));
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
					"clientuid", MWaterServer.getClientId(context));
			
			JSONArray arr = new JSONArray(codesjson);

			// Get available codes string
			SharedPreferences prefs = context.getSharedPreferences(PREF_NAMES, Context.MODE_PRIVATE);
			List<String> availableCodes = stringToList(prefs.getString(AVAILABLE_CODES, ""));

			// Add new codes
			for (int i=0;i<arr.length();i++)
				availableCodes.add(arr.getString(i));

			// Save codes
			Editor editor = prefs.edit();
			editor.putString(AVAILABLE_CODES, listToString(availableCodes));
			editor.commit();
			
			return true;
		} catch (RESTClientException e) {
			return false;
		} catch (JSONException e) {
			return false;
		}
	}

	static String listToString(List<String> codes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < codes.size(); i++)
		{
			if (i > 0)
				sb.append(",");
			sb.append(codes.get(i));
		}
		return sb.toString();
	}

	static List<String> stringToList(String codes) {
		if (codes.length() == 0)
			return new ArrayList<String>();
		
		return new ArrayList<String>(Arrays.asList(codes.split(",")));
	}

	public static class NoMoreCodesException extends Exception {
		private static final long serialVersionUID = -4836074561313007370L;

	}
}
