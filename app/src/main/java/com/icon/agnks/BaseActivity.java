package com.icon.agnks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.icon.utils.Logger;

import com.icon.bluetooth.TestActivity;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        if (!Logger.isInit) {
            Logger.initCacheDir(getBaseContext().getCacheDir().getAbsolutePath());
            Logger.setLevel(Log.ERROR | Log.DEBUG | Log.INFO);
        }
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
