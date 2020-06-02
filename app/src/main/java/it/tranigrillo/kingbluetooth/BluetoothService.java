package it.tranigrillo.kingbluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothService {
    private static final String NAME = Build.MODEL;
    private static final String TAG = "ConnectionManager";
    private static final UUID DEVICE_UUID = UUID.fromString("e6f21f64-da8c-4764-8e7f-a729e9857996");
    private final BluetoothAdapter bluetoothAdapter;
    private AcceptThread insecureAcceptTread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private Context context;
    private ProgressDialog progressDialog;
    private Intent connection;
    private Intent messages;

    public BluetoothService(Context context, BluetoothAdapter bluetoothAdapter) {
        this.context = context;
        this.bluetoothAdapter = bluetoothAdapter;
        connection = new Intent("connectionData");
        messages = new Intent("messagesData");
        start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {

        //      the local serverSocket
        private final BluetoothServerSocket serverSocket;

        private AcceptThread() {
            // Use a temporary object that is later assigned to serverSocket
            // because serverSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, DEVICE_UUID);
                Log.d(TAG, "AcceptThread: Setting up Server using: " + DEVICE_UUID);
            }
            catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            serverSocket = tmp;
        }

        @Override
        public void run() {

            Log.d(TAG, "run: AcceptThread Running.");
            BluetoothSocket socket = null;

            // Keep listening until exception occurs or a socket is returned.
                Log.d(TAG, "run: listening:...");
                try {
                    Log.d(TAG, "run: RFCOM server socket start.....");
                    socket = serverSocket.accept();
                }
                catch (IOException e) {
                    Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());

                }
            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                connected(socket);
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "AcceptedTread: IOException:" + e.getMessage());
                }
            }
            Log.d(TAG, "END insecureAcceptThread ");
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage());
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {

        private final BluetoothSocket bluetoothSocket;
        private final BluetoothDevice device;

        ConnectThread(BluetoothDevice device) {
            Log.d(TAG, "ConnectThread: started.");
            this.device = device;

            // Use a temporary object that is later assigned to bluetoothSocket
            // because bluetoothSocket is final.
            BluetoothSocket tmp = null;
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: " + DEVICE_UUID);
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                tmp = device.createRfcommSocketToServiceRecord(DEVICE_UUID);
                Log.d(TAG, "ConnectThread: InsecureRfcommSocket created.");
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread: IOException: " + e.getMessage());
            }
            bluetoothSocket = tmp;
        }

        @Override
        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                bluetoothSocket.connect();
                Log.d(TAG, "run: ConnectThread connected.");
                connection.putExtra("connection", context.getResources().getString(R.string.connected));
                connection.putExtra("device", device.getAddress());
                LocalBroadcastManager.getInstance(context).sendBroadcast(connection);
            }
            catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    bluetoothSocket.close();
                    Log.d(TAG, "connectThread: run: Closed Socket.");
                }
                catch (IOException closeException) {
                    Log.e(TAG, "connectThread: run: Unable to close connection in socket " + closeException.getMessage());
                }
                Log.d(TAG, "run: connectThread: Could not connect to UUID: " + DEVICE_UUID);
                progressDialog.dismiss();
                connection.putExtra("connection", context.getResources().getString(R.string.connected));
                showToastInThread(context, "Could not connect to the device");
                return;
            }
            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connected(bluetoothSocket);
        }

        // Closes the client socket and causes the thread to finish.
        void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                bluetoothSocket.close();
            }
            catch (IOException e) {
                Log.e(TAG, "cancel: close() of bluetoothSocket in ConnectThread failed. " + e.getMessage());
            }
        }
    }


    public void showToastInThread(final Context context,final String str){
        Looper.prepare();
        Toast.makeText(context, str,Toast.LENGTH_LONG).show();
        Looper.loop();
    }

    /**
     * Start the AcceptThread to begin a session in listening (server) mode.
     * Called by the Activity onResume()
     */
    public synchronized void start() {
        // Cancel any thread attempting to make a connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (insecureAcceptTread == null) {
            insecureAcceptTread = new AcceptThread();
            insecureAcceptTread.start();
        }
    }

    public synchronized void stop() {
        // Cancel any thread attempting to make a connection
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        start();
    }

    /**
     *AcceptThread starts and sits waiting for a connection.
     *Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     */

    public void startClient(BluetoothDevice device){
        Log.d(TAG, "startClient: Attempting Connection...");
        //initprogress dialog
        progressDialog = ProgressDialog.show(context,"Connecting Bluetooth","Please Wait...",true);
        connectThread = new ConnectThread(device);
        connectThread.start();
    }


    /**
     * Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     * receiving incoming data through input/output streams respectively.
     **/

    private class ConnectedThread extends Thread {
        private final BluetoothSocket bluetoothSocketSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private byte[] buffer; // buffer store for the stream

        ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting...");
            bluetoothSocketSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progress dialog when connection is established
            try {
                progressDialog.dismiss();
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            }
            catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }
            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        @Override
        public void run() {

            buffer = new byte[1024]; // buffer store for the stream
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, numBytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                    messages.putExtra("message", incomingMessage);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(messages);
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage());
//                    new BluetoothService(context, bluetoothAdapter);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {

            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                outputStream.write(bytes);

//                // Share the sent message with the UI activity.
//                Message writtenMsg = handler.obtainMessage(
//                        MessageConstants.MESSAGE_WRITE, -1, -1, buffer);
//                writtenMsg.sendToTarget();
            }
            catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage());
//                // Send a failure message back to the activity.
//                Message writeErrorMsg =
//                        handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
//                Bundle bundle = new Bundle();
//                bundle.putString("toast",
//                        "Couldn't send data to the other device");
//                writeErrorMsg.setData(bundle);
//                handler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                bluetoothSocketSocket.close();
            }
            catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private void connected(BluetoothSocket bluetoothSocket) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread(bluetoothSocket);
        connectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        try { connectedThread.write(out);}
        catch (NullPointerException e){
            Log.e(TAG, "write:" + e.getMessage());
            Toast.makeText(context, "Connection Error: restart connection", Toast.LENGTH_LONG).show();
        }
    }
}
