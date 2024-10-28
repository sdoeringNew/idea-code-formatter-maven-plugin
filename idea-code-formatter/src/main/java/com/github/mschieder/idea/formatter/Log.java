package com.github.mschieder.idea.formatter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Log {
    private static void tryInvokeLoggingMethod(final Class<?> loggingClazz, final String logLevel, String message)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Class<?> loggerFactory = Class.forName("org.slf4j.LoggerFactory");
        final Method getLoggerMethod = loggerFactory.getDeclaredMethod("getLogger", Class.class);
        final Object logger = getLoggerMethod.invoke(null, loggingClazz);
        final Method logMethod = logger.getClass().getDeclaredMethod(logLevel);
        logMethod.invoke(logger, message);
    }

    public static void info(final Class<?> loggingClazz, final String message) {
        try {
            tryInvokeLoggingMethod(loggingClazz, "info", message);
        } catch (final ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            System.out.println(message); // Seems like there is simply no logging class.
        }
    }

    public static void error(final Class<?> loggingClazz, final String message) {
        try {
            tryInvokeLoggingMethod(loggingClazz, "error", message);
        } catch (final ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            System.err.println(message); // Seems like there is simply no logging class.
        }
    }
}
