package com.iam360.facetracking;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.iam360.views.settings.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        if (savedInstanceState != null) {
            return;
        }
        // Create a new Fragment to be placed in the activity layout
        SettingsFragment settingsFrag = new SettingsFragment();

        settingsFrag.setArguments(getIntent().getExtras());

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container_settings, settingsFrag).commit();
    }
}
