package ca.ilanguage.rhok.imageupload.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

class PetrifilmSnapPreviewView extends CameraPreviewViewBase {

//	public PetrifilmSnapPreviewView(Context context) {
//		super(context, null);
//	}
//
	public PetrifilmSnapPreviewView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected Bitmap processFrame(byte[] data) {
		int frameSize = getFrameWidth() * getFrameHeight();
		int[] rgba = new int[frameSize];

		Process(getFrameWidth(), getFrameHeight(), data, rgba);

		Bitmap bmp = Bitmap.createBitmap(getFrameWidth(), getFrameHeight(),
				Bitmap.Config.ARGB_8888);
		bmp.setPixels(rgba, 0/* offset */, getFrameWidth() /* stride */, 0, 0,
				getFrameWidth(), getFrameHeight());
		return bmp;
	}

	public native void Process(int width, int height, byte yuv[], int[] rgba);

	static {
		System.loadLibrary("native_sample");
	}
}
