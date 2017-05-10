package com.iam360.views.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.iam360.facetracking.CameraActivity;
import com.iam360.facetracking.ManualActivity;
import com.iam360.facetracking.R;

/**
 * A fragment representing a list of Items.
 * <p/
 */
public class SettingsFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SettingsFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static SettingsFragment newInstance(int columnCount) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings_list, container, false);
        ListView settingsList = (ListView) view.findViewById(R.id.settings_list);
        settingsList.setAdapter(new SettingsAdapter(getContext(), new String[]{getResources().getString(R.string.user_manual)},new View.OnClickListener[]{v -> openUserGuide()}));
        view.findViewById(R.id.manual_close).setOnClickListener(v -> goBackToCameraView());
        return view;
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
