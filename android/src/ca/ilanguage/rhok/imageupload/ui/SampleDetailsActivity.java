package ca.ilanguage.rhok.imageupload.ui;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import ca.ilanguage.rhok.imageupload.App;
import ca.ilanguage.rhok.imageupload.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class SampleDetailsActivity extends Activity {
	String filepath;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sample_details);
		filepath = App.getProcessedImageFolder(this) + File.separator
				+ getIntent().getStringExtra("filename");
		ImageView v = (ImageView) findViewById(R.id.result_image);

		FileInputStream in;
		BufferedInputStream buf;
		try {
			in = new FileInputStream(filepath);
			buf = new BufferedInputStream(in);
			Bitmap bMap = BitmapFactory.decodeStream(buf);
			v.setImageBitmap(bMap);
			if (in != null) {
				in.close();
			}
			if (buf != null) {
				buf.close();
			}
		} catch (Exception e) {
			Log.e("Error reading file", e.toString());
		}
		TextView t = (TextView) findViewById(R.id.result_text);
		try {
			filepath = App.getResultsFolder(this)
					+ File.separator
					+ getIntent().getStringExtra("filename").replace(".jpg",
							".xml");
			in = new FileInputStream(filepath);
			BufferedReader source = new BufferedReader(
					new InputStreamReader(in));

			String contents = "";
			String line = "";
			while ((line = source.readLine()) != null) {
				contents = contents + "\n" + line;
			}
			source.close();
			
			/*
			 * TODO read in the xml file and parse it using a library.
			 */
//			Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filepath);
//			t.setText(d.getElementById("contours").getTextContent());
			t.setText(contents);
		} catch (Exception e) {
			Log.e("Error reading file", e.toString());
		}
	}
	
}
