package com.iam360.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.iam360.views.BaseActivity;

public class ManualActivity extends BaseActivity implements View.OnClickListener{
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
