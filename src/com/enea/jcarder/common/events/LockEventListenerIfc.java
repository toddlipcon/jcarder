package com.enea.jcarder.common.events;

import java.io.IOException;

public interface LockEventListenerIfc {

    void onLockEvent(int lockId,
                     int lockingContextId,
                     int lastTakenLockId,
                     int lastTakenLockingContextId,
                     long threadId)throws IOException;
}
