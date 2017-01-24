package com.iam360.videorecording;

import android.content.Context;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Class waits for a Image, read and saves it.
 * Created by Charlotte on 21.12.2016.
 */
public class ImageListener implements ImageReader.OnImageAvailableListener {
    private static final String TAG = "ImageListener";
    private File file;
    private Context context;

    public ImageListener(File file, Context context) {
        this.file = file;
        this.context = context;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = null;
        try {
            image = reader.acquireLatestImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);
            save(bytes);
            MediaRecorderWrapper.addImageToGallery(file, context);
        } catch (IOException e) {
            Log.e(TAG, "Problem reading Image", e);
        } finally {
            if (image != null) {
                image.close();
            }
        }
    }

    private void save(byte[] bytes) throws IOException {
        OutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(bytes);


        } finally {
            if (null != output) {
                output.close();
            }
        }
    }
}

