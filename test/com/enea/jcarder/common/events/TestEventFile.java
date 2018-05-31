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

package com.enea.jcarder.common.events;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.enea.jcarder.util.logging.Logger;
import com.enea.jcarder.common.events.LockEventListenerIfc.LockEventType;

public final class TestEventFile {

    @Test
    public void writeReadTest() throws IOException {
        File file = File.createTempFile(TestEventFile.class.getName(),
                                        null);
        EventFileWriter writer = new EventFileWriter(new Logger(null), file);
        final int lockId = 5476;
        final int lockingContextId = 523;
        final int lastTakenLockId = 21;
        final int lastTakenLockingContextId = 541;
        final long threadId = 3121258129311216611L;
        final int nrOfLogEvents = 3;
        for (int i = 0; i < nrOfLogEvents; i++) {
            writer.onLockEvent(LockEventType.MONITOR_ENTER,
                               lockId,
                               lockingContextId,
                               threadId);
        }
        writer.close();

        LockEventListenerIfc listenerMock = mock(LockEventListenerIfc.class);
        new EventFileReader(new Logger(null)).parseFile(file, listenerMock);

        verify(listenerMock, times(nrOfLogEvents))
            .onLockEvent(LockEventType.MONITOR_ENTER,
                         lockId,
                         lockingContextId,
                         threadId);

        file.delete();
    }

    @Test
    public void interruptedWriterTest() throws Exception {
        File file = File.createTempFile(TestEventFile.class.getName(),
                                        null);
        EventFileWriter writer = new EventFileWriter(new Logger(null), file);
        final int lockId = 5476;
        final int lockingContextId = 523;
        final int lastTakenLockId = 21;
        final int lastTakenLockingContextId = 541;
        final long threadId = 3121258129311216611L;

        final int nrOfLogEvents = 3000;

        // Start another thread which will interrupt us a lot
        final Thread testThread = Thread.currentThread();
        Thread interrupter = new Thread() {
                public void run() {
                    while (!Thread.interrupted()) {
                        testThread.interrupt();
                    }
                }
            };
        interrupter.start();

        // Try to write a bunch of events while we're being interrupted
        for (int i = 0; i < nrOfLogEvents; i++) {
            writer.onLockEvent(LockEventType.MONITOR_ENTER,
                               lockId,
                               lockingContextId,
                               threadId);
        }

        // Stop the thread that's interrupting us and wait for it to stop
        interrupter.interrupt();

        while (interrupter.isAlive()) {
            try {
                interrupter.join();
            } catch (InterruptedException ie) {
                // We expect this
            }
        }
        testThread.interrupted(); // clear our interrupt status!

        writer.close();

        // Verify we got all our events
        LockEventListenerIfc listenerMock = mock(LockEventListenerIfc.class);
        new EventFileReader(new Logger(null)).parseFile(file, listenerMock);

        verify(listenerMock, times(nrOfLogEvents))
            .onLockEvent(LockEventType.MONITOR_ENTER,
                         lockId,
                         lockingContextId,
                         threadId);

        file.delete();
    }
}
