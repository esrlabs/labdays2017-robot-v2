#!/usr/bin/env python3

import ev3dev.ev3 as ev3
from time import time, sleep

class Robot:

#  motorLeft, motorRight = None
#  infrared1, infrared2, ultrasonic = None
    
#  time1, time2, time3 = 0.0
  
  def __init__(self):
    self.originatorId = 'BRICK_1'
    self.motorLeftId = 'ENGINE_1'
    self.motorRightId = 'ENGINE_2'
    self.infrared1Id = 'IR_1'
    self.infrared2Id = 'IR_2'
    self.ultrasonicId = 'US_1'

    self.motorLeft = ev3.LargeMotor('outA')
    self.motorRight = ev3.LargeMotor('outB')
    self.infrared1 = ev3.InfraredSensor('in1')
    self.infrared2 = ev3.InfraredSensor('in4')
    self.ultrasonic = ev3.UltrasonicSensor('in2')

  def setSpeed(self, value):
    time1 = time()
	
    self.motorLeft.run_direct(duty_cycle_sp = value)
	
    time2 = time()
	
    self.motorRight.run_direct(duty_cycle_sp = value)
	
    time3 = time()
	
    line1 = '{}:{:f}:{:f}:{}:{:f}'.format(self.originatorId, time1, time2, self.motorLeftId, value)
    line2 = '{}:{:f}:{:f}:{}:{:f}'.format(self.originatorId, time2, time3, self.motorRightId, value)
    
    print(line1)
    print(line2)
	
  def getSensorData(self):
    time1 = time()
    value = self.infrared1.value()
    time2 = time()
	
    line1 = '{}:{:f}:{:f}:{}:{:f}'.format(self.originatorId, time1, time2, self.infrared1Id, value)
	
    time1 = time()
    value = self.infrared2.value()
    time2 = time()
	
    line2 = '{}:{:f}:{:f}:{}:{:f}'.format(self.originatorId, time1, time2, self.infrared2Id, value)
	
    time1 = time()
    value = self.ultrasonic.value()
    time2 = time()
	
    line3 = '{}:{:f}:{:f}:{}:{:f}'.format(self.originatorId, time1, time2, self.ultrasonicId, value)
	
    print(line1)
    print(line2)
    print(line3)

	

def main():
    robot_ctrl = Robot()
    robot_ctrl.setSpeed(-25)
    sleep(1)
    robot_ctrl.getSensorData()
    sleep(1)
    robot_ctrl.setSpeed(25)
    sleep(2)
    robot_ctrl.setSpeed(0)

if __name__ == '__main__':
    main()

