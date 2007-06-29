package com.enea.jcarder.util.logging;

import com.enea.jcarder.util.logging.Logger.Level;

/**
 * This interface must be implemented by classes that handles log messages from
 * the Logger class.
 */
public interface Handler {
    /**
     * Handle a published message.
     *
     * This method is called by a Logger class each time it receives a message
     * to be logged.
     *
     * @param level Log level of the message.
     * @param message The message.
     */
    void publish(Level level, String message);
}
