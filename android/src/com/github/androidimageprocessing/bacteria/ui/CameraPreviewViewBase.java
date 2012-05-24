package com.github.androidimageprocessing.bacteria.ui;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
//import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class CameraPreviewViewBase extends SurfaceView implements
SurfaceHolder.Callback, PreviewCallback {
    private static final String TAG = "com.github.androidimageprocessing.bacteria";

    private Camera camera;
    private SurfaceHolder holder;
    private int frameWidth;
    private int frameHeight;

    public CameraPreviewViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (this.isInEditMode())
            return;

        holder = getHolder();
        holder.addCallback(this);
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public Camera getCamera() {
        return camera;
    }

    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {

        // AR note, format ignored, should always be NV12 (YUV)
        Log.i(TAG, "surfaceChanged");
        if (camera != null) {
            Camera.Parameters params = camera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
            frameWidth = width;
            frameHeight = height;

            // selecting optimal camera preview size
            {
                double minDiff = Double.MAX_VALUE;
                for (Camera.Size size : sizes) {
                    if (Math.abs(size.height - height) < minDiff) {
                        frameWidth = size.width;
                        frameHeight = size.height;
                        minDiff = Math.abs(size.height - height);
                    }
                }
            }

            params.setPreviewSize(frameWidth, frameHeight);
            camera.setParameters(params);
            try {
                camera.setPreviewDisplay(null);
            } catch (IOException e) {
                Log.e(TAG, "mCamera.setPreviewDisplay fails: " + e);
            }

            // Print focus modes
            for (String focusMode : params.getSupportedFocusModes()) {
                Log.i(TAG, "Focus modes: "+focusMode);
            }
            //			Log.i(TAG, "Focus mode was: "+ params.getFocusMode());
            //			params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
            Log.i(TAG, "Focus mode is: "+ params.getFocusMode());

            camera.setPreviewCallback(this);
            //			mCamera.setPreviewCallbackWithBuffer(this);
            //			Camera.Size size = params.getPreviewSize();
            //			byte[] data = new byte[size.width
            //					* size.height
            //					* ImageFormat
            //							.getBitsPerPixel(params.getPreviewFormat()) / 8];
            //			mCamera.addCallbackBuffer(data);
            camera.startPreview();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        camera = Camera.open();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        if (camera != null) {
            synchronized (this) {
                camera.stopPreview();
                camera.setPreviewCallback(null);
                camera.release();
                camera = null;
            }
        }
    }

    protected abstract void processFrame(byte[] data, int[] processed);

    int[] mProcessedImage = null;
    int mProcessedWidth = 0;
    int mProcessedHeight = 0;

    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mProcessedImage == null || frameWidth != mProcessedWidth || frameHeight != mProcessedHeight) {
            mProcessedImage = new int[frameWidth * frameHeight];
            mProcessedWidth = frameWidth;
            mProcessedHeight = frameHeight;
        }

        processFrame(data, mProcessedImage);

        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            canvas.drawBitmap(mProcessedImage, 0, frameWidth, 0, 0, frameWidth, 
                    frameHeight, false, null);
            holder.unlockCanvasAndPost(canvas);
        }
        //mCamera.addCallbackBuffer(data);
    }
}
