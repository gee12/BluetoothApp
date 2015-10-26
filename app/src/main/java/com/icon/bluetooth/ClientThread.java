package com.icon.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.icon.agnks.Logger;
import com.icon.utils.Utils;

import java.io.IOException;
import java.lang.reflect.Method;

public class ClientThread extends Thread {

    private volatile Communicator communicator;

    private final BluetoothSocket socket;
    private BluetoothAdapter bluetoothAdapter;
    private final CommunicationThread.CommunicatorService communicatorService;
    private boolean isConnected;

    public ClientThread(BluetoothDevice remoteDevice, CommunicationThread.CommunicatorService communicatorService, BluetoothAdapter bluetoothAdapter, boolean isInsecure) {

        this.communicatorService = communicatorService;
        this.bluetoothAdapter = bluetoothAdapter;
        this.isConnected = false;

        BluetoothSocket tmp = null;
        try {
            // 1
//            tmp = (isInsecure) ? remoteDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(TestActivity.UUID))
//                    : remoteDevice.createRfcommSocketToServiceRecord(UUID.fromString(TestActivity.UUID));

            // 2
//            boolean temp = remoteDevice.fetchUuidsWithSdp();
//            UUID uuid = null;
//            if( temp ){
//                uuid = remoteDevice.getUuids()[0].getUuid();
//            }
//            tmp = remoteDevice.createRfcommSocketToServiceRecord(uuid);

            // 3
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

    public synchronized Communicator getCommunicator() {
        return communicator;
    }

    public void run() {
        if (socket == null) return;

        bluetoothAdapter.cancelDiscovery();
        try {
            Logger.add("ClientThread: Before connecting to BluetoothSocket", Log.DEBUG);
            socket.connect();

            Logger.add("ClientThread: Connected to BluetoothSocket", Log.DEBUG);
            synchronized (this) {
                communicator = communicatorService.createCommunicationThread(socket);
                Logger.add("ClientThread: Communicator created", Log.DEBUG);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    communicator.startCommunication();
                    Logger.add("ClientThread: Start ClientThread (communicator.startCommunication())", Log.DEBUG);
                }
            }).start();

            isConnected = true;

        } catch (Exception connectException) {
            Logger.add("ClientThread: Run exception. INFO: " + connectException.getLocalizedMessage(), connectException);

            closeSocket();
        } finally {
//            synchronized (this) {
                communicatorService.connectToDevice(socket.getRemoteDevice(), isConnected);
//            }
        }
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