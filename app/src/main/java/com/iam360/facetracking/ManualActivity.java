package com.iam360.facetracking;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

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
