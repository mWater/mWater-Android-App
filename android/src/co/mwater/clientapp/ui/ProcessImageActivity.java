package co.mwater.clientapp.ui;

import java.io.FileOutputStream;
import java.io.IOException;

import co.mwater.clientapp.PetrifilmAnalysisResults;
import co.mwater.clientapp.PetrifilmImageProcessor;

import co.mwater.clientapp.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ProcessImageActivity extends Activity implements Runnable {
	String inPath;
	String outPath;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.process_image_activity);

		inPath = getIntent().getStringExtra("inpath");
		outPath = getIntent().getStringExtra("outpath");
		
		// Start lengthy operation in a background thread
		new Thread(this).start();
	}

	public void run() {
		// Process image
		PetrifilmImageProcessor processor = new PetrifilmImageProcessor();
		try {
			PetrifilmAnalysisResults results = processor.process(inPath);

			FileOutputStream fos;
			fos = new FileOutputStream(outPath);
			fos.write(results.jpeg);
			fos.close();

			Intent result = new Intent();
			result.putExtra("outpath", outPath);
			result.putExtra("colonies", results.colonies);
			setResult(RESULT_OK, result);

			finish();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
