package com.iam360.views.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.iam360.engine.connection.BluetoothConnectionReceiver;
import com.iam360.engine.connection.BluetoothConnector;
import com.iam360.myapplication.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BluetoothConnectionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BluetoothConnectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BluetoothConnectionFragment extends Fragment {
    private static final int BLUETOOTH_REQUEST = 3;
    private OnFragmentInteractionListener mListener;
    private int neededPerms = 0;

    private static final int BLUETOOTH__LOCATION_REQUEST = 2;
    private BluetoothConnector bluetoothConnector;
    private Timer timer;

    public BluetoothConnectionFragment() {
    }

    /**
     * @return A new instance of fragment BluetoothConnectionFragment.
     */
    public static BluetoothConnectionFragment newInstance() {
        BluetoothConnectionFragment fragment = new BluetoothConnectionFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!bluetoothConnector.hasDevices())
                    createTurnOnEngineScreen();
            }
        };
        timer = new Timer("notConnecteed");
        timer.schedule(task, 500);
        super.onCreate(savedInstanceState);

    }

    private void createTurnOnEngineScreen() {
        ImageView view = (ImageView) getView().findViewById(R.id.imageBTEngine);
        view.setImageResource(R.drawable.ORBIT_black);
        view = (ImageView) getView().findViewById(R.id.BTText);
        view.setImageResource(R.drawable.textTurnOnBT);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.bluetooth_frag, container, false);
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
        checkPermissions();
    }

    private void checkPermissions() {
        if (BluetoothAdapter.getDefaultAdapter() != null && BluetoothAdapter.getDefaultAdapter().isEnabled() && ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            findEngine();
        } else {
            if (BluetoothAdapter.getDefaultAdapter() == null || !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                neededPerms++;
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BLUETOOTH_REQUEST);
            }
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                neededPerms++;
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, BLUETOOTH__LOCATION_REQUEST);
            }
        }
    }

    private void findEngine() {
        bluetoothConnector = new BluetoothConnector(BluetoothAdapter.getDefaultAdapter(), getContext());
        bluetoothConnector.setListener(() -> finishedLoading());
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothConnectionReceiver.CONNECTED);
        filter.addAction(BluetoothConnectionReceiver.DISCONNECTED);
        getContext().registerReceiver(bluetoothConnector, filter);
        bluetoothConnector.connect();

    }

    private void finishedLoading() {
        ImageView view = (ImageView)getView().findViewById(R.id.imageBTEngine);
        view.setImageResource(R.drawable.ORBIT_color);
        view = (ImageView)getView().findViewById(R.id.BTText);
        view.setImageResource(R.drawable.BT_connected);
        mListener.connected();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getContext().unregisterReceiver(bluetoothConnector);
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void connected();
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == BLUETOOTH__LOCATION_REQUEST || requestCode == BLUETOOTH_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(this.getClass().getSimpleName(), "bluetooth Req");
                neededPerms--;
                if (neededPerms == 0) {
                    checkPermissions();
                }
            } else {
                Log.e(this.getClass().getSimpleName(), "No Bluetooth permission");
            }
        }
    }

}
