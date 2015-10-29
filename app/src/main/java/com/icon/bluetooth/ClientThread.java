package com.icon.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.icon.agnks.Bluetooth;
import com.icon.agnks.Logger;
import com.icon.utils.Utils;

import java.lang.reflect.Method;

public class ClientThread extends Thread {

    public interface ClientListener {
//        Communicator createCommunicationThread(BluetoothSocket socket);
        void onConnectionCompleted(BluetoothDevice remoteDevice, boolean isConnected);
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
                Logger.add("ClientThread: Create BluetoothSocket. Socket RemoteDevice info: " + Utils.getDeviceInfo(device), Log.INFO);
        }
    }

    public void setClientListener(ClientListener listener) {
        this.clientListener = listener;
    }

    public void setCommunicationListener(CommunicationThread.CommunicationListener listener) {
        this.communicationListener = listener;
    }

    public synchronized Communicator getCommunicator() {
        return communicator;
    }

    @Override
    public void run() {
        if (socket == null) return;

        Bluetooth.cancelDiscovery();
        try {
            Logger.add("ClientThread: Before connecting to BluetoothSocket", Log.INFO);
            socket.connect();

            Logger.add("ClientThread: Connected to BluetoothSocket", Log.INFO);
            synchronized (this) {
                communicator = new CommunicationThread(socket, communicationListener);
            }
            Logger.add("ClientThread: CommunicationThread created", Log.INFO);
            isConnected = true;

        } catch (Exception connectException) {
            Logger.add("ClientThread: Run exception. INFO: " + connectException.getLocalizedMessage(), connectException);

            cancel();
        } finally {
            clientListener.onConnectionCompleted(socket.getRemoteDevice(), isConnected);
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public BluetoothDevice getBluetoothDevice() {
        if (socket != null) return socket.getRemoteDevice();
        return null;
    }

    public void cancel() {
        if (communicator != null) communicator.stopCommunication();
    }
}