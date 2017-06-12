import socket
import cv2

ip = '192.168.178.21'
ip = "172.31.3.95"

def keyup(e):
    print ('up', e.keycode)
def keydown(e):
    print ('down', e.char)

def send(platform, cmd):
    # if not platform.isOpen():
    #     print("not connected")
    # platform.send((cmd + "\r\n").encode())
    platform.sendto((cmd).encode(), (ip, 8080))
    print (cmd)

def stop(platform):
    send(platform, "stop 0")

platform = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
platform.sendto("hello".encode(), (ip, 8080))
speed = 150

rot_speed = 0

def init_rot_speed(speed=30):
    global rot_speed
    rot_speed = 0

def manual_ctrl():
    global rot_speed, speed
    dir = 0
    while True:
        cv2.namedWindow('main')
        k = cv2.waitKey(1)
        if k & 0xff == ord('q'):
            break
        elif k == 2424832:
            rot_speed+=10
            if rot_speed > 225:
                rot_speed = 225
            cmd = "left " if rot_speed > 0 else "right "
            send(platform, cmd+str(abs(rot_speed)+30))
            # dir = 0
        elif k == 2490368:
            if dir == 1:
                speed += 20
            else:
                speed = 100
            init_rot_speed()
            send(platform, "fwd "+str(speed))
            dir = 1
        elif k == 2555904:
            rot_speed-=10
            if rot_speed < -225:
                rot_speed = -225
            cmd = "left " if rot_speed > 0 else "right "
            send(platform, cmd+str(abs(rot_speed)+30))
            # dir = 0
        elif k == 2621440:
            if dir == 2:
                speed += 20
            else:
                speed = 100
            send(platform, "back "+str(speed))
            init_rot_speed()
            dir = 2
        elif k == 32:
            stop(platform)
            dir = 0
            init_rot_speed()
        elif k & 0xff != 255:
            print(k)

if __name__ == "__main__":
    manual_ctrl()
