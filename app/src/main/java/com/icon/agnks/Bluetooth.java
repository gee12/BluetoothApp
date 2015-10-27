package com.icon.agnks;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.icon.activities.R;
import com.icon.bluetooth.ClientThread;
import com.icon.bluetooth.CommunicationThread;

/**
 * Created by Ivan on 06.10.2015.
 */
public class Bluetooth {

    public static final int DEF_ANSWER_MAX_DELAY = 3000;

    public static BluetoothAdapter Adapter;
    public static int ResponceMsecMax = DEF_ANSWER_MAX_DELAY;
    public static boolean IsAutoEnable;
    private static BroadcastReceiver discoverFoundDeviceReceiver;
    private static BroadcastReceiver discoveryEndReceiver;
    private static ClientThread currentClientThread;

    public static boolean init(Context context) {
        Adapter = getAdapter(context);
        if (Adapter == null) return false;

        ResponceMsecMax = Settings.getPref(context.getString(R.string.pref_key_answer_max_delay), ResponceMsecMax);
        IsAutoEnable = Settings.getPref(context.getString(R.string.pref_key_is_need_bt_auto_enable), false);

        return true;
    }

    public static boolean isEnabled() {
        return !(Adapter == null || !Adapter.isEnabled());
    }

    public static void enable() {
        Logger.add("Включаем Bluetooth", Logger.INFO);
        Adapter.enable();
    }

    public static void enable(Activity act, int requestCode) {
        Logger.add("Включаем Bluetooth", Logger.INFO);
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        act.startActivityForResult(intent, requestCode);
    }

    /**
     * Включение Bluetooth при необходимости.
     * Если IsAutoEnable==True, включаем программно.
     * Иначе выводим запрос на экран (результат ловим в onActivityResult с использованием requestCode)
     * @param act
     * @param requestCode
     */
    public static void enableIfNeed(Activity act, int requestCode) {
        if (isEnabled()) return;
        if (IsAutoEnable) {
            enable();
        } else {
            enable(act, requestCode);
        }
    }

    public static void disable() {
        Logger.add("Выключаем Bluetooth", Logger.INFO);
        if (Adapter != null) Adapter.disable();
    }

    public static void startDiscovery() {
        if (Adapter != null) Adapter.startDiscovery();
    }

    public static void cancelDiscovery() {
        if (Adapter != null) Adapter.cancelDiscovery();
    }

//    public static void registerFoundDeviceReceiver(Context context, BroadcastReceiver receiver) {
//        context.registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
//        Logger.add("Bluetooth: Регистрация FoundDeviceReceiver", Logger.INFO);
//    }
//    public static void registerEndDiscoveryReceiver(Context context, BroadcastReceiver receiver) {
//        context.registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
//        Logger.add("Bluetooth: Регистрация EndDiscoveryReceive", Logger.INFO);
//    }
//    public static void unregisterFoundDeviceReceiver(Context context) {
//        unregisterReceiver(context, discoverFoundDeviceReceiver);
//    }
//    public static void unregisterEndDiscoveryReceiver(Context context) {
//        unregisterReceiver(context, discoveryEndReceiver);
//    }

    public static void registerDiscoveryReceivers(Context context, final DiscoveryListener listener) {
        //
        discoverFoundDeviceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        final BluetoothDevice btDevice = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        listener.onFounded(btDevice);
                    }
                } catch(Exception ex) {
                    Logger.add("Bluetooth: discoverFoundDeviceReceiver.onReceive()", ex);
                }
            }
        };
        //
        discoveryEndReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    listener.onFinished();
                } catch(Exception ex) {
                    Logger.add("Bluetooth: discoveryEndReceiver.onReceive()", ex);
                }
            }
        };
        //
        context.registerReceiver(discoverFoundDeviceReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        context.registerReceiver(discoveryEndReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        Logger.add("Bluetooth: Регистрация FoundDeviceReceiver и DiscoveryEndReceiver", Logger.INFO);
    }

    public static void unregisterDiscoveryReceivers(Context context) {
        unregisterReceiver(context, discoverFoundDeviceReceiver);
        unregisterReceiver(context, discoveryEndReceiver);
    }

    public static void unregisterReceiver(Context context, BroadcastReceiver receiver) {
        if (receiver != null) {
            try {
                context.unregisterReceiver(receiver);
                receiver = null;
            } catch (Exception e) {
                Logger.add("Bluetooth: Не удалось отключить BroadcastReceiver", Logger.ERROR);
            }
        }
    }

    public static BluetoothDevice getRemoteDevice(String mac) {
        return Adapter.getRemoteDevice(mac);
    }

    public static BluetoothAdapter getAdapter(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            return manager.getAdapter();
        } else {
            return BluetoothAdapter.getDefaultAdapter();
        }
    }

    public static ClientThread createDeviceCommunication(BluetoothDevice btDevice,
                                                 ClientThread.ClientListener clientListener,
                                                 CommunicationThread.CommunicationListener communicationListener) {

        ClientThread client = new ClientThread(btDevice, clientListener, communicationListener);
        client.start();
        currentClientThread = client;
        return client;
    }

    public static void cancelDeviceCommunication() {
        if (currentClientThread != null) {
            currentClientThread.cancel();
            currentClientThread = null;
        }
    }

    public static ClientThread getCurrentClientThread() {
        return currentClientThread;
    }

    public static interface DiscoveryListener {
        void onFounded(BluetoothDevice bluetoothDevice);
        void onFinished();
    }
}
