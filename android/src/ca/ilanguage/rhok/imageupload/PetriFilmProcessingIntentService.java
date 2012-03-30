package ca.ilanguage.rhok.imageupload;

import java.io.FileOutputStream;
import java.io.IOException;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class PetriFilmProcessingIntentService extends IntentService {
	public static final String STATUS = "Processing image.";
	String inPath;
	String outPath;

	public PetriFilmProcessingIntentService(String name) {
		super(name);
	}

	public PetriFilmProcessingIntentService() {
		super("PortableMicrobiologyLab");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		inPath = intent.getStringExtra("inpath");
		outPath = intent.getStringExtra("outpath");

		// Process image
		PetrifilmImageProcessor processor = new PetrifilmImageProcessor();
		try {
			PetrifilmAnalysisResults results = processor.process(inPath);

			FileOutputStream fos;
			fos = new FileOutputStream(outPath);
			fos.write(results.jpeg);
			fos.close();
			
			saveResultsToXML(results, outPath);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("PortableMicrobiologyLab", e.getMessage());
		}
	}
	private void saveResultsToXML(PetrifilmAnalysisResults results, String outpath){
		
	}

}
