package com.enea.jcarder.common.events;

import java.io.File;
import java.io.IOException;
import org.junit.Test;

import com.enea.jcarder.common.events.EventFileReader;
import com.enea.jcarder.common.events.EventFileWriter;
import com.enea.jcarder.common.events.LockEventListenerIfc;
import com.enea.jcarder.util.logging.Logger;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
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
            writer.onLockEvent(lockId,
                               lockingContextId,
                               lastTakenLockId,
                               lastTakenLockingContextId,
                               threadId);
        }
        writer.close();
        LockEventListenerIfc listenerMock =
            createStrictMock(LockEventListenerIfc.class);
        for (int i = 0; i < nrOfLogEvents; i++) {
            listenerMock.onLockEvent(lockId,
                                     lockingContextId,
                                     lastTakenLockId,
                                     lastTakenLockingContextId,
                                     threadId);
        }
        replay(listenerMock);
        new EventFileReader(new Logger(null)).parseFile(file, listenerMock);
        verify(listenerMock);
        file.delete();
    }
}
