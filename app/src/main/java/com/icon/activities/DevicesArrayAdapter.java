package com.icon.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.icon.agnks.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan on 07.10.2015.
 */
public class DevicesArrayAdapter extends BaseAdapter implements ListAdapter {//extends ArrayAdapter<Device> {

    public interface DevicesListener {
        void onAdd(Device device);
        void onEdit(Device device);
        void onDelete(Device device);
        void onProgramming(Device device);
    }

    public static final int LIST_ITEM_TYPE_NEW = 0;
    public static final int LIST_ITEM_TYPE_EXIST = 1;

    private Context context;
    private ListView listView;
    private final List<Device> list;
    private final List<Device> dbDevices;
    private DevicesListener devicesListener;
    private int checkboxesVisibility = View.GONE;

    public DevicesArrayAdapter(Context context, ListView listView, List<Device> devices, DevicesListener devicesListener) {
        this.context = context;
        this.listView = listView;
        this.list = new ArrayList<>();
        this.dbDevices = new ArrayList<>();
        this.devicesListener = devicesListener;
        reinit(devices, false);
    }


    public void reinit(List<Device> devices, boolean isNeedNotify) {
        List<Device> toAdd = new ArrayList<>(devices);

        this.list.clear();
        this.list.addAll(toAdd);

        this.dbDevices.clear();
        for (Device device : toAdd) {
            if (device.IsSaved)
                this.dbDevices.add(device);
        }

        if (isNeedNotify) notifyDataSetChanged();
    }

    /*
    *
     */
    @Override
    public int getItemViewType(int position) {
        final Device device = getItem(position);
        return getDeviceType(device);
    }

    public int getDeviceType(Device device) {
        return (device.IsSaved) ? LIST_ITEM_TYPE_EXIST : LIST_ITEM_TYPE_NEW;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Device getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).Id;
    }

    public int getPosition(Device device) {
        for(Device item : list) {
            if(item.equals(device))
                return list.indexOf(item);
        }
        return -1;
    }

    public boolean contains(Device device) {
        for (Device item : list) {
            if (item.MacAddress.equalsIgnoreCase(device.MacAddress))//item.equals(device))
                return true;
        }
        return false;
    }

    /*
    *
    */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;//convertView;//super.getView(position, convertView, parent);
//        if (view == null) {
            final Device device = getItem(position);
            int type = getDeviceType(device);
        //
            if (type == LIST_ITEM_TYPE_EXIST) {
                view = LayoutInflater.from(context).inflate(R.layout.template_list_item_exist, parent, false);

                // delete
                ImageButton buttonDelete = (ImageButton) view.findViewById(R.id.template_list_item_delete);
                buttonDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        devicesListener.onDelete(device);
                    }
                })
                ;
                // edit
                ImageButton buttonEdit = (ImageButton) view.findViewById(R.id.template_list_item_edit);
                buttonEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        devicesListener.onEdit(device);
                    }
                });

                // programming
                ImageButton buttonProgr = (ImageButton) view.findViewById(R.id.template_list_item_progr);
                buttonProgr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        devicesListener.onProgramming(device);
                    }
                });

                // checkbox
                CheckBox checkBox = (CheckBox)view.findViewById(R.id.checkBox_item);
                checkBox.setVisibility(checkboxesVisibility);
                checkBox.setChecked(device.IsSelected);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        device.IsSelected = isChecked;
                    }
                });
            //
            } else if (type == LIST_ITEM_TYPE_NEW) {
                view = LayoutInflater.from(context).inflate(R.layout.template_list_item_new, parent, false);

                ImageButton buttonAdd = (ImageButton) view.findViewById(R.id.template_list_item_add);

                // add
                buttonAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        devicesListener.onAdd(device);
                    }
                });
            }

            if (view != null) {
                ((TextView) view.findViewById(R.id.textView_device_name)).setText(device.CustomName);
                ((TextView) view.findViewById(R.id.textView_mac_address)).setText(device.MacAddress);
                setDeviceViewState(device, view);
            }
//        }
        return view;
    }

    /*
    *
     */
    public void setDeviceViewState(Device device, View view) {
        int resColor;
        int state = device.State;
        switch (state) {
            case Device.STATE_ONLINE:
                resColor = R.color.col_device_online;
                break;
//            case Device.STATE_BOND:
//                resColor = R.color.col_device_bond;
//                break;
            default:
                resColor = R.color.col_device_offline;
                break;
        }
        view.setBackgroundResource(resColor);
    }

    /*
    *
     */
    public View getViewByPosition(int pos) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public void setCheckboxesVisibility(int vis, boolean isNeednotify) {
        checkboxesVisibility = vis;
        if (isNeednotify) notifyDataSetChanged();
    }

    public void setCheckboxesChecked(boolean isChecked) {
        for (Device device : list) {
            if (device.IsSaved) device.IsSelected = isChecked;
        }
        notifyDataSetChanged();
    }

    public List<Device> getCheckedDevices() {
        List<Device> res = new ArrayList<>();
        for (Device device : list) {
            if (device.IsSelected) res.add(device);
        }
        return res;
    }

    /*
    *
    */
    public void startDiscovery() {
        for (Device device : dbDevices) {
            device.State = Device.STATE_OFFLINE;
        }
        reinit(dbDevices, true);
    }

    /*
    *
    */
    public void addFoundedDevice(Device device) {
        //
        if (contains(device)) {
            setFoundedToDBDevices(device);
        } else {
            addFoundedLikeNewDevice(device);
        }
    }

    public void setFoundedToDBDevices(Device device) {

        int oldPosition = getPosition(device);
        if (oldPosition >= 0) {
            Device exist = getItem(oldPosition);

            exist.DeviceName = device.DeviceName;
            exist.IsSaved = true;
            exist.State = Device.STATE_ONLINE;

            // add to database devices
            if (!dbDevices.contains(exist)) dbDevices.add(exist);
            update(oldPosition, exist);
        }
    }

    private void addFoundedLikeNewDevice(Device device) {
        device.IsSaved = false;
        device.State = Device.STATE_ONLINE;
        list.add(device);
        notifyDataSetChanged();
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    public void add(Device device) {
        list.add(device);
        notifyDataSetChanged();
    }

    public void update(Device device) {
        int index = getPosition(device);
        list.set(index, device);
        notifyDataSetChanged();
    }

    public void update(int index, Device device) {
        list.set(index, device);
        notifyDataSetChanged();
    }

    public void updateWithoutNotify(Device device) {
        int index = getPosition(device);
        list.set(index, device);
    }

    public void updateWithoutNotify(int index, Device device) {
        list.set(index, device);
    }

    public void delete(Device device) {
        list.remove(device);

        dbDevices.remove(device);

        notifyDataSetChanged();
    }

    public List<Device> getAllDevices() {
        return list;
    }

}
