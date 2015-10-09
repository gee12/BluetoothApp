package interosite.ru.bluetoothdemo;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.icon.utils.Logger;
import com.icon.utils.Utils;

import com.icon.bluetooth.TestActivity;

public class ClientThread_old extends Thread {

    private volatile Communicator communicator;

    private final BluetoothSocket socket;
    private BluetoothAdapter bluetoothAdapter;
    private final CommunicatorService communicatorService;
    private boolean isConnected;

    public ClientThread_old(BluetoothDevice remoteDevice, CommunicatorService communicatorService, BluetoothAdapter bluetoothAdapter, boolean isInsecure) {

        this.communicatorService = communicatorService;
        this.bluetoothAdapter = bluetoothAdapter;

        BluetoothSocket tmp = null;
        //bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //bluetoothAdapter = Context.getSystemService(Context.BLUETOOTH_SERVICE);
        try {
            tmp = (isInsecure) ? remoteDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(TestActivity.UUID))
                    : remoteDevice.createRfcommSocketToServiceRecord(UUID.fromString(TestActivity.UUID));
        } catch (IOException ex) {
            Logger.add("ClientThread_old: Creating BluetoothSocket", ex, Log.ERROR);
        }
        socket = tmp;

        if (socket != null) {
            BluetoothDevice device = socket.getRemoteDevice();
            if (device != null)
                Logger.add("ClientThread_old: Create BluetoothSocket. Socket RemoteDevice info: " + Utils.getDeviceInfo(device), Log.DEBUG);
        }
    }

    public synchronized Communicator getCommunicator() {
        return communicator;
    }

    public void run() {
        bluetoothAdapter.cancelDiscovery();
        try {
            Logger.add("ClientThread_old: Before connecting to BluetoothSocket", Log.DEBUG);
            socket.connect();
            Logger.add("ClientThread_old: Connected to BluetoothSocket", Log.DEBUG);
            synchronized (this) {
                communicator = communicatorService.createCommunicatorThread(socket);
                Logger.add("ClientThread_old: Communicator created", Log.DEBUG);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    communicator.startCommunication();
                    Logger.add("ClientThread_old: Start ClientThread_old (communicator.startCommunication())", Log.DEBUG);
                }
            }).start();

            isConnected = true;
        } catch (Exception connectException) {
            Logger.add("ClientThread_old: Run exception", connectException, Log.ERROR);
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.add("ClientThread_old: Close BluetoothSocket", ex, Log.ERROR);
            }
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void cancel() {
        if (communicator != null) communicator.stopCommunication();
    }
}