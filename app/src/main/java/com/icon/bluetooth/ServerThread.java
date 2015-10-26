package com.icon.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.icon.agnks.Logger;

import java.io.IOException;
import java.util.UUID;

public class ServerThread extends Thread {

    private final BluetoothServerSocket bluetoothServerSocket;
    private final CommunicationThread.CommunicatorService communicatorService;

    public ServerThread(CommunicationThread.CommunicatorService communicatorService, BluetoothAdapter bluetoothAdapter, boolean isInsecure) {
        this.communicatorService = communicatorService;
        //final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final UUID uuid = UUID.fromString(TestActivity.UUID);

        BluetoothServerSocket tmp = null;
        try {
            tmp = (isInsecure) ? bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("BluetoothApp", uuid)
                    : bluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothApp", uuid);
        } catch (IOException ex) {
            Logger.add("ServerThread_old: Create BluetoothServerSocket", ex);
        }
        bluetoothServerSocket = tmp;
    }

    public void run() {

        BluetoothSocket socket = null;

        Logger.add("ServerThread_old: Started ServerThread_old", Log.DEBUG);

        while (true) {
            if (bluetoothServerSocket == null) continue;
            try {
                socket = bluetoothServerSocket.accept();

                bluetoothServerSocket.close();
            } catch (IOException ex) {
                Logger.add("ServerThread_old: Run", ex);
                break;
            }
            if (socket != null) {
//                socket.getOutputStream().
                communicatorService.createCommunicationThread(socket).startCommunication();
            }
        }
    }

    public void cancel() {
        try {
            bluetoothServerSocket.close();
        } catch (IOException ex) {
            Logger.add("ServerThread_old: Close BluetoothServerSocket", ex);
        }
    }
}

