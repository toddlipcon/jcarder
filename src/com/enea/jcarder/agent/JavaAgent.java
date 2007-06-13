package com.enea.jcarder.agent;

import java.lang.instrument.Instrumentation;

import com.enea.jcarder.agent.instrument.ClassTransformer;
import com.enea.jcarder.agent.instrument.InstrumentConfig;
import com.enea.jcarder.util.BuildInformation;
import com.enea.jcarder.util.Logger;

/**
 * This is the main class of the JCarder Java agent. It will initialize JCarder
 * and register a ClassTransformer that is called by the JVM each time a class
 * is loaded.
 */
public final class JavaAgent {

    private static final String DUMP_PROPERTY = "jcarder.dump";
    private static final String LOGLEVEL_PROPERTY = "jcarder.loglevel";

    private final static InstrumentConfig smConfig = new InstrumentConfig();
    private final Logger mLogger = Logger.getLogger(this);

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
        mLogger.info("Starting " + BuildInformation.getShortInfo() + ".");
        handleProperties();
        EventListener listener = EventListener.create();
        ClassTransformer classTransformer = new ClassTransformer(smConfig);
        instrumentation.addTransformer(classTransformer);
        mLogger.info("Dead Lock Agent initialized\n");
        StaticEventListener.setListener(listener);
    }

    private static void handleProperties() {
        // jcarder.loglevel
        String logLevelValue = System.getProperty(LOGLEVEL_PROPERTY, "fine");
        Logger.Level logLevel = Logger.Level.fromString(logLevelValue);
        if (logLevel != null) {
            Logger.setFileLogLevel(logLevel);
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
