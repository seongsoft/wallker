package com.seongsoft.wallker.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by BeINone on 2016-10-17.
 */

//public class TCPClient {
//
//    private static final String TAG = "TCPClient";
//    private final Handler mHandler;
//    private String ipNumber, incomingMessage;
//    private int port;
//    private BufferedReader in;
//    private PrintWriter out;
//    private MessageCallback mListener;
//    private boolean mRun = false;
//
//
//    /**
//     * TCPClient class constructor, which is created in AsyncTasks after the button click.
//     * @param handler Handler passed as an argument for updating the UI with sent messages
//     * @param ipNumber String retrieved from IpGetter class that is looking for ip number.
//     * @param listener Callback interface object
//     */
//    public TCPClient(Handler handler, String ipNumber, int port, MessageCallback listener) {
//        mHandler = handler;
//        this.ipNumber = ipNumber;
//        this.port = port;
//        mListener = listener;
//    }
//
//    /**
//     * Public method for sending the message via OutputStream object.
//     * @param message Message passed as an argument and sent via OutputStream object.
//     */
//    public void sendMessage(String message) {
//        if (out != null && !out.checkError()) {
//            out.println(message);
//            out.flush();
////            mHandler.sendEmptyMessageDelayed(MapActivity.SENDING, 1000);
//            Log.d(TAG, "Sent Message: " + message);
//
//        }
//    }
//
//    /**
//     * Public method for stopping the TCPClient object ( and finalizing it after that ) from AsyncTask
//     */
//    public void stopClient() {
//        Log.d(TAG, "Client stopped!");
//        mRun = false;
//    }
//
//    public void run() {
//        mRun = true;
//
//        try {
//            // Creating InetAddress object from ipNumber passed via constructor from IpGetter class.
//            InetAddress serverAddress = InetAddress.getByName(ipNumber);
//
//            Log.d(TAG, "Connecting...");
//
//            /**
//             * Sending empty message with static int value from MapActivity
//             * to update UI ( 'Connecting...' ).
//             *
//             * @see com.example.turnmeoff.MapActivity.CONNECTING
//             */
////            mHandler.sendEmptyMessageDelayed(MapActivity.CONNECTING, 1000);
//
//            /**
//             * Here the socket is created with hardcoded port.
//             * Also the port is given in IpGetter class.
//             *
//             * @see com.example.turnmeoff.IpGetter
//             */
//            Socket socket = new Socket(serverAddress, port);
//            Log.d(TAG, socket.toString());
//
//            try {
//                // Create PrintWriter object for sending messages to server.
//                out = new PrintWriter(
//                        new BufferedWriter(
//                                new OutputStreamWriter(socket.getOutputStream())), true);
//
//                // Create BufferedReader object for receiving messages from server.
//                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//                Log.d(TAG, "In/Out created");
//
//                //
////                mHandler.sendEmptyMessageDelayed(MapActivity.SENDING, 2000);
//
//                //Listen for the incoming messages while mRun = true
//                while (mRun) {
//                    incomingMessage = in.readLine();
//                    Log.d(TAG, "incomingMessage: " + incomingMessage);
//                    Log.d(TAG, "listener: " + mListener);
//                    if (incomingMessage != null && mListener != null) {
//                        Log.d(TAG, "callback");
//
//                        /**
//                         * Incoming message is passed to MessageCallback object.
//                         * Next it is retrieved by AsyncTask and passed to onPublishProgress method.
//                         *
//                         */
//                        mListener.callbackMessageReceiver(incomingMessage);
//
//                    }
//                    incomingMessage = null;
//
//                }
//
//                Log.d(TAG, "Received Message: " + incomingMessage);
//
//            } catch (Exception e) {
//
//                Log.d(TAG, "Error", e);
////                mHandler.sendEmptyMessageDelayed(MapActivity.ERROR, 2000);
//
//            } finally {
//
//                out.flush();
//                out.close();
//                in.close();
//                socket.close();
////                mHandler.sendEmptyMessageDelayed(MapActivity.SENT, 3000);
//                Log.d(TAG, "Socket Closed");
//            }
//
//        } catch (Exception e) {
//
//            Log.d(TAG, "Error", e);
////            mHandler.sendEmptyMessageDelayed(MapActivity.ERROR, 2000);
//
//        }
//
//    }
//
//    public boolean isRunning() {
//        return mRun;
//    }
//
//    /**
//     * Callback Interface for sending received messages to 'onPublishProgress' method in AsyncTask.
//     *
//     */
//    public interface MessageCallback {
//        /**
//         * Method overriden in AsyncTask 'doInBackground' method while creating the TCPClient object.
//         * @param message Received message from server app.
//         */
//        void callbackMessageReceiver(String message);
//    }
//
//}

public class TCPClient {

    private static final String TAG = "TCPClient";
    private static final String IP = "10.156.145.88";
    private static final int PORT = 52925;

    private MessageCallback mListener;
    private Socket mSocket;
    private BufferedReader mReader;
    private BufferedWriter mWriter;
    private boolean mRun;

    public TCPClient(MessageCallback listener) {
        mListener = listener;
    }

    public void sendMessage(String message) {
        try {
            if (mWriter != null) {
                mWriter.write(message);
                mWriter.flush();
                Log.d(TAG, "writed: " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        mRun = true;

        try {
            mSocket = new Socket(IP, PORT);

            mReader = new BufferedReader(
                    new InputStreamReader(mSocket.getInputStream()));
            mWriter = new BufferedWriter(
                    new OutputStreamWriter(mSocket.getOutputStream()));

            while (mRun) {
                String line = mReader.readLine();
                Log.d(TAG, "read: " + line);
                mListener.callbackMessageReceiver(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public void stopClient() {
        mRun = false;
    }

    public boolean isRunning() {
        return mRun;
    }

    private void stop() {
        try {
            if (mReader != null) {
                mReader.close();
            }
            if (mWriter != null) {
                mWriter.flush();
                mWriter.close();
            }
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface MessageCallback {
        /**
         * Method overriden in AsyncTask 'doInBackground' method while creating the TCPClient object.
         * @param message Received message from server app.
         */
        void callbackMessageReceiver(String message);
    }

}
