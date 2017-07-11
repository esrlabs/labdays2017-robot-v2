package esrrobot.labdays.esrlabs.esrrobotpositionfinder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "EsrRobotPositionFinder";
    private MQTTConnection mqtt;

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

        try {
            mqtt = new MQTTConnection(this);
        } catch (MqttException e) {
            Log.e(LOG_TAG, "cannot create MQTT connection",e);
        }

        mqtt.connect(new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(LOG_TAG, "message arrived 2.0:" + message);
            }
        });

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
