package com.iam360.motor.connection;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.iam360.myapplication.R;

import java.util.List;

/**
 * Class to provide the List View the founded bluetooth devices
 * Created by Charlotte on 12.11.2016.
 */
public class BluetoothDataAdapter extends ArrayAdapter<BluetoothDevice> {
    public BluetoothDataAdapter(Context context, List<BluetoothDevice> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothDevice device = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_bluetooth, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        // Populate the data into the template view using the data object
        tvName.setText(device.getName());
        // Return the completed view to render on screen
        return convertView;
    }
}
