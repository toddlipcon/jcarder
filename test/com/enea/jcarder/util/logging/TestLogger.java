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

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        Handler mockHandler = createStrictMock(Handler.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(mockHandler);
        Logger logger = new Logger(handlers);

        mockHandler.publish(Logger.Level.SEVERE, "foo");
        replay(mockHandler);

        logger.severe("foo");
        verify(mockHandler);
    }

    @Test
    public void testWarning() {
        Handler mockHandler = createStrictMock(Handler.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(mockHandler);
        Logger logger = new Logger(handlers);

        mockHandler.publish(Logger.Level.WARNING, "foo");
        replay(mockHandler);

        logger.warning("foo");
        verify(mockHandler);
    }

    @Test
    public void testInfo() {
        Handler mockHandler = createStrictMock(Handler.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(mockHandler);
        Logger logger = new Logger(handlers);

        mockHandler.publish(Logger.Level.INFO, "foo");
        replay(mockHandler);

        logger.info("foo");
        verify(mockHandler);
    }

    @Test
    public void testConfig() {
        Handler mockHandler = createStrictMock(Handler.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(mockHandler);
        Logger logger = new Logger(handlers);

        mockHandler.publish(Logger.Level.CONFIG, "foo");
        replay(mockHandler);

        logger.config("foo");
        verify(mockHandler);
    }

    @Test
    public void testFine() {
        Handler mockHandler = createStrictMock(Handler.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(mockHandler);
        Logger logger = new Logger(handlers);

        mockHandler.publish(Logger.Level.FINE, "foo");
        replay(mockHandler);

        logger.fine("foo");
        verify(mockHandler);
    }

    @Test
    public void testFiner() {
        Handler mockHandler = createStrictMock(Handler.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(mockHandler);
        Logger logger = new Logger(handlers);

        mockHandler.publish(Logger.Level.FINER, "foo");
        replay(mockHandler);

        logger.finer("foo");
        verify(mockHandler);
    }

    @Test
    public void testFinest() {
        Handler mockHandler = createStrictMock(Handler.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(mockHandler);
        Logger logger = new Logger(handlers);

        mockHandler.publish(Logger.Level.FINEST, "foo");
        replay(mockHandler);

        logger.finest("foo");
        verify(mockHandler);
    }

    @Test
    public void testIsLoggable() {
        Logger logger = new Logger(null, Logger.Level.WARNING);
        assertTrue(logger.isLoggable(Logger.Level.SEVERE));
        assertTrue(logger.isLoggable(Logger.Level.WARNING));
        assertFalse(logger.isLoggable(Logger.Level.INFO));
    }
}
