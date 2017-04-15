package com.iam360.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.iam360.views.manual.ManualFragmentPageAdapter;
import com.iam360.views.manual.ManualPageFragment;

public class ManualActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageButton closeButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
        closeButton = (ImageButton) findViewById(R.id.manual_close);
        closeButton.bringToFront();
        closeButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(this, CameraActivity.class));

    }
}
