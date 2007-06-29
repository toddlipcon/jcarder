package com.enea.jcarder.util.logging;

import java.io.IOException;

import com.enea.jcarder.util.logging.Logger.Level;

/**
 * Log handler that publishes log messages via an Appendable.
 *
 * The Appendable does not need to be thread-safe; AppendableHandler
 * synchronizes calls to Appendable's methods.
 */
public class AppendableHandler implements Handler {
    private final Appendable mDestination;
    private final Level mLevel;

    /**
     * Constructor setting default value of log level.
     *
     * The default is to log everything, i.e., log level FINEST.
     *
     * @param destination Destination of the log messages.
     */
    public AppendableHandler(Appendable destination) {
        this(destination, Logger.Level.FINEST);
    }

    /**
     * Constructor setting default value of log level.
     *
     * @param destination Destination of the log messages.
     * @param logLevel Log level.
     */
    public AppendableHandler(Appendable destination, Logger.Level logLevel) {
        mDestination = destination;
        mLevel = logLevel;
    }

    public void publish(Level level, String message) {
        if (level.compareTo(mLevel) <= 0) {
            try {
                synchronized (mDestination) {
                    String formattedMessage =
                        level.toString() + ": " + message + "\n";
                    mDestination.append(formattedMessage);
                }
            } catch (IOException e) {
                // Ignore.
            }
        }
    }
}
