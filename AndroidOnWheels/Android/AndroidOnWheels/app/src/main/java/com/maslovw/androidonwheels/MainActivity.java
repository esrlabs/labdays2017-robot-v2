package com.maslovw.androidonwheels;
import android.os.Handler;
import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.content.ServiceConnection;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Handler mHandler;

    private void run_around(){
        Log.w(TAG, "Automation connect and drive");
        mPlatform.connect();
        SystemClock.sleep(1000);
        mPlatform.driveFwd(150);
        SystemClock.sleep(1000);
        mPlatform.driveBack(150);
        SystemClock.sleep(1000);
        mPlatform.stop();
    }

    BtSerial btSerial = new BtSerial();
    ServerUdpCommand serverUdpCommand;

    TextView msg;
    TextView ip;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mPlatformBtLe = ((PlatformBtLe.LocalBinder) service).getService();

            Toast.makeText(MainActivity.this, "init", Toast.LENGTH_SHORT).show();

            if (!mPlatformBtLe.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mPlatform = new Platform(mPlatformBtLe);
            isInit = true;
            mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
              @Override
              public void run() {
                  run_around();
              }
          }, 5000);
            // Automatically connects to the device upon successful start-up initialization.
            //mPlatformBtLe.connect(mDeviceAddress);

            msg = (TextView) MainActivity.this.findViewById(R.id.msg);
            ip = (TextView) MainActivity.this.findViewById(R.id.infoip);
            serverUdpCommand = new ServerUdpCommand(
                    new ServerUdpCommand.Receiver() {
                        @Override
                        public boolean onReceive(final String string) {

                            MainActivity.this.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    msg.setText("received " + string);
                                }
                            });

                            return MainActivity.this.mPlatform.onReceive(string);
                        }

                        @Override
                        public void onOpened(final String ip) {

                            MainActivity.this.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    MainActivity.this.ip.setText("received " + ip);
                                }
                            });

                        }
                    }
            );
            serverUdpCommand.execute(mPlatform);
            btSerial.connect(MainActivity.this, MainActivity.this, mPlatform);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mPlatformBtLe = null;
        }
    };

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent gattServiceIntent = new Intent(this, PlatformBtLe.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_COARSE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "OS START");
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public PlatformBtLe mPlatformBtLe;
    private final static String TAG = "MainAct";
    public Platform mPlatform;
    private boolean isInit = false;



    public void onButtonConnectClick(View b) {
        mPlatform.connect();
    }

    public void onButtonFwd(View b) {
        mPlatform.driveFwd(100);
    }

    public void onButtonDown(View b) {
        mPlatform.driveBack(100);
    }

    public void onButtonStop(View b) {
        mPlatform.stop();
    }

}

