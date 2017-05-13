package com.iam360.views.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.iam360.facetracking.CameraActivity;
import com.iam360.facetracking.ManualActivity;
import com.iam360.facetracking.R;

/**
 * A fragment representing a list of Items.
 * <p/
 */
public class SettingsFragment extends Fragment {

    private SettingsAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SettingsFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings_list, container, false);
        view.findViewById(R.id.settings_back).setOnClickListener(v -> goBackToCameraView());
        view.findViewById(R.id.settings_row).setOnClickListener(v -> openUserGuide());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void goBackToCameraView() {
        startActivity(new Intent(getContext(), CameraActivity.class));

    }

    private void openUserGuide() {
        startActivity(new Intent(getContext(), ManualActivity.class));
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
