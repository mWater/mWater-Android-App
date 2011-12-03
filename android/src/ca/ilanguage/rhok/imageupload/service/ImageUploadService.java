package ca.ilanguage.rhok.imageupload.service;

import ca.ilanguage.rhok.imageupload.pref.PreferenceConstants;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class ImageUploadService extends IntentService {
	protected static String TAG = "NotifyingTranscriptionIntentService";

	private NotificationManager mNM;
	private Notification mNotification;
	private int NOTIFICATION = 7030;
	private Boolean mShowNotification = true;
	private PendingIntent mContentIntent;
	private int mAuBlogIconId = R.drawable.stat_aublog;

	public NotifyingTranscriptionIntentService() {
		super(TAG);
	}

	private Boolean mTranscriptionReturned = false;
	private int mMaxFileUploadOverMobileNetworkSize = 0;
	private int mMaxUploadFileSize = 15000000; // Set maximum upload size to
												// 1.5MB roughly 15 minutes of
												// audio,
	// users shouldn't abuse transcription service by sending meetings and other
	// sorts of audio.
	// If you change this value, change the value in the arrays.xml as well look
	// for:
	// 15 minutes (AuBlog\'s max transcription length)
	private String mAudioFilePath = "";
	private String mAudioResultsFileStatus = "";
	private Uri mUri;
	private String mAuBlogInstallId;
	private String mPostContents = "";

	private String mDBLastModified = "";
	private Cursor mCursor;
	private String[] PROJECTION = new String[] { AuBlogHistory._ID, // 0
			AuBlogHistory.LAST_MODIFIED, AuBlogHistory.TIME_EDITED,// 2
			AuBlogHistory.AUDIO_FILE, AuBlogHistory.AUDIO_FILE_STATUS // 4
	};
	private String mFileNameOnServer = "";
	private int mSplitType = 0;
	private String mTranscription = "";
	private String mTranscriptionStatus = "";

	// Use a layout id for a unique identifier
	private static int AUBLOG_NOTIFICATIONS = R.layout.status_bar_notifications;
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
	 * corresponding AuBlog draft
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
			mAudioFilePath = intent.getExtras().getString(
					EXTRA_AUDIOFILE_FULL_PATH);
			mAudioResultsFileStatus = intent.getExtras().getString(
					EXTRA_AUDIOFILE_STATUS);
			mPostContents = intent.getExtras().getString(
					EXTRA_CURRENT_CONTENTS);
			

		} catch (Exception e) {
			// Toast.makeText(SRTGeneratorActivity.this,
			// "Error "+e,Toast.LENGTH_LONG).show();
		}
		if (mAudioFilePath.length() > 0) {
			mNotificationMessage = mAudioFilePath;
		} else {
			mNotificationMessage = "No file";
			return;
		}
		if (mPostContents == null) {
			mPostContents = "";
		}
		
		if (mNM == null) {
			mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		}
		// The PendingIntent to launch our activity if the user selects this
		// notification
		// View the results file
		Intent notifyingIntent = new Intent();
		notifyingIntent.setAction(android.content.Intent.ACTION_VIEW);
		notifyingIntent.setDataAndType(Uri.fromFile(outSRTFile), "text/*");
		mContentIntent = PendingIntent.getActivity(this, 0, notifyingIntent, 0);

		mNotification = new Notification(mAuBlogIconId,
				"AuBlog Transcription in progress", System.currentTimeMillis());
		mNotification.setLatestEventInfo(this, "AuBlog Transcription",
				"Checking for Wifi connection...", mContentIntent);
		mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		// startForeground(startId, mNotification);
		if (mShowNotification) {
			mNM.notify(NOTIFICATION, mNotification);
		}

		Intent inten = new Intent(
				EditBlogEntryActivity.TRANSCRIPTION_STILL_CONTACTING_INTENT);
		inten.setData(mUri);
		inten.putExtra(EXTRA_AUDIOFILE_STATUS,
				mAudioResultsFileStatus);
		sendBroadcast(inten);

		/*
		 * Check if wifi is active, or if this file can be uploaded as per the
		 * users preference settings
		 */
		SharedPreferences prefs = getSharedPreferences(
				PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
		mAuBlogInstallId = prefs.getString(
				PreferenceConstants.AUBLOG_INSTALL_ID, "0");
		mMaxFileUploadOverMobileNetworkSize = prefs.getInt(
				PreferenceConstants.PREFERENCE_MAX_UPLOAD_ON_MOBILE_NETWORK,
				2000000);
		Boolean wifiOnly = prefs.getBoolean(
				PreferenceConstants.PREFERENCE_UPLOAD_WAIT_FOR_WIFI, true);
		File audioFile = new File(mAudioFilePath);
		// audioFile.length() < mMaxFileUploadOverMobileNetworkSize ||
		ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();

		if (audioFile.length() < mMaxUploadFileSize
				&& ((audioFile.length() < mMaxFileUploadOverMobileNetworkSize || wifiOnly == false)
						|| (wifi == State.CONNECTED || wifi == State.CONNECTING) || mAudioFilePath
							.endsWith(".srt"))) {
			// if the audio file
			// A: is smaller than max upload size, and
			// either B: smaller than the limit allowed on mobile, or user
			// doesnt care about being connected to wifi
			// or C: the wifi is on
			// or D: its a subtitle so send it always
			// then, upload it for transcription. otherwise say it was too big
			// to upload

			/*
			 * Upload file
			 */

			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpContext localContext = new BasicHttpContext();
				Long uniqueId = System.currentTimeMillis();
				HttpPost httpPost = new HttpPost(
						NonPublicConstants.NONPUBLIC_TRANSCRIPTION_WEBSERVICE_URL
								+ NonPublicConstants.NONPUBLIC_TRANSCRIPTION_WEBSERVICE_API_KEY
								+ mAudioFilePath
										.replace(
												PreferenceConstants.OUTPUT_AUBLOG_DIRECTORY
														+ "audio/", ""));

				MultipartEntity entity = new MultipartEntity(
						HttpMultipartMode.BROWSER_COMPATIBLE);

				// ByteArrayOutputStream bos = new ByteArrayOutputStream();
				// bitmap.compress(CompressFormat.JPEG, 100, bos);
				// byte[] data = bos.toByteArray();
				entity.addPart("title", new StringBody("thetitle"));
				// /entity.addPart("returnformat", new StringBody("json"));
				// entity.addPart("uploaded", new
				// ByteArrayBody(data,"myImage.jpg"));
				// entity.addPart("aublogInstallID",new
				// StringBody(mAuBlogInstallId));
				String splitCode = "" + mSplitType;
				entity.addPart("splitCode", new StringBody(splitCode));
				entity.addPart("file", new FileBody(audioFile));
				// /entity.addPart("photoCaption", new
				// StringBody("thecaption"));
				httpPost.setEntity(entity);

				mNotification
						.setLatestEventInfo(this, "AuBlog Transcription",
								"Connecting to transcription server...",
								mContentIntent);
				if (mShowNotification) {
					mNM.notify(NOTIFICATION, mNotification);
				}

				HttpResponse response = httpClient.execute(httpPost,
						localContext);

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));

				String firstLine = reader.readLine();
				mNotification.setLatestEventInfo(this, "AuBlog Transcription",
						firstLine, mContentIntent);
				if (mShowNotification) {
					mNM.notify(NOTIFICATION, mNotification);
				}
				reader.readLine();// mFileNameOnServer =
									// reader.readLine().replaceAll(":filename","");
				mFileNameOnServer = reader.readLine().replaceAll(":path", "");
				/*
				 * Read response into timecodes
				 */
				String line = "";
				while ((line = reader.readLine()) != null) {
					mTimeCodes.add(line);
				}
				reader.close();

				// mAudioResultsFileStatus=mAudioResultsFileStatus+":::"+"File saved on server as "+mFileNameOnServer+" .";
			} catch (Exception e) {
				Log.e(e.getClass().getName(), e.getMessage(), e);
				// this is showing up for when the audio is not sent, but the
				// client srt is...
				// mNotificationMessage = "...";// null;
			}


		} else {
			// no wifi, and the file is larger than the users settings for
			// upload over mobile network.
			mNotificationMessage = "Dication was not sent for transcription: no wifi or too long. Check Aublog settings.";
			mNotification.setLatestEventInfo(this, "AuBlog Transcription",
					mNotificationMessage, mContentIntent);
			if (mShowNotification) {
				mNM.notify(NOTIFICATION, mNotification);
			}
			mAudioResultsFileStatus = mAudioResultsFileStatus
					+ ":::"
					+ "Dictation audio wasn't sent for transcription, either user has wifi only or the file is larger than the settings the user has chosen, or its larger than 10min.";
			saveMetaDataToDatabase();

		}// end if for max file size for upload

		
			Intent i = new Intent(EditBlogEntryActivity.DICTATION_SENT_INTENT);
			i.putExtra(DictationRecorderService.EXTRA_AUDIOFILE_STATUS,
					mAudioResultsFileStatus);
			sendBroadcast(i);
			tracker.trackEvent(
					mAuBlogInstallId, // Category
					"Dictation Uploaded", // Action
					"Client uploaded an mp3 to the server: "
							+ System.currentTimeMillis() + " : "
							+ mAuBlogInstallId, // Label
					(int) System.currentTimeMillis()); // Value

			mNotificationMessage = "Dication sent for transcription.";
			mNotification.setLatestEventInfo(this, "AuBlog Transcription",
					mNotificationMessage, mContentIntent);
			if (mShowNotification) {
				mNM.notify(NOTIFICATION, mNotification);
			}
		
		// mNM.cancel(NOTIFICATION);

	}// end onhandle intent

	private void saveMetaDataToDatabase() {
		/*
		 * Save to database
		 */
		mCursor = getContentResolver()
				.query(mUri, PROJECTION, null, null, null);
		if (mCursor != null) {
			// Requery in case something changed while paused (such as the
			// title)
			mCursor.requery();
			// Make sure we are at the one and only row in the cursor.
			mCursor.moveToFirst();
			try {
				// compare the last time this service modified the database,
				// if its earlier than the database modified time, then someone
				// else has written in this entry in the database (they might
				// have written in different fields, in just incase if they
				// wrote in the audiofile or audiofilestatus,
				int result = mDBLastModified.compareTo(mCursor.getString(1));
				if (result < 0) {
					// some other activity or service has edited the important
					// fields in the database!
					// if they edited the filename, over write it with this file
					// name because this one is in process of recording.
					// if they changed the status message, add their status
					// message and a note about "being walked on"
					/*
					 * To avoid save walking, check if it contains it. if i
					 * contains it do nothing. if it doesnt contain it, then
					 * send the walking message. Recording service
					 * running:::/sdcard/AuBlog/audio/
					 * 13137125920433253_2011-08-20_06.05_1313877906018_transcription-wifi-with-exceptions.mp3:::Recording
					 * started.:::maybebluetooth:::1313877911675:::Walking on
					 * this status message that was in the
					 * database.------:::Recording stopped.:::Attached a 529
					 * second Recording. :::Recording flagged for
					 * transcription.:::Sent to transcription service.:::File
					 * saved on server as
					 * data/e1fd831e1f6b913b9c8504d081271b4f.mp3
					 * .:::Transcription server response saved as .srt in the
					 * AuBlog folder.:::Walking on this status message that was
					 * in the database.---Recording service
					 * running:::/sdcard/AuBlog/audio/
					 * 13137125920433253_2011-08-20_06.05_1313877906018_transcription-wifi-with-exceptions.mp3:::Recording
					 * started.:::maybebluetooth:::1313877911675:::Walking on
					 * this status message that was in the
					 * database.------:::Recording stopped.:::Attached a 529
					 * second Recording. :::Recording flagged for
					 * transcription.:::Sent to transcription service.---
					 */
					if (!(mAudioResultsFileStatus
							.contains(mCursor.getString(4)))) {
						mAudioResultsFileStatus = mAudioResultsFileStatus
								+ ":::Walking on this status message that was in the database.--__"
								+ mCursor.getString(4) + "-__";
					}
				}
				ContentValues values = new ContentValues();
				values.put(AuBlogHistory.AUDIO_FILE_STATUS,
						mAudioResultsFileStatus);
				values.put(AuBlogHistory.TRANSCRIPTION_STATUS,
						mTranscriptionStatus);
				values.put(AuBlogHistory.TRANSCRIPTION_RESULT, mTranscription);
				getContentResolver().update(mUri, values, null, null);
				mDBLastModified = Long.toString(System.currentTimeMillis());
				getContentResolver().notifyChange(AuBlogHistory.CONTENT_URI,
						null);

				// Tell the user we saved recording meta info to the database.
				// Toast.makeText(this, "Audiofile info saved to DB.",
				// Toast.LENGTH_SHORT).show();
				// mNotification.setLatestEventInfo(this, "AuBlog Dictation",
				// "Saved to DB", mContentIntent);
				// mNM.notify(NOTIFICATION, mNotification);

			} catch (IllegalArgumentException e) {

			} catch (Exception e) {

			}

		}// end if where cursor has content.

	}

}