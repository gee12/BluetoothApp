package com.icon.utils;

import android.bluetooth.BluetoothDevice;
import android.text.format.Time;
import android.view.View;

import com.icon.agnks.Device;

/**
 * Created by Ivan on 02.10.2015.
 */
public class Utils {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public final static int RADIX_DEC = 10;
    public final static int RADIX_HEX = 16;

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

    public static byte[] stringToBytes(String s, String separ, int radix) throws NumberFormatException {
        String[] ss = s.split(separ);
        byte[] res = new byte[ss.length];
        for (int i = 0; i < ss.length; i++) {
            res[i] = (byte)Integer.parseInt(ss[i], radix);//Byte.parseByte(ss[i], radix);
        }
        return res;
    }

//    public static String bytesToHex(byte[] a) {
//        StringBuilder sb = new StringBuilder(a.length * 2);
//        for(byte b : a)
//            sb.append(String.format("%02x", b & 0xff));
//        return sb.toString();
//    }

    public static String toString(byte[] bytes, String separ, int radix) {
        if (radix == RADIX_HEX) return toHexString(bytes, separ);

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            if (sb.length() != 0) sb.append(separ);
            sb.append(b);
        }
        return sb.toString();
    }

//    public static String toHexString(byte[] bytes, String separ) {
//        StringBuilder sb = new StringBuilder();
//        for(byte b : bytes) {
//            if (sb.length() != 0) sb.append(separ);
//            sb.append(Integer.toHexString(b));
//        }
//        return sb.toString();
//    }

    public static String toHexString(byte[] bytes, String separ) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            if (sb.length() != 0) sb.append(separ);
            int v = b & 0xFF;
            sb.append(hexArray[v >>> 4]);
            sb.append(hexArray[v & 0x0F]);
        }
        return sb.toString();
    }

    public static String toHexString2(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String toHexString(int[] ints) {
        StringBuilder sb = new StringBuilder();
        for(int i : ints)
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
        if (device == null) return null;
        return String.format("[%s, %s]", device.getName(), device.getAddress());
    }

    public static String getDeviceInfo(Device device) {
        if (device == null) return null;
        return String.format("[%s, %s]", device.CustomName, device.MacAddress);
    }

    public static void append(StringBuilder sb, String... ss) {
        if (sb == null) return;
        for (String s : ss) {
            sb.append(s);
        }
    }

//    public static int toVisibility(boolean isEnabled, boolean goneIsNonEnabled) {
//        return (isEnabled) ? View.VISIBLE: (goneIsNonEnabled) ? View.GONE : View.INVISIBLE;
//    }
}
