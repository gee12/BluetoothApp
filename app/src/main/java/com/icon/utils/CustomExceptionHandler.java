package com.icon.utils;

import com.icon.agnks.Logger;

/**
 * Created by Ivan on 09.10.2015.
 *
 * Обработчик необработанных исключений времени исполнения
 */
public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
    Thread.UncaughtExceptionHandler oldHandler;

    public CustomExceptionHandler() {
        oldHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Logger.add(thread, throwable);
        if (oldHandler != null)
            oldHandler.uncaughtException(thread, throwable);
    }
}
