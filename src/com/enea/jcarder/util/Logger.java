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
    public static enum Level {
        SEVERE,
        WARNING,
        INFO,
        FINE,
        FINER,
        FINEST;

        /**
         * Parse a string and return the corresponding log level.
         *
         * @param string The string to parse.
         * @return
         */
        public static Level fromString(String string) {
            for (Level level : values()) {
                if (string.equalsIgnoreCase(level.toString())) {
                    return level;
                }
            }
            return null;
        }
    };

    private static PrintWriter smWriter;

    private static volatile Level smMainFileLogLevel = Level.FINE;

    // The log level for a created logger object cannot be changed for
    // performance reasons.
    private final Level mFileLogLevel;

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
        handleFileLog(Level.SEVERE, "SEVERE", msg, t);
    }

    public void severe(String msg) {
        handleConsoleLog(msg);
        handleFileLog(Level.SEVERE, "SEVERE", msg);
    }

    public void warning(String msg) {
        handleConsoleLog(msg);
        handleFileLog(Level.WARNING, "WARNING", msg);
    }

    public void info(String msg) {
        handleConsoleLog(msg);
        handleFileLog(Level.INFO, "INFO", msg);
    }

    public void fine(String msg) {
        handleFileLog(Level.FINE, "FINE", msg);
    }

    public void finer(String msg) {
        handleFileLog(Level.FINER, "FINER", msg);
    }

    public void finest(String msg) {
        handleFileLog(Level.FINEST, "FINEST", msg);
    }

    public boolean isFinerEnabled() {
        return shouldHandleFileLog(Level.FINER);
    }

    public boolean isFinestEnabled() {
        return shouldHandleFileLog(Level.FINEST);
    }

    public static void setFileLogLevel(Level logLevel) {
        smMainFileLogLevel = logLevel;
    }

    private static void handleConsoleLog(String msg) {
        System.err.println(msg);
    }

    private void handleFileLog(Level logLevel, String prefix, String msg) {
        if (shouldHandleFileLog(logLevel)) {
            synchronized (smWriter) {
                smWriter.println(prefix + ": " + msg);
            }
        }
    }

    private void handleFileLog(Level logLevel,
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

    private boolean shouldHandleFileLog(Level logLevel) {
        return (mFileLogLevel.compareTo(logLevel) >= 0) && smWriter != null;
    }

    public static void flush() {
        System.err.flush();
        if (smWriter != null) {
            smWriter.flush();
        }
    }
}
