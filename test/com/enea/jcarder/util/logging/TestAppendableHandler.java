package com.enea.jcarder.util.logging;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

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
        Appendable streamMock = createStrictMock(Appendable.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(new AppendableHandler(streamMock));
        Logger logger = new Logger(handlers);

        expect(streamMock.append("SEVERE: foo\n")).andReturn(streamMock);
        replay(streamMock);

        logger.severe("foo");
        verify(streamMock);
    }

    @Test
    public void testLogLevel() throws IOException {
        Appendable streamMock = createStrictMock(Appendable.class);
        Collection<Handler> handlers = new ArrayList<Handler>();
        handlers.add(new AppendableHandler(streamMock, Logger.Level.SEVERE));
        Logger logger = new Logger(handlers);

        expect(streamMock.append("SEVERE: foo\n")).andReturn(streamMock);
        replay(streamMock);

        logger.severe("foo");
        logger.warning("bar");
        verify(streamMock);
    }

    @Test
    public void testMessageFormat() throws IOException {
        Appendable streamMock = createStrictMock(Appendable.class);
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

        expect(streamMock.append("[WARNING] --> {foo}.")).andReturn(streamMock);
        expect(streamMock.append("foo {message")).andReturn(streamMock);
        replay(streamMock);

        logger.warning("foo");
    }
}
