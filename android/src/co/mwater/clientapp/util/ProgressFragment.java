package co.mwater.clientapp.util;

import java.util.LinkedList;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import co.mwater.clientapp.util.ProgressTask.ActivityTask;

public class ProgressFragment extends DialogFragment {
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
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		synchronized (lock) {
			ready = false;
		}
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