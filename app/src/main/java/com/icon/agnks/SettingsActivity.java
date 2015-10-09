package com.icon.agnks;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.icon.utils.Global;
import com.icon.utils.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by Ivan on 09.10.2015.
 */
public class SettingsActivity extends PreferenceActivity /*implements SharedPreferences.OnSharedPreferenceChangeListener*/ {
    protected Method mLoadHeaders = null;
    protected Method mHasHeaders = null;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    /**
     * Checks to see if using new v11+ way of handling PrefsFragments.
     * @return Returns false pre-v11, else checks to see if using headers.
     */

    public boolean isNewV11Prefs() {
        if (mHasHeaders!=null && mLoadHeaders!=null) {
            try {
                return (Boolean)mHasHeaders.invoke(this);
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        return false;
    }

    @Override
    public void onCreate(Bundle aSavedState) {
        //onBuildHeaders() will be called during super.onCreate()
        try {
            mLoadHeaders = getClass().getMethod("loadHeadersFromResource", int.class, List.class );
            mHasHeaders = getClass().getMethod("hasHeaders");
        } catch (NoSuchMethodException e) {
        }
        super.onCreate(aSavedState);
        if (!isNewV11Prefs()) {
            addPreferencesFromResource(R.xml.pref_log);
            addPreferencesFromResource(R.xml.pref_bluetooth);
        }

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        SettingsActivity.this.onSharedPreferenceChanged(prefs, key);
                    }
                };
    }

    @Override
    public void onBuildHeaders(List<Header> aTarget) {
        try {
            mLoadHeaders.invoke(this,new Object[]{R.xml.pref_headers,aTarget});
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String isNeedLogKey = getString(R.string.pref_key_is_need_log);
        String maxSizeKey = getString(R.string.pref_key_log_max_size);
        String lastLinesKey = getString(R.string.pref_key_is_need_log);
        if (key.equals(isNeedLogKey)) {
            Logger.IsNeedLog = sharedPreferences.getBoolean(isNeedLogKey, true);
        } else if (key.equals(maxSizeKey)) {
            Logger.LogFileMaxSize = Integer.parseInt(sharedPreferences.getString(maxSizeKey, String.valueOf(Logger.DEF_LOG_FILE_MAX_SIZE_KBYTES)));
        } else if (key.equals(lastLinesKey)) {
            Logger.LogFileMaxEndLines = sharedPreferences.getInt(lastLinesKey, Logger.DEF_LOG_FILE_MAX_END_LINES);
        }
    }

    @TargetApi(11)
    static public class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle aSavedState) {
            super.onCreate(aSavedState);
            Context anAct = getActivity().getApplicationContext();
            int thePrefRes = anAct.getResources().getIdentifier(getArguments().getString("pref-resource"),
                    "xml",anAct.getPackageName());
            addPreferencesFromResource(thePrefRes);
        }
    }

    public static void putPref(String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Global.globalContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getPref(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Global.globalContext);
        return preferences.getString(key, null);
    }

    public static String getPref(String key, String defValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Global.globalContext);
        return preferences.getString(key, defValue);
    }

    public static boolean getPref(String key, boolean defValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Global.globalContext);
        return preferences.getBoolean(key, defValue);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(listener);
    }
}
