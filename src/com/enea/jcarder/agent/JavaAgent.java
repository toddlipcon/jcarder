/*
 * JCarder -- cards Java programs to keep threads disentangled
 *
 * Copyright (C) 2006-2007 Enea AB
 * Copyright (C) 2007 Ulrik Svensson
 * Copyright (C) 2007 Joel Rosdahl
 *
 * This program is made available under the GNU GPL version 2, with a special
 * exception for linking with JUnit. See the accompanying file LICENSE.txt for
 * details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.enea.jcarder.agent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

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
    private static final String OUTPUTDIR_PROPERTY = "jcarder.outputdir";

    private final InstrumentConfig mConfig = new InstrumentConfig();
    private Logger mLogger;
    PrintWriter mLogWriter;
    private File mOutputDir;
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
        javaAgent.init(args, instrumentation);
    }

    private void init(String args, Instrumentation instrumentation)
    throws Exception {
        handleProperties(args);
        initLogger();
        mLogger.info("Starting " + BuildInformation.getShortInfo() + " agent");
        logJvmInfo();
        EventListener listener = EventListener.create(mLogger, mOutputDir);
        ClassTransformer classTransformer =
            new ClassTransformer(mLogger, mOutputDir, mConfig);
        instrumentation.addTransformer(classTransformer);
        StaticEventListener.setListener(listener);
        mLogger.info("JCarder agent initialized\n");
    }

    private void initLogger() {
        File logFile = new File(mOutputDir, LOG_FILENAME);
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
            new AppendableHandler(System.err,
                                  Logger.Level.INFO,
                                  "{message}\n");

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

    private void logJvmInfo() {
        Enumeration<?> properties = System.getProperties().propertyNames();
        while (properties.hasMoreElements()) {
            String key = (String) properties.nextElement();
            if (key.startsWith("java.vm.")) {
                mLogger.config(key + ": " + System.getProperty(key));
            }
        }
    }

    private void handleProperties(String args) throws IOException {
        if (args != null) {
            String[] argpairs = args.split(",");
            for (String pair : argpairs) {
                String[] keyval = pair.split("=", 2);
                if (keyval.length != 2) {
                    System.err.println("Couldn't parse keyval pair: " + pair);
                    continue;
                }

                String key = keyval[0];
                String val = keyval[1];
                System.err.println("Setting " + key + " to " + val);
                System.setProperty("jcarder." + key, val);
            }
        }
        handleDumpProperty();
        handleLogLevelProperty();
        handleOutputDirProperty();
    }

    private void handleDumpProperty() {
        mConfig.setDumpClassFiles(Boolean.getBoolean(DUMP_PROPERTY));
    }

    private void handleLogLevelProperty() {
        String logLevelValue = System.getProperty(LOGLEVEL_PROPERTY, "fine");
        Logger.Level logLevel = Logger.Level.fromString(logLevelValue);
        if (logLevel != null) {
            mLogLevel = logLevel;
        } else {
            System.err.print("Bad loglevel; should be one of ");
            System.err.println(Logger.Level.getEnumeration());
            System.err.println();
            System.exit(1);
        }
    }

    private void handleOutputDirProperty() throws IOException {
        String property = System.getProperty(OUTPUTDIR_PROPERTY, ".");
        property = property.replace(
            "@TIME@", String.valueOf(System.currentTimeMillis()));
        mOutputDir = new File(property).getCanonicalFile();
        if (!mOutputDir.isDirectory()) {
            mOutputDir.mkdirs();
        }
    }
}
