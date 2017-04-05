package com.iam360.views.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.iam360.motor.connection.BluetoothConnector;
import com.iam360.motor.connection.BluetoothMotorControlService;
import com.iam360.myapplication.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BluetoothConnectionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BluetoothConnectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BluetoothConnectionFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

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
        super.onCreate(savedInstanceState);

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
        findMotor();
    }

    private void findMotor() {
        BluetoothConnector bluetoothConnector = new BluetoothConnector(BluetoothAdapter.getDefaultAdapter(), getContext());
        bluetoothConnector.setListener(() -> finishedLoading());
        bluetoothConnector.connect();

    }
    private void finishedLoading() {
        ImageView imageView = (ImageView) getView().findViewById(R.id.ConnectionImage);
        imageView.setImageResource(R.drawable.signal_blue);
        //Add and Change Text to view.
        mListener.onConnected();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onConnected();
    }


}
