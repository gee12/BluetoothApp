package com.icon.utils;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * Created by Ivan on 19.10.2015.
 */
public class ClearLogsDialogPreference extends DialogPreference {
    public ClearLogsDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            Logger.clearLogs();
            notifyChanged();
        }
    }

//    @Override
//    public CharSequence getSummary() {
//        return "Логи очищены";
//    }
}
