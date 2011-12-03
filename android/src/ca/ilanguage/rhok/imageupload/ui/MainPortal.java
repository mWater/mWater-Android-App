package ca.ilanguage.rhok.imageupload.ui;

import ca.ilanguage.rhok.imageupload.R;
import ca.ilanguage.rhok.imageupload.service.TakePicture;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainPortal extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	public void onWaterSourceClick(View v) {
		Intent intent = new Intent(this, TakePicture.class);

		startActivity(intent);
	}

	public void onWaterResultsClick(View v) {
		Intent intent = new Intent(this, TakePicture.class);

		startActivity(intent);
	}

	public void onSyncServerClick(View v) {
		startActivity(new Intent(this, ServerSync.class));
	}

}