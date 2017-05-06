package com.iam360.views.record;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.iam360.myapplication.BluetoothCameraApplicationContext;
import com.iam360.myapplication.R;

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
    //TODO add ui change method which calls listener for each Button
    //TODO think about how to run the time....
    private ImageButton settings;
    private ImageButton trackingPointsSetting;
    private ImageView trackingPointsGrid;
    private ImageButton tracking;
    private ImageButton cameraMode;
    private ImageButton camera;
    private ImageButton recording;
    private TextView time;
    private TextView counter;

    private boolean isRecording = false;
    private boolean isFilmMode = true;
    private boolean frontCamera = true;


    private Timer timer = new Timer("recordingTimer");

    private OnFragmentInteractionListener mListener;
    private TimerTask task;

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
                getActivity().runOnUiThread(() -> time.setText(s));
            }
        };
        timer.schedule(task, 0, 1000);
    }
    public void stopTimer(){
        time.setText("00:00:00");
        task.cancel();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initButtons();
    }

    private void initButtons() {
        settings = (ImageButton) getView().findViewById(R.id.settings);
        trackingPointsSetting = (ImageButton) getView().findViewById(R.id.trackingPoints);
        tracking = (ImageButton) getView().findViewById(R.id.tracking);
        cameraMode = (ImageButton) getView().findViewById(R.id.cameraMode);
        camera = (ImageButton) getView().findViewById(R.id.changeCamera);
        recording = (ImageButton) getView().findViewById(R.id.recordingButton);
        trackingPointsGrid = (ImageView) getView().findViewById(R.id.trackingGrid);
        time = (TextView) getView().findViewById(R.id.time);
        counter = (TextView) getView().findViewById(R.id.counter);
        settings.setOnClickListener(v -> settingsClicked());
        trackingPointsSetting.setOnClickListener(v -> trackingPointsClicked());
        cameraMode.setOnClickListener(v -> cameraModeClicked());
        camera.setOnClickListener(v -> cameraClicked());
        recording.setOnClickListener(v -> recordingClicked());
        tracking.setOnClickListener(v -> onTrackingClicked());
    }

    private void onTrackingClicked() {
        if (!((BluetoothCameraApplicationContext) getContext().getApplicationContext()).getBluetoothService().isTracking()) {
            tracking.setImageResource(R.drawable.tracking_on);
        } else {
            tracking.setImageResource(R.drawable.tracking_off);
        }
        mListener.onTrackingClicked(!((BluetoothCameraApplicationContext) getContext().getApplicationContext()).getBluetoothService().isTracking());
    }

    private void recordingClicked() {
        if(!isFilmMode){
            counter.setVisibility(View.VISIBLE);
            timer.schedule(new TimerTask() {
                int count = 2;
                @Override
                public void run() {
                    count --;
                    if(count == 0){
                        counter.setVisibility(View.INVISIBLE);
                        mListener.onRecordingClicked(isFilmMode,false);
                    }else{
                        getActivity().runOnUiThread(() -> counter.setText(String.valueOf(count)));
                        this.cancel();
                    }
                }
            }, 1000, 1000);
        }else{
            isRecording = isFilmMode? !isRecording : isRecording;
            mListener.onRecordingClicked(isFilmMode,isRecording && isFilmMode);
            if(isRecording && isFilmMode){
                recording.setImageResource(R.drawable.start);
            }else{
                recording.setImageResource(R.drawable.start_photo);
            }
        }
    }

    private void cameraClicked() {
        frontCamera = !frontCamera;
        //TODO change some view elements
        mListener.onCameraClicked(frontCamera);
    }

    private void cameraModeClicked() {
        isFilmMode = !isFilmMode;
        if(isFilmMode){
            cameraMode.setImageResource(R.drawable.video_modus);
            time.setVisibility(View.VISIBLE);
        }else{
            cameraMode.setImageResource(R.drawable.camera_mode);
            time.setVisibility(View.INVISIBLE);
        }
        mListener.onCameraModeClicked(isFilmMode);
    }

    private void trackingPointsClicked() {
        if(trackingPointsGrid.getVisibility() == View.VISIBLE){
            trackingPointsGrid.setVisibility(View.INVISIBLE);
        }else{
            trackingPointsGrid.setVisibility(View.VISIBLE);
        }
        mListener.onTrackingPointsClicked(trackingPointsGrid.getVisibility() != View.VISIBLE);

    }

    private void settingsClicked() {
        //TODO change some view elements
        mListener.onSettingsClicked();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recorder_overlay, container, false);

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

    public interface OnFragmentInteractionListener {
        void onSettingsClicked();

        void onTrackingPointsClicked(boolean isCurrentlyOn);

        void onTrackingClicked(boolean isTrackingNowOn);

        void onCameraModeClicked(boolean shouldNowFilm);

        void onCameraClicked(boolean frontCamera);

        void onRecordingClicked(boolean shouldRecord, boolean startRecord);
    }
}
