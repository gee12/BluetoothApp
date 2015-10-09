package interosite.ru.bluetoothdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.icon.utils.Logger;
import com.icon.utils.Utils;

public class ConnectedThread_old extends Thread implements Communicator {

    interface CommunicationListener {
        void onMessage(String message);
        void onClientConnect(BluetoothDevice device);
    }

    private final BluetoothSocket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final CommunicationListener listener;

    public ConnectedThread_old(BluetoothSocket socket, CommunicationListener listener) {
        this.socket = socket;
        this.listener = listener;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException ex) {
            Logger.add("ConnectedThread_old: Create InputStream,OutputStream", ex, Log.ERROR);
        }
        inputStream = tmpIn;
        outputStream = tmpOut;
    }

    @Override
    public void startCommunication() {
        if (listener == null) return;

        listener.onClientConnect(socket.getRemoteDevice());

        byte[] buffer = new byte[1024];

        int bytes;

        Logger.add("ConnectedThread_old: Run the communicator", Log.INFO);

        while (true) {
            try {
                bytes = inputStream.read(buffer);
                Logger.add("ConnectedThread_old: Read " + bytes + " bytes", Log.INFO);

                listener.onMessage(new String(buffer).substring(0, bytes));

            } catch (IOException ex) {
                Logger.add("ConnectedThread_old: Run the communicator", ex, Log.ERROR);
                break;
            }
        }
    }

    public void write(String message) {
        try {
            Logger.add("ConnectedThread_old: Write: [" + message + "]", Log.INFO);
            outputStream.write(message.getBytes());
        } catch (IOException ex) {
            Logger.add("ConnectedThread_old: Write to OutputStream", ex, Log.ERROR);
        }
    }

    public void write(byte[] bytes) {
        try {
            Logger.add("ConnectedThread_old: Write: [" + Utils.bytesToHex(bytes) + "]", Log.INFO);
            outputStream.write(bytes);
        } catch (IOException ex) {
            Logger.add("ConnectedThread_old: Write to OutputStream", ex, Log.ERROR);
        }
    }

    @Override
    public void stopCommunication() {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.add("ConnectedThread_old: Close BluetoothSocket", ex, Log.ERROR);
        }
    }

}