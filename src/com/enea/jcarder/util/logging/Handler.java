/*
 * JCarder -- cards Java programs to keep threads disentangled
 *
 * Copyright (C) 2006-2007 Enea AB
 * Copyright (C) 2007 Ulrik Svensson
 * Copyright (C) 2007 Joel Rosdahl
 *
 * This program is made available under the GNU GPL version 2. See the
 * accompanying file LICENSE.txt for details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 */

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
