package com.iam360.views.settings;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.iam360.myapplication.R;
import com.iam360.views.settings.SettingsFragment.OnListFragmentInteractionListener;
import com.iam360.views.settings.dummy.DummyContent.DummyItem;

import java.util.List;
import java.util.Map;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class SettingsAdapter extends BaseAdapter {

    private final View.OnClickListener[] listeners;
    Context context;
    String[] data;

    private static LayoutInflater inflater = null;

    public SettingsAdapter(Context context, String[] data, View.OnClickListener[] listeners) {
        this.context = context;
        this.data = data;
        this.listeners = listeners;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.settings_row, null);
        TextView text = (TextView) vi.findViewById(R.id.text);
        text.setText(data[position]);
        vi.setOnClickListener(listeners[position]);
        return vi;
    }
}
