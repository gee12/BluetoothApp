package com.icon.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.icon.agnks.Bluetooth;
import com.icon.agnks.Logger;
import com.icon.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class CommunicationThread extends Thread implements Communicator {

    public interface CommunicationListener {
        void onMessage(byte[] bytes);
        void responceTimeElapsed();
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
            Logger.add("CommunicationThread: Create InputStream,OutputStream", ex);
        }
        inputStream = tmpIn;
        outputStream = tmpOut;
    }

    @Override
    public void startCommunication() {
        if (listener == null) {
            Logger.add("CommunicationThread: CommunicationListener object is null", Log.ERROR, true);
            return;
        }

        byte[] buffer = new byte[1024];
        int bytes;

        int responceMsec = Bluetooth.ResponceMsecMax;
        long startTime = System.currentTimeMillis();

//        inputStream.reset();

        while (startTime + responceMsec > System.currentTimeMillis()) {
            try {
                if (inputStream.available() != 0) {
                    // read responce
                    bytes = inputStream.read(buffer);
                    Logger.add("CommunicationThread: Read " + bytes + " bytes", Log.DEBUG);

                    listener.onMessage(Arrays.copyOfRange(buffer, 0, bytes));

                    return;
                }
            } catch (IOException ex) {
                Logger.add("CommunicationThread: Run the communicator", ex);
                break;
            }
        }
        //
        listener.responceTimeElapsed();
        Logger.add("CommunicationThread: Responce time " + responceMsec + " (msec) elapsed", Log.DEBUG);
    }

    public void write(byte[] bytes) {
        try {
            Logger.add("CommunicationThread: Write: [" + Utils.toString(bytes, ",", Utils.RADIX_HEX) + "]", Log.DEBUG);
            outputStream.write(bytes);
        } catch (IOException ex) {
            Logger.add("CommunicationThread: Write to OutputStream", ex);
        }
    }

    @Override
    public void stopCommunication() {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.add("CommunicationThread: Close BluetoothSocket", ex);
        }
    }
}