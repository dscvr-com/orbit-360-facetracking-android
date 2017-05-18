package com.iam360.views.record.engine;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.iam360.views.record.RecorderPreviewListener;

/**
 * Created by Emi on 15/05/2017.
 */

public class InMemoryImageProvider implements SurfaceProvider {

    private final static String TAG = "InMemoryImageProvider";
    private final static int START_DECODER = 0;
    private final static int FETCH_FRAME = 1;
    private final static int EXIT_DECODER = 2;

    private CodecSurface codecSurface;
    private HandlerThread decoderThread;
    private Handler decoderHandler;
    private SurfaceProviderCallback callback;
    private Size size;
    private boolean running;
    private RecorderPreviewListener dataListener;

    /**
     * Object lifecycle:
     *
     * 1) startBackgroundThread
     * 2) createSurface
     * 3) startFrameCapture
     * 4) stopFrameCapture
     * 5) destroySurface
     * 6) stopBackgroundThread
     */
    public InMemoryImageProvider() {
        running = false;
    }

    public void startBackgroundThread() {
        if(decoderHandler != null) {
            throw new IllegalStateException("Background thread already started");
        }
        this.decoderThread = new HandlerThread("InMemoryImageProviderWorker");
        this.decoderThread.start();
        this.decoderHandler = new Handler(decoderThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == START_DECODER) {
                    Log.d(TAG, Thread.currentThread().getName());
                    Log.d(TAG, "Create codec surface");
                    codecSurface = new CodecSurface(size.getWidth(), size.getHeight());

                    SurfaceProviderCallback c = callback;
                    callback = null;
                    c.SurfaceReady(InMemoryImageProvider.this, codecSurface.getSurface(), size);
                } else if (msg.what == FETCH_FRAME) {
                   // Log.d(TAG, Thread.currentThread().getName());
                    //Log.d(TAG, "Fetch frame await.");
                    if(codecSurface != null) {
                        try {
                            if (codecSurface.awaitNewImage()) {
                                codecSurface.drawImage(false);
                                dataListener.imageDataReady(codecSurface.fetchPixels(), codecSurface.mWidth,
                                        codecSurface.mHeight, CodecSurface.colorFormat);
                                //Log.d(TAG, "Fetch frame done");
                            } else {
                                Log.e(TAG, "Fetch frame failed");
                                Thread.sleep(10, 0);
                            }
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            Log.d(TAG, "Interrupt while waiting for image");
                        }
                        if(running) {
                            decoderHandler.obtainMessage(FETCH_FRAME).sendToTarget();
                        }
                    } else {
                        Log.d(TAG, "Codec surface is gone. Exiting looper.");
                    }
                } else if (msg.what == EXIT_DECODER) {
                    Log.d(TAG, Thread.currentThread().getName());
                    Log.d(TAG, "Destroying decoder.");
                    codecSurface.release();
                    codecSurface = null;
                    SurfaceProviderCallback c = callback;
                    callback = null;
                    c.SurfaceDestroyed(InMemoryImageProvider.this);
                }
            }
        };

    }

    @Override
    public Size[] getOutputSizes(StreamConfigurationMap map) {
       return map.getOutputSizes(SurfaceTexture.class);
    }

    public CodecSurface getSurfaceOwner() {
        return codecSurface;
    }

    @Override
    public Surface getSurface() {
        if(codecSurface != null) {
            return codecSurface.getSurface();
        } else {
            return null;
        }
    }

    @Override
    public void createSurface(Size size, SurfaceProviderCallback callback) {
        if(decoderHandler == null) {
            throw new IllegalStateException("Background thread not started");
        }
        if(this.codecSurface != null) {
            throw new IllegalStateException("Surface already created.");
        }
        if(this.callback != null) {
            throw new IllegalStateException("Operation in progress");
        }
        if(callback == null) {
            throw new IllegalArgumentException("No callback given");
        }

        this.callback = callback;
        this.size = new Size(size.getHeight(), size.getWidth());

        decoderHandler.obtainMessage(START_DECODER).sendToTarget();
   }

    @Override
    public void destroySurface(SurfaceProviderCallback callback) {
        if(decoderHandler == null) {
            throw new IllegalStateException("Background thread not started");
        }
        if(this.codecSurface == null) {
            throw new IllegalStateException("Surface is not created.");
        }
        if(this.callback != null) {
            throw new IllegalStateException("Operation in progress");
        }
        if(callback == null) {
            throw new IllegalArgumentException("No callback given");
        }
        decoderHandler.obtainMessage(EXIT_DECODER).sendToTarget();
        this.callback = callback;
    }

    public void startFrameFetching(RecorderPreviewListener dataListener) {
        Log.d(TAG, Thread.currentThread().getName());
        Log.d(TAG, "Start frame fetching message...");
        if(decoderHandler == null) {
            throw new IllegalStateException("Background thread not started");
        }
        if(running) {
            throw new IllegalStateException("Already running");
        }
        if(this.codecSurface == null) {
            throw new IllegalStateException("Surface is not created.");
        }
        if(this.callback != null) {
            throw new IllegalStateException("Operation in progress");
        }
        if(dataListener == null) {
            throw new IllegalArgumentException("DataListener is required");
        }
        this.dataListener = dataListener;
        running = true;

        decoderHandler.obtainMessage(FETCH_FRAME).sendToTarget();
    }
    public void stopFrameFetching() {
        Log.d(TAG, Thread.currentThread().getName());
        Log.d(TAG, "Stop frame fetching message...");
        if(!running) {
            throw new IllegalStateException("Not running");
        }
        running = false;
    }

    public void stopBackgroundThread() throws InterruptedException {
        Log.d(TAG, Thread.currentThread().getName());
        Log.d(TAG, "Stop background thread...");
        if(decoderHandler == null) {
            throw new IllegalStateException("Background thread not started");
        }
        if(running) {
            throw new IllegalStateException("Capture still running");
        }

        if(this.callback != null) {
            throw new IllegalStateException("Operation in progress");
        }

        decoderThread.quitSafely();
        decoderThread.join();

        if(this.codecSurface != null) {
            throw new IllegalStateException("Surface still allocated, but thread is terminated.");
        }

        decoderThread = null;
        decoderHandler = null;
        Log.d(TAG, "Background thread stopped...");

    }
}
