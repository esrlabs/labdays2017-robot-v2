#!/usr/bin/env python
import time
from docopt import docopt
import paho.mqtt.client as mqtt


class DataPublisher(object):

    def __init__(self, topic):
        self._mqtt_client = mqtt.Client()
        self._mqtt_client.on_connect = self.on_connect
        self._topic = topic

    def start(self, ip, port, keepalive):
        self._mqtt_client.connect(ip, port, keepalive)
        self._mqtt_client.loop_start()
        self.run()

    def run(self):
        while True:
            time.sleep(2)
            self._mqtt_client.publish("{}/MSG_1".format(self._topic), "testmsg")

    def on_connect(self, client, userdata, flags, rc):
        if rc == 0:
            print("Connected to RASP successfully")
        else:
            print("Connection to RASP failed")


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
    publisher = DataPublisher(topic)
    publisher.start(ip, port, keepalive)


if __name__ == '__main__':
    main()
