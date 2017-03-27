package com.iam360.views.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.iam360.myapplication.R;

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
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment BluetoothConnectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BluetoothConnectionFragment newInstance(String param1, String param2) {
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
        searchForBluetooth();
    }

    private void searchForBluetooth() {

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class BluetoothLeScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            Log.i(this.getClass().getCanonicalName(), String.format("Device Found %s %s", device.getName(), device.getAddress()));
            //dataAdapter.add(device);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    }

}
