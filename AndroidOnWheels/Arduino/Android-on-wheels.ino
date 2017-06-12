

/* 
This is a test sketch for the Adafruit assembled Motor Shield for Arduino v2
It won't work with v1.x motor shields! Only for the v2's with built in PWM
control

For use with the Adafruit Motor Shield v2 
---->	http://www.adafruit.com/products/1438
*/

#include <Wire.h>
#include <Adafruit_MotorShield.h>
#include "utility/Adafruit_MS_PWMServoDriver.h"
#include "Bamper.h"
#include "bt.h"
// Create the motor shield object with the default I2C address
Adafruit_MotorShield AFMS = Adafruit_MotorShield(); 

// Select which 'port' M1, M2, M3 or M4. In this case, M1
Adafruit_DCMotor *left_motor = AFMS.getMotor(4);
Adafruit_DCMotor *right_motor = AFMS.getMotor(3);
// You can also make another motor on port M2
//Adafruit_DCMotor *myOtherMotor = AFMS.getMotor(2);

int photo_transistor_pin0 = A0;
int photo_transistor_pin1 = A1;
int photo_transistor_pin2 = A2;
int sensor_value = 0;

void test_motor()
{

  for (int i=60; i > 100; i++) {
    drive(left_motor,100, FORWARD);
    delay(100);
  }

  left_motor->run(RELEASE);
  right_motor->run(RELEASE);
}

void setup() {

  Serial.begin(9600);           // set up Serial library at 9600 bps
  Serial.println("Adafruit Motorshield v2 - DC Motor test!");

  //AFMS.begin();  // create with the default frequency 1.6KHz
  AFMS.begin(1000);  // OR with a different frequency, say 1KHz
  
  // Set the speed to start, from 0 (off) to 255 (max speed)
  // turn on motor
  left_motor->run(RELEASE);
  right_motor->run(RELEASE);
  //bamperSetup();
  btSetup();
  test_motor();
}

void drive(Adafruit_DCMotor *aMotor,int s, int dir) 
{
  aMotor->setSpeed(s);
  aMotor->run(dir);
}

void drive(char id, char speed, char dir)
{
  static char ldir = dir;
  static char rdir = dir;
  switch(id)
  {
    case 1:
      ldir = dir;
      drive(left_motor, speed, (dir == 1) ? FORWARD: BACKWARD);
      break;
    case 2:
      rdir = dir;
      drive(right_motor, speed, (dir == 1) ? FORWARD: BACKWARD);
      break;

    case 3:
      {
        if (ldir != dir || rdir != dir)
        {
          left_motor->run(RELEASE);
          right_motor->run(RELEASE);
        }
        ldir = dir;
        rdir = dir;       
        left_motor->setSpeed(speed);
        right_motor->setSpeed(speed);
  
        int _dir = (dir == 1) ? FORWARD: BACKWARD;
        left_motor->run(_dir);
        right_motor->run(_dir);
      }
      break;    
  }
}

#define SPEED 80
#define SPEED_BACKWARD 5

void driveR()
{
  drive(right_motor, SPEED_BACKWARD, BACKWARD);
  drive(left_motor, SPEED, FORWARD);
}

void driveL()
{
  drive(left_motor, SPEED_BACKWARD, BACKWARD);
  drive(right_motor, SPEED, FORWARD);
}

void driveF()
{
  drive(right_motor, SPEED, FORWARD);
  drive(left_motor, SPEED, FORWARD);
}

void driveB()
{
  drive(right_motor, SPEED, BACKWARD);
  drive(left_motor, SPEED, BACKWARD);
}


void rotaten()
{
  drive(right_motor, SPEED, BACKWARD);
  drive(left_motor, SPEED, FORWARD);
}


void stop()
{
  drive(right_motor, 0, RELEASE);
  drive(left_motor, 0, RELEASE);
}

static const int offset = 600;

void loop() {
  // value_ == 1 on the black line
  // value_ == 0 on the white surface
  //auto valueL = (analogRead(photo_transistor_pin2) > offset) ? 1 : 0; 
  //auto valueF = (analogRead(photo_transistor_pin0) > offset) ? 1 : 0;
  //auto valueR = (analogRead(photo_transistor_pin1) > offset) ? 1 : 0;

  btLoop();
  return;

 /* if (bamperLoop())
  {
    //stop();
    rotaten();
    return;
  }
  if (valueL && !valueR)
  {
    //Serial.println("right");
    driveL();
    return;
  }
  else if (valueR && !valueL)
  {
    //Serial.println("left");
    driveR();
    return;
  }
  else if (valueF && !valueL && !valueR)
  {
    driveF();
    return;
  }
  else if (valueF && valueL && valueR)
  {
    stop();
    return;
  }
  else
  {    
    // slow down
    //drive(right_motor, SPEED-20, FORWARD);
    //drive(left_motor, SPEED-20, FORWARD);
  }
*/
}
