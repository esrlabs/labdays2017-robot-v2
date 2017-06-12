#include <BLEAttribute.h>
#include <BLECentral.h>
#include <BLECharacteristic.h>
#include <BLECommon.h>
#include <BLEDescriptor.h>
#include <BLEPeripheral.h>
#include <BLEService.h>
#include <BLETypedCharacteristic.h>
#include <BLETypedCharacteristics.h>
#include <BLEUuid.h>
#include <CurieBLE.h>


/*
 * Copyright (c) 2016 Intel Corporation.  All rights reserved.
 * See the bottom of this file for the license terms.
 */
void driveR();
void driveL();

void driveF();
void driveB();
void stop();
void drive(char id, char speed, char dir);

// cycle counter to stop
// motors if there was no
// cmd in a long period of time
unsigned long cycle = 0;

#include <CurieBLE.h>

const int ledPin = 13; // set ledPin to use on-board LED
BLEPeripheral blePeripheral; // create peripheral instance

BLEService ledService("19B10000-E8F2-537E-4F6C-D104768A1214"); // create service

// create switch characteristic and allow remote device to read and write
//BLECharCharacteristic switchChar("19B10001-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);
BLECharacteristic switchChar("19B10001-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite, 8);
static unsigned char btValue = 0;

void blePeripheralConnectHandler(BLECentral& central) {
  // central connected event handler
  Serial.print("Connected event, central: ");
  Serial.println(central.address());
  
  digitalWrite(ledPin, LOW);
}

void blePeripheralDisconnectHandler(BLECentral& central) {
  // central disconnected event handler
  stop();
  Serial.print("Disconnected event, central: ");
  Serial.println(central.address());
  
  
  digitalWrite(ledPin, HIGH);
  btValue = 0;
}

void switchCharacteristicWritten(BLECentral& central, BLECharacteristic& characteristic) {
  // central wrote new value to characteristic, update LED
  Serial.print("Characteristic event, written: ");
  Serial.println(switchChar[0]);
  cycle = 0;
  
  if (switchChar.valueLength() > 1)
  {
    drive(switchChar[0], switchChar[1], switchChar[2]);
    if (switchChar.valueLength() > 4)
    {
      drive(switchChar[4], switchChar[5], switchChar[6]);
    }
    cycle = 0;
    return;
  }

  btValue = switchChar[0];

  switch(btValue)
  {
    case 0:
      stop();
    break;

    case 1:
      driveF();
      break;

    case 2:
      driveL();
      break;

    case 3:
      driveR();
      break;

    case 4:
      driveB();
      break;
  }
  cycle = 0;
}


void btSetup() {
  //Serial.begin(9600);
  pinMode(ledPin, OUTPUT); // use the LED on pin 13 as an output

  // set the local name peripheral advertises
  blePeripheral.setLocalName("BtLePlatform");
  // set the UUID for the service this peripheral advertises
  blePeripheral.setAdvertisedServiceUuid(ledService.uuid());

  // add service and characteristic
  blePeripheral.addAttribute(ledService);
  blePeripheral.addAttribute(switchChar);

  // assign event handlers for connected, disconnected to peripheral
  blePeripheral.setEventHandler(BLEConnected, blePeripheralConnectHandler);
  blePeripheral.setEventHandler(BLEDisconnected, blePeripheralDisconnectHandler);

  // assign event handlers for characteristic
  switchChar.setEventHandler(BLEWritten, switchCharacteristicWritten);
// set an initial value for the characteristic
//  switchChar.setValue(0);

  // advertise the service
  blePeripheral.begin();
  
  digitalWrite(ledPin, HIGH);
  Serial.println(("Bluetooth device active, waiting for connections..."));
}

unsigned char val[4] = {0, 1, 2 ,3};
unsigned char btLoop() {
  // poll peripheral
  if (++cycle % 2000 == 0)
  {
    Serial.println("cycle");
    stop();
    switchChar.setValue(val, 4);
  }
  blePeripheral.poll();
  return btValue;
}


/*
  Copyright (c) 2016 Intel Corporation. All rights reserved.

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-
  1301 USA
*/
