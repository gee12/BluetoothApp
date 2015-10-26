package com.icon.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.icon.agnks.Logger;

/**
 * Created by Ivan on 09.10.2015.
 */
public class LogActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        TextView textView = (TextView)findViewById(R.id.textView);
        String logs = Logger.readLogs();
        textView.setText(logs);
    }
}
