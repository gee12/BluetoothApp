package com.icon.activities;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.icon.agnks.Bluetooth;
import com.icon.agnks.Device;
import com.icon.agnks.Logger;
import com.icon.bluetooth.ClientThread;
import com.icon.bluetooth.CommunicationThread;
import com.icon.utils.MessageBox;
import com.icon.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ProgrDevicesGroupActivity extends Activity implements ClientThread.ClientListener, CommunicationThread.CommunicationListener {

    public final static String SEPAR = " ";
    public static final int START_DEVICE_INDEX = -1;
    public static final int MODE_NONE = 0;
    public static final int MODE_CHECKING_CONNECTION = 1;
    public static final int MODE_COMMUNICATION = 2;

    private Set<BluetoothDevice> onlineBTDevices = new HashSet<>();
    private Iterator<BluetoothDevice> onlineBTDevicesIter = onlineBTDevices.iterator();
    private List<Device> devices = new ArrayList<>();
    private static ClientThread currentClientThread;

    private Button buttonCheckConnect;
    private Button buttonSendMessage;
    private ProgressBar progressBar;
    private ScrollView scrollView;
    private TextView textViewDevices;
    private static TextView textView; // !! без static не добавляет текст в onMessage()
    private RadioGroup radioGroup;
    private EditText editText;
    private boolean isConnectChecked;
    private int radix = Utils.RADIX_DEC;
    public int currentDeviceIndex = 0;
    public int currentMode = MODE_NONE;
    private byte[] messageBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_progr);

        textView = (TextView) findViewById(R.id.data_text);
        textView.setMovementMethod(new ScrollingMovementMethod());
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        editText = (EditText) findViewById(R.id.message_text);
        buttonSendMessage = (Button) findViewById(R.id.button_send_message);
        buttonCheckConnect = (Button) findViewById(R.id.button_check_connection);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textViewDevices = (TextView)findViewById(R.id.textview_devices);

        Bluetooth.enableIfNeed(this, DevicesManageActivity.REQUEST_ENABLE_BT);

        devices = getIntent().getParcelableArrayListExtra(Device.KEY_DEVICE_OBJECTS);
        if (devices == null) {
            MessageBox.longer(this, "Список устройств не определен");
            finish();
        }

        radioGroup = (RadioGroup)findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_dec:
                        radix = Utils.RADIX_DEC;
                        break;
                    case R.id.radio_hex:
                        radix = Utils.RADIX_HEX;
                }
            }
        });

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

    /**
     *
     * @param view
     */
    public void checkConnect(View view) {
        if (currentMode == MODE_NONE) {
            checkConnect();
        } else if (currentMode == MODE_CHECKING_CONNECTION) {
            stopCheckConnect();
        }
    }

    /**
     *
     */
    public void stopCheckConnect() {
        Logger.add("ProgrDevicesGroupActivity: Принудительно останавливаем проверку соединения с устройствами", Logger.INFO);

        currentDeviceIndex = START_DEVICE_INDEX;
        Bluetooth.cancelCurrentDeviceCommunication();

        currentMode = MODE_NONE;
        isConnectChecked = false;
        progressBar.setVisibility(View.INVISIBLE);
        buttonCheckConnect.setText(R.string.str_check_connect);
        buttonSendMessage.setEnabled(false);
        editText.setEnabled(false);
        setRadioGroupEnable(false);
        textView.setText("");
    }

    /**
     *
     */
    public void checkConnect() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        buttonCheckConnect.setText(R.string.str_stop_check_connect);
        buttonSendMessage.setEnabled(false);
        editText.setEnabled(false);
        setRadioGroupEnable(false);
        textView.setText("");
        textViewDevices.setText("");
        Logger.add("ProgrDevicesGroupActivity: Старт проверки соединения с устройствами..", Logger.INFO);

        currentMode = MODE_CHECKING_CONNECTION;
        onlineBTDevices.clear();
        firstConnect();
    }

    public void firstConnect() {
        currentDeviceIndex = START_DEVICE_INDEX;
        nextConnect();
    }

    public boolean nextConnect() {
        currentDeviceIndex++;
        Device next = getDevice(currentDeviceIndex);
        if (next == null) return false;

        BluetoothDevice btDevice = Bluetooth.getRemoteDevice(next.MacAddress);
        createClientThread(btDevice);
        return true;
    }

    public void resetOnlineConnectIter() {
        onlineBTDevicesIter = onlineBTDevices.iterator();
    }

    public boolean nextOnlineConnect() {
        boolean hasNext = onlineBTDevicesIter.hasNext();
        if (hasNext) {
            BluetoothDevice temp = onlineBTDevicesIter.next();

            BluetoothDevice next = Bluetooth.getRemoteDevice(temp.getAddress());

            createClientThread(next);
        }
        return hasNext;
    }

    public void createClientThread(BluetoothDevice btDevice) {
        Bluetooth.cancelCurrentDeviceCommunication();
        currentClientThread = Bluetooth.createDeviceCommunication(btDevice, this, this);
    }

    @Override
    public void onConnectionCompleted(final BluetoothDevice remoteDevice, final boolean isConnected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String deviceInfo = Utils.getDeviceInfo(remoteDevice);
                if (currentMode == MODE_CHECKING_CONNECTION) {
                    if (isConnected) {
                        onlineBTDevices.add(remoteDevice);
                    }
                    String s = (isConnected) ? "доступен" : "не отвечает";
                    textViewDevices.append(String.format("%s: %s\n", deviceInfo, s));
//                MessageBox.shoter(ProgrDevicesGroupActivity.this, String.format("%s: %s", deviceInfo, s));
                    Logger.add(String.format("ProgrDevicesGroupActivity: Устройство %s: %s", deviceInfo, s), Log.INFO);

                    boolean isNextExist = nextConnect();
                    if (!isNextExist) {
                        checkConnectionFinished();
                    }
                } else if (currentMode == MODE_COMMUNICATION) {
                    if (isConnected) {
                        new WriteReadTask().execute(messageBytes);
                    } else {
                        appendTextData(deviceInfo + ": не отвечает");

                        // next
                        sendMessage();
                    }
                }
            }
        });
    }

    private void checkConnectionFinished() {
        Bluetooth.cancelCurrentDeviceCommunication();

        currentMode = MODE_NONE;
        isConnectChecked = true;
        progressBar.setVisibility(View.INVISIBLE);
        buttonCheckConnect.setText(R.string.str_check_connect);
        buttonSendMessage.setEnabled(true);
        setRadioGroupEnable(true);
        editText.setEnabled(true);

        MessageBox.shoter(ProgrDevicesGroupActivity.this, "Проверка соединения закончена");
        Logger.add("ProgrDevicesGroupActivity: Проверка соединения с устройствами закончена", Logger.INFO);

    }

    @Override
    public void onMessage(final byte[] bytes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BluetoothDevice device = getCurrentOnlineDevice();
                String deviceInfo = (device != null) ? device.getName() : "unknown";

                String message = Utils.toString(bytes, SEPAR, Utils.RADIX_HEX);
                String s = deviceInfo + ": " + message;
                appendTextData(s);
//                MessageBox.shoter(ProgrDevicesGroupActivity.this, s);

                Logger.add("ProgrDeviceActivity: " + deviceInfo + ": [" + message + "]", Log.INFO);

                // next
                sendMessage();
            }
        });
    }

    @Override
    public void onResponceTimeElapsed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BluetoothDevice device = getCurrentOnlineDevice();
                String deviceInfo = (device != null) ? device.getName() : "unknown";

                String s = deviceInfo + ": время ожидания истекло (" + Bluetooth.MaxTimeout + " мсек)";
                appendTextData(s);
//                MessageBox.shoter(ProgrDevicesGroupActivity.this, s);
                Logger.add("ProgrDeviceActivity: " + s, Log.INFO);

                // next
                sendMessage();
            }
        });
    }

    /**
     *
     * @param view
     */
    public void sendMessage(View view) {
        messageBytes = parseMessage(editText.getText().toString());
        if (messageBytes == null) {
            MessageBox.shoter(this, "Не удается распарсить данные");
        } else if (isConnectChecked) {
            currentMode = MODE_COMMUNICATION;
            Logger.add("ProgrDevicesGroupActivity: Старт отправки данных на доступные устройства", Logger.INFO);
            resetOnlineConnectIter();
            sendMessage();
            appendTextData("---Отправлено: " + Utils.toString(messageBytes, SEPAR, radix));

            //
            progressBar.setVisibility(View.VISIBLE);
            editText.setText("");
            editText.setEnabled(false);
            buttonCheckConnect.setEnabled(false);
            buttonSendMessage.setEnabled(false);
            setRadioGroupEnable(false);
        } else {
            MessageBox.shoter(this, "Сначала проверьте соединение с устройствами");
            Bluetooth.cancelCurrentDeviceCommunication();
        }
    }

    public void sendMessage() {
        boolean isNextExist = nextOnlineConnect();
        if (!isNextExist) {
            sendMessageFinished();
        }
    }

    public void sendMessageFinished() {
        currentMode = MODE_NONE;
        String s = "Отправка данных закончена";
        Logger.add("ProgrDevicesGroupActivity: " + s, Logger.INFO);
        MessageBox.shoter(this, s);
        progressBar.setVisibility(View.INVISIBLE);
        buttonSendMessage.setEnabled(true);
        editText.setEnabled(true);
        buttonCheckConnect.setEnabled(true);
        setRadioGroupEnable(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (clientThread != null) {
//            clientThread.setClientListener(this);
//            clientThread.setCommunicationListener(this);
//        }
    }

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

    private void setRadioGroupEnable(boolean isEnabled) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(isEnabled);
        }
    }

    /**
     *
     * @return
     */
    private Device getDevice(int index) {
        if (index >= devices.size() || index < 0) {
            return null;
        }
        return devices.get(index);
    }

    private BluetoothDevice getCurrentOnlineDevice() {
        return (currentClientThread!=null) ? currentClientThread.getBluetoothDevice() : null;
    }

    /**
     *
     * @param str
     * @return
     */
    public byte[] parseMessage(String str) {
        byte[] res = null;
        try {
            res = Utils.stringToBytes(str, SEPAR, radix);
        } catch(Exception ex) {
        }
        return res;
    }

    /**
     *
     */
    @Override
    protected void onStop() {
        Bluetooth.cancelCurrentDeviceCommunication();

        super.onStop();
    }

    /**
     * Передача данных и прием ответа в отдельном потоке
     */
    private class WriteReadTask extends AsyncTask<byte[], Void, Void> {
        @Override
        protected Void doInBackground(byte[]... params) {
            try {
                if (currentClientThread == null) {
                    Logger.add("WriteReadTask.doInBackground(): currentClientThread is null", Logger.DEBUG);
                    return null;
                }
                currentClientThread.getCommunicator().writeAndListenResponce(params[0]);
            } catch (Exception ex) {
                Logger.add(ex);
            }
            return null;
        }
    }

}
