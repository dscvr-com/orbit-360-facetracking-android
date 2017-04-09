package com.iam360.views.manual;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iam360.myapplication.R;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Lotti on 4/8/2017.
 */

public class ManualAdapter extends BaseAdapter {
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    private static final int TYPE_COUNT = 2;
    private final Context context;


    private ArrayList<String> data = new ArrayList();
    private ArrayList<Integer>  imageList= new ArrayList();

    public ManualAdapter(Context con) {
        this.context = con;
    }

    public void addTextItem(String text){
        data.add(text);
        notifyDataSetChanged();
    }

    public void addImageItem(int ressourceId){
        data.add(String.valueOf(ressourceId));
        imageList.add(data.indexOf(ressourceId));
        notifyDataSetChanged();
    }
    @Override
    public int getItemViewType(int position) {
        return imageList.contains(position) ? TYPE_IMAGE : TYPE_TEXT;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public String getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        System.out.println("getView " + position + " " + convertView + " type = " + type);
            switch (type) {
                case TYPE_IMAGE:
                    ImageView imgView = new ImageView(context);
                    imgView.setLayoutParams(new ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));
                    imgView.setImageResource(Integer.parseInt(getItem(position)));
                    convertView = imgView;
                    break;
                case TYPE_COUNT:
                    TextView textView = new TextView(context);
                    textView.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    textView.setText(getItem(position));
                    convertView = textView;
                    break;
            }
       return convertView;
    }

}
