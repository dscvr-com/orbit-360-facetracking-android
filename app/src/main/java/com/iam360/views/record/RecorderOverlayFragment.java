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
    private ImageButton camera;
    private ImageButton recording;
    private TextView time;
    private TextView counter;
    private TextView middle;
    private TextView left;
    private TextView right;

    private boolean isRecording = false;
    private boolean isFilmMode = false;
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
    public void stopTimer(){
        time.setText("00:00:00");
        task.cancel();
    }

    public void onSwipeLeft(){
        if(isFilmMode){
           isFilmMode = !isFilmMode;
            left.setVisibility(View.VISIBLE);
            right.setVisibility(View.INVISIBLE);
            middle.setText(R.string.Photo);
            time.setVisibility(View.INVISIBLE);
        }
    }

    public void onSwipeRight(){
        if(!isFilmMode){
            isFilmMode = !isFilmMode;
            time.setVisibility(View.VISIBLE);
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.VISIBLE);
            middle.setText(R.string.Video);
        }
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
        camera = (ImageButton) getView().findViewById(R.id.changeCamera);
        recording = (ImageButton) getView().findViewById(R.id.recordingButton);
        trackingPointsGrid = (ImageView) getView().findViewById(R.id.trackingGrid);
        middle = (TextView) getView().findViewById(R.id.textMiddel);
        left = (TextView) getView().findViewById(R.id.textLeft);
        right = (TextView) getView().findViewById(R.id.textRight);
        time = (TextView) getView().findViewById(R.id.time);
        counter = (TextView) getView().findViewById(R.id.counter);
        settings.setOnClickListener(v -> settingsClicked());
        trackingPointsSetting.setOnClickListener(v -> trackingPointsClicked());
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
                int count = 3;
                @Override
                public void run() {
                    count --;
                    if(count == 0){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                counter.setVisibility(View.INVISIBLE);
                            }
                        });
                        mListener.onRecordingClicked(isFilmMode,false);
                        this.cancel();
                    }else{
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                counter.setText(String.valueOf(count));
                            }
                        });
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

        void onCameraClicked(boolean frontCamera);

        void onRecordingClicked(boolean shouldRecord, boolean startRecord);
    }
}
