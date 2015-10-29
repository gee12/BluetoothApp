package com.icon.agnks;

import android.os.Environment;
import android.util.Log;

import com.icon.utils.FileUtils;
import com.icon.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Ivan on 30.09.2015.
 */
public class Logger {
    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;
    public static final int ASSERT = Log.ASSERT;
    public static final int UNCAUGHT = Log.ASSERT + 1;

    public static final String DEF_TAG = "agnks";
    public static final String DEF_LOG_DIR = "/";
    public static final String DEF_LOG_FILE_NAME = DEF_TAG + ".log";
    public static final int DEF_LOG_FILE_MAX_SIZE_KBYTES = 32;
    public static final int DEF_LOG_FILE_MAX_END_LINES = 20;

    public static String Tag = DEF_TAG;
    public static String LogDir = DEF_LOG_DIR;
    public static String CacheDir = DEF_LOG_DIR;
    public static String LogFileName = DEF_LOG_FILE_NAME;
    public static File LogFile = null;
    public static boolean isInit;
    public static int Level = -1;

    public static boolean IsNeedLog = true;
    public static int LogFileMaxSize = DEF_LOG_FILE_MAX_SIZE_KBYTES;
    public static int LogFileMaxEndLines = DEF_LOG_FILE_MAX_END_LINES;

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

    /**
     * Initialized
     * @param logDir
     * @param cacheDir
     * @param logFileName
     * @return
     */
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

    public static boolean isNeedLog() {
//        String key_is_need_log = Global.globalContext.getString(R.string.pref_key_is_need_log);
//        String value = SettingsActivity.getPref(key_is_need_log);
//        return Boolean.parseBoolean(value);

//        return (BuildConfig.DEBUG);
        return IsNeedLog;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Запись пользовательского и системного лога (если требуется)
     * @param message
     * @param isNeedSystemLog
     */
    public static void add(String message, int type, boolean isNeedSystemLog) {
        if (isNeedLog() && isLevelSuit(type)) {
            String strType = systemlog(message, type, isNeedSystemLog);
            write(strType, message);
        }
    }

    /**
     * Запись пользовательского и системного лога (если требуется) об ошибке
     * @param ex
     * @param isNeedSystemLog
     */
    public static void add(Exception ex, boolean isNeedSystemLog) {
        if (isNeedLog() && isLevelSuit(ERROR)) {
            String message = ex.getMessage();
            String strType = systemlog(message, ex, isNeedSystemLog);
            write(strType, message);
        }
    }

    /**
     * Запись пользовательского и системного лога (если требуется) об ошибке с добавлением сообщения
     * @param message
     * @param ex
     * @param isNeedSystemLog
     */
    public static void add(String message, Exception ex, boolean isNeedSystemLog) {
        if (isNeedLog() && isLevelSuit(ERROR)) {
            String allMessage = message + "\n" + ex.getMessage();
            String strType = systemlog(allMessage, ex, isNeedSystemLog);
            write(strType, allMessage);
        }
    }

    /**
     * Запись пользовательского и системного лога
     * @param message
     * @param type
     */
    public static void add(String message, int type) {
        if (isNeedLog() && isLevelSuit(type)) {
            String strType = systemlog(message, type, true);
            write(strType, message);
        }
    }

    /**
     * Запись пользовательского и системного лога об ошибке
     * @param ex
     */
    public static void add(Exception ex) {
        if (isNeedLog() && isLevelSuit(ERROR)) {
            String message = ex.getMessage();
            String strType = systemlog(message, ex, true);
            write(strType, message);
        }
    }

    /**
     * Запись пользовательского и системного лога об ошибке с добавлением сообщения
     * @param message
     * @param ex
     */
    public static void add(String message, Exception ex) {
        if (isNeedLog() && isLevelSuit(ERROR)) {
            String allMessage = message + "\n" + ex.getMessage();
            String strType = systemlog(allMessage, ex, true);
            write(strType, allMessage);
        }
    }

    public static void add(Thread thread, Throwable throwable) {
        if (isNeedLog() && isLevelSuit(UNCAUGHT)) {
            String allMessage = thread.getName() + ": " + throwable.getMessage();
            Log.e(Tag, allMessage, throwable);
            write("UNCAUGHT", allMessage);
        }
    }

    /**
     * Get log type String and write system log if need
     * @param message
     * @param type
     * @param isNeedWriteLog
     * @return
     */
    public static String systemlog(String message, int type, boolean isNeedWriteLog) {
        switch(type) {
            case DEBUG:
                if (isNeedWriteLog) Log.d(Tag, message);
                return "DEBUG";
            case VERBOSE:
                if (isNeedWriteLog) Log.v(Tag, message);
                return "VERBOSE";
            case INFO:
                if (isNeedWriteLog) Log.i(Tag, message);
                return "INFO";
            case WARN:
                if (isNeedWriteLog) Log.w(Tag, message);
                return "WARN";
            case ERROR:
                if (isNeedWriteLog) Log.e(Tag, message);
                return "ERROR";
            case UNCAUGHT:
                if (isNeedWriteLog) Log.e(Tag, message);
                return "UNCAUGHT";
        }
        return "NONE";
    }

    /**
     * Получение строкового наименования типа лога, и запись системного лога (если требуется)
     * @param message
     * @param ex
     * @param isNeedWriteLog
     * @return
     */
    public static String systemlog(String message, Exception ex, boolean isNeedWriteLog) {
        if (isNeedWriteLog) Log.e(Tag, message, ex);
        return systemlog(message, ERROR, false);
    }

    public static void systemlog(String message, Exception ex) {
        Log.e(Tag, message, ex);
    }

    public static void systemlog(Exception ex) {
        Log.e(Tag, ex.getLocalizedMessage(), ex);
    }

    /**
     * Запись пользовательского лог-файла
     * @param type
     * @param message
     */
    public static void write(String type, String message) {
        write(type + ": " + /*ReflectiveUtils.getLocation() +*/ message);
    }

    /**
     * Запись пользовательского лог-файла
     * @param message
     */
    public static void write(String message) {
        if (LogFile == null) {
            LogFile = defaultInitLogFile();
        }
        try {
            if (!LogFile.exists()) {
                LogFile.getParentFile().mkdirs();
                LogFile.createNewFile();
            }
            // обрезаем лог-файл, если размер в кБ > LogFileMaxSize
            int lineNum = cutIfMore(LogFile, LogFileMaxSize*1024, LogFileMaxEndLines);

            FileWriter f = new FileWriter(LogFile, true);
            if (lineNum > 0) {
                f.write(formatLogMessage("Лог-файл был обрезан, т.к. его размер превысил [" + LogFileMaxSize +"] КБайт. Сохранены последние [" + lineNum + "] строки"));
            }
            f.write(formatLogMessage(message));
            f.flush();
            f.close();
        } catch (Exception ex) {
            Log.e(Tag, "Ошибка при записи лог-файла:\n" + ex.getMessage(), ex);
        }
    }

    public static String formatLogMessage(String message) {
        return String.format("%s - %s\n", Utils.timeNow().format("%d.%m.%Y %H:%M:%S"), message);
    }

    public static int cutIfMore(File file, long maxBytes, int maxLines) throws IOException {
        long size = file.length();
        if (size > maxBytes) {
            return FileUtils.cutFileFromEnd(file, maxLines);
        }
        return 0;
    }

    /**
     * Чтение пользовательского лог-файла
     * @return
     */
    public static String readLogs() {
        if (!LogFile.exists()) {
            return "Лог-файл отсутствует..";
        }
        FileInputStream fis = null;
        StringBuffer sb = null;
        try {
            fis = new FileInputStream(LogFile);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            sb = new StringBuffer();
            String line;
            while((line = in.readLine()) != null){
                sb.append(line);
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception ex) {
            Log.e(Tag, "Ошибка при чтении лог-файла:\n" + ex.getMessage(), ex);
        }
        return "";
    }

    /**
     * Очистка пользовательского лог-файла
     */
    public static void clearLogs() {
        if (!LogFile.exists()) {
            return;
        }
        FileWriter f = null;
        try {
            f = new FileWriter(LogFile, false);
            f.write("");
            f.flush();
            f.close();
        } catch (IOException ex) {
            Log.e(Tag, "Ошибка при очистке лог-файла:\n" + ex.getMessage(), ex);
        }
    }
}
