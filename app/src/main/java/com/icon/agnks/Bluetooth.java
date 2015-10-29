package com.icon.agnks;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;

import com.icon.activities.R;
import com.icon.bluetooth.ClientThread;
import com.icon.bluetooth.CommunicationThread;
import com.icon.utils.Utils;

/**
 * Created by Ivan on 06.10.2015.
 */
public class Bluetooth {

    public static final int DEF_MAX_TIMEOUT = 3000;

    public static BluetoothAdapter Adapter;
    public static int MaxTimeout = DEF_MAX_TIMEOUT;
    public static boolean IsAutoEnable;
    private static BroadcastReceiver discoverFoundDeviceReceiver;
    private static BroadcastReceiver discoveryEndReceiver;
    private static ClientThread currentClientThread;

    /**
     * Инициализация Bluetooth-адаптера и получение значений настроек
     * @param context
     * @return
     */
    public static boolean init(Context context) {
        Adapter = getAdapter(context);
        if (Adapter == null) return false;

        MaxTimeout = Settings.getPref(context.getString(R.string.pref_key_max_timeout), MaxTimeout);
        IsAutoEnable = Settings.getPref(context.getString(R.string.pref_key_is_need_bt_auto_enable), false);
        return true;
    }

    /**
     *
     * @return
     */
    public static boolean isEnabled() {
        return !(Adapter == null || !Adapter.isEnabled());
    }

    /**
     * Включение Bluetooth без запроса
     */
    public static void enable() {
        Logger.add("Включаем Bluetooth", Logger.INFO);
        Adapter.enable();
    }

    /**
     * Включение Bluetooth с запросом к пользователю
     * @param act
     * @param requestCode
     */
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

    /**
     * Отключение Bluetooth
     */
    public static void disable() {
        Logger.add("Выключаем Bluetooth", Logger.INFO);
        if (Adapter != null) Adapter.disable();
    }

    /**
     * Поиск активных устройств
     */
    public static void startDiscovery() {
        if (Adapter != null) Adapter.startDiscovery();
    }

    /**
     * Остановка поиска активных устройств
     */
    public static void cancelDiscovery() {
        if (Adapter != null) Adapter.cancelDiscovery();
    }

    /**
     * Регистрация обработчиков поиска активных устройств в активности
     * @param context
     * @param listener
     */
    public static void registerDiscoveryReceivers(Context context, final OnDiscoveryListener listener) {
        // при обнаружении активного устройства
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
        // при завершении поиска
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

    /**
     * Сброс регистрации обработчиков
     * @param context
     */
    public static void unregisterDiscoveryReceivers(Context context) {
        unregisterReceiver(context, discoverFoundDeviceReceiver);
        unregisterReceiver(context, discoveryEndReceiver);
    }

    /**
     *
     * @param context
     * @param receiver
     */
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

    /**
     * Получение Bluetooth-адаптера на устройстве
     * @param context
     * @return
     */
    public static BluetoothAdapter getAdapter(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            return manager.getAdapter();
        } else {
            return BluetoothAdapter.getDefaultAdapter();
        }
    }

    /**
     * Получение BluetoothDevice устройства по mac-адресу
     * @param mac
     * @return
     */
    public static BluetoothDevice getRemoteDevice(String mac) {
        return Adapter.getRemoteDevice(mac);
    }

    /**
     * Создание нового объекта ClientThread для соединения с BluetoothDevice устройством
     * в отдельном потоке
     * и сохранение его в статической переменной currentClientThread
     * @param btDevice
     * @param clientListener
     * @param communicationListener
     * @return
     */
    public static ClientThread createDeviceCommunication(BluetoothDevice btDevice,
                                                 ClientThread.ClientListener clientListener,
                                                 CommunicationThread.CommunicationListener communicationListener) {

        Logger.add("Bluetooth: Создаем ClientThread для " + Utils.getDeviceInfo(btDevice), Logger.INFO);
        ClientThread client = new ClientThread(btDevice, clientListener, communicationListener);
        client.start();
        currentClientThread = client;
        return client;
    }

    /**
     * Закрытие текущего соединения в currentClientThread
     */
    public static void cancelCurrentDeviceCommunication() {
        if (currentClientThread != null) {
            Logger.add("Bluetooth: Закрываем ClientThread для " + Utils.getDeviceInfo(currentClientThread.getBluetoothDevice()), Logger.INFO);
            currentClientThread.cancel();
            currentClientThread = null;
        }
    }

    /**
     *
     * @return
     */
    public static ClientThread getCurrentClientThread() {
        return currentClientThread;
    }

    /**
     * Интерфейс для создания обработчика поиска активных bluetooth-устройств
     */
    public interface OnDiscoveryListener {
        void onFounded(BluetoothDevice bluetoothDevice);
        void onFinished();
    }

    public interface OnConnectionTimeoutListener {
        void onTimeoutElapsed();
    }

    /**
     * Таймер для проверки лимита времени на соединение
     */
    public static class ConnectionTimer extends CountDownTimer {
        OnConnectionTimeoutListener listener;

        public ConnectionTimer(long timeout, OnConnectionTimeoutListener listener) {
            super(timeout, timeout);
            this.listener = listener;
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            listener.onTimeoutElapsed();
        }
    }

}
