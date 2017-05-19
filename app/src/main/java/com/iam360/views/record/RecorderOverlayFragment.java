package com.iam360.views.record;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iam360.facetracking.BluetoothCameraApplicationContext;
import com.iam360.facetracking.CameraActivity;
import com.iam360.facetracking.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * fragment for new view Elements for Recorder
 * <p>
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecorderOverlayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class RecorderOverlayFragment extends Fragment {
    private ImageButton settings;
    private ImageButton trackingPointsSetting;
    private ImageView trackingPointsGrid;
    private ImageButton tracking;
    private ImageButton camera;
    private ImageButton recording;
    private TextView time;
    private TextView counter;
    private boolean isRecording = false;


    private Timer timer = new Timer("recordingTimer");

    private OnFragmentInteractionListener mListener;
    private TimerTask task;
    private TimerTask photoTask;

    public RecorderOverlayFragment() {
        // Required empty public constructor
    }

    public void startTimer() {
        task = new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                i++;
                String s = String.format("%02d:%02d:%02d", i / 360, (i / 60) % 60, i % 60);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        time.setText(s);
                    }
                });
            }
        };
        timer.schedule(task, 0, 1000);
    }

    public void stopTimer() {
        time.setText("00:00:00");
        task.cancel();
    }

    public void onSwipeLeft() {
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            changeToCamera();
        }
    }

    public void onSwipeRight() {
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            changeToVideo();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private boolean isFilmMode() {
        return ((BluetoothCameraApplicationContext) getContext().getApplicationContext()).isFilmMode();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        initButtons();
        if (isFilmMode()) {
            changeToVideo();
        } else {
            changeToCamera();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initButtons() {
        settings = (ImageButton) getView().findViewById(R.id.settings);
        trackingPointsSetting = (ImageButton) getView().findViewById(R.id.trackingPoints);
        tracking = (ImageButton) getView().findViewById(R.id.tracking);
        camera = (ImageButton) getView().findViewById(R.id.changeCamera);
        recording = (ImageButton) getView().findViewById(R.id.recordingButton);
        trackingPointsGrid = (ImageView) getView().findViewById(R.id.trackingGrid);
        time = (TextView) getView().findViewById(R.id.time);
        counter = (TextView) getView().findViewById(R.id.counter);
        settings.setOnClickListener(v -> settingsClicked());
        trackingPointsSetting.setOnClickListener(v -> trackingPointsClicked());
        camera.setOnClickListener(v -> cameraClicked());
        recording.setOnClickListener(v -> recordingClicked());
        tracking.setOnClickListener(v -> onTrackingClicked());
        setFilmMode(isFilmMode());
        changeTracking();
    }

    public void onTrackingClicked() {
        mListener.onTrackingClicked(!((BluetoothCameraApplicationContext) getContext().getApplicationContext()).getBluetoothService().isTracking());
        changeTracking();
    }

    private void changeTracking(){
        changeTracking(((BluetoothCameraApplicationContext) getContext().getApplicationContext()).isTracking());
    }

    private void changeTracking(boolean b) {
        if (b) {
            tracking.setImageResource(R.drawable.tracking_on);
        } else {
            tracking.setImageResource(R.drawable.tracking_off);
        }
    }

    public void recordingClicked() {
        if (!isFilmMode()) {
            takePicture();
        } else {
//            isRecording = isFilmMode() != isRecording;
            mListener.onRecordingClicked(isFilmMode(), isRecording && isFilmMode());
//            if (isRecording && isFilmMode()) {
//                recording.setImageResource(R.drawable.start);
//            } else {
//                recording.setImageResource(R.drawable.start_photo);
//            }
        }
    }

    private void takePicture() {
        counter.setVisibility(View.VISIBLE);
        counter.setText("");
        if(photoTask!= null){
            photoTask.cancel();
        }
        photoTask = new TimerTask() {
            int count = 4;
            boolean shouldTrackAfterPhoto = ((BluetoothCameraApplicationContext)getActivity().getApplicationContext()).isTracking();

            @Override
            public void run() {
                count--;
                if (count == 0) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            counter.setVisibility(View.INVISIBLE);
                            changeTracking(true);
                        }
                    });
                    mListener.onTrackingClicked(shouldTrackAfterPhoto);
                    mListener.onRecordingClicked(isFilmMode(), false);
                    this.cancel();
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tracking.setImageResource(R.drawable.tracking_off);
                            changeTracking(false);
                            counter.setText(String.valueOf(count));
                        }
                    });
                    mListener.onTrackingClicked(false);
                }
            }
        };
        timer.schedule(photoTask, 0, 1000);
    }

    private void cameraClicked() {
        mListener.onCameraClicked();
    }

    public void trackingPointsClicked() {
        if (trackingPointsGrid.getVisibility() == View.VISIBLE) {
            trackingPointsGrid.setVisibility(View.INVISIBLE);
        } else {
            trackingPointsGrid.setVisibility(View.VISIBLE);
        }
        mListener.onTrackingPointsClicked(trackingPointsGrid.getVisibility() == View.VISIBLE);

    }

    private void settingsClicked() {
        mListener.onSettingsClicked();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_recorder_overlay, container, false);
        inflate.bringToFront();
        return inflate;

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onSwipeBottom() {
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            changeToCamera();
        }
    }

    private void changeToCamera() {
        checkVideoStopped();
        ((BluetoothCameraApplicationContext) getContext().getApplicationContext()).setFilmMode(false);
        getLeftText().setVisibility(View.VISIBLE);
        getRightText().setVisibility(View.INVISIBLE);
        getMiddelText().setText(R.string.Photo);
        if (time != null) {
            time.setVisibility(View.INVISIBLE);
        }
    }

    private void checkVideoStopped() {
        if (isRecording) {
            recordingClicked();
        }
    }

    private TextView getLeftText() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return (TextView) getView().findViewById(R.id.textLeftLand);
        } else {
            return (TextView) getView().findViewById(R.id.textLeft);
        }
    }

    private TextView getMiddelText() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return (TextView) getView().findViewById(R.id.textMiddelLand);
        } else {
            return (TextView) getView().findViewById(R.id.textMiddel);
        }
    }

    private TextView getRightText() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return (TextView) getView().findViewById(R.id.textRightLand);
        } else {
            return (TextView) getView().findViewById(R.id.textRight);
        }
    }


    public void onSwipeTop() {
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            changeToVideo();
        }
    }

    private void changeToVideo() {
        photoTask.cancel();
        ((BluetoothCameraApplicationContext) getContext().getApplicationContext()).setFilmMode(true);
        if (time != null) {
            time.setVisibility(View.VISIBLE);
        }
        getLeftText().setVisibility(View.INVISIBLE);
        getRightText().setVisibility(View.VISIBLE);
        getMiddelText().setText(R.string.Video);
    }

    public void setFilmMode(boolean filmMode) {
       if(filmMode){
           changeToVideo();
       }else{
           changeToCamera();
       }
    }


    public interface OnFragmentInteractionListener {
        void onSettingsClicked();

        void onTrackingPointsClicked(boolean isCurrentlyOn);

        void onTrackingClicked(boolean isTrackingNowOn);

        void onCameraClicked();

        void onRecordingClicked(boolean shouldRecord, boolean startRecord);
    }
}
