package com.maslovw.androidonwheels;

import android.os.AsyncTask;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by viacheslav.maslov on 30-Dec-16.
 */

public class ServerUdpCommand extends AsyncTask<Platform, Integer, Long> {
    Platform mPlatform;
    private boolean mIsActive = true;
    private Receiver mReceiver = null;

    private final String TAG = "server";

    public interface Receiver {
        boolean onReceive(String string);
        void onOpened(String ip);
    }

    ServerUdpCommand(Receiver receiver) {
        mReceiver = receiver;
    }

    ServerUdpCommand() {

    }

    @Override
    protected Long doInBackground(Platform... platforms) {
        if (platforms.length != 1) {
            return null;
        }
        mReceiver.onOpened(getIpAddress());
        Server server = new Server();
        mIsActive = true;
        server.listen();

        return null;
    }

    public void stop() {
        mIsActive = false;
    }


    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "SiteLocalAddress: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    class Server {
        private DatagramPacket dp;
        private DatagramSocket ds = null;

        public void listen() {
            Log.d(TAG, "listen. start");
            byte[] msg = new byte [4096];
            dp = new DatagramPacket(msg, msg.length);
            try {
                ds = new DatagramSocket(8080);

                while (ServerUdpCommand.this.mIsActive) {
                    ds.receive(dp);
                    Log.d(TAG, "dp.address " + dp.getAddress());
                    String rcvString = new String(msg, 0, dp.getLength());
                    Log.w(TAG, rcvString);
                    if (mReceiver != null) {
                        mIsActive = (mReceiver.onReceive(rcvString));
                    }
                }
            }
            catch (Exception e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
            finally {
                if (ds != null) {
                    ds.close();
                }
            }

            Log.d(TAG, "listen. stop");
        }
    }
}
