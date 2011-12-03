package ca.ilanguage.rhok.imageupload.service;

import java.io.File;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;



import ca.ilanguage.rhok.imageupload.R;
import ca.ilanguage.rhok.imageupload.pref.PreferenceConstants;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class ImageUploadService extends IntentService {
	protected static String TAG = "NotifyingTranscriptionIntentService";

	private NotificationManager mNM;
	private Notification mNotification;
	private int NOTIFICATION = 7030;
	private Boolean mShowNotification = true;
	private PendingIntent mContentIntent;
	private int mImageUploadIconId = R.drawable.image_upload_icon;

	public ImageUploadService() {
		super(TAG);
	}

	private String mImageFilePath = "";
	private String mAudioResultsFileStatus = "";
	private Uri mUri;

	private String mFileNameOnServer = "";
	private int mSplitType = 0;
	private String mTranscription = "";
	private String mTranscriptionStatus = "";

	// Use a layout id for a unique identifier
	private static int ImageUpload_NOTIFICATIONS = R.layout.status_bar_notifications;
	private String mNotificationMessage;

	@Override
	public void onCreate() {

		super.onCreate();

	}

	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * This method is called each time an intent is delivered to this service.
	 * 
	 * 1. check if audio file is shorter than the users upload preferences,
	 * check if wifi is on 2. if conditions are satisfied, upload the file and
	 * set a pending intent that can load the result from the server into the
	 * corresponding ImageUpload draft
	 * 
	 * 
	 */
	@Override
	protected void onHandleIntent(Intent intent) {

		/*
		 * get data from extras bundle, store it in the member variables
		 */
		try {
			mUri = intent.getData();
			mImageFilePath = intent.getExtras().getString(
					PreferenceConstants.EXTRA_IMAGEFILE_FULL_PATH);
			//TODO get other key value metadata from the extras and include them in the Multipart form upload
			
		} catch (Exception e) {
			// Toast.makeText(SRTGeneratorActivity.this,
			// "Error "+e,Toast.LENGTH_LONG).show();
		}
		if (mImageFilePath.length() > 0) {
			mNotificationMessage = mImageFilePath;
		} else {
			mNotificationMessage = "No file";
			return;
		}
		

		if (mNM == null) {
			mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		}
		// The PendingIntent to launch our activity if the user selects this
		// notification
		// View the results file
		Intent notifyingIntent = new Intent();
		notifyingIntent.setAction(android.content.Intent.ACTION_VIEW);
		notifyingIntent.setDataAndType(Uri.fromFile(new File(mImageFilePath)), "image/*");
		mContentIntent = PendingIntent.getActivity(this, 0, notifyingIntent, 0);

		mNotification = new Notification(mImageUploadIconId,
				"ImageUpload Transcription in progress",
				System.currentTimeMillis());
		mNotification.setLatestEventInfo(this, "ImageUpload Transcription",
				"Checking for Wifi connection...", mContentIntent);
		mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		// startForeground(startId, mNotification);
		if (mShowNotification) {
			mNM.notify(NOTIFICATION, mNotification);
		}

		
		File audioFile = new File(mImageFilePath);


		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			Long uniqueId = System.currentTimeMillis();
			HttpPost httpPost = new HttpPost(
					PreferenceConstants.TRANSCRIPTION_WEBSERVICE_URL
							+ PreferenceConstants.TRANSCRIPTION_WEBSERVICE_API_KEY
							+ mImageFilePath
									.replace(
											PreferenceConstants.OUTPUT_IMAGE_DIRECTORY
													+ "images/", ""));

			MultipartEntity entity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);

			// ByteArrayOutputStream bos = new ByteArrayOutputStream();
			// bitmap.compress(CompressFormat.JPEG, 100, bos);
			// byte[] data = bos.toByteArray();
			entity.addPart("title", new StringBody("thetitle"));
			// /entity.addPart("returnformat", new StringBody("json"));
			// entity.addPart("uploaded", new
			// ByteArrayBody(data,"myImage.jpg"));
			// entity.addPart("ImageUploadInstallID",new
			// StringBody(mImageUploadInstallId));
			String splitCode = "" + mSplitType;
			entity.addPart("latitude", new StringBody("putthelatitudehere));
			entity.addPart("file", new FileBody(audioFile));
			// /entity.addPart("photoCaption", new
			// StringBody("thecaption"));
			httpPost.setEntity(entity);

			mNotification.setLatestEventInfo(this, "ImageUpload Transcription",
					"Connecting to transcription server...", mContentIntent);
			if (mShowNotification) {
				mNM.notify(NOTIFICATION, mNotification);
			}

			HttpResponse response = httpClient.execute(httpPost, localContext);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));

			/*
			 * Assuming first line of HTTP response contains success or other message to present to user
			 */
			String firstLine = reader.readLine();
			mNotification.setLatestEventInfo(this, "Image Upload ",
					firstLine, mContentIntent);
			if (mShowNotification) {
				mNM.notify(NOTIFICATION, mNotification);
			}
//			reader.readLine();// mFileNameOnServer =
//								// reader.readLine().replaceAll(":filename","");
//			mFileNameOnServer = reader.readLine().replaceAll(":path", "");

			// mAudioResultsFileStatus=mAudioResultsFileStatus+":::"+"File saved on server as "+mFileNameOnServer+" .";
		} catch (Exception e) {
			Log.e(e.getClass().getName(), e.getMessage(), e);
			// this is showing up for when the audio is not sent, but the
			// client srt is...
			// mNotificationMessage = "...";// null;
		}

		mNotificationMessage = "Image sent.";
		mNotification.setLatestEventInfo(this, "ImageUpload Transcription",
				mNotificationMessage, mContentIntent);
		if (mShowNotification) {
			mNM.notify(NOTIFICATION, mNotification);
		}

		// mNM.cancel(NOTIFICATION);

	}// end onhandle intent


}