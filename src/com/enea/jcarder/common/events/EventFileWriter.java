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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.ThreadSafe;

import com.enea.jcarder.util.Counter;
import com.enea.jcarder.util.logging.Logger;

import com.enea.jcarder.common.events.LockEventListenerIfc.LockEventType;
import static com.enea.jcarder.common.events.EventFileReader.EVENT_LENGTH;

@ThreadSafe
public final class EventFileWriter implements LockEventListenerIfc {
    private final ByteBuffer mBuffer =
        ByteBuffer.allocateDirect(EVENT_LENGTH * 1024);
    private final FileChannel mFileChannel;
    private final Logger mLogger;
    private final Counter mWrittenLockEvents;
    private boolean mShutdownHookExecuted = false;

    private final ExecutorService mWriterThread;

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

        mWriterThread = Executors.newSingleThreadExecutor();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { shutdownHook(); }
        });
    }

    private void writeHeader() throws IOException {
        mBuffer.putLong(EventFileReader.MAGIC_COOKIE);
        mBuffer.putInt(EventFileReader.MAJOR_VERSION);
        mBuffer.putInt(EventFileReader.MINOR_VERSION);
        writeBuffer();
    }

    private void onWriterThread(Callable<Void> c) throws IOException {
        try {
            mWriterThread.submit(c).get();
        } catch (RejectedExecutionException ree) {
            throw new IOException(ree);
        } catch (ExecutionException ee) {
            if (ee.getCause() instanceof IOException) {
                throw (IOException)ee.getCause();
            } else {
                throw new IOException(ee);
            }
        } catch (InterruptedException ie) {
            // Just re-set current thread's interruption status, since
            // we want to appear as if we don't exist to the instrumented
            // application.
            Thread.currentThread().interrupt();
        }
    }

    public void onLockEvent(final LockEventType type,
                            final int lockId,
                            final int lockingContextId,
                            final long threadId) throws IOException {
        onWriterThread(new Callable<Void>() {
                public Void call() throws IOException {
                    mBuffer.put(type.typeId);
                    mBuffer.putInt(lockId);
                    mBuffer.putInt(lockingContextId);
                    mBuffer.putLong(threadId);
                    mWrittenLockEvents.increment();
                    if (mBuffer.remaining() < EVENT_LENGTH || mShutdownHookExecuted) {
                        writeBuffer();
                    }
                    return null;
                }
            });
    }

    private void writeBuffer() throws IOException {
        mBuffer.flip();
        mFileChannel.write(mBuffer);
        while (mBuffer.hasRemaining()) {
            Thread.yield();
            mFileChannel.write(mBuffer);
        }
        mBuffer.clear();
    }

    public synchronized void close() throws IOException {
        mWriterThread.shutdown();
        try {
            mWriterThread.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            throw new IOException("Failed to terminate writer thread", ie);
        }

        writeBuffer();
        mFileChannel.close();
    }

    private synchronized void shutdownHook() {
        try {
            if (!mWriterThread.isShutdown()) {
                close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mShutdownHookExecuted = true;
    }
}
