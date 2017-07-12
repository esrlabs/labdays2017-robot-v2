#!/usr/bin/env python3

from comm.CommServer import CommServer

from time import time, sleep
import queue
from threading import Timer


class Pi:
    def __init__(self):
        self.originatorId = 'RASP'
        self.speedCommand = 'SPEED'
		
		self.phone_qrcode = ''
		self.phone_angle = 0.0
		self.phone_xpos = 0.0
		self.phone_ypos = 0.0
		self.brick_ir1 = 0
		
		self.motor_left_speed = 0
		self.motor_right_speed = 0

        self._msg_queue = queue.Queue()
        self.commserver = CommServer('#/out', 'brick/in', self.data_received)
        self.commserver.est_conn('172.32.2.167', 1883, 60)

    def data_received(self, msg):
        sensordata = msg.split("\n")
		
		got_new_data_from_brick = False
		ir1_value = 0
		ir2_value = 0
		us_value = 0
		
		for i in sensordata.length:
		    dataparts = sensordata[i].split(":")
		  
		    if dataparts[0] == 'BRICK_1':
			    if dataparts.length == 5:
				    if dataparts[3] == 'IR_1':
					    ir1_value = int(dataparts[4])
					elif dataparts[3] == 'IR_2':
					    ir2_value = int(dataparts[4])
					elif dataparts[3] == 'US':
					    us_value = int(dataparts[4])
						
					got_new_data_from_brick = True
		  
		    elif dataparts[0] == 'PHONE_1':
			    if dataparts.length == 5:
				    if dataparts[3] == 'QR_1':
					    qr_data = dataparts[4].split(",")
						if qr_data.length == 4:
						    self.phone_qrcode = qr_data[0]
						    self.phone_angle = qr_data[1]
						    self.phone_xpos = qr_data[2]
						    self.phone_ypos = qr_data[3]
						
		if got_new_data_from_brick:
		    # ToDo Send message to webserver
			
			
			# Evaluate value of IR_1 and generate command for brick
			self.brick_ir1 = ir1_value
			
			threshold1 = 35
			threshold2 = 50
			
			if ir1_value < threshold1:
			    self.motor_left_speed = 30
				self.motor_right_speed = 0
			elif ir1_value > threshold2:
			    self.motor_left_speed = 30
				self.motor_right_speed = 30
				
			# ToDo Send command to brick

    def run_queue_listener(self):
        self._msg_queue.put(None)
        while True:
            e = self._msg_queue.get(timeout=10)
            if not e:
                #self.getSensorData()
                self.reset_queue_listener()
            else:
                #e[0](e[1])
				pass

    def reset_queue_listener(self):
        self.t = Timer(0.5, lambda: self._msg_queue.put(None))
        self.t.start()


    def setSpeed(self, left_speed, right_speed):
        line = '{}:{},{}'.format(self.speedCommand, left_speed, right_speed)
        self.commserver.send_msg(line)
        print("Successfully send {} to MQTT".format(line))


def main():
    pi = Pi()
    
    pi.run_queue_listener()


if __name__ == '__main__':
    main()
