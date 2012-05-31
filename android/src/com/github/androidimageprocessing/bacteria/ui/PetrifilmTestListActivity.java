package com.github.androidimageprocessing.bacteria.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.github.androidimageprocessing.bacteria.App;
import com.github.androidimageprocessing.bacteria.PetriFilmProcessingIntentService;
import com.github.androidimageprocessing.bacteria.PetrifilmTest;

import com.github.androidimageprocessing.bacteria.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PetrifilmTestListActivity extends ListActivity {
    private static final String TAG = "com.github.androidimageprocessing.bacteria";
    public static final int POPULATING_DIALOG = 54;

    List<PetrifilmTest> petrifilmtests;

    UITimerTask timerTask = new UITimerTask();

    static int PETRI_IMAGE_REQUEST = 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        timerTask.start(new Runnable() {
            public void run() {
                new PopulatePetrifilmTestsListTask().execute();
            }
        }, 3000);
    }

    @Override
    public void onPause() {
        super.onPause();
        timerTask.stop();
    }

    public void onNewPetrifilmTestClick(View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("New PetrifilmTest");
        alert.setMessage("Enter petrifilm test code");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String name = input.getText().toString();
                Intent intent = new Intent(PetrifilmTestListActivity.this,
                        PetrifilmCameraActivity.class);
                intent.putExtra("filename", name + ".jpg");
                startActivityForResult(intent, PETRI_IMAGE_REQUEST);
            }
        });

        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        PetrifilmTest petrifilmtest = petrifilmtests.get(position);
        if (petrifilmtest.processed) {
            Intent intent = new Intent(this, PetrifilmTestDetailsActivity.class);
            intent.putExtra("name", petrifilmtest.name);
            Log.d(TAG,
                    "Showing processed image "
                    + petrifilmtests.get(position).name);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Still processing...", Toast.LENGTH_SHORT)
            .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PETRI_IMAGE_REQUEST && resultCode == RESULT_OK) {
            String filename = data.getStringExtra("filename");

            // Send image to be processed and saved
            Intent intent = new Intent(this,
                    PetriFilmProcessingIntentService.class);
            // AR intent.putExtra("inpath", App.getOriginalImageFolder(this)
            //		+ File.separator + filename);
            try {
                intent.putExtra("inpath", App.getOriginalImageFolder(getApplicationContext()) + File.separator + filename);
            } catch (IOException e) {
                // TODO Handle this exception (popup a dialog with the error)
                Log.e(TAG, e.toString());
            }

            intent.putExtra("outimage", filename);
            Log.d(TAG, "Calling process image");
            startService(intent);
            new PopulatePetrifilmTestsListTask().execute();
        }
    }

    public class PopulatePetrifilmTestsListTask extends
    AsyncTask<Void, Void, Boolean> {
        List<PetrifilmTest> tests;

        @Override
        protected Boolean doInBackground(Void... params) {
            File dir = null;

            // AR
            try {
                dir = new File(App.getOriginalImageFolder(getApplicationContext()));
            } catch (IOException e) {
                // TODO Handle this exception (popup a dialog with the error)
                Log.e(TAG, e.toString());
            }

            if (dir != null) { // AR
                String[] petrifilmtestFiles = dir.list();
                tests = new ArrayList<PetrifilmTest>();
                for (int s = 0; s < petrifilmtestFiles.length; s++) {
                    // Trim .jpg
                    PetrifilmTest petrifilmtest = new PetrifilmTest();
                    petrifilmtest.name = petrifilmtestFiles[s].substring(0,
                            petrifilmtestFiles[s].length() - 4);
                    // AR
                    File results = null;
                    try {
                        results = new File(App.getResultsFolder(getApplicationContext()), petrifilmtest.name + ".xml");
                    } catch (IOException e) {
                        // TODO Handle this exception (popup a dialog with the error)
                        Log.e(TAG, e.toString());
                    }


                    if (results != null && results.exists()) {
                        petrifilmtest.processed = true;
                    }

                    tests.add(petrifilmtest);
                }
            }
            return true;
        }

        protected void onPreExecute() {
            // Temporarily removing for auto-refresh. put back in when done by
            // intent
            // showDialog(POPULATING_DIALOG);
        }

        protected void onPostExecute(Boolean result) {
            // Temporarily removing for auto-refresh. put back in when done by
            // intent
            // dismissDialog(POPULATING_DIALOG);
            petrifilmtests = tests;
            setListAdapter(new PetrifilmTestAdapter(
                    PetrifilmTestListActivity.this, R.layout.list_row,
                    petrifilmtests));
            Log.d(TAG, "Refreshed list");
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        if (id == POPULATING_DIALOG) {
            dialog = new ProgressDialog.Builder(this)
            .setCancelable(true)
            .setTitle("Please wait")
            .setMessage(
            "Populating petrifilmtests, this may take a moment.")
            .create();
            return dialog;
        } else {
            dialog = super.onCreateDialog(id);
        }
        return dialog;
    }

    class PetrifilmTestAdapter extends ArrayAdapter<PetrifilmTest> {
        private List<PetrifilmTest> items;

        public PetrifilmTestAdapter(Context context, int textViewResourceId,
                List<PetrifilmTest> petrifilmtests) {
            super(context, textViewResourceId, petrifilmtests);
            this.items = petrifilmtests;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_row, null);
            }
            PetrifilmTest o = items.get(position);
            if (o != null) {
                TextView tt = (TextView) v.findViewById(R.id.name);
                TextView bt = (TextView) v.findViewById(R.id.state);
                if (tt != null) {
                    tt.setText("Name: " + o.name);
                }
                if (bt != null) {
                    bt.setText("Status: "
                            + (o.processed ? "Processed" : "Pending"));
                }
            }
            return v;
        }
    }
}
