package com.icon.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

/**
 * Created by Ivan on 06.10.2015.
 */
public class BluetoothUtils {
    public static BluetoothAdapter getAdapter(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            return manager.getAdapter();
        } else {
            return BluetoothAdapter.getDefaultAdapter();
        }
    }
}
