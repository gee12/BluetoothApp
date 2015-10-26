package com.icon.activities;

import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView textView = (TextView)findViewById(R.id.textView);
        if (textView != null) {
            String text = String.format("Приложение АГНКС, версия %s", BuildConfig.VERSION_NAME);
            textView.setText(text);
        }
    }
}
