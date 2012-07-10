package co.mwater.clientapp.ui;

import com.actionbarsherlock.app.ActionBar.Tab;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import com.actionbarsherlock.app.ActionBar;

public class TabListener<T extends Fragment> implements ActionBar.TabListener {
	private Fragment fragment;
	private final Activity activity;
	private final String tag;
	private final Class<T> clazz;
	private final Bundle args;

	/**
	 * Constructor used each time a new tab is created.
	 * 
	 * @param activity
	 *            The host Activity, used to instantiate the fragment
	 * @param tag
	 *            The identifier tag for the fragment
	 * @param clz
	 *            The fragment's Class, used to instantiate the fragment
	 */
	public TabListener(Activity activity, String tag, Class<T> clz, Bundle args) {
		this.activity = activity;
		this.tag = tag;
		this.clazz = clz;
		this.args = args;
	}

	/* The following are each of the ActionBar.TabListener callbacks */

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// Check if the fragment is already initialized
		if (fragment == null) {
			// If not, instantiate and add it to the activity
			fragment = Fragment.instantiate(activity, clazz.getName(), args);
			ft.add(android.R.id.content, fragment, tag);
		} else {
			// If it exists, simply attach it in order to show it
			ft.attach(fragment);
		}
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if (fragment != null) {
			// Detach the fragment, because another one is being attached
			ft.detach(fragment);
		}
	}

	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// User selected the already selected tab. Usually do nothing.
	}
}
