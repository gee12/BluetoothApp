package com.icon.activities;

import android.os.Bundle;
import android.widget.TextView;

import com.icon.agnks.Access;
import com.icon.agnks.Bluetooth;
import com.icon.agnks.Database;

/**
 * Created by Ivan on 27.10.2015.
 */
public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String accessTypeName = Access.getAccessTypeName();
        TextView textView = (TextView)findViewById(R.id.textView);
        textView.setText("Тип доступа: " + accessTypeName);

        // инициализируем базу данных
        Database.load(this);
    }

    @Override
    protected void onStop() {

        // выключаем Bluetooth, если включали автоматически
        if (Bluetooth.IsAutoEnable) {
            Bluetooth.disable();
        }
        //
        Bluetooth.cancelCurrentDeviceCommunication();

        super.onStop();
    }
}
