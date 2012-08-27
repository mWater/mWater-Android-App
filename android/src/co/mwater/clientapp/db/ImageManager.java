package co.mwater.clientapp.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Stack;

import co.mwater.clientapp.dbsync.RESTClient;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

/**
 * Manages image download of thumbnails
 * @author Clayton
 *
 */
public class ImageManager {
	private ImageQueue imageQueue = new ImageQueue();
	private Thread imageLoaderThread = new Thread(new ImageQueueManager());
	RESTClient restClient;
	Context context;

	public static ImageManager defaultImageManager; 
	
 	public ImageManager(Context context, RESTClient restClient) {
		this.context = context;
		this.restClient = restClient;

		// Make background thread low priority, to avoid affecting UI
		// performance
		imageLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
	}

	public void displayThumbnailImage(String uid, ImageView imageView, int defaultDrawableId) {
		queueThumbnailImage(uid, imageView, defaultDrawableId);
		imageView.setImageResource(defaultDrawableId);
	}

	private void queueThumbnailImage(String uid, ImageView imageView, int defaultDrawableId) {
		// This ImageView might have been used for other images, so we clear
		// the queue of old tasks before starting.
		imageQueue.Clean(imageView);
		ImageRef p = new ImageRef(uid, imageView, defaultDrawableId);

		synchronized (imageQueue.imageRefs) {
			imageQueue.imageRefs.push(p);
			imageQueue.imageRefs.notifyAll();
		}

		// Start thread if it's not started yet
		if (imageLoaderThread.getState() == Thread.State.NEW) {
			imageLoaderThread.start();
		}
	}

	private Bitmap getBitmap(File cacheFile, RESTClient client, String command, String... args) {
		try {
			// Check cache
			if (!cacheFile.exists()) {
				// Download file
				byte[] bytes = client.getBytes(command, args);
				if (bytes == null)
					return null;

				// Save to file
				FileOutputStream fos = new FileOutputStream(cacheFile);
				fos.write(bytes);
				fos.close();
			}

			Bitmap bitmap = BitmapFactory.decodeFile(cacheFile.getPath());
			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/** Classes **/

	private class ImageRef {
		public String uid;
		public ImageView imageView;
		public int defDrawableId;

		public ImageRef(String u, ImageView i, int defaultDrawableId) {
			uid = u;
			imageView = i;
			defDrawableId = defaultDrawableId;
		}
	}

	// stores list of images to download
	private class ImageQueue {
		private Stack<ImageRef> imageRefs =
				new Stack<ImageRef>();

		// removes all instances of this ImageView
		public void Clean(ImageView view) {

			for (int i = 0; i < imageRefs.size();) {
				if (imageRefs.get(i).imageView == view)
					imageRefs.remove(i);
				else
					++i;
			}
		}
	}

	private class ImageQueueManager implements Runnable {
		public void run() {
			try {
				while (true) {
					// Thread waits until there are images in the
					// queue to be retrieved
					if (imageQueue.imageRefs.size() == 0) {
						synchronized (imageQueue.imageRefs) {
							imageQueue.imageRefs.wait();
						}
					}

					// When we have images to be loaded
					if (imageQueue.imageRefs.size() != 0) {
						ImageRef imageToLoad;

						synchronized (imageQueue.imageRefs) {
							imageToLoad = imageQueue.imageRefs.pop();
						}

						// Get file to load into
						File cacheFile;
						try {
							cacheFile = new File(ImageStorage.getCachedThumbnailImagePath(context, imageToLoad.uid));
						} catch (IOException e) {
							continue;
						}

						Bitmap bmp = getBitmap(cacheFile, restClient, "downloadimagethumbnail", 
								"clientuid", MWaterServer.getClientUid(context), 
								"imageuid", imageToLoad.uid);
						BitmapDisplayer bmpDisplayer =
								new BitmapDisplayer(bmp, imageToLoad.imageView, imageToLoad.defDrawableId);

						Activity a = (Activity)imageToLoad.imageView.getContext();

						a.runOnUiThread(bmpDisplayer);
					}

					if (Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {
			}
		}
	}

	// Used to display bitmap in the UI thread
	private class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		ImageView imageView;
		int defDrawableId;

		public BitmapDisplayer(Bitmap b, ImageView i, int defaultDrawableId) {
			bitmap = b;
			imageView = i;
			defDrawableId = defaultDrawableId;
		}

		public void run() {
			if (bitmap != null)
				imageView.setImageBitmap(bitmap);
			else
				imageView.setImageResource(defDrawableId);
		}
	}
}