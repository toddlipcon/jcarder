package com.enea.jcarder.common.events;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import net.jcip.annotations.NotThreadSafe;

import com.enea.jcarder.util.Counter;
import com.enea.jcarder.util.Logger;

import static com.enea.jcarder.common.events.EventFileReader.EVENT_LENGTH;

@NotThreadSafe
public final class EventFileWriter implements LockEventListenerIfc {
    private final ByteBuffer mBuffer = ByteBuffer.allocateDirect(EVENT_LENGTH);
    private final FileChannel mFileChannel;
    private final Logger mLogger = Logger.getLogger("com.enea.jcarder");
    private final Counter mWrittenLockEvents;

    public EventFileWriter(File file) throws IOException {
        mLogger.info("Opening for writing: " + file.getAbsolutePath());
        RandomAccessFile raFile = new RandomAccessFile(file, "rw");
        raFile.setLength(0);
        mFileChannel = raFile.getChannel();
        mWrittenLockEvents = new Counter("Written Lock Events",
                                         mLogger,
                                         100000);
        writeHeader();
    }

    private void writeHeader() throws IOException {
        ByteBuffer header = ByteBuffer.allocate(8 + 4 + 4);
        header.putLong(EventFileReader.MAGIC_COOKIE);
        header.putInt(EventFileReader.MAJOR_VERSION);
        header.putInt(EventFileReader.MINOR_VERSION);
        header.flip();
        mFileChannel.write(header);
    }

    public void onLockEvent(int lockId,
                            int lockingContextId,
                            int lastTakenLockId,
                            int lastTakenLockingContextId,
                            long threadId) throws IOException {
        mBuffer.rewind();
        mBuffer.putInt(lockId);
        mBuffer.putInt(lockingContextId);
        mBuffer.putInt(lastTakenLockId);
        mBuffer.putInt(lastTakenLockingContextId);
        mBuffer.putLong(threadId);
        mBuffer.rewind();
        mFileChannel.write(mBuffer);
        mWrittenLockEvents.increment();
    }

    public void close() throws IOException {
        mFileChannel.close();
    }
}
