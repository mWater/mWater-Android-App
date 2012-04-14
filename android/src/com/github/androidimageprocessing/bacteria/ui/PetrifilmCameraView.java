package com.github.androidimageprocessing.bacteria.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

class PetrifilmCameraView extends CameraPreviewViewBase {
	public PetrifilmPreviewResults results = new PetrifilmPreviewResults();

	public PetrifilmCameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void processFrame(byte[] data, Bitmap bitmap) {
		Process(getFrameWidth(), getFrameHeight(), data, bitmap, results);
	}

	public native void Process(int width, int height, byte yuv[], Bitmap bitmap, PetrifilmPreviewResults results);

	static {
		System.loadLibrary("native_sample");
	}

}
