package com.icon.agnks;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.icon.utils.Utils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Created by Ivan on 29.09.2015.
 */
public class Device implements /*Serializable,*/ Comparable<Device>, Parcelable {

    public static final String KEY_DEVICE_ID = "DeviceIdKey";
    public static final String KEY_DEVICE_OBJECT = "DeviceObject";
//    public static final String KEY_DEVICE_NAME = "DeviceName";
    public static final String KEY_DEVICE_CUSTOM_NAME = "CustomName";
//    public static final String KEY_DEVICE_MAC_ADDRESS = "MacAddress";

    public static String UNKNOWN_DEVICE = "Неизвестное устройство";
    public static String DEF_MAC_ADDRESS = "0000";

    public static final int STATE_OFFLINE = 1;
//    public static final int STATE_BOND = 2;
    public static final int STATE_ONLINE = 3;

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

    public Device(Parcel parcel) {
        this.Id = parcel.readInt();
        this.CustomName = parcel.readString();
        this.DeviceName = parcel.readString();
        this.MacAddress = parcel.readString();
        this.State = parcel.readInt();
        this.IsSaved = parcel.readByte() != 0;
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

        return (MacAddress.equalsIgnoreCase(otherDevice.MacAddress));
//        return new EqualsBuilder().
//                append(MacAddress, otherDevice.MacAddress).
//                isEquals();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Id);
        dest.writeString(CustomName);
        dest.writeString(DeviceName);
        dest.writeString(MacAddress);
        dest.writeInt(State);
        dest.writeByte(Utils.toByte(IsSaved));
    }

    public static final Parcelable.Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel source) {
            return new Device(source);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };
}
