#!/usr/bin/env python
import queue
from docopt import docopt
import paho.mqtt.client as mqtt
from threading import Thread, Timer


class CommServer(Thread):
    def __init__(self, sub_topic, send_topic):
        self._mqtt_client = mqtt.Client()
        self._mqtt_client.on_connect = self.on_connect
        self._mqtt_client.on_message = self.on_message
        self._sub_topic = sub_topic
        self._send_topic = send_topic

    def start_subscription(self, ip, port, keepalive):
        self._mqtt_client.connect(ip, port, keepalive)
        self._mqtt_client.loop_start()

    def send_msg(self, message):
        self._mqtt_client.publish("{}/out".format(self._send_topic), message)

    def on_connect(self, client, userdata, flags, rc):
        if rc == 0:
            print("Connected from BRICK_1 successfully")
        else:
            print("Connection from BRICK_1 failed")
        self._mqtt_client.subscribe("{}/#".format(self._topic))

    def on_message(self, client, userdata, msg):
        # ToDo Add Callback to Brick_01.py
        pass


def main():
    '''
    usage:
    brick [--send_topic name] [--sub_topic name] [--ip address] [--port number] [--keepalive seconds]

    options:

    --ip adress             ip of the MQTT Broker [default: 172.32.2.167]
    --port number           port of the MQTT Broker [default: 1883]
    --keepalive seconds     maximum period in seconds allowed between communications with the broker [default: 60]
    --sub_topic name        Topic Channel to subscribe to messages [default: brick]
    --send_topic name       Topic Channel to send messages [default: log]
    --help
    '''
    args = docopt(main.__doc__)
    sub_topic = args['--sub_topic']
    send_topic = args['--send_topic']
    ip = args['--ip']
    port = int(args['--port'], 10)
    keepalive = int(args['--keepalive'], 10)
    cmd_server = CommServer(send_topic, sub_topic)
    cmd_server.start_subscription(ip, port, keepalive)


if __name__ == '__main__':
    main()
