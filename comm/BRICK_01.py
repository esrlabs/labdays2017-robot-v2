#!/usr/bin/env python
import queue
from docopt import docopt
import paho.mqtt.client as mqtt
from threading import Thread


class CommandServer(Thread):

    def __init__(self, topic):
        self._msg_queue = queue.Queue()
        self._mqtt_client = mqtt.Client()
        self._mqtt_client.on_connect = self.on_connect
        self._mqtt_client.on_message = self.on_message
        self._topic = topic

    def start(self, ip, port, keepalive):
        self._mqtt_client.connect(ip, port, keepalive)
        self._mqtt_client.loop_start()
        self.run()

    def run(self):
        while True:
            e = self._msg_queue.get(timeout=10)
            print(e)

    def on_connect(self, client, userdata, flags, rc):
        if rc == 0:
            print("Connected from BRICK_1 successfully")
        else:
            print("Connection from BRICK_1 failed")
        self._mqtt_client.subscribe("{}/#".format(self._topic))

    def on_message(self, client, userdata, msg):
        self._msg_queue.put(msg.topic + " " + str(msg.payload))


def main():
    '''
    usage:
    brick [--topic name] [--ip address] [--port number] [--keepalive seconds]

    options:

    --ip adress             ip of the MQTT Broker [default: 172.32.2.167]
    --port number           port of the MQTT Broker [default: 1883]
    --keepalive seconds     maximum period in seconds allowed between communications with the broker [default: 60]
    --topic name            Topic Channel to send messages [default: brick]
    --help
    '''
    args = docopt(main.__doc__)
    topic = args['--topic']
    ip = args['--ip']
    port = int(args['--port'], 10)
    keepalive = int(args['--keepalive'], 10)
    cmd_server = CommandServer(topic)
    cmd_server.start(ip, port, keepalive)

if __name__ == '__main__':
    main()
