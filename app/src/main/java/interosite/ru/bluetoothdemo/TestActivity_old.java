package interosite.ru.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.icon.agnks.BaseListActivity;
import com.icon.agnks.R;
import com.icon.utils.Logger;
import com.icon.utils.MessageBox;
import com.icon.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class TestActivity_old extends BaseListActivity {

    public final byte[] TEST_BYTES = Utils.toBytes(0xF0, 0x9E, 0x7B, 0, 0, 0, 0x19, 0xF1);

    public final static String UUID = "5e354ea0-668a-11e5-a837-0800200c9a66";
//    public final static String TAG = TestActivity.class.getName();
    public final static int REQUEST_ENABLE_BT = 1;
//    public byte[] command;

    private class WriteTask extends AsyncTask<byte[], Void, Void> {
        @Override
        protected Void doInBackground(byte[]... params) {
            try {
                clientThreadOld.getCommunicator().write(params[0]);
            } catch (Exception ex) {
                Logger.add(ex, Log.ERROR);
            }
            return null;
        }
    }

    private ServerThread_old serverThreadOld;
    private ClientThread_old clientThreadOld;
    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver discoverDevicesReceiver;
    private BroadcastReceiver discoveryFinishedReceiver;

    private final List<BluetoothDevice> discoveredDevices = new ArrayList<>();

    private ArrayAdapter<BluetoothDevice> listAdapter;
    private TextView textData;
    private EditText textMessage;
    private Button buttonDiscovery;
    private ProgressBar progressBar;
    private boolean isDiscoveryAtWork;

    boolean isInsecureConnect = true;

    private final CommunicatorService communicatorService = new CommunicatorService() {
        @Override
        public Communicator createCommunicatorThread(BluetoothSocket socket) {

            return new ConnectedThread_old(socket, new ConnectedThread_old.CommunicationListener() {

                @Override
                public void onClientConnect(final BluetoothDevice device) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String client = Utils.getDeviceInfo(device);
                            textData.setText(client + "\n" + textData.getText().toString());

                            createClient(device);
//                            MessageBox.shoter(getBaseContext(), "К вам подключилось устройство:" + client);
                        }
                    });
                }

                @Override
                public void onMessage(final String message) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textData.setText(textData.getText().toString() + "\n" + message);
                        }
                    });
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_old);

        textData = (TextView) findViewById(R.id.data_text);
        textMessage = (EditText) findViewById(R.id.message_text);
        buttonDiscovery = (Button) findViewById(R.id.discovery_button);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
                bluetoothAdapter = manager.getAdapter();
            } else {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }

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
            Logger.add(ex, Log.ERROR);
        }
        setListAdapter(listAdapter);
    }

    public void makeDiscoverable(View view) {
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(i);
    }

    public void discoverDevices(View view) {

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

    public void setDiscoveryState(boolean isAtWork) {
        isDiscoveryAtWork = isAtWork;
        if (isAtWork) {
            buttonDiscovery.setText(getString(R.string.str_stop_discover_devices));
            discoveredDevices.clear();
            listAdapter.notifyDataSetChanged();
            progressBar.setVisibility(ProgressBar.VISIBLE);
        } else {
            buttonDiscovery.setText(getString(R.string.str_discover_devices));
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            MessageBox.shoter(getBaseContext(), "Поиск закончен. Выберите устройство для соединения");

            unregisterReceivers();

            bluetoothAdapter.cancelDiscovery();
        }
        getListView().setEnabled(!isAtWork);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (bluetoothAdapter != null)
            bluetoothAdapter.cancelDiscovery();

        unregisterReceivers();

        if (clientThreadOld != null) {
            clientThreadOld.cancel();
        }
        if (serverThreadOld != null) serverThreadOld.cancel();
    }

    public void unregisterReceivers() {
        if (discoverDevicesReceiver != null) {
            try {
                unregisterReceiver(discoverDevicesReceiver);
                discoverDevicesReceiver = null;
            } catch (Exception e) {
                Logger.add("Не удалось отключить ресивер discoverDevicesReceiver", Log.ERROR);
            }
        }

        if (discoveryFinishedReceiver != null) {
            try {
                unregisterReceiver(discoveryFinishedReceiver);
                discoveryFinishedReceiver = null;
            } catch (Exception e) {
                Logger.add("Не удалось отключить ресивер discoveryFinishedReceiver", Log.ERROR);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (bluetoothAdapter == null) {
            MessageBox.shoter(this, "Не найден Bluetooth адаптер на устройстве");
            finish();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
            return;
        }

        createServer();
    }

    public void createServer() {
        try {
            serverThreadOld = new ServerThread_old(communicatorService, bluetoothAdapter, isInsecureConnect);
            serverThreadOld.start();
        } catch (Exception ex) {
            Logger.add(ex, Log.ERROR);
        }

        discoveredDevices.clear();
        listAdapter.notifyDataSetChanged();
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        if (clientThreadOld != null) {
            clientThreadOld.cancel();
        }
        BluetoothDevice selectedDevice = discoveredDevices.get(position);
        createClient(selectedDevice);

    }

    public void createClient(BluetoothDevice remoteDevice) {
        clientThreadOld = new ClientThread_old(remoteDevice, communicatorService, bluetoothAdapter, isInsecureConnect);
        clientThreadOld.start();

        String strResult;
        String strLogRes;
        if (clientThreadOld.isConnected()) {
            strResult = " установлено";
            strLogRes = "";
        } else {
            strResult = " не установлено";
            strLogRes = "NOT";
        }
        String deviceInfo = Utils.getDeviceInfo(remoteDevice);
        MessageBox.longer(this, "Соединение с устройством " + deviceInfo + strResult);
        Logger.add("TestActivity: ClientThread_old " + strLogRes + " connected. RemoteDevice info: " + deviceInfo, Log.INFO);
    }

    public void onSecureChange(View view) {
        CheckBox checkBox = (CheckBox)view;
        isInsecureConnect = checkBox.isChecked();
    }

    public void sendMessage(View view) {
        sendMessage(textMessage.getText().toString().getBytes());
    }

    public void sendTestMessage(View view) {
        textMessage.setText(Utils.bytesToHex(TEST_BYTES));
        sendMessage(TEST_BYTES);
    }

    public void sendMessage(byte[] bytes) {
        if (clientThreadOld != null) {
            new WriteTask().execute(bytes);
            textMessage.setText("");
        } else {
            Toast.makeText(this, "Сначала соединитесь с другим устройством", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK){
                createServer();
            } else {
                MessageBox.shoter(this, "Для работы необходимо включить Bluetooth");
                finish();
            }
        }
    }


}
