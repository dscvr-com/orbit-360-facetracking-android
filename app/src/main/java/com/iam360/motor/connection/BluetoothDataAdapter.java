package com.iam360.motor.connection;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.iam360.myapplication.R;

import java.util.HashSet;
import java.util.List;

/**
 * Class to provide the List View the founded bluetooth devices
 * Created by Charlotte on 12.11.2016.
 */
public class BluetoothDataAdapter extends ArrayAdapter<BluetoothDevice> {
    HashSet<BluetoothDevice> values;

    public BluetoothDataAdapter(Context context, List<BluetoothDevice> objects) {
        super(context, 0, objects);
        this.values = new HashSet<>(objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothDevice device = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_bluetooth, parent, false);
        }
        // Lookup view for data population
        TextView name = (TextView) convertView.findViewById(R.id.tvName);
        // Populate the data into the template view using the data object
        String state = getState(device.getBondState());
        name.setText(device.getName() + "\n" + state);
        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public void add(BluetoothDevice object) {
        if (!values.contains(object)) {
            super.add(object);
            values.add(object);
        }
    }

    private String getState(int bluetoothState) {
        if (BluetoothDevice.BOND_NONE == bluetoothState) {
            return getContext().getString(R.string.bond);
        }
        if (BluetoothDevice.BOND_BONDING == bluetoothState) {
            return getContext().getString(R.string.bonding);
        }
        if (BluetoothDevice.BOND_BONDED == bluetoothState) {
            return getContext().getString(R.string.bonded);
        }
        return "";
    }

}
