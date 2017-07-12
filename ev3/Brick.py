#!/usr/bin/env python3

from robo_io.MotorControl import MotorControl
from robo_io.SensorArray import SensorArray
from comm.CommServer import CommServer

import ev3dev.ev3 as ev3
from time import time, sleep
import queue
from threading import Timer


class Brick:
    def __init__(self):
        self.originatorId = 'BRICK_1'
        self.motorLeftId = 'ENGINE_1'
        self.motorRightId = 'ENGINE_2'
        self.infrared1Id = 'IR_1'
        self.infrared2Id = 'IR_2'
        self.ultrasonicId = 'US_1'

        self.motors = MotorControl('outA', 'outB')
        self.sensors = SensorArray('in1', 'in4', 'in2')
        self._msg_queue = queue.Queue()
        self.commserver = CommServer('brick', 'log', self.data_received)
        self.commserver.est_conn()

    def data_received(self, msg):
        command_id, command_data = msg.split(":")
        self._msg_queue.put((self.setSpeed, command_data))

    def run_queue_listener(self):
        while True:
            e = self._msg_queue.get(timeout=10)
            if not e:
                self.getSensorData()
                self.reset_queue_listener()
            else:
                e[0](e[1])

    def reset_queue_listener(self):
        t = Timer(0.1, self.run_queue_listener)
        t.start()
        self._msg_queue.put(None)

    def setSpeed(self, command_data):
        time1 = time()
        leftspeed, rightspeed = int(command_data.split(","), 10)
        self.motors.setSpeedLeft(leftspeed)

        time2 = time()

        self.motors.setSpeedRight(rightspeed)

        time3 = time()

        line1 = '{}:{:f}:{:f}:{}:{:f}'.format(self.originatorId, time1, time2, self.motorLeftId, value)
        line2 = '{}:{:f}:{:f}:{}:{:f}'.format(self.originatorId, time2, time3, self.motorRightId, value)

        self.commserver.send_msg(self, line1)
        self.commserver.send_msg(self, line2)
        print("Successfully send {} and {} to MQTT".format(line1, line2))

    def getSensorData(self):
        time1 = time()
        value = self.sensors.getData('IR_1')
        time2 = time()

        line1 = '{}:{:f}:{:f}:{}:{:f}'.format(self.originatorId, time1, time2, self.infrared1Id, value)

        time1 = time()
        value = self.sensors.getData('IR_2')
        time2 = time()

        line2 = '{}:{:f}:{:f}:{}:{:f}'.format(self.originatorId, time1, time2, self.infrared2Id, value)

        time1 = time()
        value = self.sensors.getData('US')
        time2 = time()

        line3 = '{}:{:f}:{:f}:{}:{:f}'.format(self.originatorId, time1, time2, self.ultrasonicId, value)

        self.commserver.send_msg(self, line1)
        self.commserver.send_msg(self, line2)
        self.commserver.send_msg(self, line3)
        print("Successfully send {}, {} and {} to MQTT".format(line1, line2, line3))


def main():
    brick = Brick()
    brick.setSpeed(-25)
    sleep(1)
    brick.getSensorData()
    sleep(1)
    brick.setSpeed(25)
    sleep(2)
    brick.setSpeed(0)


if __name__ == '__main__':
    main()
