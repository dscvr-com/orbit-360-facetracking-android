package com.iam360.views;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.iam360.engine.connection.BluetoothConnectionReceiver;

/**
 * Created by Lotti on 4/23/2017.
 */

public abstract class BaseActivity extends AppCompatActivity {
    private BluetoothConnectionReceiver  reciever = new BluetoothConnectionReceiver();
    private IntentFilter filter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
       filter = new IntentFilter();
        filter.addAction(BluetoothConnectionReceiver.CONNECTED);
        filter.addAction(BluetoothConnectionReceiver.DISCONNECTED);
        registerReceiver(reciever,filter);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(reciever);
        super.onPause();
    }
}
