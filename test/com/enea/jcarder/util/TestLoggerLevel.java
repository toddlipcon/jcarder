package com.enea.jcarder.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestLoggerLevel {

    @Test
    public final void testFromString() {
        assertEquals(null, Logger.Level.fromString("nonexistent level"));
        assertEquals(Logger.Level.SEVERE, Logger.Level.fromString("severe"));
        assertEquals(Logger.Level.WARNING, Logger.Level.fromString("warning"));
        assertEquals(Logger.Level.INFO, Logger.Level.fromString("info"));
        assertEquals(Logger.Level.FINE, Logger.Level.fromString("fine"));
        assertEquals(Logger.Level.FINER, Logger.Level.fromString("finer"));
        assertEquals(Logger.Level.FINEST, Logger.Level.fromString("finest"));
    }

}
