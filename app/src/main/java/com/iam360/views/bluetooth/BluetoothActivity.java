package com.iam360.views.bluetooth;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.iam360.myapplication.R;

public class BluetoothActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            // Create a new Fragment to be placed in the activity layout
            BluetoothConnectionFragment bluetoothFrag = new BluetoothConnectionFragment();

            bluetoothFrag.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, bluetoothFrag).commit();
        }
    }

}
