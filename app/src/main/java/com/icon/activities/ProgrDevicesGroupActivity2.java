package com.icon.activities;

import android.app.Activity;
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

import com.icon.agnks.Bluetooth;
import com.icon.agnks.Device;
import com.icon.agnks.Logger;
import com.icon.bluetooth.ClientThread;
import com.icon.bluetooth.CommunicationThread;
import com.icon.utils.MessageBox;
import com.icon.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ProgrDevicesGroupActivity2 extends Activity implements
        ClientThread.ClientListener,
        CommunicationThread.CommunicationListener,
        Bluetooth.OnConnectionTimeoutListener {

    public final static String SEPAR = " ";
    public static final int START_DEVICE_INDEX = -1;

    private List<Device> devices = new ArrayList<>();
    private static ClientThread currentClientThread;

    private Button buttonSendMessage;
    private ProgressBar progressBar;
    private ScrollView scrollView;
    private static TextView textView; // !! без static не добавляет текст в onMessage()
    private RadioGroup radioGroup;
    private EditText editText;
    private int radix = Utils.RADIX_DEC;
    private int currentDeviceIndex = 0;
    private byte[] messageBytes;
    private WriteReadTask task;
    private Bluetooth.ConnectionTimer clientThreadTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_progr2);

        textView = (TextView) findViewById(R.id.data_text);
        textView.setMovementMethod(new ScrollingMovementMethod());
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        editText = (EditText) findViewById(R.id.message_text);
        buttonSendMessage = (Button) findViewById(R.id.button_send_message);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

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

    public void resetIterator() {
        currentDeviceIndex = START_DEVICE_INDEX;
    }

    public boolean nextConnect() {
        currentDeviceIndex++;
        Device next = getDevice(currentDeviceIndex);
        if (next == null) return false;

        BluetoothDevice btDevice = Bluetooth.getRemoteDevice(next.MacAddress);
        createClientThread(btDevice);
        return true;
    }

    private Device getDevice(int index) {
        if (index >= devices.size() || index < 0) {
            return null;
        }
        return devices.get(index);
    }

    /**
     *
     * @param btDevice
     */
    public void createClientThread(BluetoothDevice btDevice) {
        Bluetooth.cancelCurrentDeviceCommunication();

        // запускаем таймер проверки максимального времени на подключение
        clientThreadTimer = new Bluetooth.ConnectionTimer(Bluetooth.MaxTimeout, this);
        clientThreadTimer.start();
        // создаем подключение в новом потоке
        currentClientThread = Bluetooth.createDeviceCommunication(btDevice, this, this);
    }

    /**
     *
     */
    @Override
    public void onTimeoutElapsed() {
        if (clientThreadTimer!=null) {
            clientThreadTimer.cancel();
            clientThreadTimer = null;
        }
        Device device = getDevice(currentDeviceIndex);
        String deviceInfo = Utils.getDeviceInfo(device);
        String s = deviceInfo + ": не удается подключиться (ожидание > " + Bluetooth.MaxTimeout + " мсек)";
        appendTextData(s);
        Logger.add("ProgrDeviceActivity: " + s, Log.INFO);

        Bluetooth.cancelCurrentDeviceCommunication();
        // next
        sendMessage();
    }


    @Override
    public void onConnectionCompleted(final BluetoothDevice remoteDevice, final boolean isConnected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // отключаем таймер
                if (clientThreadTimer != null) {
                    clientThreadTimer.cancel();
                    clientThreadTimer = null;
                }

                if (isConnected) {
                    task = new WriteReadTask();
                    task.execute(messageBytes);
                } else {
                    Device device = getDevice(currentDeviceIndex);
                    String deviceInfo = Utils.getDeviceInfo(device);
                    String s = deviceInfo + ": подключение отклонено";
                    appendTextData(s);
                    Logger.add("ProgrDeviceActivity: " + s, Log.INFO);

                    // next
                    sendMessage();
                }
            }
        });
    }

        @Override
        public void onMessage ( final byte[] bytes){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Device device = getDevice(currentDeviceIndex);
                    String deviceInfo = Utils.getDeviceInfo(device);
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
                Device device = getDevice(currentDeviceIndex);
//                String deviceInfo = (device != null) ? device.CustomName : "unknown";
                String deviceInfo = Utils.getDeviceInfo(device);
                String s = deviceInfo + ": нет ответа (ожидание > " + Bluetooth.MaxTimeout + " мсек)";
                appendTextData(s);
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
        } else {
            Logger.add("ProgrDevicesGroupActivity: Старт отправки данных на доступные устройства", Logger.INFO);
            resetIterator();
            sendMessage();
            appendTextData("---Отправлено: " + Utils.toString(messageBytes, SEPAR, radix));
            //
            setControlsEnabled(false);
        }
    }

    public void setControlsEnabled(boolean isEnabled) {
        int vis = (!isEnabled) ? View.VISIBLE: View.INVISIBLE;
        progressBar.setVisibility(vis);
        if (!isEnabled) editText.setText("");
        editText.setEnabled(isEnabled);
        buttonSendMessage.setEnabled(isEnabled);
        setRadioGroupEnable(isEnabled);
    }

    public void sendMessage() {
        boolean isNextExist = nextConnect();
        if (!isNextExist) {
            sendMessageFinished();
        }
    }

    public void sendMessageFinished() {
        String s = "Отправка данных закончена";
        Logger.add("ProgrDevicesGroupActivity: " + s, Logger.INFO);
        MessageBox.shoter(this, s);
        setControlsEnabled(true);
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
    private static class WriteReadTask extends AsyncTask<byte[], Void, Void> {

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
