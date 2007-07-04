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

package com.enea.jcarder.common.events;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import net.jcip.annotations.ThreadSafe;

import com.enea.jcarder.util.Counter;
import com.enea.jcarder.util.logging.Logger;

import static com.enea.jcarder.common.events.EventFileReader.EVENT_LENGTH;

@ThreadSafe
public final class EventFileWriter implements LockEventListenerIfc {
    private final ByteBuffer mBuffer =
        ByteBuffer.allocateDirect(EVENT_LENGTH * 1024);
    private final FileChannel mFileChannel;
    private final Logger mLogger;
    private final Counter mWrittenLockEvents;
    private boolean mShutdownHookExecuted = false;

    public EventFileWriter(Logger logger, File file) throws IOException {
        mLogger = logger;
        mLogger.info("Opening for writing: " + file.getAbsolutePath());
        RandomAccessFile raFile = new RandomAccessFile(file, "rw");
        raFile.setLength(0);
        mFileChannel = raFile.getChannel();
        mWrittenLockEvents = new Counter("Written Lock Events",
                                         mLogger,
                                         100000);
        writeHeader();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { shutdownHook(); }
        });
    }

    private void writeHeader() throws IOException {
        ByteBuffer header = ByteBuffer.allocate(8 + 4 + 4);
        header.putLong(EventFileReader.MAGIC_COOKIE);
        header.putInt(EventFileReader.MAJOR_VERSION);
        header.putInt(EventFileReader.MINOR_VERSION);
        header.flip();
        mFileChannel.write(header);
    }

    public synchronized void onLockEvent(int lockId,
                                         int lockingContextId,
                                         int lastTakenLockId,
                                         int lastTakenLockingContextId,
                                         long threadId) throws IOException {
        mBuffer.putInt(lockId);
        mBuffer.putInt(lockingContextId);
        mBuffer.putInt(lastTakenLockId);
        mBuffer.putInt(lastTakenLockingContextId);
        mBuffer.putLong(threadId);
        mWrittenLockEvents.increment();
        if (mBuffer.remaining() < EVENT_LENGTH || mShutdownHookExecuted) {
            writeBuffer();
        }
    }

    private void writeBuffer() throws IOException {
        mBuffer.flip();
        mFileChannel.write(mBuffer);
        mBuffer.clear();
    }

    public synchronized void close() throws IOException {
        writeBuffer();
        mFileChannel.close();
    }

    private synchronized void shutdownHook() {
        try {
            if (mFileChannel.isOpen()) {
                writeBuffer();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mShutdownHookExecuted = true;
    }
}
