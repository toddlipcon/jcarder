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

package com.enea.jcarder.common.contexts;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import net.jcip.annotations.ThreadSafe;

import com.enea.jcarder.common.Lock;
import com.enea.jcarder.common.LockingContext;
import com.enea.jcarder.util.logging.Logger;

@ThreadSafe
public final class ContextFileWriter
implements ContextWriterIfc {
    private final FileChannel mChannel;
    private int mNextFilePosition = 0;
    private final Logger mLogger;
    private ByteBuffer mBuffer = ByteBuffer.allocateDirect(8192);
    private boolean mShutdownHookExecuted = false;

    public ContextFileWriter(Logger logger, File file) throws IOException {
        mLogger = logger;
        mLogger.info("Opening for writing: " + file.getAbsolutePath());
        RandomAccessFile raFile = new RandomAccessFile(file, "rw");
        raFile.setLength(0);
        mChannel = raFile.getChannel();
        writeHeader();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { shutdownHook(); }
        });
    }

    private synchronized void shutdownHook() {
        try {
            if (mChannel.isOpen()) {
                writeBuffer();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mShutdownHookExecuted = true;
    }

    private void writeBuffer() throws IOException {
        mBuffer.flip();
        mChannel.write(mBuffer);
        while (mBuffer.hasRemaining()) {
            Thread.yield();
            mChannel.write(mBuffer);
        }
        mBuffer.clear();
    }

    private void writeHeader() throws IOException {
        mBuffer.putLong(ContextFileReader.MAGIC_COOKIE);
        mBuffer.putInt(ContextFileReader.MAJOR_VERSION);
        mBuffer.putInt(ContextFileReader.MINOR_VERSION);
        mNextFilePosition += 8 + 4 + 4;
    }

    public synchronized void close() throws IOException {
        writeBuffer();
        mChannel.close();
    }

    private void writeString(String s) throws IOException {
        ByteBuffer encodedString = ContextFileReader.CHARSET.encode(s);
        final int length = encodedString.remaining();
        assureBufferCapacity(4 + length);
        mBuffer.putInt(length);
        mBuffer.put(encodedString);
        mNextFilePosition += 4 + length;
    }

    private void writeInteger(int i) throws IOException {
        assureBufferCapacity(4);
        mBuffer.putInt(i);
        mNextFilePosition += 4;
    }

    private void assureBufferCapacity(int size) throws IOException {
        if (mBuffer.remaining() < size || mShutdownHookExecuted) {
            writeBuffer();
        }

        // Grow buffer if it can't hold the requested size.
        while (mBuffer.capacity() < size) {
            mBuffer = ByteBuffer.allocateDirect(2 * mBuffer.capacity());
        }
    }

    public synchronized int writeLock(Lock lock) throws IOException {
        final int startPosition = mNextFilePosition;
        writeString(lock.getClassName());
        writeInteger(lock.getObjectId());
        flushBufferIfNeeded();
        return startPosition;
    }

    public synchronized int writeContext(LockingContext context)
    throws IOException {
        final int startPosition = mNextFilePosition;
        writeString(context.getThreadName());
        writeString(context.getLockReference());
        writeString(context.getMethodWithClass());
        flushBufferIfNeeded();
        return startPosition;
    }

    private void flushBufferIfNeeded() throws IOException {
        if (mShutdownHookExecuted) {
            writeBuffer();
        }
    }
}
