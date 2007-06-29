package com.enea.jcarder.agent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Collection;

import com.enea.jcarder.agent.instrument.ClassTransformer;
import com.enea.jcarder.agent.instrument.InstrumentConfig;
import com.enea.jcarder.util.BuildInformation;
import com.enea.jcarder.util.logging.AppendableHandler;
import com.enea.jcarder.util.logging.Handler;
import com.enea.jcarder.util.logging.Logger;

/**
 * This is the main class of the JCarder Java agent. It will initialize JCarder
 * and register a ClassTransformer that is called by the JVM each time a class
 * is loaded.
 */
public final class JavaAgent {

    private static final String DUMP_PROPERTY = "jcarder.dump";
    private static final String LOGLEVEL_PROPERTY = "jcarder.loglevel";
    private static final String LOG_FILENAME = "jcarder.log";

    private final static InstrumentConfig smConfig = new InstrumentConfig();
    private Logger mLogger;
    PrintWriter mLogWriter;
    private Logger.Level mLogLevel;

    private JavaAgent() { }

    /**
     * This method is called by the JVM when the JVM is started with the
     * -javaagent command line parameter.
     */
    public static void premain(final String args,
                               final Instrumentation instrumentation)
    throws Exception {
        JavaAgent javaAgent = new JavaAgent();
        javaAgent.init(instrumentation);
    }

    private void init(Instrumentation instrumentation)
    throws Exception {
        handleProperties();
        initLogger();
        mLogger.info("Starting " + BuildInformation.getShortInfo() + ".");
        EventListener listener = EventListener.create(mLogger);
        ClassTransformer classTransformer =
            new ClassTransformer(mLogger, smConfig);
        instrumentation.addTransformer(classTransformer);
        mLogger.info("Dead Lock Agent initialized\n");
        StaticEventListener.setListener(listener);
    }

    private void initLogger() {
        File logFile = new File(LOG_FILENAME);
        if (logFile.exists()) {
            logFile.delete();
        }
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(logFile);
        } catch (IOException e) {
            System.err.println("Failed to open log file \""
                               + logFile + "\": " + e.getMessage());
            return;
        }
        mLogWriter = new PrintWriter(new BufferedWriter(fileWriter));
        AppendableHandler fileHandler = new AppendableHandler(mLogWriter);
        AppendableHandler consoleHandler =
            new AppendableHandler(System.err, Logger.Level.INFO);

        Thread hook = new Thread() {
            public void run() {
                mLogWriter.flush();
            }
        };
        Runtime.getRuntime().addShutdownHook(hook);

        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(fileHandler);
        handlers.add(consoleHandler);
        mLogger = new Logger(handlers, mLogLevel);
    }

    private void handleProperties() {
        // jcarder.loglevel
        String logLevelValue = System.getProperty(LOGLEVEL_PROPERTY, "fine");
        Logger.Level logLevel = Logger.Level.fromString(logLevelValue);
        if (logLevel != null) {
            mLogLevel = logLevel;
        } else {
            System.err.print("Bad loglevel; should be one of ");
            boolean first = true;
            for (Logger.Level level : Logger.Level.values()) {
                if (first) {
                    first = false;
                } else {
                    System.err.print(", ");
                }
                System.err.print(level.toString());
            }
            System.err.println();
            System.exit(1);
        }

        // jcarder.dump
        smConfig.setDumpClassFiles(Boolean.getBoolean(DUMP_PROPERTY));
    }
}
