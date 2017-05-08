package com.iam360.views.record;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.iam360.myapplication.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RotationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class RotationFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Timer timer;
    private static final int[] LANDSCAPE_VALUES = new int[]{R.drawable.bl1, R.drawable.bl2, R.drawable.bl3};
    private static final int[] PORTRAIT_VALUES = new int[]{R.drawable.bp1, R.drawable.bp2, R.drawable.bp3};
    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            tick();
        }
    };
    private ImageView image;
    private int counter = 1;

    public RotationFragment() {
        // Required empty public constructor
    }

    private void tick() {
        if (image != null) {
            counter++;
            if (counter > 0) {
                counter = counter % 3;
            }
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        image.setImageResource(PORTRAIT_VALUES[counter]);
                    }
                });
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        image.setImageResource(LANDSCAPE_VALUES[counter]);
                    }
                });
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        timer = new Timer();
        timer.schedule(task, 1000, 1000);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rotation, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        getView().findViewById(R.id.closeButton).setOnClickListener((view) -> mListener.onClosePressed());
        image = (ImageView) getView().findViewById(R.id.imageToShow);
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
        task.cancel();
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onClosePressed();
    }
}
