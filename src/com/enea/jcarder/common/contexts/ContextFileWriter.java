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

package com.enea.jcarder.common.contexts;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import net.jcip.annotations.NotThreadSafe;

import com.enea.jcarder.common.Lock;
import com.enea.jcarder.common.LockingContext;
import com.enea.jcarder.util.logging.Logger;

@NotThreadSafe
public final class ContextFileWriter
implements ContextWriterIfc {
    private final FileChannel mChannel;
    private int mNextFilePosition = 0;
    private final Logger mLogger;

    public ContextFileWriter(Logger logger, File file) throws IOException {
        mLogger = logger;
        mLogger.info("Opening for writing: " + file.getAbsolutePath());
        RandomAccessFile raFile = new RandomAccessFile(file, "rw");
        raFile.setLength(0);
        mChannel = raFile.getChannel();
        writeHeader();
    }

    private void writeHeader() throws IOException {
        ByteBuffer header = ByteBuffer.allocate(8 + 4 + 4);
        header.putLong(ContextFileReader.MAGIC_COOKIE);
        header.putInt(ContextFileReader.MAJOR_VERSION);
        header.putInt(ContextFileReader.MINOR_VERSION);
        header.flip();
        mNextFilePosition += mChannel.write(header);
    }

    public void close() throws IOException {
        mChannel.close();
    }

    private void writeString(String s) throws IOException {
        ByteBuffer encodedString = ContextFileReader.CHARSET.encode(s);
        ByteBuffer encodedStringLength = ByteBuffer.allocate(4);
        encodedStringLength.putInt(encodedString.remaining());
        encodedStringLength.flip();
        mNextFilePosition += mChannel.write(encodedStringLength);
        mNextFilePosition += mChannel.write(encodedString);
    }

    private void writeInteger(int i) throws IOException {
        ByteBuffer integerBuffer = ByteBuffer.allocate(4);
        integerBuffer.putInt(i);
        integerBuffer.flip();
        mNextFilePosition += mChannel.write(integerBuffer);
    }

    public int writeLock(Lock lock) throws IOException {
        final int startPosition = mNextFilePosition;
        writeString(lock.getClassName());
        writeInteger(lock.getObjectId());
        return startPosition;
    }

    public int writeContext(LockingContext context) throws IOException {
        final int startPosition = mNextFilePosition;
        writeString(context.getThreadName());
        writeString(context.getLockReference());
        writeString(context.getMethodWithClass());
        return startPosition;
    }
}
