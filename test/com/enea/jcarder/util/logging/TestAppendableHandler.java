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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

public class TestAppendableHandler {
    @Test
    public void testConstruction() {
        new AppendableHandler(null);
        new AppendableHandler(null, Logger.Level.SEVERE);
        new AppendableHandler(null, Logger.Level.SEVERE, "");
    }

    @Test
    public void testSimplePublish() throws IOException {
        Appendable streamMock = mock(Appendable.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(new AppendableHandler(streamMock));
        Logger logger = new Logger(handlers);

        logger.severe("foo");

        verify(streamMock).append("SEVERE: foo\n");
    }

    @Test
    public void testLogLevel() throws IOException {
        Appendable streamMock = mock(Appendable.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(new AppendableHandler(streamMock, Logger.Level.SEVERE));
        Logger logger = new Logger(handlers);

        logger.severe("foo");
        logger.warning("bar");

        verify(streamMock).append("SEVERE: foo\n");
        verifyNoMoreInteractions(streamMock);
    }

    @Test
    public void testMessageFormat() throws IOException {
        Appendable streamMock = mock(Appendable.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        AppendableHandler handler1 =
            new AppendableHandler(streamMock,
                                  Logger.Level.WARNING,
                                  "[{level}] --> {{message}}.");
        AppendableHandler handler2 =
            new AppendableHandler(streamMock,
                                  Logger.Level.WARNING,
                                  "{message} {message");
        handlers.add(handler1);
        handlers.add(handler2);
        Logger logger = new Logger(handlers);

        logger.warning("foo");

        verify(streamMock).append("[WARNING] --> {foo}.");
        verify(streamMock).append("foo {message");
    }
}
