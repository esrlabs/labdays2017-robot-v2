package esrrobot.labdays.esrlabs.esrrobotpositionfinder;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


/**
 * Created by molka.elleuch on 11/07/2017.
 */

public class MQTTConnection {
    String clientID = MqttClient.generateClientId();
    MqttAndroidClient client;

    public MQTTConnection(Context context) throws MqttException {
        client = new MqttAndroidClient(context, "tcp://172.32.2.167:1883", clientID);
    }

    public boolean connect(final IMqttMessageListener messageListener) {
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("MQTTConnection", "onConnectionSuccess");
                    String[] topics = {"phone/in"};
                    int[] qos = {1};
                    try {
                        IMqttToken tkn = client.subscribe(topics, qos, new IMqttMessageListener[]{messageListener});
                        /*tkn.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                Log.d("MQTTConnection", "onSubscriptionSucess");
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                Log.e("MQTTConnection", "onSubscriptionFailure", exception);
                            }
                        });*/


                    } catch (MqttException e) {
                        Log.e("MQTTConnection", "cannot subscribe", e);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("MQTTConnection", "onFailure", exception);
                }
            });
            return true;
        } catch (MqttException e) {
            Log.e("MQTTConnection", "cannot connect", e);
            return false;
        }
    }

    public void publishMessage(String m){
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(m.getBytes());
            client.publish("phone/out", message);
            Log.d("MQTTConnection", "Message Published");
        } catch (MqttException e) {
            Log.e("MQTTConnection", "Error Publishing: ", e);
        }
    }
}
