import socket
from threading import Timer
import time

Directions = {"Unknown": 0,
              "Forward": 1,
              "Backward": 2}

class Platform:

    def __init__(self, ip, port):
        self.address = None
        self.socket = None
        self.speed = 0
        self.direction = Directions["Unknown"]
        self.slower_timer = None
        self.connect(ip,port)

    def connect(self, ip, port):
        try:
            self.address = (ip, port)
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            self.send('stop 0')
        except:
            print("Can't connect to the ", ip, port)

    def send(self, cmd):
        print("send " + cmd)
        self.socket.sendto(cmd.encode(), self.address)

    def stop(self):
        self.send("stop 0")

    def forward(self, speed):
        self.speed = abs(speed)
        self.send("fwd " + str(self.speed))

    def back(self, speed):
        self.speed = -abs(speed)
        self.send("back " + str(-self.speed))

    def accelerate(self, acceleration):
        speed = abs(self.speed)
        acceleration = abs(acceleration)
        if speed < 255:
            speed += acceleration
            if self.speed < 0:
                self.back(speed)
            else:
                self.forward(speed)
        self.slower_timer = Timer(1.0, self.slow_down, [10])
        self.slower_timer.start()

    def slow_down(self, acceleration):
        speed = abs(self.speed)
        acceleration = abs(acceleration)
        if speed < acceleration:
            self.stop()
        else:
            speed -= acceleration
            if self.speed < 0:
                self.back(speed)
            else:
                self.forward(speed)



if __name__ == "__main__":
    print("connecting")
    # platform = Platform("192.168.178.21", 8080)
    platform = Platform("172.31.3.95", 8080)
    time.sleep(1)
    platform.forward(100)
    time.sleep(1)
    platform.back(199)
    time.sleep(10)

