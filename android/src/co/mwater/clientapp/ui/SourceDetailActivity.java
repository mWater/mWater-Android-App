package co.mwater.clientapp.ui;

import co.mwater.clientapp.databinding.DataBinder;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SourcesTable;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import co.mwater.clientapp.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SourceDetailActivity extends SherlockFragmentActivity {
	public static final String TAG = SourceDetailActivity.class.getSimpleName();
	private Uri uri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String id = getIntent().getStringExtra("id");
		uri = Uri.withAppendedPath(MWaterContentProvider.SOURCES_URI, id);
		
		getSupportActionBar().setTitle("Source: 34256");
		setContentView(R.layout.source3);

//		// Setup action bar for tabs
//		ActionBar actionBar = getSupportActionBar();
//		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//		actionBar.setDisplayShowTitleEnabled(true);
//
//		Bundle args = new Bundle();
//		args.putParcelable("uri", uri);
//		
//		Tab tab = actionBar.newTab().setText("Info")
//				.setTabListener(new TabListener<SourceDetailInfoFragment>(this, "info", SourceDetailInfoFragment.class, args));
//		actionBar.addTab(tab);
//
//		tab = actionBar.newTab().setText("Samples")
//				.setTabListener(new TabListener<SourceDetailInfoFragment>(this, "samples", SourceDetailInfoFragment.class, args));
//		actionBar.addTab(tab);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.source_detail_menu, menu);

		// Add listeners
		menu.findItem(R.id.menu_ok).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				finish();
				return true;
			}
		});

		menu.findItem(R.id.menu_delete).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				deleteSource();
				return true;
			}
		});
		
		return super.onCreateOptionsMenu(menu);
	}

	void deleteSource() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				getContentResolver().delete(uri, null, null);
				finish();
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Permanently delete source?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", null).show();
	}
}
