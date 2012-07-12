package co.mwater.clientapp.ui.petrifilm;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


import co.mwater.clientapp.R;
import co.mwater.clientapp.petrifilmanalysis.PetrifilmImages;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class PetrifilmTestDetailsActivityOld extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.petrifilmtest_details);
        String name = getIntent().getStringExtra("name");
        // File imagefile = new File(App.getProcessedImageFolder(this), name + ".jpg"); //AR inside the try-catch now
        //ImageView v = (ImageView) findViewById(R.id.result_image);

        FileInputStream in;
        BufferedInputStream buf;
        try {
            File imagefile = new File(PetrifilmImages.getProcessedImageFolder(getApplicationContext()), name + ".jpg");
            in = new FileInputStream(imagefile);
            buf = new BufferedInputStream(in);
            Bitmap bMap = BitmapFactory.decodeStream(buf);
            //v.setImageBitmap(bMap);
            if (in != null) {
                in.close();
            }
            if (buf != null) {
                buf.close();
            }
        } catch (Exception e) {
            Log.e("Error reading file", e.toString());
        }

        //TextView t = (TextView) findViewById(R.id.result_text);
//        try {
////            File xmlfile = new File(App.getResultsFolder(getApplicationContext()), name + ".xml");
////            in = new FileInputStream(xmlfile);
////            BufferedReader source = new BufferedReader(
////                    new InputStreamReader(in));
////
////            String contents = "";
////            String line = "";
////            while ((line = source.readLine()) != null) {
////                contents = contents + "\n" + line;
////            }
////            source.close();
//
//            /*
//             * TODO read in the xml file and parse it using a library.
//             */
//            //			Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filepath);
//            //			t.setText(d.getElementById("contours").getTextContent());
////            Pattern pattern = Pattern.compile("[0-9][0-9]+");
////            Matcher matcher = pattern.matcher(contents);
////            String colonies = contents;
////            if (matcher.find()){
////                colonies = matcher.group(0);
////            }
////            t.setText(t.getText().toString()+" "+colonies);
//
//        } catch (Exception e) {
//            Log.e("Error reading file", e.toString());
//        }
    }

}
