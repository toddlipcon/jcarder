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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.enea.jcarder.util.logging.Logger;

public class TestLoggerLevel {
    @Test
    public final void testFromString() {
        assertEquals(null, Logger.Level.fromString("nonexistent level"));
        assertEquals(Logger.Level.SEVERE, Logger.Level.fromString("severe"));
        assertEquals(Logger.Level.WARNING, Logger.Level.fromString("warning"));
        assertEquals(Logger.Level.INFO, Logger.Level.fromString("info"));
        assertEquals(Logger.Level.CONFIG, Logger.Level.fromString("config"));
        assertEquals(Logger.Level.FINE, Logger.Level.fromString("fine"));
        assertEquals(Logger.Level.FINER, Logger.Level.fromString("finer"));
        assertEquals(Logger.Level.FINEST, Logger.Level.fromString("finest"));
    }
}
