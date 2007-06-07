package com.enea.jcarder.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

import com.enea.jcarder.agent.instrument.ClassTransformer;
import com.enea.jcarder.agent.instrument.InstrumentConfig;
import com.enea.jcarder.util.BuildInformation;
import com.enea.jcarder.util.Logger;

/**
 * This is the main class of the jcarder java agent. It will initialize jcarder
 * and register a ClassTransformer that is called by the JVM each time a class
 * is loaded.
 */
public final class JavaAgent {

    private static InstrumentConfig smInstrumentConfig = new InstrumentConfig();
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
        javaAgent.start(args, instrumentation);
    }

    private void init(final Instrumentation instrumentation)
    throws IOException {
        EventListenerIfc listener = EventListener.create();
        ClassTransformer classTransformer = new ClassTransformer(smInstrumentConfig);
        instrumentation.addTransformer(classTransformer);
        mLogger.info("Dead Lock Agent initialized\n");
        StaticEventListener.setDeadLockActionListener(listener);
    }

    private void start(final String args,
                       final Instrumentation instrumentation)
    throws Exception {
        mLogger.info("Starting " + BuildInformation.getShortInfo() + ".");
        handleArguments(args);
        init(instrumentation);
    }

    private static void handleArguments(final String allArgs) {
        if (allArgs != null) {
            for (String arg : allArgs.split("=")) {
                if (arg.equals("finer")) {
                    Logger.setFileLogLevel(Logger.FINER);
                } else if (arg.equals("finest")) {
                    Logger.setFileLogLevel(Logger.FINEST);
                } else if (arg.equals("dump")) {
                    smInstrumentConfig.setDumpClassFiles(true);
                } else {
                    System.err.println("Invalid jcarder parameter: " + allArgs);
                    System.err.println("Valid arguments are:"
                                       + "\n  =dump (Dump transformed classes to file)"
                                       + "\n  =finer or =finest (file log threshold)");
                    System.exit(-1);
                }
            }
        }
    }
}
