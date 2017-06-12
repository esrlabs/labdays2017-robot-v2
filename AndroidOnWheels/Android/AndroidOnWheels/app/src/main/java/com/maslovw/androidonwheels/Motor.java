package com.maslovw.androidonwheels;

/**
 * Created by viacheslav.maslov on 21-Dec-16.
 */

public class Motor {
    private String _name = "";
    private byte _speed = 0;
    private byte _id = 0;

    public enum Direction {
        UNKNOWN,
        FWD,
        BACK
    }
    private Direction _direction = Direction.UNKNOWN;

    private byte dirToByte(Direction dir)
    {
        switch (dir)
        {
            case FWD:
                return (byte)1;
            case BACK:
                return (byte)2;
            default:
                return 0;
        }
    }

    public Motor(String name, byte id)
    {
        _name = name;
        _id = id;
    }

    public boolean setSpeed(int speed, Direction direction)
    {
        if (speed > 254)
            speed = 254;
        _speed = (byte)speed;
        _direction = direction;
        return false;
    }

    public byte get_speed()
    {
        return _speed;
    }

    public Direction get_direction()
    {
        return _direction;
    }

    public byte[] getArrary()
    {
        if (_direction == Direction.UNKNOWN)
        {
            return null;
        }
        byte[] ret = new byte[] {
                 _id,
                _speed,
                dirToByte(_direction),
                0
        };
        return ret;
    }
}
