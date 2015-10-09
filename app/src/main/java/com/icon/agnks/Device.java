package com.icon.agnks;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Created by Ivan on 29.09.2015.
 */
public class Device implements Serializable, Comparable<Device> {

    public static final String KEY_DEVICE_ID = "DeviceIdKey";
    public static final String KEY_DEVICE_OBJECT = "DeviceObject";
//    public static final String KEY_DEVICE_NAME = "DeviceName";
//    public static final String KEY_CUSTOM_NAME = "CustomName";
//    public static final String KEY_MAC_ADDRESS = "MacAddress";

    public static String UNKNOWN_DEVICE = "Неизвестное устройство";
    public static String DEF_MAC_ADDRESS = "0000";

    public static final int STATE_OFFLINE = 0;
    public static final int STATE_BOND = 1;
    public static final int STATE_ONLINE = 2;

//    public BluetoothDevice BTDevice;
    public int Id;
    public String CustomName;
    public String DeviceName;
    public String MacAddress;
    public int State;
    public boolean IsSaved;
//    public boolean AutoLinking;

    public Device() {
        init(UNKNOWN_DEVICE, UNKNOWN_DEVICE, DEF_MAC_ADDRESS, STATE_OFFLINE);
    }

    public Device(BluetoothDevice device) {
        String name = device.getName();
        init(name, name, device.getAddress(), STATE_OFFLINE);
    }

    public Device(BluetoothDevice device, int state) {
        String name = device.getName();
        init(name, name, device.getAddress(), state);
    }

    public Device(String name, String addr, int state) {
        init(name, name, addr, state);
    }

    public void init(/*BluetoothDevice device,*/ String deviceName, String customName, String addr, int state) {
//        this.BTDevice = device;
        this.CustomName = deviceName;
        this.DeviceName = customName;
        this.MacAddress = addr;
        this.State = state;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Device))return false;
        Device otherDevice = (Device)other;

//        return (MacAddress.equalsIgnoreCase(otherDevice.MacAddress));
        return new EqualsBuilder().
                append(MacAddress, otherDevice.MacAddress).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 34).
                append(MacAddress).
                toHashCode();
    }

    @Override
    public int compareTo(Device another) {
        return (equals(another)) ? 0 : -1;
    }
}
