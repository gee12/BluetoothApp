package com.icon.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.icon.agnks.Bluetooth;
import com.icon.agnks.Logger;
import com.icon.utils.Utils;

import java.io.IOException;
import java.lang.reflect.Method;

public class ClientThread extends Thread {

    public interface ClientListener {
//        Communicator createCommunicationThread(BluetoothSocket socket);
        void connectionCompleted(BluetoothDevice remoteDevice, boolean isConnected);
    }

    private final BluetoothSocket socket;
    private ClientListener clientListener;
    private volatile Communicator communicator;
    CommunicationThread.CommunicationListener communicationListener;
    private boolean isConnected;

    public ClientThread(BluetoothDevice remoteDevice, ClientListener clientListener, CommunicationThread.CommunicationListener communicationListener) {

        this.clientListener = clientListener;
        this.communicationListener = communicationListener;
        this.isConnected = false;

        BluetoothSocket tmp = null;
        try {
            Method m = remoteDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
            tmp = (BluetoothSocket) m.invoke(remoteDevice, 1);

        } catch (Exception ex) {
            Logger.add("ClientThread: Creating BluetoothSocket", ex);
        }
        socket = tmp;

        if (socket != null) {
            BluetoothDevice device = socket.getRemoteDevice();
            if (device != null)
                Logger.add("ClientThread: Create BluetoothSocket. Socket RemoteDevice info: " + Utils.getDeviceInfo(device), Log.DEBUG);
        }
    }

    public void setClientListener(ClientListener listener) {
        this.clientListener = listener;
    }

    public void setCommunicationListener(CommunicationThread.CommunicationListener listener) {
        this.communicationListener = listener;
    }

//    public synchronized Communicator getCommunicator() {
//        return communicator;
//    }

    public void run() {
        if (socket == null) return;

//        bluetoothAdapter.cancelDiscovery();
        Bluetooth.cancelDiscovery();
        try {
            Logger.add("ClientThread: Before connecting to BluetoothSocket", Log.DEBUG);
            socket.connect();

            Logger.add("ClientThread: Connected to BluetoothSocket", Log.DEBUG);
            synchronized (this) {
//                communicator = clientListener.createCommunicationThread(socket);
                communicator = new CommunicationThread(socket, communicationListener);
            }
            Logger.add("ClientThread: Communicator created", Log.DEBUG);

            isConnected = true;

        } catch (Exception connectException) {
            Logger.add("ClientThread: Run exception. INFO: " + connectException.getLocalizedMessage(), connectException);

            closeSocket();
        } finally {
            clientListener.connectionCompleted(socket.getRemoteDevice(), isConnected);
        }
    }

    /**
     *
     */
    public void startResponceListening() {
        if (communicator == null) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.add("ClientThread: communicator.startCommunication()", Log.DEBUG);
                communicator.startCommunication();
            }
        }).start();
    }

    /**
     *
     * @param bytes
     */
    public void sendBytes(byte[] bytes) {
        if (communicator == null) return;

        communicator.write(bytes);
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void cancel() {
        if (communicator != null) communicator.stopCommunication();
//        if (socket != null) closeSocket();
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.add("ClientThread: Close BluetoothSocket", ex);
        }
    }
}