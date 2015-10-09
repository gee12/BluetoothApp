package com.icon.agnks;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.icon.bluetooth.BluetoothUtils;
import com.icon.db.DBHelper;
import com.icon.utils.Logger;
import com.icon.utils.MessageBox;

import java.util.List;

public class DevicesManageActivity extends BaseListActivity {

    public final static int REQUEST_ENABLE_BT = 1;
    public final static int REQUEST_ADD_DEVICE = 2;
    public final static int REQUEST_EDIT_DEVICE = 3;
//    public final static int REQUEST_DELETE_DEVICE = 4;

    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver discoverDevicesReceiver;
    private BroadcastReceiver discoveryFinishedReceiver;

    private DevicesArrayAdapter listAdapter;
    private Button buttonDiscovery;
    private ProgressBar progressBar;
    private boolean isDiscoveryAtWork;
    private DBHelper dbHelper;
    private Device currentDevice;

    /*
    *
    */
    public class DialogClickListener implements DialogInterface.OnClickListener {
        Device device;
        public DialogClickListener(Device device) {
            this.device = device;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    deleteDevice(device);
                default:
                    dialog.dismiss();
            }
        }
    }

    /*
    *
    */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_manage);

        buttonDiscovery = (Button) findViewById(R.id.discovery_button);
        progressBar = (ProgressBar) findViewById(R.id.progressBarList);

        if (savedInstanceState != null) return;

        //
        DevicesArrayAdapter.DevicesListener devicesListener = new DevicesArrayAdapter.DevicesListener() {
            @Override
            public void onAdd(Device device) {
                // show add activity
                DevicesManageActivity.this.currentDevice = device;
                Intent intent = new Intent(DevicesManageActivity.this, EditDeviceActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(Device.KEY_DEVICE_OBJECT, device);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_ADD_DEVICE);
            }

            @Override
            public void onEdit(Device device) {
                // show edit activity
                DevicesManageActivity.this.currentDevice = device;
                Intent intent = new Intent(DevicesManageActivity.this, EditDeviceActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(Device.KEY_DEVICE_OBJECT, device);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_EDIT_DEVICE);
            }

            @Override
            public void onDelete(Device device) {
                MessageBox.yesNoDialog(DevicesManageActivity.this, new DialogClickListener(device), "Удалить устройство из базы данных?");
            }

            @Override
            public void onProgramming(Device device) {
                // show programming activity
                Intent intent = new Intent(DevicesManageActivity.this, ProgrDeviceActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(Device.KEY_DEVICE_OBJECT, device);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        };

        //
        dbHelper = new DBHelper(this);
        List<Device> dbDevices = dbHelper.getAllToList();
        //
        try {
            bluetoothAdapter = BluetoothUtils.getAdapter(this);
            listAdapter = new DevicesArrayAdapter(this, getListView(), dbDevices, devicesListener);
        } catch(Exception ex) {
            Logger.add(ex, Log.ERROR);
        }
        setListAdapter(listAdapter);
    }

    /*
    *
     */
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
                        final BluetoothDevice btDevice = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        final Device device = new Device(btDevice, Device.STATE_ONLINE);
                        listAdapter.addFoundedDevice(device);
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

    /*
    *
     */
    private void setDiscoveryState(boolean isAtWork) {
        isDiscoveryAtWork = isAtWork;
        if (isAtWork) {
            buttonDiscovery.setText(getString(R.string.str_stop_discover_devices));
            listAdapter.startDiscovery();
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

    /*
    *
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK){
                MessageBox.shoter(this, "Для работы необходимо включить Bluetooth");
                finish();
            }
        } else if (requestCode == REQUEST_ADD_DEVICE) {
            if (resultCode == RESULT_OK){
//                Device added = (Device) data.getSerializableExtra(Device.KEY_DEVICE_OBJECT);
                Device added = DevicesManageActivity.this.currentDevice;
                added.CustomName = data.getStringExtra(Device.KEY_DEVICE_CUSTOM_NAME);
                addDevice(added);
            }
        } else if (requestCode == REQUEST_EDIT_DEVICE) {
            if (resultCode == RESULT_OK){
//                Device updated = (Device) data.getSerializableExtra(Device.KEY_DEVICE_OBJECT);
                Device updated = DevicesManageActivity.this.currentDevice;
                updated.CustomName = data.getStringExtra(Device.KEY_DEVICE_CUSTOM_NAME);
                updateDevice(updated);
            }
        }
    }

    private void addDevice(Device device) {
        long res = (device != null) ? dbHelper.insert(device) : -1;
        if (res > -1) {
            listAdapter.setFoundedToDBDevices(device);
        } else {
            MessageBox.shoter(this, "Не удается добавить устройство в базу данных");
        }
    }

    private void updateDevice(Device device) {
        int res = (device != null) ? dbHelper.update(device) : -1;
        if (res > -1) {
            listAdapter.update(device);
        } else {
            MessageBox.shoter(this, "Не удается редактировать устройство в базе данных");
        }
    }

    private void deleteDevice(Device device) {
        if (dbHelper.delete(device) > -1) {
            listAdapter.delete(device);
        } else {
            MessageBox.shoter(DevicesManageActivity.this, "Не удается удалить устройство из базы данных");
        }
    }

    /*
    *
     */
    public void unregisterReceivers() {
        if (discoverDevicesReceiver != null) {
            try {
                unregisterReceiver(discoverDevicesReceiver);
                discoverDevicesReceiver = null;
            } catch (Exception e) {
                Logger.add("DevicesManageActivity: Не удалось отключить ресивер discoverDevicesReceiver", Log.ERROR);
            }
        }

        if (discoveryFinishedReceiver != null) {
            try {
                unregisterReceiver(discoveryFinishedReceiver);
                discoveryFinishedReceiver = null;
            } catch (Exception e) {
                Logger.add("DevicesManageActivity: Не удалось отключить ресивер discoveryFinishedReceiver", Log.ERROR);
            }
        }
    }

    /*
    *
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothAdapter != null)
            bluetoothAdapter.cancelDiscovery();

        unregisterReceivers();
    }
}
