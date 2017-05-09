package com.iam360.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


//TODO  work with loading splashscreen first and then this with the same screen but not animated?
//here load gif

public class StartActivity extends AppCompatActivity {

    public static final String KEY_MANUAL = "manual";
    static {
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if(sharedPref.contains(KEY_MANUAL)){
            startActivity(new Intent(this, CameraActivity.class));
        }else{
            sharedPref.edit().putBoolean(KEY_MANUAL, true).apply();
            startActivity(new Intent(this, ManualActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //FIXME Step here to first view

    }
}
