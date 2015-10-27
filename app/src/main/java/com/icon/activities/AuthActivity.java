package com.icon.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.icon.agnks.Access;
import com.icon.agnks.Bluetooth;
import com.icon.agnks.Settings;
import com.icon.utils.CustomExceptionHandler;
import com.icon.utils.Global;
import com.icon.agnks.Logger;
import com.icon.utils.MessageBox;

/**
 * Created by Ivan on 19.10.2015.
 */
public class AuthActivity extends Activity {
    TextView passTextEdit;

    protected void onCreate(Bundle savedInstanceState) {
        // set custom uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // global context
        if (savedInstanceState == null) {
            Global.globalContext = this;
        }
        //
        startAppSetups();

        // controls
        passTextEdit = (EditText)findViewById(R.id.edittext_pass);
        TextView textView = (TextView)findViewById(R.id.textView_app);
        if (textView != null) {
            String text = String.format("Приложение АГНКС, версия %s", BuildConfig.VERSION_NAME);
            textView.setText(text);
        }
    }

    /**
     * Установка настроек при старте приложения
     */
    private void startAppSetups() {
        // log
        if (!Logger.isInit) {
            Logger.initCacheDir(getBaseContext().getCacheDir().getAbsolutePath());
            Logger.setLevel(Logger.ERROR | Logger.DEBUG | Logger.INFO | Logger.UNCAUGHT);

            Logger.IsNeedLog = Settings.getPref(getString(R.string.pref_key_is_need_log), true);
            Logger.LogFileMaxSize = Settings.getPref(getString(R.string.pref_key_log_max_size), Logger.LogFileMaxSize);
            Logger.LogFileMaxEndLines = Settings.getPref(getString(R.string.pref_key_log_last_lines), Logger.LogFileMaxEndLines);

            String start = String.format("Старт 'АГНКС'. AppId: [%s], Версия: [%s]", BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME);
            Logger.add(start, Logger.INFO);
        }

        // access
        Access.UserPass = Settings.getPref(getString(R.string.pref_key_user_pass), Access.UserPass);
        Access.AdminPass = Settings.getPref(getString(R.string.pref_key_admin_pass), Access.AdminPass);
    }

    /**
     * Проверяем введенный пароль и запускаем главную активность
     * @param view
     */
    public void onApply(View view) {
        String pass = passTextEdit.getText().toString();

//        int res = Access.ADMIN_TYPE;
        int res = Access.checkAccess(pass);

        if (res == Access.EMPTY_TYPE) {
            MessageBox.shoter(this, "Введите пароль");
            return;
        }
        if (res == Access.WRONG_TYPE) {
            MessageBox.shoter(this, "Неверный пароль");
            return;
        }
        if (Access.isAccessAllowed(res)) {
            Access.setAccessType(res);

            if (initBluetooth()) {
                toMainActivity();
            }
        }
    }

    /**
     * Инициализируем и включаем Bluetooth
     * @return
     */
    private boolean initBluetooth() {
        if (!Bluetooth.init(this)) {
            MessageBox.shoter(this, "Не удается найти устройство Bluetooth");
            return false;
        }

        Bluetooth.enableIfNeed(this, DevicesManageActivity.REQUEST_ENABLE_BT);
        return Bluetooth.isEnabled();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DevicesManageActivity.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                toMainActivity();
            } else {
                MessageBox.shoter(this, "Для работы приложения необходимо включить Bluetooth");
//                finish();
            }
        }
    }

    /**
     * Переходим на главную активность
     */
    private void toMainActivity() {
        String accessTypeName = Access.getAccessTypeName();
        MessageBox.shoter(this, "Учетная запись: " + accessTypeName);
        Logger.add("Вход под учетной записью: " + accessTypeName, Logger.INFO);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
