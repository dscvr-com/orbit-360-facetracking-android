package com.iam360.views.record;

import android.graphics.Bitmap;
import android.hardware.camera2.CameraDevice;

public interface RecorderPreviewListener {
    void imageDataReady(byte[] data, int width, int height, Bitmap.Config colorFormat);

    void cameraOpened(CameraDevice device);

    void cameraClosed(CameraDevice device);
}