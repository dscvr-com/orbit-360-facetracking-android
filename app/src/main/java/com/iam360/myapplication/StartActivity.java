package com.iam360.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


//TODO  work with loading splashscreen first and then this with the same screen but not animated?
//here load gif

public class StartActivity extends AppCompatActivity {

    static {
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        startActivity(new Intent(this, ManualActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //FIXME Step here to first view

    }
}
