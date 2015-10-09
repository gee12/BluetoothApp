package com.icon.agnks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.icon.utils.CustomExceptionHandler;
import com.icon.utils.Global;
import com.icon.utils.Logger;

import com.icon.bluetooth.TestActivity;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set custom uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        if (savedInstanceState == null) {
            Global.globalContext = this;
        }

        if (!Logger.isInit) {
            Logger.initCacheDir(getBaseContext().getCacheDir().getAbsolutePath());
            Logger.setLevel(Logger.ERROR | Logger.DEBUG | Logger.INFO | Logger.UNCAUGHT);

            Logger.IsNeedLog = SettingsActivity.getPref(getString(R.string.pref_key_is_need_log), true);
            Logger.LogFileMaxSize = Integer.parseInt(SettingsActivity.getPref(getString(R.string.pref_key_log_max_size)));
            Logger.LogFileMaxEndLines = Integer.parseInt(SettingsActivity.getPref(getString(R.string.pref_key_log_last_lines)));

            Logger.add("Запуск: ", Logger.INFO);
        }


        Button button = (Button)findViewById(R.id.button_in_base);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextView textView = (TextView)findViewById(R.id.textView1);
                textView.setText(String.valueOf(Logger.LogFileMaxSize));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        onMenuItemSelected(this, id);
        return super.onOptionsItemSelected(item);
    }

    public static void onMenuItemSelected(Context context, int menuItemId)
    {
        switch(menuItemId) {
            case R.id.action_manage_devices:
                showActivity(context, DevicesManageActivity.class);
                break;
            case R.id.action_test:
                showActivity(context, TestActivity.class);
                break;
            case R.id.action_settings:
                showActivity(context, SettingsActivity.class);
                break;
            case R.id.action_about:
                showActivity(context, AboutActivity.class);
                break;
        }
    }

    public static void showActivity(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
    }
}
