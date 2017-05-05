package com.iam360.engine.control;

import android.content.Context;
import android.content.Intent;

import com.iam360.engine.connection.BluetoothConnectionCallback;

/**
 * Created by Lotti on 5/4/2017.
 */

public class RemoteButtonListener implements BluetoothConnectionCallback.ButtonValueListener {
    public static final String UPPER_BUTTON_PRESSED = "com.iam360.engine.connection.REMOTE_UPPER_BUTTON";
    public static final String LOWER_BUTTON_PRESSED = "com.iam360.engine.connection.REMOTE_LOWER_BUTTON";
    private final Context context;
    private boolean isUpper;

    public RemoteButtonListener(boolean isUpper, Context context){
        this.isUpper = isUpper;
        this.context = context;
    }
    @Override
    public void buttomPressed() {
        Intent i;
        if(isUpper){
            i = new Intent(UPPER_BUTTON_PRESSED);
        }else{
            i = new Intent(LOWER_BUTTON_PRESSED);
        }
        context.sendBroadcast(i);
    }
}
