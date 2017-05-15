package com.iam360.engine.control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.iam360.engine.connection.BluetoothConnector;

import static com.iam360.engine.control.RemoteButtonListener.LOWER_BUTTON_PRESSED;
import static com.iam360.engine.control.RemoteButtonListener.UPPER_BUTTON_PRESSED;

/**
 * Created by Lotti on 5/14/2017.
 */

public class ButtomReciever extends BroadcastReceiver {
    private final ButtomRecieveListener upper;
    private final ButtomRecieveListener lower;

    public ButtomReciever(ButtomRecieveListener upper, ButtomRecieveListener lower){
        this.upper = upper;
        this.lower = lower;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case UPPER_BUTTON_PRESSED:
                upper.recieved();
                break;
            case LOWER_BUTTON_PRESSED:
                lower.recieved();
                break;
        }
    }

    public interface ButtomRecieveListener{
        void recieved();
    }
}
