package iam360.com.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import com.iam360.views.record.RecorderPreviewView;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String TAG = "MainActivity";
    RecorderPreviewView recordPreview;

    @Override
    public void onResume() {
        super.onResume();
        if(recordPreview!= null){
            recordPreview.onResume();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if(recordPreview!= null) {
            recordPreview.onPause();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestCameraPermission();
        recordPreview = new RecorderPreviewView(this);
        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_main);
        layout.addView(recordPreview);
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);

            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Log.e(TAG, "No Camera permission");
                }
                return;
            }

        }
    }
}

