package com.maslovw.androidonwheels;

import android.Manifest;
import android.content.ServiceConnection;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by viacheslav.maslov on 15-Dec-16.
 */

public class PlatformBtLe extends Service {

    private final static String TAG = PlatformBtLe.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothScanner;
    private String mBluetoothDeviceAddress = "98:4F:EE:0F:BA:88";
    public  BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private BluetoothDevice mDevice;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;


    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean  initialize( ) {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        Log.d(TAG, "Init.start");
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothScanner == null) {
            Log.e(TAG, "Unabele to obtain a scanner");
//            return false;
        }
        mHandler = new Handler();
        Log.d(TAG, "init.finished");
        return true;
    }

    public String btAddress;

    private ScanCallback mScanCallBack = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
//            Toast.makeText(getApplicationContext()
//                    , String.format("OnScanResult %d %s", callbackType, result.toString())
//                    , Toast.LENGTH_SHORT).show();
            Log.w(TAG, String.format("Scan res: %s", result.toString()));
            btAddress = result.getDevice().getAddress();
            Log.w(TAG, btAddress);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Toast.makeText(getApplicationContext()
                    , String.format("onBatchScanResults %d", results.size())
                    , Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(getApplicationContext()
                    , String.format("onScanFailed %d", errorCode)
                    , Toast.LENGTH_SHORT).show();

        }
    };

    private Handler mHandler;

    public boolean scan() {
          mHandler.postDelayed(new Runnable() {
              @Override
              public void run() {
                  Log.w(TAG, "Timeout -> stop scanning");
                  mBluetoothScanner.stopScan(mScanCallBack);
              }
          }, 1000);
        mBluetoothScanner.startScan(mScanCallBack);
        return false;
    }
    public boolean connect(String address) {
        Log.d(TAG, "connecting " + address);
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                //return true;
            } else {
                Log.e(TAG, "can't connect");
                return false;
            }
        }

        mDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (mDevice == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = mDevice.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;

        if (mBluetoothGatt == null)
        {
            Log.e(TAG, "mBluetoothGatt is null");
            return false;
        }
        if (!discover_service())
        {
            return discover_service();
        }

        return true;
    }

    BluetoothGattCharacteristic mControlCharacteristics;

    private boolean discover_service()
    {

        for (BluetoothGattService serv: mBluetoothGatt.getServices()) {
            for (BluetoothGattCharacteristic car: serv.getCharacteristics()) {
                Log.w(TAG, String.format("char: %s", car.getUuid().toString()));
                if (car.getUuid().toString().equals("19b10001-e8f2-537e-4f6c-d104768a1214"))
                {
                    Log.w(TAG, String.format("add this char: %s", car.getUuid().toString()));
                    mControlCharacteristics = car;
                }
            }
        }
        return mControlCharacteristics != null;
    }
    public boolean send(byte[] value)
    {
        if (mDevice == null)
        {
            connect(mBluetoothDeviceAddress);
        }

        if (mBluetoothManager.getConnectionState(mDevice, BluetoothProfile.GATT_SERVER) != BluetoothProfile.STATE_CONNECTED)
        {
            if(!connect(mBluetoothDeviceAddress))
                return false;
        }
        
        if (mControlCharacteristics == null)
        {
            if (!discover_service())
            {
                return false;
            }
        }
//        if(mBluetoothGatt.readCharacteristic(mControlCharacteristics)) {
//            byte[] rval = mControlCharacteristics.getValue();
//            if (rval != null)
//                Log.w(TAG, "read:" + rval[1]);
//        }
        mControlCharacteristics.setValue(value);
        boolean res = mBluetoothGatt.writeCharacteristic(mControlCharacteristics);
//        Log.w(TAG, String.format("write res %d", res));
        return res;
    }
    public boolean send(int value)
    {
        if (mControlCharacteristics == null)
        {
            if (!discover_service())
            {
                return false;
            }
        }
        byte[] dataToWrite = new byte[]{(byte)value};
        mControlCharacteristics.setValue(dataToWrite);
        boolean res = mBluetoothGatt.writeCharacteristic(mControlCharacteristics);
//        Log.w(TAG, String.format("write res %d", res));
        return res;
    }

    public final static String ACTION_GATT_CONNECTED =
            "com.android_on_wheels.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.android_on_wheels.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.android_on_wheels.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.android_on_wheels.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.android_on_wheels.bluetooth.le.EXTRA_DATA";

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
        }
    };
    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public class LocalBinder extends Binder {
        PlatformBtLe getService() {
            return PlatformBtLe.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular android_on_wheels, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();




}
