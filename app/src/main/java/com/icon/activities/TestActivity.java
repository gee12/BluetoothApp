package com.icon.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.icon.agnks.Bluetooth;
import com.icon.agnks.Logger;
import com.icon.bluetooth.ClientThread;
import com.icon.bluetooth.CommunicationThread;
import com.icon.utils.MessageBox;
import com.icon.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends BaseListActivity implements ClientThread.ClientListener, CommunicationThread.CommunicationListener {

    public final byte[] TEST_BYTES = Utils.toBytes(0xF0, 0x9E, 0x7B, 0, 0, 0, 0x19, 0xF1);

    public final static String UUID = "5e354ea0-668a-11e5-a837-0800200c9a66";
    public final static int REQUEST_ENABLE_BT = 1;

    private class WriteTask extends AsyncTask<byte[], Void, Void> {
        @Override
        protected Void doInBackground(byte[]... params) {
            try {
                clientThread.getCommunicator().write(params[0]);
//                clientThread.sendBytes(params[0]);
            } catch (Exception ex) {
                Logger.add(ex);
            }
            return null;
        }
    }

    private ClientThread clientThread;
    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver discoverDevicesReceiver;
    private BroadcastReceiver discoveryFinishedReceiver;

    private final List<BluetoothDevice> discoveredDevices = new ArrayList<>();
    private ArrayAdapter<BluetoothDevice> listAdapter;
    private TextView textData;
    private EditText textMessage;
    private Button buttonDiscovery;
    private Button buttonSendMessage;
    private ProgressBar progressBarList;
    private ProgressBar progressBarMain;
    private boolean isDiscoveryAtWork;

    @Override
    public void onMessage(final byte[] bytes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String message = Utils.toString(bytes, ",", Utils.RADIX_HEX);
                appendTextData("Принято: " + message);
                Logger.add("TestActivity: Message received: " + message, Log.INFO);
            }
        });
    }

    @Override
    public void onResponceTimeElapsed() {

    }

        @Override
        public void onConnectionCompleted(final BluetoothDevice remoteDevice, final boolean isConnected) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    buttonSendMessage.setEnabled(isConnected);
                    String deviceInfo = Utils.getDeviceInfo(remoteDevice);
                    String strResult = (isConnected) ? " установлено" : " НЕ установлено";
                    MessageBox.longer(TestActivity.this, "Соединение с устройством " + deviceInfo + strResult);
                    String strLogRes = (isConnected) ? "" : "NOT";
                    Logger.add("TestActivity: ClientThread " + strLogRes + " connected. RemoteDevice info: " + deviceInfo, Log.INFO);
                    progressBarMain.setVisibility(View.INVISIBLE);
                    setButtonsEnabled(true);
                }
            });
        }
//    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        textData = (TextView) findViewById(R.id.data_text);
        textMessage = (EditText) findViewById(R.id.message_text);
        buttonDiscovery = (Button) findViewById(R.id.discovery_button);
        buttonSendMessage = (Button) findViewById(R.id.button_send_message);
        progressBarList = (ProgressBar) findViewById(R.id.progressBarList);
        progressBarMain = (ProgressBar) findViewById(R.id.progressBarMain);

        try {
            bluetoothAdapter = Bluetooth.getAdapter(this);

            listAdapter = new ArrayAdapter<BluetoothDevice>(getBaseContext(), android.R.layout.simple_list_item_1, discoveredDevices) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    final BluetoothDevice device = getItem(position);

                    ((TextView) view.findViewById(android.R.id.text1)).setText(device.getName());

                    return view;
                }
            };
        } catch(Exception ex) {
            Logger.add(ex);
        }
        setListAdapter(listAdapter);
    }

    public void discoverDevices(View view) {

        if (bluetoothAdapter == null) {
            MessageBox.shoter(this, "Не найден Bluetooth адаптер на устройстве");
            finish();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
            return;
        }

        setDiscoveryState(!isDiscoveryAtWork);

        if (!isDiscoveryAtWork) return;

        if (discoverDevicesReceiver == null) {
            discoverDevicesReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        deviceFound((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                    }
                }
            };
        }

        if (discoveryFinishedReceiver == null) {
            discoveryFinishedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    setDiscoveryState(false);
                }
            };
        }

        registerReceiver(discoverDevicesReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(discoveryFinishedReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        bluetoothAdapter.startDiscovery();
    }

    public void deviceFound(BluetoothDevice device) {
        if (!discoveredDevices.contains(device)) {
            discoveredDevices.add(device);
            listAdapter.notifyDataSetChanged();
        }
    }

    private void setDiscoveryState(boolean isAtWork) {
        isDiscoveryAtWork = isAtWork;
        if (isAtWork) {
            buttonDiscovery.setText(getString(R.string.str_stop_discover_devices));
            discoveredDevices.clear();
            listAdapter.notifyDataSetChanged();
            progressBarList.setVisibility(ProgressBar.VISIBLE);
            textData.setText("");
        } else {
            buttonDiscovery.setText(getString(R.string.str_discover_devices));
            progressBarList.setVisibility(ProgressBar.INVISIBLE);
            MessageBox.shoter(getBaseContext(), "Поиск закончен. Выберите устройство для соединения");

            unregisterReceivers();

            bluetoothAdapter.cancelDiscovery();
        }
        getListView().setEnabled(!isAtWork);
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        if (clientThread != null) {
            clientThread.cancel();
        }
        BluetoothDevice selectedDevice = discoveredDevices.get(position);
        createClient(selectedDevice);
        textData.setText("");
    }

    public void createClient(BluetoothDevice remoteDevice) {
        clientThread = Bluetooth.createDeviceCommunication(remoteDevice, this, this);

        progressBarMain.setVisibility(ProgressBar.VISIBLE);
        setButtonsEnabled(false);
    }

    private void setButtonsEnabled(boolean isVisible) {
        buttonDiscovery.setEnabled(isVisible);
        buttonSendMessage.setEnabled(isVisible);
    }

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
            Toast.makeText(this, "Сначала соединитесь с другим устройством", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK){
                MessageBox.shoter(this, "Для работы необходимо включить Bluetooth");
                finish();
            }
        }
    }

    private void appendTextData(String text) {
        textData.setText(textData.getText() + "\n" + text);
    }

    public void unregisterReceivers() {
        if (discoverDevicesReceiver != null) {
            try {
                unregisterReceiver(discoverDevicesReceiver);
                discoverDevicesReceiver = null;
            } catch (Exception e) {
                Logger.add("TestActivity: Не удалось отключить ресивер discoverDevicesReceiver", Log.ERROR);
            }
        }

        if (discoveryFinishedReceiver != null) {
            try {
                unregisterReceiver(discoveryFinishedReceiver);
                discoveryFinishedReceiver = null;
            } catch (Exception e) {
                Logger.add("TestActivity: Не удалось отключить ресивер discoveryFinishedReceiver", Log.ERROR);
            }
        }
    }

    @Override
    protected void onStop() {
        if (bluetoothAdapter != null)
            bluetoothAdapter.cancelDiscovery();

        unregisterReceivers();

        if (clientThread != null) {
            clientThread.cancel();
        }
        super.onStop();
    }
}
