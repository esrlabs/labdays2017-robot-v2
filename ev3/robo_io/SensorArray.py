#!/usr/bin/env python3

import ev3dev.ev3 as ev3
from time import time, sleep

class SensorArray:

  def __init__(self, in_ir1, in_ir2, in_us):
    self.infrared1 = ev3.InfraredSensor(in_ir1)
    self.infrared2 = ev3.InfraredSensor(in_ir2)
    self.ultrasonic = ev3.UltrasonicSensor(in_us)

  def getData(self, sensor_type):
    if sensor_type == 'IR_1':
      return self.infrared1.value()
    elif sensor_type == 'IR_2':
      return self.infrared2.value()
    elif sensor_type == 'US':
      return self.ultrasonic.value()
    else:
      return 0

  def getAllData(self):
    return { 'IR_1' : self.getData('IR_1'),
             'IR_2' : self.getData('IR_2'),
             'US'   : self.getData('US') }

def main():
    sensor_array = SensorArray('in1', 'in4', 'in2')
    
    print(sensor_array.getAllData())
    sleep(1)
    print(sensor_array.getData('IR_1'))
    print(sensor_array.getData('IR_2'))
    print(sensor_array.getData('US'))
    sleep(1)
    print(sensor_array.getAllData())
    print(sensor_array.getData('IR_1'))
    print(sensor_array.getData('IR_2'))
    print(sensor_array.getData('US'))
    sleep(2)
    print(sensor_array.getAllData())

if __name__ == '__main__':
    main()

