package com.icon.activities;

/**
 * Created by Ivan on 26.10.2015.
 */

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.icon.agnks.Logger;

/**
 * Диалог очистки лог-файла (используется в pref_log.xml)
 */
public class ClearLogsDialogPreference extends DialogPreference {
    public ClearLogsDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {

            // очищаем лог-файл
            Logger.clearLogs();
            notifyChanged();
        }
    }
}