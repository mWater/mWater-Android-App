package com.github.androidimageprocessing.bacteria.ui;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class CameraPreviewViewBase extends SurfaceView implements
		SurfaceHolder.Callback, PreviewCallback {
	private static final String TAG = "PetrifilmTest::SurfaceView";

	private Camera mCamera;
	private SurfaceHolder mHolder;
	private int mFrameWidth;
	private int mFrameHeight;

	public CameraPreviewViewBase(Context context, AttributeSet attrs) {
		super(context, attrs);
		mHolder = getHolder();
		mHolder.addCallback(this);
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	public int getFrameWidth() {
		return mFrameWidth;
	}

	public int getFrameHeight() {
		return mFrameHeight;
	}

	public Camera getCamera() {
		return mCamera;
	}

	public void surfaceChanged(SurfaceHolder _holder, int format, int width,
			int height) {
		Log.i(TAG, "surfaceCreated");
		if (mCamera != null) {
			Camera.Parameters params = mCamera.getParameters();
			List<Camera.Size> sizes = params.getSupportedPreviewSizes();
			mFrameWidth = width;
			mFrameHeight = height;

			// selecting optimal camera preview size
			{
				double minDiff = Double.MAX_VALUE;
				for (Camera.Size size : sizes) {
					if (Math.abs(size.height - height) < minDiff) {
						mFrameWidth = size.width;
						mFrameHeight = size.height;
						minDiff = Math.abs(size.height - height);
					}
				}
			}

			params.setPreviewSize(getFrameWidth(), getFrameHeight());
			mCamera.setParameters(params);
			try {
				mCamera.setPreviewDisplay(null);
			} catch (IOException e) {
				Log.e(TAG, "mCamera.setPreviewDisplay fails: " + e);
			}
			
			mCamera.setPreviewCallback(this);
//			mCamera.setPreviewCallbackWithBuffer(this);
//			Camera.Size size = params.getPreviewSize();
//			byte[] data = new byte[size.width
//					* size.height
//					* ImageFormat
//							.getBitsPerPixel(params.getPreviewFormat()) / 8];
//			mCamera.addCallbackBuffer(data);
			mCamera.startPreview();
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "surfaceCreated");
		mCamera = Camera.open();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "surfaceDestroyed");
		if (mCamera != null) {
			synchronized (this) {
				mCamera.stopPreview();
				mCamera.setPreviewCallback(null);
				mCamera.release();
				mCamera = null;
			}
		}
	}

	protected abstract void processFrame(byte[] data, Bitmap bitmap);

	Bitmap bmp = null;

	public void onPreviewFrame(byte[] data, Camera camera) {
		if (bmp == null || bmp.getWidth() != getFrameWidth()
				|| bmp.getHeight() != getFrameHeight())
			bmp = Bitmap.createBitmap(getFrameWidth(), getFrameHeight(),
					Bitmap.Config.ARGB_8888);

		processFrame(data, bmp);

		Canvas canvas = mHolder.lockCanvas();
		if (canvas != null) {
			canvas.drawBitmap(bmp, (canvas.getWidth() - getFrameWidth()) / 2,
					(canvas.getHeight() - getFrameHeight()) / 2, null);
			mHolder.unlockCanvasAndPost(canvas);
		}
		//mCamera.addCallbackBuffer(data);
	}
}