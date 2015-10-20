package com.icon.utils;

import android.bluetooth.BluetoothDevice;
import android.text.format.Time;

/**
 * Created by Ivan on 02.10.2015.
 */
public class Utils {
    public static Time timeNow() {
        Time time = new Time();
        time.setToNow();
        return time;
    }

    public static byte[] toBytes(int... ints) {
        byte[] result = new byte[ints.length];
        for (int i = 0; i < ints.length; i++) {
            result[i] = (byte) ints[i];
        }
        return result;
    }

    public static byte[] toBytes(char... chars) {
        byte[] result = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            result[i] = (byte) chars[i];
        }
        return result;
    }

    public static String bytesToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b : a)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    public static String intsToHex(int[] a) {
        StringBuilder sb = new StringBuilder();
        for(int i : a)
            sb.append(Integer.toHexString(i));
        return sb.toString();
    }

    public static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    public static byte toByte(boolean bool) {
        return (byte)(bool ? 1 : 0);
    }

    public static String getDeviceInfo(BluetoothDevice device) {
        return String.format("[%s, %s]", device.getName(), device.getAddress());
    }

}
