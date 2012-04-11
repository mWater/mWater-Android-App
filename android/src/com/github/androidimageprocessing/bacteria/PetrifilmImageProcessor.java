package com.github.androidimageprocessing.bacteria;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

/**
 * Processes a petri-film image
 * 
 * @author Clayton
 * 
 */
public class PetrifilmImageProcessor {

	public PetrifilmAnalysisResults process(String imagePath) throws IOException {
		PetrifilmAnalysisResults results = new PetrifilmAnalysisResults();

		// Read jpeg
		byte[] jpeg = readFile(imagePath);
		process(jpeg, results);
		return results;
	}

	static byte[] readFile(String path) throws IOException {
		FileInputStream is = new FileInputStream(path);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		int bytesRead;
		while ((bytesRead = is.read(b)) != -1) {
			bos.write(b, 0, bytesRead);
		}
		return bos.toByteArray();
	}

	native void process(byte[] jpeg, PetrifilmAnalysisResults results);
}
