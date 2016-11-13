package com.iam360.motor.connection;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;

/**
 * Class to manage the Bluetooth-Connection to the motor.
 * Created by Charlotte on 07.11.2016.
 */
public class MotorBluetoothConnectorView extends FrameLayout {
    public static final String TAG = "MotorBluetoothConnectorView";
    private final BluetoothDataAdapter dataAdapter;
    private BluetoothAdapter adapter;
    private ListView list;
    private Activity context;

    public MotorBluetoothConnectorView(Activity context) {
        super(context);
        this.context = context;
        list = new ListView(context);
        adapter = BluetoothAdapter.getDefaultAdapter();
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        list.setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) context.findViewById(android.R.id.content);
        root.addView(progressBar);

        dataAdapter = new BluetoothDataAdapter(context, loadData());
        list.setAdapter(dataAdapter);
        addView(list);
    }

    private ArrayList<BluetoothDevice> loadData() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(new BluetoothBroadcastReceiver(), filter);
        if (adapter.getBondedDevices() != null) {
            return new ArrayList<>(adapter.getBondedDevices());
        } else {
            return new ArrayList<>();
        }


    }


    private final class BluetoothBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //TODO implement some message
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                dataAdapter.add(device);
            }
        }
    }
}
