#!/usr/bin/env python3

import ev3dev.ev3 as ev3
from time import time, sleep

class MotorControl:

  def __init__(self, out_left, out_right):
    self.motorLeft = ev3.LargeMotor(out_left)
    self.motorRight = ev3.LargeMotor(out_right)
    self.motorLeft.run_direct(duty_cycle_sp = 0)
    self.motorRight.run_direct(duty_cycle_sp = 0)

  def setSpeedLeft(self, value):
    self.motorLeft.duty_cycle_sp = value

  def setSpeedRight(self, value):
    self.motorRight.duty_cycle_sp = value

  def setSpeed(self, value):
    self.setSpeedLeft(value)
    self.setSpeedRight(value)
    

def main():
    motor_ctrl = MotorControl('outA', 'outB')
    
    motor_ctrl.setSpeedLeft(-25)
    motor_ctrl.setSpeedRight(-25)
    sleep(1)
    motor_ctrl.setSpeedLeft(15)
    motor_ctrl.setSpeedRight(15)
    sleep(2)
    motor_ctrl.setSpeedLeft(-15)
    motor_ctrl.setSpeedRight(15)
    sleep(2)
    motor_ctrl.setSpeed(25)
    sleep(1)
    motor_ctrl.setSpeedLeft(0)
    motor_ctrl.setSpeedRight(0)

if __name__ == '__main__':
    main()

