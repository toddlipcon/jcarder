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
import java.nio.charset.Charset;

import net.jcip.annotations.NotThreadSafe;

import com.enea.jcarder.common.Lock;
import com.enea.jcarder.common.LockingContext;
import com.enea.jcarder.util.logging.Logger;

@NotThreadSafe
public final class ContextFileReader
implements ContextReaderIfc {
    // TODO Make the directory of the database files configurable?
    public static final String EVENT_DB_FILENAME = "jcarder_events.db";
    public static final String CONTEXTS_DB_FILENAME = "jcarder_contexts.db";
    static final long MAGIC_COOKIE = 3927194112434171438L;
    static final int MAJOR_VERSION = 1;
    static final int MINOR_VERSION = 0;
    static final Charset CHARSET = Charset.forName("UTF-8");
    private final Logger mLogger;
    private final ByteBuffer mBuffer;

    public ContextFileReader(Logger logger, File file) throws IOException {
        mLogger = logger;
        RandomAccessFile raFile = new RandomAccessFile(file, "r");
        mLogger.info("Opening for reading: " + file.getAbsolutePath());
        FileChannel roChannel = raFile.getChannel();
        if (roChannel.size() > Integer.MAX_VALUE) {
            throw new IOException("File too large: " + file.getAbsolutePath());
        }
        mBuffer = roChannel.map(FileChannel.MapMode.READ_ONLY,
                                0,
                                (int) roChannel.size());
        roChannel.close();
        raFile.close();
        validateHeader(file.getAbsolutePath());
    }

    private void validateHeader(String filename) throws IOException {
        mBuffer.rewind();
        if (MAGIC_COOKIE != mBuffer.getLong()) {
            throw new IOException("Invalid file contents in: " + filename);
        }
        final int majorVersion = mBuffer.getInt();
        final int minorVersion = mBuffer.getInt();
        if (majorVersion != MAJOR_VERSION) {
            throw new IOException("Incompatible version: "
                                  + majorVersion + "." + minorVersion
                                  + " in: " + filename);
        }
    }

    private String readString() {
        final int stringBytes = mBuffer.getInt();
        mBuffer.limit(stringBytes + mBuffer.position());
        final String result = CHARSET.decode(mBuffer).toString();
        mBuffer.limit(mBuffer.capacity());
        return result;
    }

    public LockingContext readContext(int id) {
        mBuffer.position(id);
        return new LockingContext(readString(),
                                  readString(),
                                  readString());
    }

    public Lock readLock(int id) {
        mBuffer.position(id);
        String className = readString();
        int objectId = mBuffer.getInt();
        return new Lock(className, objectId);
    }
}
