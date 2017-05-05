package com.iam360.views.record;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.iam360.myapplication.R;

/**
 * fragment for new view Elements for Recorder
 *
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecorderOverlayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class RecorderOverlayFragment extends Fragment {
    //TODO add ui change method which calls listener for each Button
    //TODO think about how to run the time....
    private ImageButton settings;
    private ImageButton trackingPoints;
    private ImageButton tracking;
    private ImageButton cameraMode;
    private ImageButton camera;
    private ImageButton recording;
    private TextView time;

    private OnFragmentInteractionListener mListener;

    public RecorderOverlayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    }

    private void initButtons() {
        settings = (ImageButton) getView().findViewById(R.id.settings);
        trackingPoints = (ImageButton) getView().findViewById(R.id.trackingPoints);
        tracking = (ImageButton) getView().findViewById(R.id.tracking);
        cameraMode = (ImageButton) getView().findViewById(R.id.cameraMode);
        camera = (ImageButton) getView().findViewById(R.id.camera);
        recording = (ImageButton) getView().findViewById(R.id.recordingButton);
        time = (TextView) getView().findViewById(R.id.time);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recorder_overlay, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

        void onTrackingPointsClicked();

        void onTrackingClicked();

        void onCameraModeClicked();

        void onCameraClicked();

        void onRecordingClicked();
    }
}
