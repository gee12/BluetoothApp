package com.icon.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.icon.agnks.Device;
import com.icon.agnks.Bluetooth;
import com.icon.bluetooth.ClientThread;
import com.icon.bluetooth.CommunicationThread;
import com.icon.agnks.Logger;
import com.icon.utils.MessageBox;
import com.icon.utils.Utils;

public class ProgrDeviceActivity extends Activity implements ClientThread.ClientListener, CommunicationThread.CommunicationListener {

//    public final byte[] TEST_BYTES = Utils.toBytes(0xF0, 0x9E, 0x7B, 0, 0, 0, 0x19, 0xF1);
//    public final static String UUID = "5e354ea0-668a-11e5-a837-0800200c9a66";
    public static final String TAG_IS_CONNECTED = "TagIsConnected";
    public static final String TAG_RADIX = "TagRadix";
    public static final String TAG_TEXT = "TagText";
    public final static String SEPAR = " ";

//    private BluetoothAdapter bluetoothAdapter;
    private ClientThread clientThread;
    private Device device;

    private Button buttonSetupConnect;
    private Button buttonSendMessage;
    private ProgressBar progressBarMain;
    private ScrollView scrollView;
    private TextView textView;
    private EditText editText;
    private boolean isConnected;
    private int radix = Utils.RADIX_DEC;

    /*
    *
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progr);

        textView = (TextView) findViewById(R.id.data_text1);
        textView.setMovementMethod(new ScrollingMovementMethod());
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        editText = (EditText) findViewById(R.id.message_text);
        buttonSendMessage = (Button) findViewById(R.id.button_send_message);
        buttonSetupConnect = (Button) findViewById(R.id.button_setup_connect);
        progressBarMain = (ProgressBar) findViewById(R.id.progressBarMain);

        Bluetooth.enableIfNeed(this, DevicesManageActivity.REQUEST_ENABLE_BT);

        device = getIntent().getParcelableExtra(Device.KEY_DEVICE_OBJECT);
        if (device == null) {
            MessageBox.longer(this, "Устройство не определено");
            finish();
        }

        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.radio_dec:
                        radix = Utils.RADIX_DEC;
                        break;
                    case R.id.radio_hex:
                        radix = Utils.RADIX_HEX;
                }
            }
        });

        if (savedInstanceState != null) {
            isConnected = savedInstanceState.getBoolean(TAG_IS_CONNECTED);
            radix = savedInstanceState.getInt(TAG_RADIX);
            device = savedInstanceState.getParcelable(Device.KEY_DEVICE_OBJECT);
            String text = savedInstanceState.getString(TAG_TEXT);

            if (isConnected) clientThread = Bluetooth.getCurrentClientThread();
            textView.setText(text);
            int radioId = (radix == Utils.RADIX_DEC) ? R.id.radio_dec : R.id.radio_hex;
            radioGroup.check(radioId);
            int connStringId = (isConnected) ? R.string.str_close_connect : R.string.str_setup_connect;
            buttonSetupConnect.setText(connStringId);
            buttonSendMessage.setEnabled(isConnected);
            editText.setEnabled(isConnected);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(TAG_IS_CONNECTED, isConnected);
        outState.putInt(TAG_RADIX, radix);
        outState.putString(TAG_TEXT, textView.getText().toString());
        outState.putParcelable(Device.KEY_DEVICE_OBJECT, device);

    }

    /**
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
            BluetoothDevice btDevice = Bluetooth.getRemoteDevice(device.MacAddress);
            createClient(btDevice);
        } else {
            closeClient();
        }
    }

    /**
     *
     * @param remoteDevice
     */
    public void createClient(BluetoothDevice remoteDevice) {
        clientThread = Bluetooth.createDeviceCommunication(remoteDevice, this, this);

        progressBarMain.setVisibility(ProgressBar.VISIBLE);
        buttonSendMessage.setEnabled(false);
        buttonSetupConnect.setEnabled(false);
        editText.setEnabled(false);
        textView.setText("");
    }

    @Override
    public void connectionCompleted(final BluetoothDevice remoteDevice, final boolean isConnected) {
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
                int connStringId = (isConnected) ? R.string.str_close_connect : R.string.str_setup_connect;
                buttonSetupConnect.setText(connStringId);
                buttonSendMessage.setEnabled(true);
                buttonSetupConnect.setEnabled(true);
                editText.setEnabled(true);
            }
        });
    }

    @Override
    public void onMessage(final byte[] bytes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String message = Utils.toString(bytes, SEPAR, Utils.RADIX_HEX);
                appendTextData("Принято: " + message);
                MessageBox.shoter(ProgrDeviceActivity.this, message);
//                textView.setText(message);
                Logger.add("ProgrDeviceActivity: Received: [" + message + "]", Log.INFO);
            }
        });
    }

    @Override
    public void responceTimeElapsed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                appendTextData("Время ожидания ответа истекло (" + Bluetooth.ResponceMsecMax + " мсек)");
            }
        });
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        if (clientThread != null) {
//            clientThread.setClientListener(this);
//            clientThread.setCommunicationListener(this);
//        }
//    }

    public void closeClient() {
        if (clientThread != null) {
            clientThread.cancel();
            clientThread = null;
        }
        isConnected = false;
        buttonSetupConnect.setText(R.string.str_setup_connect);
        buttonSendMessage.setEnabled(false);
        editText.setEnabled(false);
        textView.setText("");
    }

    /**
     *
     * @param view
     */
    public void sendMessage(View view) {
        sendMessage(editText.getText().toString());
        editText.setText("");
    }

    public void sendMessage(String str) {
        if (clientThread != null && clientThread.isConnected()) {
            byte[] bytes = null;
            try {
                bytes = Utils.stringToBytes(str, SEPAR, radix);
            } catch(Exception ex) {
                MessageBox.shoter(this, "Не удается распарсить данные");
            }
            if (bytes != null) {
                new WriteTask().execute(bytes);
                appendTextData("Отправлено: " + Utils.toString(bytes, SEPAR, radix));
            }
        } else {
            MessageBox.shoter(this, "Сначала соединитесь с другим устройством");
        }
    }

    int i = 0;
    /**
     *
     * @param text
     */
    private void appendTextData(String text) {
        textView.append(text + "\n");
        scrollToBottom();
    }

    private void scrollToBottom() {
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.smoothScrollTo(0, textView.getBottom());
            }
        });
    }

    /**
     *
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

//        closeClient();
    }

    /**
     *
     */
    private class WriteTask extends AsyncTask<byte[], Void, Void> {
        @Override
        protected Void doInBackground(byte[]... params) {
            try {
//                clientThread.getCommunicator().write(params[0]);
                clientThread.sendBytes(params[0]);
                clientThread.startResponceListening();
            } catch (Exception ex) {
                Logger.add(ex);
            }
            return null;
        }
    }

}
