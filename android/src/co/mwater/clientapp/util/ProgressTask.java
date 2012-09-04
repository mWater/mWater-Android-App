package co.mwater.clientapp.util;

import java.util.LinkedList;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

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

	protected boolean isCancelled() {
		if (progressFragment.isDestroyed())
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

	static class ProgressFragment extends DialogFragment {
		private static final String TAG = ProgressFragment.class.getSimpleName();

		public static final String DEFAULT_TAG = "ProgressFragment";

		protected final Object lock = new Object();

		protected Boolean ready = false;
		protected List<ActivityTask> pendingCallbacks = new LinkedList<ActivityTask>();

		boolean destroyed = false;

		public ProgressFragment() {
		}

		public boolean isDestroyed() {
			return destroyed;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Retain instance so that it survives orientation changes
			setRetainInstance(true);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			ProgressDialog dialog = new ProgressDialog(this.getActivity());
			dialog.setTitle(getArguments().getString("title"));
			dialog.setMessage(getArguments().getString("message"));
			dialog.setIndeterminate(true);
			return dialog;
		}

		@Override
		public void onDetach() {
			super.onDetach();

			synchronized (lock) {
				ready = false;
			}
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			synchronized (lock) {
				ready = true;

				int numPendingCallbacks = pendingCallbacks.size();

				while (numPendingCallbacks-- > 0)
					runNow(pendingCallbacks.remove(0));
			}
		}

		protected void addPending(ActivityTask task) {
			synchronized (lock) {
				pendingCallbacks.add(task);
			}
		}

		public boolean isReady() {
			synchronized (lock) {
				return ready;
			}
		}

		public void runWhenReady(ActivityTask task) {
			if (isReady())
				runNow(task);

			else
				addPending(task);
		}

		protected void runNow(final ActivityTask task) {
			final FragmentActivity activity = getActivity();
			activity.runOnUiThread(new Runnable() {
				public void run() {
					task.run(activity);
				}
			});
		}

		/**
		 * workaround for issue #17423
		 */
		@Override
		public void onDestroyView() {
			if (getDialog() != null && getRetainInstance())
				getDialog().setDismissMessage(null);

			super.onDestroyView();
		}

		@Override
		public void onDestroy() {
			Log.d(TAG, "onDestroy");
			destroyed = true;
			super.onDestroy();
		}
	}
}