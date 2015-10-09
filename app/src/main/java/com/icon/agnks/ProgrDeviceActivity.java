package com.icon.agnks;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.icon.bluetooth.BluetoothUtils;
import com.icon.bluetooth.ClientThread;
import com.icon.bluetooth.CommunicationThread;
import com.icon.bluetooth.Communicator;
import com.icon.utils.Logger;
import com.icon.utils.MessageBox;
import com.icon.utils.Utils;

public class ProgrDeviceActivity extends Activity {

    public final byte[] TEST_BYTES = Utils.toBytes(0xF0, 0x9E, 0x7B, 0, 0, 0, 0x19, 0xF1);
    public final static String UUID = "5e354ea0-668a-11e5-a837-0800200c9a66";

    private BluetoothAdapter bluetoothAdapter;
    private ClientThread clientThread;
    private Device device;

    private Button buttonSetupConnect;
    private Button buttonSendMessage;
    private ProgressBar progressBarMain;
    private TextView textData;
    private EditText textMessage;
    private boolean isConnected;

    private class WriteTask extends AsyncTask<byte[], Void, Void> {
        @Override
        protected Void doInBackground(byte[]... params) {
            try {
                clientThread.getCommunicator().write(params[0]);
            } catch (Exception ex) {
                Logger.add(ex, Log.ERROR);
            }
            return null;
        }
    }

    private final CommunicationThread.CommunicatorService communicatorService = new CommunicationThread.CommunicatorService() {
        @Override
        public Communicator createCommunicationThread(BluetoothSocket socket) {

            return new CommunicationThread(socket, new CommunicationThread.CommunicationListener() {

                @Override
                public void onMessage(final String message) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            appendTextData("Принято: " + message);
                            Logger.add("DevicesManageActivity: Message received: " + message, Log.INFO);
                        }
                    });
                }
            });
        }

        @Override
        public void connectToDevice(final BluetoothDevice remoteDevice, final boolean isConnected) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProgrDeviceActivity.this.isConnected = isConnected;
                    buttonSendMessage.setEnabled(isConnected);
                    String deviceInfo = Utils.getDeviceInfo(remoteDevice);
                    String strResult = (isConnected) ? " установлено" : " НЕ установлено";
                    MessageBox.shoter(ProgrDeviceActivity.this, "Соединение с устройством " + deviceInfo + strResult);
                    String strLogRes = (isConnected) ? "" : "NOT";
                    Logger.add("ProgrDeviceActivity: ClientThread " + strLogRes + " connected. RemoteDevice info: " + deviceInfo, Log.INFO);
                    progressBarMain.setVisibility(View.INVISIBLE);
                    String connState = (isConnected) ? getString(R.string.str_close_connect) : getString(R.string.str_setup_connect);
                    buttonSetupConnect.setText(connState);
                    setButtonsEnabled(true);
                }
            });
        }
    };

    /*
    *
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progr);

        textData = (TextView) findViewById(R.id.data_text);
        textMessage = (EditText) findViewById(R.id.message_text);
        buttonSendMessage = (Button) findViewById(R.id.button_send_message);
        buttonSetupConnect = (Button) findViewById(R.id.button_setup_connect);
        progressBarMain = (ProgressBar) findViewById(R.id.progressBarMain);

        try {
            bluetoothAdapter = BluetoothUtils.getAdapter(this);
        } catch(Exception ex) {
            Logger.add("ProgrDeviceActivity: Create BluetoothAdapter", ex, Log.ERROR);
        } finally {
            if (bluetoothAdapter == null) {
                MessageBox.shoter(this, "Не найден Bluetooth адаптер на устройстве");
                finish();
            }
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, DevicesManageActivity.REQUEST_ENABLE_BT);
            return;
        }

        device = (Device)getIntent().getSerializableExtra(Device.KEY_DEVICE_OBJECT);
        if (device == null) {
            MessageBox.longer(this, "Устройство не определено");
            finish();
        }

    }

    /*
    *
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DevicesManageActivity.REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK){
                MessageBox.shoter(this, "Для работы необходимо включить Bluetooth");
                finish();
            }
        }
    }

    public void setupConnect(View view) {
        if (!isConnected) {
            buttonSetupConnect.setText(R.string.str_connection_is_established);
            BluetoothDevice btDevice = bluetoothAdapter.getRemoteDevice(device.MacAddress);
            createClient(btDevice);
        } else {
            closeClient();
        }
    }

    /*
    *
    */
    public void createClient(BluetoothDevice remoteDevice) {
        clientThread = new ClientThread(remoteDevice, communicatorService, bluetoothAdapter, false);
        clientThread.start();

        progressBarMain.setVisibility(ProgressBar.VISIBLE);
        setButtonsEnabled(false);
    }

    public void closeClient() {
        if (clientThread != null) {
            clientThread.cancel();
        }
        isConnected = false;
        buttonSetupConnect.setText(R.string.str_setup_connect);
    }

    /*
    *
    */
    private void setButtonsEnabled(boolean isEnabled) {
        buttonSendMessage.setEnabled(isEnabled);
        buttonSetupConnect.setEnabled(isEnabled);
    }

    /*
    *
    */
    public void sendMessage(View view) {
        sendMessage(textMessage.getText().toString());
        textMessage.setText("");
    }

    public void sendMessage(String str) {
        if (clientThread != null && clientThread.isConnected()) {
            char[] chars = (str + "\r\n").toCharArray();
            byte[] bytes = Utils.toBytes(chars);

            new WriteTask().execute(bytes);

//            Toast.makeText(this, "Сообщение [" + str + "] отправлено", Toast.LENGTH_SHORT).show();
            appendTextData("Отправлено: " + str);
        } else {
            MessageBox.shoter(this, "Сначала соединитесь с другим устройством");
        }
    }

    /*
    *
     */
    private void appendTextData(String text) {
        textData.setText(textData.getText() + "\n" + text);
    }

    /*
    *
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        closeClient();
    }
}
