package com.icon.agnks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.icon.utils.Access;
import com.icon.utils.CustomExceptionHandler;
import com.icon.utils.Global;
import com.icon.utils.Logger;

import com.icon.bluetooth.TestActivity;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
//        Button button = (Button)findViewById(R.id.button_in_base);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final TextView textView = (TextView)findViewById(R.id.textView1);
//                textView.setText(String.valueOf(Logger.LogFileMaxSize));
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return onCreateOptionsMenu(this, menu);
    }

    /**
     * Check access
     * @param activity
     * @param menu
     * @return
     */
    public static boolean onCreateOptionsMenu(Activity activity, Menu menu) {
        int menuRes = R.menu.menu_user;
        if (Access.isAdmin())
            menuRes = R.menu.menu_admin;
        else if (Access.isUser())
            menuRes = R.menu.menu_user;
        activity.getMenuInflater().inflate(menuRes, menu);

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
