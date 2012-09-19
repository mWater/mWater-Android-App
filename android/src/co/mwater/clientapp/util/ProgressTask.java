package co.mwater.clientapp.util;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Runs a task in a background thread while displaying a progress dialog.
 * 
 * Subclass and override runInBackground to use. Call runOnActivity to do work
 * on the activity.
 * 
 * @author Clayton
 * 
 */
public abstract class ProgressTask {
	ProgressFragment progressFragment;

	abstract protected void runInBackground();

	public interface ActivityTask {
		void run(FragmentActivity activity);
	}

	protected void runOnActivity(ActivityTask task) {
		progressFragment.runWhenReady(task);
	}

	public void updateProgress(int completed, int total) {
		progressFragment.updateProgress(completed, total);
	}

	/**
	 * Forces closing of progress display before end
	 */
	public void finish() {
		// Clear fragment
		ProgressTask.this.runOnActivity(new ActivityTask() {
			public void run(FragmentActivity activity) {
				activity.getSupportFragmentManager().beginTransaction().remove(progressFragment).commit();
			}
		});
	}

	public boolean isCancelled() {
		if (progressFragment.isDestroyed() || progressFragment.isCancelled())
			return true;
		return false;
	}

	public void execute(FragmentActivity activity, String title, String message) {
		// Create progress fragment
		progressFragment = new ProgressFragment();

		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("message", message);
		progressFragment.setArguments(args);
		activity.getSupportFragmentManager()
				.beginTransaction().add(progressFragment, ProgressFragment.DEFAULT_TAG).commit();

		// Start async task
		ProgressAsyncTask asyncTask = new ProgressAsyncTask();
		asyncTask.execute();
	}

	class ProgressAsyncTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			ProgressTask.this.runInBackground();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// Clear fragment
			ProgressTask.this.runOnActivity(new ActivityTask() {
				public void run(FragmentActivity activity) {
					activity.getSupportFragmentManager().beginTransaction().remove(progressFragment).commit();
				}
			});
		}
	}
}