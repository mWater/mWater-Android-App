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
    protected void processFrame(byte[] data, int[] processed) {
        Process(getFrameWidth(), getFrameHeight(), data, processed, results);
    }

    public native void Process(int width, int height, byte[] yuv, int[] process, PetrifilmPreviewResults results);

    static {
        System.loadLibrary("native_sample");
    }

}
