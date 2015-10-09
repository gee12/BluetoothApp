package com.icon.agnks;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class DevicesManageActivity_old extends BaseListActivity implements AdapterView.OnItemLongClickListener {

    private ArrayAdapter<Device> mAdapter;
    private ArrayList<Device> devicesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_manage);

//        devicesList.add(new Device("устройство1", "mac-addr1"));
//        devicesList.add(new Device("устройство2", "mac-addr2"));
//        devicesList.add(new Device("устройство3", "mac-addr3"));
//        devicesList.add(new Device("устройство4", "mac-addr4"));
//        devicesList.add(new Device("устройство5", "mac-addr5"));

        mAdapter = new ArrayAdapter<Device>(this, R.layout.template_list_item_old, devicesList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;//super.getView(position, convertView, parent);
                if (view == null) {
                    view = LayoutInflater.from(getContext()).inflate(R.layout.template_list_item_old, parent, false);
                }
                final Device device = getItem(position);
                ((RadioButton) view.findViewById(R.id.template_list_item_radio_id)).setText(device.CustomName);
                return view;
            }
        };
        setListAdapter(mAdapter);
        //registerForContextMenu(this);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        super.onListItemClick(parent, v, position, id);

        Device selected = (Device)parent.getItemAtPosition(position);
        Toast.makeText(this, selected.CustomName, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Device selected = (Device)parent.getItemAtPosition(position);

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_context, menu);
    }
}
