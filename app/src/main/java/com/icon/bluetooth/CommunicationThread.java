package com.icon.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.icon.utils.Logger;
import com.icon.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CommunicationThread extends Thread implements Communicator {

    public interface CommunicatorService {
        Communicator createCommunicationThread(BluetoothSocket socket);
        void connectToDevice(BluetoothDevice remoteDevice, boolean isConnected);
    }

    public interface CommunicationListener {
        void onMessage(String message);
    }

    private final BluetoothSocket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final CommunicationListener listener;

    public CommunicationThread(BluetoothSocket socket, CommunicationListener listener) {
        this.socket = socket;
        this.listener = listener;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException ex) {
            Logger.add("CommunicationThread: Create InputStream,OutputStream", ex, Log.ERROR);
        }
        inputStream = tmpIn;
        outputStream = tmpOut;
    }

    @Override
    public void startCommunication() {
        if (listener == null) {
//            throw new IllegalArgumentException("CommunicationThread: CommunicationListener object is null");
            Logger.add("CommunicationThread: CommunicationListener object is null", Log.ERROR, true);
            return;
        }

        byte[] buffer = new byte[1024];
        int bytes;

        Logger.add("CommunicationThread: Run the communicator", Log.DEBUG);

        while (true) {
            try {
                bytes = inputStream.read(buffer);
                Logger.add("CommunicationThread: Read " + bytes + " bytes", Log.DEBUG);

                listener.onMessage(new String(buffer).substring(0, bytes));

            } catch (IOException ex) {
                Logger.add("CommunicationThread: Run the communicator", ex, Log.ERROR);
                break;
            }
        }
    }

    public void write(String message) {
        try {
            outputStream.write(message.getBytes());
            Logger.add("CommunicationThread: Write: [" + message + "]", Log.DEBUG);
        } catch (IOException ex) {
            Logger.add("CommunicationThread: Write to OutputStream", ex, Log.ERROR);
        }
    }

    public void write(byte[] bytes) {
        try {
            Logger.add("CommunicationThread: Write: [" + Utils.bytesToHex(bytes) + "]", Log.DEBUG);
            outputStream.write(bytes);
        } catch (IOException ex) {
            Logger.add("CommunicationThread: Write to OutputStream", ex, Log.ERROR);
        }
    }

    @Override
    public void stopCommunication() {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.add("CommunicationThread: Close BluetoothSocket", ex, Log.ERROR);
        }
    }

}