package com.maslovw.androidonwheels;

import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by viacheslav.maslov on 21-Dec-16.
 */

public class Platform {

    private boolean _isConnected = false;

    private Motor _leftMotor = new Motor ("Left", (byte) 1);
    private Motor _rightMotor = new Motor ("Right", (byte) 2);

    public PlatformBtLe thisBtLe;
    private int mSpeed = 0;
    private Motor.Direction mDir = Motor.Direction.UNKNOWN;

    public Platform(PlatformBtLe platformBtLe)
    {
        thisBtLe = platformBtLe;
    }

    public boolean connect()
    {
        Log.d("Platform", "connecting");
        return thisBtLe.connect("98:4F:EE:0F:BA:88");
    }

    public boolean disconnect()
    {
        return false;
    }

    private void sendData()
    {
        byte[] a = _leftMotor.getArrary();
        byte[] c;
        if(_leftMotor.get_speed() == _rightMotor.get_speed() &&
                _leftMotor.get_direction() == _rightMotor.get_direction())
        {
            c = new byte[a.length];
            System.arraycopy(a, 0, c, 0, a.length);
            c[0] = 3;
        }
        else {
            byte[] b = _rightMotor.getArrary();
            c = new byte[a.length + b.length];
            System.arraycopy(a, 0, c, 0, a.length);
            System.arraycopy(b, 0, c, a.length, b.length);
        }

        thisBtLe.send(c);
    }

    public void driveFwd(int speed)
    {
        _leftMotor.setSpeed(speed, Motor.Direction.FWD);
        _rightMotor.setSpeed(speed, Motor.Direction.FWD);
        mSpeed = speed;
        if (mSpeed >= 255)
            mSpeed = 255;
        mDir = Motor.Direction.FWD;
        sendData();
    }

    public void driveBack(int speed)
    {
        _leftMotor.setSpeed(speed, Motor.Direction.BACK);
        _rightMotor.setSpeed(speed, Motor.Direction.BACK);
        mSpeed = speed;
        if (mSpeed >= 255)
            mSpeed = 255;
        mDir = Motor.Direction.BACK;
        sendData();
    }

    public void driveRight(int speed)
    {
        if (mDir == Motor.Direction.FWD) {
            int lspeed = mSpeed + speed;
            int rspeed = mSpeed;
            if (mSpeed >= 220) {
                lspeed = mSpeed;
                rspeed = mSpeed - speed;
            }
            _leftMotor.setSpeed(lspeed, Motor.Direction.FWD);
            _rightMotor.setSpeed(rspeed, Motor.Direction.FWD);
        }
        else //if (mDir == Motor.Direction.UNKNOWN)
        {
            _leftMotor.setSpeed(speed, Motor.Direction.FWD);
            _rightMotor.setSpeed(speed, Motor.Direction.BACK);
            mDir = Motor.Direction.UNKNOWN;
        }
        sendData();
    }

    public void driveLeft(int speed)
    {
        if (mDir == Motor.Direction.FWD) {
            int lspeed = mSpeed;
            int rspeed = mSpeed + speed;
            if (mSpeed >= 220) {
                lspeed = mSpeed - speed;
                rspeed = mSpeed;
            }
            _rightMotor.setSpeed(rspeed, Motor.Direction.FWD);
            _leftMotor.setSpeed(lspeed, Motor.Direction.FWD);
        }
        else//if (mDir == Motor.Direction.UNKNOWN)
        {
            _leftMotor.setSpeed(speed, Motor.Direction.BACK);
            _rightMotor.setSpeed(speed, Motor.Direction.FWD);
            mDir = Motor.Direction.UNKNOWN;
        }
        sendData();
    }


    public void stop()
    {
        mSpeed = 0;
        mDir = Motor.Direction.UNKNOWN;
        _leftMotor.setSpeed(0, Motor.Direction.BACK);
        _rightMotor.setSpeed(0, Motor.Direction.BACK);
//        thisBtLe.send(0);
        sendData();
    }

    public boolean onReceive(String string) {
        Log.d(TAG, "Received " + string);
        String[] tokens = string.split(" ");
        try {
            int speed = Integer.parseInt(tokens[1]);
            switch (tokens[0]) {
                case "fwd":
                    this.driveFwd(Integer.parseInt(tokens[1]));
                    break;
                case "back":
                    this.driveBack(Integer.parseInt(tokens[1]));
                    break;
                case "left":
                    this.driveLeft(speed);
                    break;
                case "right":
                    this.driveRight(speed);
                    break;
                default:
                    this.stop();
            }
        } catch (Exception e)
        {
            Log.e(TAG, e.toString());
        }
        return true;
    }

}
