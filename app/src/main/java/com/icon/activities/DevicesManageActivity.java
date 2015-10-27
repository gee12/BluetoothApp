package com.icon.activities;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.icon.agnks.Device;
import com.icon.agnks.Bluetooth;
import com.icon.agnks.Database;
import com.icon.agnks.Logger;
import com.icon.utils.MessageBox;

import java.util.ArrayList;
import java.util.List;

public class DevicesManageActivity extends BaseListActivity implements Bluetooth.DiscoveryListener {

    public final static int REQUEST_ENABLE_BT = 1;
    public final static int REQUEST_ADD_DEVICE = 2;
    public final static int REQUEST_EDIT_DEVICE = 3;
    public final static String PARSEL_TAG_DEVICES = "PARSEL_TAG_DEVICES";

//    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver discoverDevicesReceiver;
    private BroadcastReceiver discoveryFinishedReceiver;

    private DevicesArrayAdapter listAdapter;
    private Button buttonDiscovery;
    private ProgressBar progressBar;
    private boolean isDiscoveryAtWork;
//    private Database database;
    private Device currentDevice;

    /**
     *
     */
    public class DeleteDeviceDialogListener implements DialogInterface.OnClickListener {
        Device device;
        public DeleteDeviceDialogListener(Device device) {
            this.device = device;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    // delete
                    deleteDevice(device);
                default:
                    dialog.dismiss();
            }
        }
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_manage);

        buttonDiscovery = (Button) findViewById(R.id.discovery_button);
        progressBar = (ProgressBar) findViewById(R.id.progressBarList);

        //
        DevicesArrayAdapter.DevicesListener devicesListener = new DevicesArrayAdapter.DevicesListener() {
            @Override
            public void onAdd(Device device) {
                // show add activity
                DevicesManageActivity.this.currentDevice = device;
                Intent intent = new Intent(DevicesManageActivity.this, EditDeviceActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(Device.KEY_DEVICE_OBJECT, device);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_ADD_DEVICE);
            }

            @Override
            public void onEdit(Device device) {
                // show edit activity
                DevicesManageActivity.this.currentDevice = device;
                Intent intent = new Intent(DevicesManageActivity.this, EditDeviceActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(Device.KEY_DEVICE_OBJECT, device);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_EDIT_DEVICE);
            }

            @Override
            public void onDelete(Device device) {
                MessageBox.yesNoDialog(DevicesManageActivity.this, new DeleteDeviceDialogListener(device), "Удалить устройство из базы данных?");
            }

            @Override
            public void onProgramming(Device device) {
                // show programming activity
                Intent intent = new Intent(DevicesManageActivity.this, ProgrDeviceActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(Device.KEY_DEVICE_OBJECT, device);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        };

        //
//        database = new Database(this);

        List<Device> devices = null;

        if (savedInstanceState != null) {
            devices = savedInstanceState.getParcelableArrayList(PARSEL_TAG_DEVICES);
        }
        else {
//            devices = database.selectAllToList();
            devices = Database.getDevices();
        }

        //
        try {
//            bluetoothAdapter = Bluetooth.getAdapter(this);
            listAdapter = new DevicesArrayAdapter(this, getListView(), devices, devicesListener);
        } catch(Exception ex) {
            Logger.add(ex);
        }
        setListAdapter(listAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        ArrayList<Device> devices = (ArrayList<Device>) listAdapter.getAllDevices();
        outState.putParcelableArrayList(PARSEL_TAG_DEVICES, devices);

        super.onSaveInstanceState(outState);
    }

    /**
     *
     * @param view
     */
    public void discoverDevices(View view) {

//        if (bluetoothAdapter == null) {
//            MessageBox.shoter(this, "Не найден Bluetooth адаптер на устройстве");
//            finish();
//            return;
//        }
//
//        if (!bluetoothAdapter.isEnabled()) {
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(intent, REQUEST_ENABLE_BT);
//            return;
//        }

        Bluetooth.enableIfNeed(this, REQUEST_ENABLE_BT);
        if (!Bluetooth.isEnabled() && Bluetooth.IsAutoEnable) {
            bluetoothNoRunMessage();
            return;
        }

        _discoverDevices();
    }
    public void _discoverDevices() {

        setDiscoveryState(!isDiscoveryAtWork);

        if (!isDiscoveryAtWork) return;

        //
//        if (discoverDevicesReceiver == null) {
//            discoverDevicesReceiver = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    try {
//                        String action = intent.getAction();
//                        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                            final BluetoothDevice btDevice = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                            final Device device = new Device(btDevice, Device.STATE_ONLINE);
//                            listAdapter.addFoundedDevice(device);
//                        }
//                    } catch(Exception ex) {
//                        Logger.add("DevicesManageActivity.discoverDevices(): BroadcastReceiver.onReceive(): ", ex);
//                    }
//                }
//            };
//        }
//        //
//        if (discoveryFinishedReceiver == null) {
//            discoveryFinishedReceiver = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    setDiscoveryState(false);
//                }
//            };
//        }
        //
//        registerReceiver(discoverDevicesReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
//        registerReceiver(discoveryFinishedReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        Bluetooth.registerDiscoveryReceivers(this, this);

//        bluetoothAdapter.startDiscovery();
        Bluetooth.startDiscovery();
    }

    @Override
    public void onFounded(BluetoothDevice bluetoothDevice) {
        final Device device = new Device(bluetoothDevice, Device.STATE_ONLINE);
        listAdapter.addFoundedDevice(device);
    }

    @Override
    public void onFinished() {
        setDiscoveryState(false);
    }

    private void bluetoothNoRunMessage() {
        MessageBox.shoter(this, "Не удается запустить Bluetooth");
    }

    /**
     *
     * @param isAtWork
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
//            bluetoothAdapter.cancelDiscovery();
            Bluetooth.cancelDiscovery();
        }
        getListView().setEnabled(!isAtWork);
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                _discoverDevices();
            } else {
                bluetoothNoRunMessage();
            }
        } else if (requestCode == REQUEST_ADD_DEVICE) {
            if (resultCode == RESULT_OK){
                Device added = DevicesManageActivity.this.currentDevice;
                added.CustomName = data.getStringExtra(Device.KEY_DEVICE_CUSTOM_NAME);
                addDevice(added);
            }
        } else if (requestCode == REQUEST_EDIT_DEVICE) {
            if (resultCode == RESULT_OK){
                Device updated = DevicesManageActivity.this.currentDevice;
                updated.CustomName = data.getStringExtra(Device.KEY_DEVICE_CUSTOM_NAME);
                updateDevice(updated);
            }
        }
    }

    private void addDevice(Device device) {
        long res = (device != null) ? Database.insert(this, device) : -1;
        if (res > -1) {
            device.Id = res;
            listAdapter.setFoundedToDBDevices(device);
        } else {
            MessageBox.shoter(this, "Не удается добавить устройство в базу данных");
        }
    }

    private void updateDevice(Device device) {
        int res = (device != null) ? Database.update(this, device) : 0;
        if (res > 0) {
            listAdapter.update(device);
        } else {
            MessageBox.shoter(this, "Не удается редактировать устройство в базе данных");
        }
    }

    private void deleteDevice(Device device) {
        int res = (device != null) ? Database.delete(this, device) : 0;
        if (res > 0) {
            listAdapter.delete(device);
        } else {
            MessageBox.shoter(DevicesManageActivity.this, "Не удается удалить устройство из базы данных");
        }
    }

    /**
     *
     */
    public void unregisterReceivers() {
        Bluetooth.unregisterReceiver(this, discoverDevicesReceiver);
        Bluetooth.unregisterReceiver(this, discoveryFinishedReceiver);
//        if (discoverDevicesReceiver != null) {
//            try {
//                unregisterReceiver(discoverDevicesReceiver);
//                discoverDevicesReceiver = null;
//            } catch (Exception e) {
//                Logger.add("DevicesManageActivity: Не удалось отключить ресивер discoverDevicesReceiver", Log.ERROR);
//            }
//        }
//
//        if (discoveryFinishedReceiver != null) {
//            try {
//                unregisterReceiver(discoveryFinishedReceiver);
//                discoveryFinishedReceiver = null;
//            } catch (Exception e) {
//                Logger.add("DevicesManageActivity: Не удалось отключить ресивер discoveryFinishedReceiver", Log.ERROR);
//            }
//        }
    }

    /*
    *
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        Bluetooth.cancelDiscovery();

        unregisterReceivers();
    }
}
