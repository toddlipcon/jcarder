package com.enea.jcarder.util.logging;

import java.io.IOException;

import com.enea.jcarder.util.logging.Logger.Level;

/**
 * This class acts as a log handler for classes that that implement the
 * Appendable interface.
 *
 * The Appendable does not need to be thread-safe; AppendableHandler
 * synchronizes calls to Appendable's methods.
 */
public class AppendableHandler implements Handler {
    private final Appendable mDestination;
    private final Level mLevel;
    private final String mMessageFormat;

    /**
     * Constructor.
     *
     * The default log level is FINEST and the default message format is
     * "{level}: {message}\n"
     *
     * @param destination
     *            Destination of the log messages.
     */
    public AppendableHandler(Appendable destination) {
        this(destination, Logger.Level.FINEST);
    }

    /**
     * Constructor.
     *
     * The default message format is "{level}: {message}\n"
     *
     * @param destination
     *            Destination of the log messages.
     * @param logLevel
     *            Log level.
     */
    public AppendableHandler(Appendable destination, Logger.Level logLevel) {
        this(destination, logLevel, "{level}: {message}\n");
    }

    /**
     * Constructor.
     *
     * Substrings like {keyword} are expanded in the message format string.
     *
     * Currently supported keywords:
     *
     * - {message} -- the message
     * - {level} -- the log level
     *
     * @param destination Destination of the log messages.
     * @param logLevel Log level.
     * @param messageFormat Message format.
     */
    public AppendableHandler(Appendable destination,
                             Logger.Level logLevel,
                             String messageFormat) {
        mDestination = destination;
        mLevel = logLevel;
        mMessageFormat = messageFormat;
    }


    public void publish(Level level, String message) {
        if (level.compareTo(mLevel) <= 0) {
            try {
                String formattedMessage = mMessageFormat
                    .replace("{level}", level.toString())
                    .replace("{message}", message);
                synchronized (mDestination) {
                    mDestination.append(formattedMessage);
                }
            } catch (IOException e) {
                // Ignore.
            }
        }
    }
}
