package com.maslovw.androidonwheels;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.TextView;


/**
 * Created by viacheslav.maslov on 29-Dec-16.
 */

public class ServerConn {
    TextView info, infoip, msg;
    String message = "";
    ServerSocket serverSocket;
    public Activity mainActivity;
    public Socket mClient;
    private static String TAG = "ServerCon";
    private Receiver mReceiver;

    ServerConn(Activity activity, Receiver receiver)
    {
        mainActivity = activity;
        mReceiver = receiver;
        info = (TextView) activity.findViewById(R.id.info);
        infoip = (TextView) activity.findViewById(R.id.infoip);
        msg = (TextView) activity.findViewById(R.id.msg);

        infoip.setText(getIpAddress());

        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    public interface Receiver {
        boolean onReceive(String string);
    }

    private class SocketServerThread extends Thread {

        static final int SocketServerPORT = 8080;
        int count = 0;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                mainActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        info.setText("I'm waiting here: "
                                + serverSocket.getLocalPort());
                    }
                });

                while (true) {
                    Socket socket = serverSocket.accept();
                    socket.setReceiveBufferSize(5);
                    mClient = socket;
                    count++;
                    message += "#" + count + " from " + socket.getInetAddress()
                            + ":" + socket.getPort() + "\n";

                    mainActivity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            msg.setText(message);
                        }
                    });

                    SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(
                            socket, count);
                    socketServerReplyThread.run();

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private class SocketServerReplyThread extends Thread {

        private Socket hostThreadSocket;
        int cnt;

        SocketServerReplyThread(Socket socket, int c) {
            hostThreadSocket = socket;
            cnt = c;
        }

        @Override
        public void run() {
            OutputStream outputStream;
            String msgReply = "Hello from Android, you are #" + cnt;

            try {
                outputStream = hostThreadSocket.getOutputStream();
                PrintStream printStream = new PrintStream(outputStream);
                printStream.print(msgReply);
                BufferedReader input = new BufferedReader(new InputStreamReader(hostThreadSocket.getInputStream()));
                String read = input.readLine();
                message += "replayed: " + msgReply + "\n" + read;
                boolean isReading = true;
                while (isReading) {
                    read = input.readLine();
                    if (read != null) {
                        if (mReceiver != null){
                            if (mReceiver.onReceive(read) == false) {
                                break;
                            }
                        }
                        Log.d(TAG, read);
                        if (read.equals("exit")) {
                                Log.d(TAG, "exit" + cnt);
                                isReading = false;
                                break;
                        }
                         mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                msg.setText(message);
                            }
                        });
                    }
                    else
                        break;
                }
                printStream.close();



            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                message += "Something wrong! " + e.toString() + "\n";
            }

            mainActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    msg.setText(message);
                }
            });
        }

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
}
