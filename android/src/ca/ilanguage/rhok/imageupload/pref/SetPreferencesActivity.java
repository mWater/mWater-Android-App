package ca.ilanguage.rhok.imageupload.pref;

import ca.ilanguage.rhok.imageupload.R;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class SetPreferencesActivity extends PreferenceActivity implements
		YesNoDialogPreference.YesNoDialogListener {
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
		getPreferenceManager().setSharedPreferencesName(
				PreferenceConstants.PREFERENCE_NAME);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

	}

	public void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			SharedPreferences prefs = getSharedPreferences(
					PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			// editor.remove(PreferenceConstants.PREFERENCE_LEVEL_ROW);

			editor.commit();
			Toast.makeText(this, "Dialog was closed", Toast.LENGTH_SHORT)
					.show();
		}
	}
}
