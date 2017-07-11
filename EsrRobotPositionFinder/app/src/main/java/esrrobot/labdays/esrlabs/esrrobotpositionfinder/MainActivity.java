package esrrobot.labdays.esrlabs.esrrobotpositionfinder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "EsrRobotPositionFinder";

    // Used to load the 'native-lib' library on application startup.
    static {
    }

    public MainActivity(){
        Log.d(LOG_TAG, "Initializing opencv");
        if(OpenCVLoader.initDebug()){
            Log.d(LOG_TAG, "Initialized opencv");
        }else{
            Log.w(LOG_TAG, "Cannot initialize opencv");
        }

        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BarcodeDetector barcodeDetector;
        MqttClient mqttClient;

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);

        tv.setText(stringFromJNI());
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
