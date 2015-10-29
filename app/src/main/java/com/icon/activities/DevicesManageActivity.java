package com.icon.activities;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.icon.agnks.Device;
import com.icon.agnks.Bluetooth;
import com.icon.agnks.Database;
import com.icon.agnks.Logger;
import com.icon.utils.MessageBox;
import com.icon.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class DevicesManageActivity extends BaseListActivity implements Bluetooth.OnDiscoveryListener {

    public final static int REQUEST_ENABLE_BT = 1;
    public final static int REQUEST_ADD_DEVICE = 2;
    public final static int REQUEST_EDIT_DEVICE = 3;
    public final static String PARSEL_TAG_DEVICES = "PARSEL_TAG_DEVICES";
    public final static String PARSEL_TAG_IS_GROUP_MODE = "PARSEL_TAG_IS_GROUP_MODE";

    private DevicesArrayAdapter listAdapter;
    private Button buttonDiscovery;
    private ProgressBar progressBar;
    private CheckBox checkBoxAll;
    private LinearLayout layoutBottomButtons;
    private MenuItem menuItemGroup;
    private boolean isDiscoveryAtWork;
    private Device currentDevice;
    private boolean isGroupMode;

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
        checkBoxAll = (CheckBox)findViewById(R.id.checkBox_all);
        layoutBottomButtons = (LinearLayout)findViewById(R.id.layout_bottom_buttons);

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

        List<Device> devices = null;
        if (savedInstanceState != null) {
            devices = savedInstanceState.getParcelableArrayList(PARSEL_TAG_DEVICES);
            isGroupMode = savedInstanceState.getBoolean(PARSEL_TAG_IS_GROUP_MODE);
        }
        else {
            devices = Database.getDevices();
        }

        //
        try {
            listAdapter = new DevicesArrayAdapter(this, getListView(), devices, devicesListener);
            int vis = (isGroupMode) ? View.VISIBLE : View.GONE;
            listAdapter.setCheckboxesVisibility(vis, false);
        } catch(Exception ex) {
            Logger.add(ex);
        }
        setListAdapter(listAdapter);

        //
        if (savedInstanceState != null) setMode(isGroupMode);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        ArrayList<Device> devices = (ArrayList<Device>) listAdapter.getAllDevices();
        outState.putParcelableArrayList(PARSEL_TAG_DEVICES, devices);
        outState.putBoolean(PARSEL_TAG_IS_GROUP_MODE, isGroupMode);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        BaseActivity.onCreateOptionsMenu(this, menu);
        menuItemGroup = menu.add((isGroupMode) ? "Откл.групповой выбор" : "Групповой выбор");
        menuItemGroup.setOnMenuItemClickListener(new GroupingMenuItemClickListener());
        return true;
    }

    private class GroupingMenuItemClickListener implements MenuItem.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            isGroupMode = !isGroupMode;
            //
            setMode(isGroupMode);
            return true;
        }
    }

    private void setMode(boolean isGroupMode) {
        if (menuItemGroup != null) {
            String text = (isGroupMode) ? "Откл.групповой выбор" : "Групповой выбор";
            menuItemGroup.setTitle(text);
        }
        int vis = (isGroupMode) ? View.VISIBLE : View.GONE;
        checkBoxAll.setVisibility(vis);
        listAdapter.setCheckboxesVisibility(vis, true);
        layoutBottomButtons.setVisibility(vis);
    }

    public void selectAll(View view) {
        CheckBox checkBox = (CheckBox)view;
        boolean isChecked = checkBox.isChecked();
        listAdapter.setCheckboxesChecked(isChecked);
    }

    public void deleteDevices(View view) {
        List<Device> checked = listAdapter.getCheckedDevices();
        StringBuilder sb = new StringBuilder();
        for (Device device : checked) {
            int res = Database.delete(this, device);
            if (res > 0) {
                listAdapter.delete(device);
            } else {
                Utils.append(sb, "Не удается удалить ", Utils.getDeviceInfo(device), "\n");
            }
        }
        if (sb.length() != 0) MessageBox.shoter(DevicesManageActivity.this, sb.toString());
    }

    public void progrDevices(View view) {
        progrDevices(ProgrDevicesGroupActivity.class);
    }

    public void progrDevices2(View view) {
        progrDevices(ProgrDevicesGroupActivity2.class);
    }

    public void progrDevices(Class<?> clazz) {
        List<Device> checked = listAdapter.getCheckedDevices();
        // show programming activity
        Intent intent = new Intent(this, clazz);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Device.KEY_DEVICE_OBJECTS, (ArrayList<? extends Parcelable>) checked);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     *
     * @param view
     */
    public void discoverDevices(View view) {

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

        Bluetooth.registerDiscoveryReceivers(this, this);
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

            Bluetooth.unregisterDiscoveryReceivers(this);
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

    @Override
    protected void onDestroy() {
        Bluetooth.unregisterDiscoveryReceivers(this);
        Bluetooth.cancelDiscovery();

        super.onDestroy();
    }
}
