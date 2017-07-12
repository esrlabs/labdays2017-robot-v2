#!/usr/bin/env python3

import io.MotorControl as MotorControl
import io.SensorArray as SensorArray

import ev3dev.ev3 as ev3
from time import time, sleep

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

  def setSpeed(self, value):
    time1 = time()
    
    self.motors.setSpeedLeft(value)
    
    time2 = time()
    
    self.motors.setSpeedRight(value)
    
    time3 = time()
    
    line1 = '{}:{:f}:{:f}:{}:{:f}'.format(self.originatorId, time1, time2, self.motorLeftId, value)
    line2 = '{}:{:f}:{:f}:{}:{:f}'.format(self.originatorId, time2, time3, self.motorRightId, value)
    
    print(line1)
    print(line2)
    
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
    
    print(line1)
    print(line2)
    print(line3)

    

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

