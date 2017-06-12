package com.maslovw.androidonwheels;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by viacheslav.maslov on 27-Dec-16.
 */
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener;




public class BtSerial {
    BluetoothSPP bt;
    Platform mPlatform;

    private final String TAG = "BtSerial";
    protected boolean connect(Context context, Activity activity, Platform platform) {
        bt = new BluetoothSPP(context);
        mPlatform = platform;
        Log.w(TAG, "connect");
        if(!bt.isBluetoothAvailable()) {
            return false;
        }

        bt.setOnDataReceivedListener(new OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {

                Log.w(TAG, String.format("data received: %s", message));
                String[] tokens = message.split(" ");
                try {
                    int speed = Integer.parseInt(tokens[1]);
                    switch (tokens[0]) {
                        case "fwd":
                            mPlatform.driveFwd(Integer.parseInt(tokens[1]));
                            break;
                        case "back":
                            mPlatform.driveBack(Integer.parseInt(tokens[1]));
                            break;
                        case "left":
                            mPlatform.driveLeft(speed);
                            break;
                        case "right":
                            mPlatform.driveRight(speed);
                            break;
                        default:
                            mPlatform.stop();
                    }
                } catch (Exception e)
                {
                    Log.e(TAG, e.toString());
                }
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Log.w(TAG, "bt is connected!");
                bt.send("text", true);
            }

            public void onDeviceDisconnected() {
                Log.d(TAG, "bt is disconnected");
            }

            public void onDeviceConnectionFailed() {
            }
        });

        onStart(activity);
        return true;
    }

    public void onStart(Activity activity) {
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            }
        }
    }

}
