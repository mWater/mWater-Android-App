package co.mwater.clientapp.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import co.mwater.clientapp.db.MWaterContentProvider;

class ContentProviderSharedPreferences implements SharedPreferences {
	Context context;
	Uri uri;
	ContentValues values;

	public ContentProviderSharedPreferences(Context context, Uri uri) {
		this.context = context;
		this.uri = uri;

		// Get row values
		values = MWaterContentProvider.getSingleRow(context, uri);
	}

	public void close() {
		// TODO
	}

	public boolean contains(String key) {
		return values.containsKey(key);
	}

	public Editor edit() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, ?> getAll() {
		Map<String, Object> map = new HashMap<String, Object>();
		for (String key : values.keySet())
			map.put(key, values.get(key));
		return map;
	}

	public boolean getBoolean(String key, boolean defValue) {
		return values.getAsBoolean(key) != null ? values.getAsBoolean(key) : defValue;
	}

	public float getFloat(String key, float defValue) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getInt(String key, int defValue) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getLong(String key, long defValue) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getString(String key, String defValue) {
		return values.getAsString(key) != null ? values.getAsString(key) : defValue;
	}

	public Set<String> getStringSet(String arg0, Set<String> arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		// TODO Auto-generated method stub

	}

	public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		// TODO Auto-generated method stub

	}

}