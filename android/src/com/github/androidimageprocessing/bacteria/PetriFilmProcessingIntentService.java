package com.github.androidimageprocessing.bacteria;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

public class PetriFilmProcessingIntentService extends IntentService {
	public static final String STATUS = "Processing image.";
	String inPath;
	String outImage;

	public PetriFilmProcessingIntentService(String name) {
		super(name);
	}

	public PetriFilmProcessingIntentService() {
		super("PortableMicrobiologyLab");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		inPath = intent.getStringExtra("inpath");
		outImage = intent.getStringExtra("outimage");
		Log.d("PortableMicrobiologyLab","Processing "+inPath);
		
		// Process image
		PetrifilmImageProcessor processor = new PetrifilmImageProcessor();
		try {
			PetrifilmAnalysisResults results = processor.process(inPath);

			FileOutputStream fos;
			fos = new FileOutputStream(App.getProcessedImageFolder(this) + File.separator + outImage);
			fos.write(results.jpeg);
			fos.close();
			
			saveResultsToXML(results, outImage.replace(".jpg", ".xml"));
			
		} catch (IOException e) {
			Log.e("PortableMicrobiologyLab", e.getMessage());
		}
		
	}
	private void saveResultsToXML(PetrifilmAnalysisResults results, String outXML){
		File sd = Environment.getExternalStorageDirectory();      
	      if (sd.canWrite()) {
	        OutputStream out = null;
	        try {
				out = new FileOutputStream(new File(App.getResultsFolder(this) + File.separator +outXML));
				out.write("<?xml version=\"1.0\"?>".getBytes());
				out.write(("<colonies>"+results.colonies+"</colonies>").getBytes());
				
				out.flush();
		        out.close();
			} catch (FileNotFoundException e) {
				Log.e("PortableMicrobiologyLab", e.getMessage());
			} catch (IOException e) {
				Log.e("PortableMicrobiologyLab", e.getMessage());
			}
	          
	          
	      }else{
	    	  Log.e("PortableMicrobiologyLab", "App cannot save to the SDCard.");
	      }
	}

}
