package com.enea.jcarder.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * We use our own simple Logging framework for several reasons:
 *  - To avoid interfering with logging from the user application. Even if we
 * were using Log4J instead of java.util.Logging.*, JCarder might interfere with
 * for example Log4J system properties.
 *  - The default java.util.logging.LogManager is reset by a shutdown hook and
 * there is no fixed order in which the shutdown hooks are executed.
 *  - Minimizing the usage of the Java standard library improves performance and
 * minimizes the risk of deadlock if the standard library is instrumented by
 * JCarder.
 */
public final class Logger {
    public static final int SEVERE = 1;
    public static final int WARNING = 2;
    public static final int INFO = 3;
    public static final int FINE = 4;
    public static final int FINER = 5;
    public static final int FINEST = 6;

    private static PrintWriter smWriter;

    private static volatile int smMainFileLogLevel = FINE;

    // The log level for a created logger object cannot be changed for
    // performance reasons.
    private final int mFileLogLevel;

    static {
        Thread hook = new Thread() {
            public void run() {
                Logger.flush();
            }
        };
        Runtime.getRuntime().addShutdownHook(hook);
        File logFile = new File("jcarder.log");
        try {
            if (logFile.exists()) {
                logFile.delete();
            }
            FileWriter fileWriter = new FileWriter(logFile);
            smWriter = new PrintWriter(new BufferedWriter(fileWriter));
        } catch (Exception e) {
            handleConsoleLog("Failed to open log file: \""
                             + logFile + "\". " + e.getMessage());
        }
    }

    private Logger(String name) {
        mFileLogLevel = smMainFileLogLevel;
    }

    public static Logger getLogger(Class c) {
        return getLogger(c.getName());
    }

    public static Logger getLogger(Object o) {
        return getLogger(o.getClass().getName()
                         + "@" + System.identityHashCode(o));
    }

    private static Logger getLogger(String s) {
        return new Logger(s);
    }

    public void severe(String msg, Throwable t) {
        handleConsoleLog(msg);
        handleFileLog(SEVERE, "SEVERE", msg, t);
    }

    public void severe(String msg) {
        handleConsoleLog(msg);
        handleFileLog(SEVERE, "SEVERE", msg);
    }

    public void warning(String msg) {
        handleConsoleLog(msg);
        handleFileLog(WARNING, "WARNING", msg);
    }

    public void info(String msg) {
        handleConsoleLog(msg);
        handleFileLog(INFO, "INFO", msg);
    }

    public void fine(String msg) {
        handleFileLog(FINE, "FINE", msg);
    }

    public void finer(String msg) {
        handleFileLog(FINER, "FINER", msg);
    }

    public void finest(String msg) {
        handleFileLog(FINEST, "FINEST", msg);
    }

    public boolean isFinerEnabled() {
        return shouldHandleFileLog(FINER);
    }

    public boolean isFinestEnabled() {
        return shouldHandleFileLog(FINEST);
    }

    public static void setFileLogLevel(int logLevel) {
        smMainFileLogLevel = logLevel;
    }

    private static void handleConsoleLog(String msg) {
        System.err.println(msg);
    }

    private void handleFileLog(int logLevel, String prefix, String msg) {
        if (shouldHandleFileLog(logLevel)) {
            synchronized (smWriter) {
                smWriter.println(prefix + ": " + msg);
            }
        }
    }

    private void handleFileLog(int logLevel,
                               String prefix,
                               String msg,
                               Throwable t) {
        if (shouldHandleFileLog(logLevel)) {
            synchronized (smWriter) {
                smWriter.println(prefix + ": " + msg);
                t.printStackTrace(smWriter);
            }
        }
    }

    private boolean shouldHandleFileLog(int logLevel) {
        return (mFileLogLevel >= logLevel) && smWriter != null;
    }

    public static void flush() {
        System.err.flush();
        if (smWriter != null) {
            smWriter.flush();
        }
    }
}
