package com.icon.agnks;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.icon.utils.Global;

/**
 * Created by Ivan on 26.10.2015.
 */
public class Settings {

    /**
     * Установка значения опции
     * @param key
     * @param value
     */
    public static void putPref(String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Global.globalContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getPref(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Global.globalContext);
        return preferences.getString(key, null);
    }

    /**
     * Получения значения опции
     * @param key
     * @param defValue
     * @return
     */
    public static String getPref(String key, String defValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Global.globalContext);
        return preferences.getString(key, defValue);
    }

    public static int getPref(String key, int defValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Global.globalContext);
        return Integer.parseInt(preferences.getString(key, String.valueOf(defValue)));
    }

    public static boolean getPref(String key, boolean defValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Global.globalContext);
        return preferences.getBoolean(key, defValue);
    }
}
