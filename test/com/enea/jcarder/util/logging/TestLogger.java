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

package com.enea.jcarder.util.logging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

public class TestLogger {
    @Test
    public void testConstruction() {
        new Logger(null);
        new Logger(null, Logger.Level.SEVERE);
    }

    @Test
    public void testSevere() {
        Handler mockHandler = mock(Handler.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(mockHandler);
        Logger logger = new Logger(handlers);

        logger.severe("foo");

        verify(mockHandler).publish(Logger.Level.SEVERE, "foo");
    }

    @Test
    public void testWarning() {
        Handler mockHandler = mock(Handler.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(mockHandler);
        Logger logger = new Logger(handlers);

        logger.warning("foo");

        verify(mockHandler).publish(Logger.Level.WARNING, "foo");
    }

    @Test
    public void testInfo() {
        Handler mockHandler = mock(Handler.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(mockHandler);
        Logger logger = new Logger(handlers);

        logger.info("foo");

        verify(mockHandler).publish(Logger.Level.INFO, "foo");
    }

    @Test
    public void testConfig() {
        Handler mockHandler = mock(Handler.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(mockHandler);
        Logger logger = new Logger(handlers);

        logger.config("foo");

        verify(mockHandler).publish(Logger.Level.CONFIG, "foo");
    }

    @Test
    public void testFine() {
        Handler mockHandler = mock(Handler.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(mockHandler);
        Logger logger = new Logger(handlers);

        logger.fine("foo");

        verify(mockHandler).publish(Logger.Level.FINE, "foo");
    }

    @Test
    public void testFiner() {
        Handler mockHandler = mock(Handler.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(mockHandler);
        Logger logger = new Logger(handlers);

        logger.finer("foo");

        verify(mockHandler).publish(Logger.Level.FINER, "foo");
    }

    @Test
    public void testFinest() {
        Handler mockHandler = mock(Handler.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(mockHandler);
        Logger logger = new Logger(handlers);

        logger.finest("foo");

        verify(mockHandler).publish(Logger.Level.FINEST, "foo");
    }

    @Test
    public void testIsLoggable() {
        Logger logger = new Logger(null, Logger.Level.WARNING);
        assertTrue(logger.isLoggable(Logger.Level.SEVERE));
        assertTrue(logger.isLoggable(Logger.Level.WARNING));
        assertFalse(logger.isLoggable(Logger.Level.INFO));
    }
}
