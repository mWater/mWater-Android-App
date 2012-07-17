package co.mwater.clientapp.ui;

import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class LoginActivity extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TODO allow landscape. bug in actionbar tabs.
		
		// Setup action bar for tabs
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(true);

		Tab tab = actionBar.newTab().setText("New User")
				.setTabListener(new TabListener<SignupFragment>(this, "signup", SignupFragment.class, null));
		actionBar.addTab(tab);

		tab = actionBar.newTab().setText("Existing User")
				.setTabListener(new TabListener<LoginFragment>(this, "login", LoginFragment.class, null));
		actionBar.addTab(tab);
	}
}
