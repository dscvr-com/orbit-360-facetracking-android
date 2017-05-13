package com.iam360.views.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.iam360.facetracking.R;

public class SettingsAdapter extends BaseAdapter {

    private final View.OnClickListener[] listeners;
    Context context;
    String[] data;

    public SettingsAdapter(Context context, String[] data, View.OnClickListener[] listeners) {
        this.context = context;
        this.data = data;
        this.listeners = listeners;

    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = ((LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.settings_row, parent, false);
        TextView text = (TextView) convertView.findViewById(R.id.text);
        text.setText(data[position]);
        convertView.setOnClickListener(listeners[position]);
        convertView.bringToFront();
        return convertView;
    }
}
