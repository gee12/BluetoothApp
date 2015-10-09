package com.icon.utils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Ivan on 30.09.2015.
 */
public class Logger {

    public static final String DEF_TAG = "agnks";
    public static final String DEF_LOG_DIR = "/";
    public static final String DEF_LOG_FILE_NAME = DEF_TAG + "_log.log";
    public static final long LOG_FILE_MAX_SIZE = 32 * 1024;
    public static final int LOG_FILE_MAX_END_LINES = 20;

    public static String Tag = DEF_TAG;
    public static String LogDir = DEF_LOG_DIR;
    public static String CacheDir = DEF_LOG_DIR;
    public static String LogFileName = DEF_LOG_FILE_NAME;
    public static File LogFile = null;
    public static boolean isInit;
    public static int Level = -1;

    public static void initCacheDir(String cacheDir) {
        init(DEF_LOG_DIR, cacheDir, DEF_LOG_FILE_NAME);
    }

    public static void init(String logDir, String logFileName) {
        LogDir = logDir;
        LogFileName = logFileName;
        init(LogDir, CacheDir, LogFileName);
    }

    public static void init(String logDir, String cacheDir, String logFileName) {
        LogDir = logDir;
        CacheDir = cacheDir;
        LogFileName = logFileName;
        LogFile = initLogFile(LogDir, CacheDir, LogFileName);
        isInit = true;
    }

    private static File initLogFile(String logDir, String cacheDir, String logFileName) {
        File file = null;
        String sdState = Environment.getExternalStorageState();
        if (sdState.equals(Environment.MEDIA_MOUNTED)) {
            File sdDir = Environment.getExternalStorageDirectory();
            file = new File(sdDir.getAbsolutePath() + "/" + logDir, logFileName);
        } else {
            file = new File(cacheDir, logFileName);
        }
        return file;
    }

    public static File defaultInitLogFile() {
        return initLogFile(DEF_LOG_DIR, DEF_LOG_DIR, DEF_LOG_FILE_NAME);
    }

    public static void setTag(String tag) {
        Tag = tag;
    }

    public static void setLevel(int level) {
        Level = level;
    }

    public static boolean isLevelSuit(int type) {
        return (Level & type) != 0;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void add(String message, int type, boolean isNeedSystemLog) {
        if (isLog() && isLevelSuit(type)) {
            String strType = systemlog(message, type, isNeedSystemLog);
            write(strType, message);
        }
    }

    public static void add(Exception ex, int type, boolean isNeedSystemLog) {
        if (isLog() && isLevelSuit(type)) {
            String message = ex.getMessage();
            String strType = systemlog(message, ex, type, isNeedSystemLog);
            write(strType, message);
        }
    }

    public static void add(String message, Exception ex, int type, boolean isNeedSystemLog) {
        if (isLog() && isLevelSuit(type)) {
            String allMessage = message + "\n" + ex.getMessage();
            String strType = systemlog(allMessage, ex, type, isNeedSystemLog);
            write(strType, allMessage);
        }
    }

    public static void add(String message, int type) {
        if (isLog() && isLevelSuit(type)) {
            String strType = systemlog(message, type, true);
            write(strType, message);
        }
    }

    public static void add(Exception ex, int type) {
        if (isLog() && isLevelSuit(type)) {
            String message = ex.getMessage();
            String strType = systemlog(message, ex, type, true);
            write(strType, message);
        }
    }

    public static void add(String message, Exception ex, int type) {
        if (isLog() && isLevelSuit(type)) {
            String allMessage = message + "\n" + ex.getMessage();
            String strType = systemlog(allMessage, ex, type, true);
            write(strType, allMessage);
        }
    }

    public static String systemlog(String message, int type, boolean isNeedWriteLog) {
        switch(type) {
            case Log.ERROR:
                if (isNeedWriteLog) Log.e(Tag, message);
                return "ERROR";
            case Log.DEBUG:
                if (isNeedWriteLog) Log.d(Tag, message);
                return "DEBUG";
            case Log.VERBOSE:
                if (isNeedWriteLog) Log.v(Tag, message);
                return "VERBOSE";
            case Log.INFO:
                if (isNeedWriteLog) Log.i(Tag, message);
                return "INFO";
            case Log.WARN:
                if (isNeedWriteLog) Log.w(Tag, message);
                return "WARN";
        }
        return "NONE";
    }

    public static String systemlog(String message, Exception ex, int type, boolean isNeedWriteLog) {
        switch(type) {
            case Log.ERROR:
                if (isNeedWriteLog) Log.e(Tag, message, ex);
                return "ERROR";
            case Log.DEBUG:
                if (isNeedWriteLog) Log.d(Tag, message, ex);
                return "DEBUG";
            case Log.VERBOSE:
                if (isNeedWriteLog) Log.v(Tag, message, ex);
                return "VERBOSE";
            case Log.INFO:
                if (isNeedWriteLog) Log.i(Tag, message, ex);
                return "INFO";
            case Log.WARN:
                if (isNeedWriteLog) Log.w(Tag, message, ex);
                return "WARN";
        }
        return "NONE";
    }

    public static void systemlog(String message, Exception ex) {
        Log.e(Tag, message, ex);
    }

    public static void systemlog(Exception ex) {
        Log.e(Tag, ex.getLocalizedMessage(), ex);
    }

    public static boolean isLog()
    {
//        return (BuildConfig.DEBUG);
        return true;
    }

    /*
    *
    */
    public static void write(String type, String message) {
        write(type + ": " + /*ReflectiveUtils.getLocation() +*/ message);
    }

    public static void write(String message) {
        if (LogFile == null) {
            LogFile = defaultInitLogFile();
        }
        try {
            if (!LogFile.exists()) {
                LogFile.getParentFile().mkdirs();
                LogFile.createNewFile();
            }
            //
            int lineNum = cutIfMore(LogFile, LOG_FILE_MAX_SIZE, LOG_FILE_MAX_END_LINES);

            FileWriter f = new FileWriter(LogFile, true);
            if (lineNum > 0) {
                f.write(formatLogMessage("Log file was cut, because has been exceeded size of [" + LOG_FILE_MAX_SIZE +"] bytes. Saved last [" + lineNum + "] lines"));
            }
            f.write(formatLogMessage(message));
            f.flush();
            f.close();
        } catch (Exception ex) {
            Log.e(Tag, "Error with log-file writing:\n" + ex.getMessage(), ex);
        }
    }

    public static String formatLogMessage(String message) {
        return String.format("\n%s - %s", Utils.timeNow().format("%d.%m.%Y %H:%M:%S"), message);
    }

    public static int cutIfMore(File file, long maxBytes, int maxLines) throws IOException {
        long size = file.length();
        if (size > maxBytes) {
            return FileUtils.cutFileFromEnd(file, maxLines);
        }
        return 0;
    }

}
