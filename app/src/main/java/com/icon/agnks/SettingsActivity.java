package com.icon.agnks;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.icon.utils.Access;
import com.icon.utils.Global;
import com.icon.utils.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by Ivan on 09.10.2015.
 */
public class SettingsActivity extends PreferenceActivity /*implements SharedPreferences.OnSharedPreferenceChangeListener*/ {
    public static final String TAG_PREF_RES = "pref_res";

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

        // добавление разделов настроек для девайсов с API < 11
        if (!isNewV11Prefs()) {

            if (Access.isAdmin())
                addPreferencesFromResource(R.xml.pref_log);
            addPreferencesFromResource(R.xml.pref_bluetooth);
            addPreferencesFromResource(R.xml.pref_access);

            makeAccess(getPreferenceManager(),getResources());
        }

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        SettingsActivity.this.onSharedPreferenceChanged(prefs, key);
                    }
                };
    }

    public static void makeAccess(PreferenceManager manager, Resources res) {
        if (!Access.isAdmin()){
            // pass
            Preference adminPassPref = manager.findPreference(res.getString(R.string.pref_key_admin_pass));
            PreferenceCategory accessPrefCat = (PreferenceCategory)manager.findPreference(res.getString(R.string.pref_key_category_access));
            if (accessPrefCat!=null && adminPassPref!=null) accessPrefCat.removePreference(adminPassPref);
        }
    }

    /**
     * Добавление разделов настроек как хеадеров для девайсов с API >= 11
     * @param aTarget
     */
    @Override
    public void onBuildHeaders(List<Header> aTarget) {
        try {
            mLoadHeaders.invoke(this, new Object[]{R.xml.pref_headers, aTarget});

            if (Access.isAdmin() && android.os.Build.VERSION.SDK_INT >= 11) {
                Header logHeader = new Header();
                logHeader.title = "Логи";
                logHeader.fragment = PrefsFragment.class.getName();

                Bundle bundle = new Bundle();
                bundle.putInt(TAG_PREF_RES, R.xml.pref_log);
                logHeader.fragmentArguments = bundle;

                aTarget.add(0, logHeader);
            }

        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }

    /**
     * Обработчик изменения настроек
     * @param sharedPreferences
     * @param key
     */
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String isNeedLogKey = getString(R.string.pref_key_is_need_log);
        String maxSizeKey = getString(R.string.pref_key_log_max_size);
        String lastLinesKey = getString(R.string.pref_key_is_need_log);
        String userPassKey = getString(R.string.pref_key_user_pass);
        String adminPassKey = getString(R.string.pref_key_admin_pass);
        if (key.equals(isNeedLogKey)) {
            Logger.IsNeedLog = sharedPreferences.getBoolean(isNeedLogKey, true);
        } else if (key.equals(maxSizeKey)) {
            Logger.LogFileMaxSize = Integer.parseInt(sharedPreferences.getString(maxSizeKey, String.valueOf(Logger.DEF_LOG_FILE_MAX_SIZE_KBYTES)));
        } else if (key.equals(lastLinesKey)) {
            Logger.LogFileMaxEndLines = sharedPreferences.getInt(lastLinesKey, Logger.DEF_LOG_FILE_MAX_END_LINES);
        } else if (key.equals(userPassKey)) {
            Access.UserPass = sharedPreferences.getString(userPassKey, Access.UserPass);
        } else if (key.equals(adminPassKey)) {
            Access.AdminPass = sharedPreferences.getString(adminPassKey, Access.AdminPass);
        }
    }

    /**
     * Класс фрагмента для девайсов с API >= 11
     */
    @TargetApi(11)
    static public class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle aSavedState) {
            super.onCreate(aSavedState);

            int prefRes = -1;
            if (aSavedState == null) {
                prefRes = getArguments().getInt(TAG_PREF_RES, -1);
            } else {
                prefRes = aSavedState.getInt(TAG_PREF_RES);
            }
            if (prefRes != -1) {
                addPreferencesFromResource(prefRes);
            } else {
                Context anAct = getActivity().getApplicationContext();
                prefRes = getResources().getIdentifier(getArguments().getString("pref-resource"),
                        "xml", anAct.getPackageName());
                addPreferencesFromResource(prefRes);
            }

            if (!Access.isAdmin()) {
                if (prefRes == R.xml.pref_access) {
//                PreferenceScreen screen = getPreferenceScreen();
//                if (screen != null && screen.getKey().equals(getString(R.string.pref_key_screen_access))) {
                    SettingsActivity.makeAccess(getPreferenceManager(),getResources());
                }
            }
        }

    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return PrefsFragment.class.getName().equals(fragmentName);
    }

    /**
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
//        getPreferenceScreen().getSharedPreferences()
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        getPreferenceScreen().getSharedPreferences()
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(listener);
    }

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
