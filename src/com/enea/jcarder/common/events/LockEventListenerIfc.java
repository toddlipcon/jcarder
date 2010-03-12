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

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public interface LockEventListenerIfc {
    void onLockEvent(LockEventType eventType,
                     int lockId,
                     int lockingContextId,
                     long threadId)
        throws IOException;

    public static enum LockEventType {
        MONITOR_ENTER(0),
        MONITOR_EXIT(1),
        LOCK_LOCK(2),
        LOCK_UNLOCK(3),
        SHARED_LOCK_LOCK(4),
        SHARED_RLOCK_UNLOCK(5);

        public final byte typeId;
        private static Map<Byte, LockEventType> ID_TO_ENUM = new
            TreeMap<Byte, LockEventType>();

        LockEventType(int typeId) {
            this.typeId = (byte)typeId;
        }

        static {
            for (LockEventType t : LockEventType.values()) {
                ID_TO_ENUM.put(t.typeId, t);
            }
        }

        static LockEventType fromByte(byte b) {
            return ID_TO_ENUM.get(b);
        }
    }


}
