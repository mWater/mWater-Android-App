package co.mwater.clientapp.ui.petrifilm;

import android.os.Handler;

public class UITimerTask implements Runnable {
	Handler handler;
	Runnable runnable;
	boolean started;
	int delay;

	public void start(Runnable runnable, int delay) {
		this.runnable = runnable;
		this.delay = delay;

		if (handler == null)
			handler = new Handler();

		started = true;
		run();
	}

	public void stop() {
		started = false;
		handler.removeCallbacks(this);
	}

	public void run() {
		runnable.run();
		handler.removeCallbacks(this);
		if (started)
			handler.postDelayed(this, delay);
	}
}
